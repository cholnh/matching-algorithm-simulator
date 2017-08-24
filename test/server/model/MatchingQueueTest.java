package server.model;
import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.model.matchingqueue.CustomMatcher;
import server.model.matchingqueue.MatchingQueue;

/**
 * MatchingQueueTest
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class MatchingQueueTest {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("MatchingQueueTest");
	MatchingQueue<CustomMatcher> matchingQueue = new MatchingQueue<CustomMatcher>();
	
	public static void main(String...args) {
		
		new MatchingQueueTest().start();
	}
	
	public void start() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					CustomMatcher recv = matchingQueue.take();
					System.out.println("도착] " + recv.toString());
				}
			}
		}).start();
		
		CustomMatcher cusMatcher1 = new CustomMatcher("client1", 3, 2);
		CustomMatcher cusMatcher2 = new CustomMatcher("client2", 3, 2);
		CustomMatcher cusMatcher3 = new CustomMatcher("client3", 3, 2);
		CustomMatcher cusMatcher4 = new CustomMatcher("client4", 3, 2);
	
		matchingQueue.put(cusMatcher1);
		delay(5000);
		matchingQueue.put(cusMatcher2);
		delay(5000);
		matchingQueue.put(cusMatcher3);
		delay(5000);
		matchingQueue.put(cusMatcher4);
	}
	
	private void delay(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
