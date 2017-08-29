package simulator;

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

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import matchingqueue.Matcher;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import simulator.control.UIMgr;
import simulator.control.util.LogMgr;

/**
 * ClientTest<br>
 * <code>ClientMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ClientTest {
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("ClientTest");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	/** 생성할 클라이언트 수 */
	private static final Integer HOW_MANY_CLIENT = 50;

	public static void main(String...args) {
		new ClientTest().clientTest();
	}
	
	public void clientTest() {	// single thread
		
		Socket sock = connectToServer();
		
		
		ConnectionToServer conn = new ConnectionToServer(sock);
		
		CustomMatcher sendNode;
		CustomMatcher recvNode;
		
		/** Node 생성 */
		Random rand = new Random();
		int opt = rand.nextInt(8);
		if(opt == 7)
			sendNode = new CustomMatcher("Client", 
					rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
		else 
			sendNode = new CustomMatcher("Client", 
					rand.nextInt(5), opt);
		
		/** Send */
		conn.nodeWrite(sendNode);
		
		/** Recv */
		if ((recvNode = conn.nodeRead()) != null) {
			List<Matcher> peer = recvNode.getPeer();
			
			String text = "Team 완성 [ ";
			
			if(!peer.isEmpty())
				text += recvNode.getClientName() + " - ";
			for(int i=0; i<peer.size(); i++) {
				CustomMatcher pnode = (CustomMatcher) peer.get(i);
				text += pnode.getClientName();
				if(i != peer.size()-1)
					text += " - ";
			}
			text += " ]";
			log(text);
		}
	}
	
	public Socket connectToServer() {
		Socket conn = null;
		try {
			conn = new Socket("127.0.0.1", 5000);
			log("connect to server");
		} catch (IOException e) {
			log("connectToServer Error", e);
		} 
		return conn;
	}
	
	class ConnectionToServer {
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
	
	@Ignore@Test
	public void nodeTest_mThread() throws Throwable {
		TestRunnable[] tests = new TestRunnable[HOW_MANY_CLIENT];
		//ClientMgr clientMgr = new ClientMgr();
		UIMgr ui = UIMgr.getInstance();
		
		for (int i = 0; i < tests.length; i++) {
			ui.setCl(HOW_MANY_CLIENT - i - 1);
			//tests[i] = clientMgr.getClientHandler(i);
		}
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tests);
		mttr.runTestRunnables();
	}
}
