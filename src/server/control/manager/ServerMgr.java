package server.control.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.model.blockingqueue.BlockingQueueMgr;
import server.model.blockingqueue.BlockingQueueNode;
import server.model.blockingqueue.Node;
import server.view.MainFrame;

/**
 * ServerMgr
 * 
 * @author Choi
 */
public class ServerMgr {
	
	/** ServerMgr INSTANCE */
	private static class Singleton {
		/** Initialization On Demand Holder Idiom */
		private static final ServerMgr INSTANCE = new ServerMgr();
	}
	public static synchronized ServerMgr getInstance() {
		return Singleton.INSTANCE;
	}
	public static synchronized ServerMgr getInstance(final Integer PORT) {
		ServerMgr.PORT = PORT;
		return getInstance();
	}
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("Server");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	/** 
	 * 스레드 풀 (WorkStealingPool)<br>
	 * <code>newWorkStealingPool</code>파라미터는  parallelism level(병렬화 수준)을 설정<br>
	 */
	public static final ExecutorService mExecutorService 
		= Executors.newWorkStealingPool(50);
	//	= Executors.newFixedThreadPool(50);	// Runtime.getRuntime().availableProcessors()
	
	/** 서버 소켓 */
	private static ServerSocket serverSoc = null;
	
	/** 서버 소켓 포트 (기본 : 5000) */
	private static Integer PORT = 5000;

	private ServerMgr() {/*Singleton*/}

	public static void main(String...args) {
		ServerMgr.getInstance().start();
	}
	
	/**
	 * 서버 핸들러<br>
	 * 서버 운영에 대한 전반적인 흐름을 정의하는 부분
	 */
	public void start() {
		Socket conn;

		/** 서버 소켓 생성 */
		makeServerSocket();
		
		/** Blocking Queue */
		BlockingQueueMgr queueMgr = BlockingQueueMgr.getInstance();
		queueMgr.coreStart();
		
		/**
		 * 클라이언트로 부터의 연결을 기다림<br>
		 * <code>waitForClient()</code> slow system call
		 */
		while((conn = waitForClient()) != null) {
			/**
			 * 스레드풀에 <code>ServerHandler</code> 워커스레드 추가
			 */
			mExecutorService.execute(getServerHandler(conn));
		}
		close();
	}

	/** 서버 종료 처리 */
	public void close() {
		try {
			
			/** 서버 소켓 종료 */
			serverSoc.close();
			
			/** 스레드 풀 종료 */
			mExecutorService.shutdown();
		} catch (IOException e) {
			log("serverSoc close Error", e);
		}
	}

	public ServerHandler getServerHandler(final Socket socket) {
		return new ServerHandler(socket);
	}

	/**
	 * 서버 스트링 핸들러<br>
	 * 
	 * @author Choi
	 */
	class ServerHandler extends Thread {
		
		private Socket socket;
		
		/** 클라이언트 연결 스트림 핸들러 */
		private ConnectionToClient conToClient;

		ServerHandler(final Socket socket) {
			this.socket = socket;
			conToClient = new ConnectionToClient(socket);
		}

