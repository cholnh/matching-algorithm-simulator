package server.model;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import server.control.log.LogMgr;
import server.model.waitingqueue.WaitingNode;
import server.model.waitingqueue.WaitingQueue;

/**
 * WaitingQueueTest
 * 
 * @version 1.0 [2017. 8. 15.]
 * @author Choi
 */
public class WaitingQueueTest {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("WaitingQueueTest");

	private static void log(String text) {
		logger.info(text);
	} 
	
	@Ignore@Test
	public void test() {
		WaitingQueue wq = WaitingQueue.getInstance();
		randomNode(wq, 10);
		System.out.println();
		System.out.println("completeCount : " + wq.getCompleteCount());
		wq.printWaitingList();
	}
	
	public static void main(String...args) {
		WaitingQueueTest t = new WaitingQueueTest();
		WaitingQueue wq = WaitingQueue.getInstance();
		t.randomNode(wq, 10);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		System.out.println("completeCount : " + wq.getCompleteCount());
		wq.printWaitingList();
	}

	private void randomNode(WaitingQueue wq, int num) {
		
		for(int i=0; i<num; i++) {
			new Thread(new MyThread(i, wq)).start();
		}
		
	}
	
	class MyThread implements Runnable {

		private int i;
		private WaitingQueue wq;
		
		public MyThread(int i, WaitingQueue wq) {
			this.i = i;
			this.wq = wq;
		}
		@Override
		public void run() {
			Random rand = new Random();
			int opt = rand.nextInt(8);
			if(opt == 7) {
				WaitingNode node = new WaitingNode("client"+i, rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
				wq.setWaitingNode(node);
			}
				
			else {
				WaitingNode node = new WaitingNode("client"+i, rand.nextInt(5), opt);
				wq.setWaitingNode(node);
			}
				
		}
		
	}
}
