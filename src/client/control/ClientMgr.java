package client.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.control.manager.UIMgr;
import server.model.blockingqueue.BlockingQueueNode;
import server.model.blockingqueue.Node;
import server.view.MainFrame;

/**
 * ClientMgr
 * 
 * @author Choi
 */
public class ClientMgr {
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("Client");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	/** 연결할 서버 IP, 포트 */
	private String SERVERIP = "127.0.0.1";
	private Integer SERVERPORT = 5000;
	
	/** 랜덤 딜레이 최소/최대 값 */
	private final Integer DELAY_MINIMUM = 1;
	private final Integer DELAY_MAXIMUM = 10;
	
	/** UI */
	private MainFrame ui = UIMgr.getInstance().getMainFrame();
	
	public ClientMgr() {}

	public ClientMgr(final String SERVERIP, final Integer SERVERPORT) {
		this();
		this.SERVERIP = SERVERIP;
		this.SERVERPORT = SERVERPORT;
	}

	/**
	 * 클라이언트 핸들러<br>
	 * 클라이언트 운영에 대한 전반적인 흐름을 정의하는 부분
	 */
	private static final Integer HOW_MANY_CLIENT = 10;
	public void start() {		
		for (int i = 0; i < HOW_MANY_CLIENT; i++) {
			ui.setCl(HOW_MANY_CLIENT - i - 1);
			
			new Thread(getClientHandler(i)).start();
		}
	}
	
	/**
	 * 클라이언트 핸들러<br>
	 * @author Choi
	 */
	class ClientHandler implements Runnable {
		
		private Integer clientIdx;
		
		/** 서버 연결 스트림 핸들러 */
		private ConnectionToServer conToServer;
		
		ClientHandler(Integer clientIdx) {
			this.clientIdx = clientIdx;
			Socket socket = connectToServer();
			conToServer = new ConnectionToServer(socket);
		}

		/** Thread 실행부 */
		@Override
		public void run() {
			Node sendNode;
			Node recvNode;
			
			/* Node 생성 */
			sendNode = getRandomNode(clientIdx);
			
			/* 대기 */
			waitRandom(sendNode);
			
			/* Send */
			ui.setClientStatus("전송중", sendNode.getClientName());
			conToServer.nodeWrite(sendNode);
			ui.setClientStatus("매칭대기중", sendNode.getClientName());
			
			/* Receive */
			if ((recvNode = conToServer.nodeRead()) != null) {
				completeHadler(sendNode, recvNode);
				clientPlus();
			}
		}
	}
	
	private void completeHadler(Node sendNode, Node recvNode) {
		ui.setClientStatus("매칭완료", sendNode.getClientName());
		
		List<BlockingQueueNode> peer = recvNode.getPeer();
		String text = recvNode.getClientName()+" Team 완성 [ ";
		
		text += recvNode.getClientName();
		if(peer.size() > 0)
			text += " - ";
		for(int i=0; i<peer.size(); i++) {
			BlockingQueueNode pnode = peer.get(i);
			text += pnode.getClientName();
			if(i != peer.size()-1)
				text += " - ";
		}
		text += " ]";
		
		ui.setClientTeam(text, sendNode.getClientName());
		log(text);
	}
	
	private void waitRandom(Node sendNode) {
		Integer num = new Random().nextInt(DELAY_MAXIMUM)+DELAY_MINIMUM;
		ui.setClientTableNode("전송대기중 "+num+"secs...", sendNode.getClientName(), sendNode.getOptionText(), ""+sendNode.getTotalPeerCount(), "");
		
		try {
			Thread.sleep(num*1000);
		} catch (InterruptedException e) {
			log("ClientHandler wait InterruptedException", e);
		}
	}
	
	private Node getRandomNode(Integer clientIdx) {
		Node sendNode;
		Random rand = new Random();
		int opt = rand.nextInt(8);
		if(opt == 7)
			sendNode = new BlockingQueueNode("Client"+clientIdx, 
					rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
		else 
			sendNode = new BlockingQueueNode("Client"+clientIdx, 
					rand.nextInt(5), opt);
		return sendNode;
	}
	
	/** 클라이언트 수 */
	private volatile Integer clientCount = 0;
	private Lock lock = new ReentrantLock();
	private void clientPlus() {
		synchronized (lock) {
			clientCount++;
		}
		ui.setCl(clientCount);
	}
	
	public ClientHandler getClientHandler(Integer Idx) {
		return new ClientHandler(Idx);
	}

	/**
	 * 연결 스트림 핸들러<br>
	 * 서버와 연결된 소켓의 <code>InputStream</code>과 <code>OutputStream</code>을 통하여<br>
	 * 주고 받는 여러 stream 처리를 수행
	 * 
	 * @author Choi
	 */
	public class ConnectionToServer {
		private Socket socket;
		BufferedInputStream bis;
		BufferedOutputStream bos;

		ConnectionToServer(final Socket socket) {
			this.socket = socket;
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				bis = new BufferedInputStream(is);
				bos = new BufferedOutputStream(os);
			} catch (Exception e) {
				log("ConnectionToServer Error", e);
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
	public ConnectionToServer getConnectionToServer(final Socket socket) {
		return new ConnectionToServer(socket);
	}

	/**
	 * 서버 소켓 연결 요청 
	 * 
	 * @return 연결된 서버 소켓 반환
	 */
	public Socket connectToServer() {
		Socket conn = null;
		try {
			conn = new Socket(this.SERVERIP, this.SERVERPORT);
			log("connect to server");
		} catch (IOException e) {
			log("connectToServer Error", e);
		} 
		return conn;
	}

	public Socket connectToServer(final String IP, final Integer PORT) {
		this.SERVERIP = IP; 
		this.SERVERPORT = PORT;
		return connectToServer();
	}
}