		/** Thread 실행부 */
		public void run() {
			Node sendNode;
			Node recvNode;
			BlockingQueueMgr queueMgr = BlockingQueueMgr.getInstance();
			BlockingQueue<BlockingQueueNode> waitingQueue = queueMgr.getWaitingQueue();
			MainFrame ui = UIMgr.getInstance().getMainFrame();
			
			/** Recv */
			if((recvNode = conToClient.nodeRead()) != null){
				
				BlockingQueueNode bnode = (BlockingQueueNode)recvNode;
			
				while(true) {
					try {
						ui.removeServerTableNode(bnode.getClientName());
						ui.setServerTableNode("대기열검색중", 
											bnode.getClientName(), 
											bnode.getOptionText(), 
											bnode.getTotalPeerCount()+"", 
											bnode.getPeerText() == null ? "" : bnode.getPeerText(), 
											bnode.getCurPeerCount()+"",
											bnode.getParentText() == null ? "" : bnode.getParentText());
						
						waitingQueue.put(bnode);								//	waiting queue에 삽입
						sendNode = bnode.getQueue().poll(30, TimeUnit.SECONDS);	// 노드가 원하는 조건을 만족(피어를 모두 채움)할 때 까지 poll
						
						if(sendNode == null) {
							// Time out!
							ui.setServerStatus("조건변경중", bnode.getClientName());
							bnode.setLoosenNode();
							bnode.getQueue().clear();
							queueMgr.removeNode(bnode);
							continue;
						}
						
						/** Send */
						conToClient.nodeWrite(sendNode);
						//ui.setServerStatus("매칭완료", sendNode.getClientName());
						break;
					} catch(InterruptedException ie) {
						// Time out handle 이곳 아님
					}
				}
			}
			
			/** 작업이 끝났으므로 서버에 연결되어있는 클라이언트 수를 수정  */
			synchronized (lock) {
				clientCount--;
			}
			ui.setSl(clientCount);
			log("exit " + socket.getInetAddress().getHostAddress() + "\n[clients : " + clientCount + "명]");
		}
	}

	/**
	 * 연결 스트림 핸들러<br>
	 * 클라이언트와 연결된 소켓의 <code>InputStream</code>과 <code>OutputStream</code>을 통하여<br>
	 * 주고 받는 여러 stream 처리를 수행
	 * 
	 * @author Choi
	 */
	class ConnectionToClient {
		private Socket socket;
		BufferedInputStream bis;
		BufferedOutputStream bos;

		ConnectionToClient(final Socket socket) {
			
			this.socket = socket;
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				bis = new BufferedInputStream(is);
				bos = new BufferedOutputStream(os);
			} catch (Exception e) {
				log("ConnectionToClient Error", e);
			}
		}
		
		public synchronized void nodeWrite(final Node sendNode) {
			try {
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(sendNode);
				out.flush();
				log("send to " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + sendNode.toString() + "\"");
			} catch (Exception e) {
				log("write Error", e);
			}
		}
		
		public synchronized Node nodeRead() {
			try {
				ObjectInputStream in = new ObjectInputStream(bis);
				Node recvNode = (Node) in.readObject();
				log("receive from " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + recvNode.toString() + "\"");
				return recvNode;
			} catch (Exception e) {
				log("read Error", e);
				return null;
			}
		}
		
		/** 종료처리 */
		public void close() {
			try {
				bis.close();
				bos.close();
			} catch (IOException e) {
				log("Connection close Error", e);
			}
		}
	}

	/** 서버 소켓 생성 */
	public void makeServerSocket() {
		if(serverSoc == null) {
			try {
				log("서버 생성");
				serverSoc = new ServerSocket(PORT);
			} catch (IOException e) {
				log("makeServerSocket Error", e);
			}
		}
	}

	/** 서버에 접속된 클라이언트 수 */
	private volatile Integer clientCount = 0;
	Lock lock = new ReentrantLock();
	/**
	 * 클라이언트 연결 대기<br>
	 * 접속을 시도하는 클라이언트와의 소켓연결을 하고  해당 소켓 <code>serverSoc</code>을 받아 반환
	 * 
	 * @return 연결된 클라이언트 소켓
	 */
	public Socket waitForClient() {
		if(serverSoc == null) return null;
		log("waitting for Client...");
		try {
			Socket conn = serverSoc.accept();
			MainFrame ui = UIMgr.getInstance().getMainFrame();
			synchronized (lock) {
				clientCount++;
			}
			ui.setSl(clientCount);
			log("connect to " + conn.getInetAddress().getHostAddress() + " [clients : " + clientCount + "명]");
			return conn;
		} catch (IOException e) {
			log("Accept Error", e);
			return null;
		}
	}
}