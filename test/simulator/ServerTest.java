package simulator;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import matchingqueue.Matcher;
import matchingqueue.MatchingQueue;
import simulator.control.util.LogMgr;

/**
 * ServerTest<br>
 * <code>ServerMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ServerTest {
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("ServerTest");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	private static ServerSocket serverSoc = null;
	public static final ExecutorService mExecutorService = Executors.newWorkStealingPool(50);
	private final Map<String, Socket> clientMap = new HashMap<String, Socket>();
	public static void main(String...args) {
		new ServerTest().nodeTest();
	}
	
	public void nodeTest() {
		
		Socket conn;

		/* 서버 소켓 생성 */
		makeServerSocket();
		
		/* Matching Queue */
		MatchingQueue<CustomMatcher> matchingQueue = new MatchingQueue<>();
		matchingQueue.setWaitingTime(1000);
	
		/* 결과 반환 thread */
		new Thread(new ServerSendHadler(matchingQueue)).start();
		
		/* 클라이언트로 부터의 연결을 기다림 slow system call */
		while((conn = waitForClient()) != null) {
			
			/* 스레드풀에 워커스레드 추가 */
			mExecutorService.execute(new ServerHandler(conn, matchingQueue));
		}
		close();
	}
	
	public Socket waitForClient() {
		if(serverSoc == null) return null;
		log("waitting for Client...");
		try {
			Socket conn = serverSoc.accept();
			return conn;
		} catch (IOException e) {
			log("Accept Error", e);
			return null;
		}
	}
	
	public void makeServerSocket() {
		if(serverSoc == null) {
			try {
				log("서버 생성");
				serverSoc = new ServerSocket(5000);
			} catch (IOException e) {
				log("makeServerSocket Error", e);
			}
		}
	}
	
	public void close() {
		try {
			
			/* 서버 소켓 종료 */
			serverSoc.close();
			
			/* 스레드 풀 종료 */
			mExecutorService.shutdown();
		} catch (IOException e) {
			log("serverSoc close Error", e);
		}
	}
	
	class ServerSendHadler extends Thread {

		private MatchingQueue<CustomMatcher> matchingQueue;
		
		public ServerSendHadler(MatchingQueue<CustomMatcher> matchingQueue) {
			this.matchingQueue = matchingQueue;
		}
		
		@Override
		public void run() {
			CustomMatcher sendNode;
			ConnectionToClient conToClient;
			
			while(!mExecutorService.isShutdown()) {
				/* 결과 대기 */
				if((sendNode = matchingQueue.take()) != null) {
					/* Send */
					conToClient = new ConnectionToClient(clientMap.get(sendNode.getClientName()));
					conToClient.nodeWrite(sendNode);
					
					for(Matcher pnode : sendNode.getPeer()) {
						CustomMatcher cmat = (CustomMatcher) pnode;
						conToClient = new ConnectionToClient(clientMap.get(cmat.getClientName()));
						conToClient.nodeWrite(sendNode);
					}
				}
			}
		}
	}
	
	/**
	 * 서버 핸들러<br>
	 * @author Choi
	 */
	class ServerHandler extends Thread {
		
		private Socket socket;
		
		/** 클라이언트 연결 스트림 핸들러 */
		private ConnectionToClient conToClient;
		private MatchingQueue<CustomMatcher> matchingQueue;
		
		ServerHandler(final Socket socket, final MatchingQueue<CustomMatcher> matchingQueue) {
			this.socket = socket;
			this.matchingQueue = matchingQueue;
			conToClient = new ConnectionToClient(socket);
		}

		/** Thread 실행부 */
		public void run() {
			
			CustomMatcher recvNode;
			
			/* Receive */
			if((recvNode = conToClient.nodeRead()) != null){
				/* Node 삽입 */
				clientMap.put(recvNode.getClientName(), socket);
				matchingQueue.put(recvNode);
			}
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
		
		public synchronized void nodeWrite(final CustomMatcher sendNode) {
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
		
		public synchronized CustomMatcher nodeRead() {
			try {
				ObjectInputStream in = new ObjectInputStream(bis);
				CustomMatcher recvNode = (CustomMatcher) in.readObject();
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
}

