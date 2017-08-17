package client;

import java.net.Socket;
import java.util.List;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import client.control.ClientMgr;
import client.control.ClientMgr.ConnectionToServer;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import server.control.manager.UIMgr;
import server.model.blockingqueue.BlockingQueueNode;
import server.model.blockingqueue.Node;
import server.view.MainFrame;

/**
 * ClientTest<br>
 * <code>ClientMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ClientTest {
	/** 생성할 클라이언트 수 */
	private static final Integer HOW_MANY_CLIENT = 50;


	@Test
	public void nodeTest_mThread() throws Throwable {
		TestRunnable[] tests = new TestRunnable[HOW_MANY_CLIENT];
		ClientMgr clientMgr = new ClientMgr();
		MainFrame ui = UIMgr.getInstance().getMainFrame();
		
		for (int i = 0; i < tests.length; i++) {
			ui.setCl(HOW_MANY_CLIENT - i - 1);
			//tests[i] = clientMgr.getClientHandler(i);
		}
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tests);
		mttr.runTestRunnables();
	}
	
	@Ignore@Test
	public void nodeTest_sThread() throws Throwable {
		ClientMgr clientMgr = new ClientMgr();
		
		Socket sock = clientMgr.connectToServer();
		
		
		ConnectionToServer conn = clientMgr.getConnectionToServer(sock);
		
		Node sendNode;
		Node recvNode;
		
		/** Node 생성 */
		Random rand = new Random();
		int opt = rand.nextInt(8);
		if(opt == 7)
			sendNode = new Node("Client", 
					rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
		else 
			sendNode = new Node("Client", 
					rand.nextInt(5), opt);
		
		/** Send */
		conn.nodeWrite(sendNode);
		
		/** Recv */
		if ((recvNode = conn.nodeRead()) != null) {
			List<BlockingQueueNode> peer = recvNode.getPeer();
			String text = "Team 완성 [ ";
			
			text += recvNode.getClientName() + " - ";
			for(int i=0; i<peer.size(); i++) {
				BlockingQueueNode pnode = peer.get(i);
				text += pnode.getClientName();
				if(i != peer.size()-1)
					text += " - ";
			}
			text += " ]";
			System.out.println(text);
		}
		
	}
}
