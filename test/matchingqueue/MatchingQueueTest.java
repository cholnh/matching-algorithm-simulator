package matchingqueue;
import java.util.Random;

import network_simulator.CustomMatcher;
import simulator.control.util.MemoryMonitor;


/**
 * MatchingQueueTest
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class MatchingQueueTest {
	
	public static void main(String...args) {
		new MatchingQueueTest().customTest_1();
	}
	
	public void customTest_1() {
		MatchingQueue<CoupleMatcher> matchingQueue = new MatchingQueue<CoupleMatcher>();
		matchingQueue.setWaitingTime(1000);
		
		receiveThread(matchingQueue);
		
		Random rand = new Random();
		for(int i=0; i<10; i++) {
			boolean sex = rand.nextBoolean();
			boolean isQueer = rand.nextInt(10) > 8 ? true : false;
			int age = rand.nextInt(49 - 19 + 1) + 19; 	// 19살 ~ 49살
			int needMaxAge = age + rand.nextInt(3);		// 연상 3살 까지
			int needMinAge = age - rand.nextInt(3);		// 연하 3살 까지
			
			CoupleMatcher subject = new CoupleMatcher(1, getRandomName(sex), sex, isQueer, age, needMaxAge, needMinAge);
			System.out.println(subject);
			matchingQueue.put(subject);
		}
	}
	
	public void customTest_2() {
		System.out.println(MemoryMonitor.getInfo());
		MatchingQueue<CustomMatcher> matchingQueue = new MatchingQueue<CustomMatcher>();
		
		//receiveThread(matchingQueue);
		
		CustomMatcher cusMatcher1 = new CustomMatcher("client1", 3, 2);
		CustomMatcher cusMatcher2 = new CustomMatcher("client2", 3, 2);
		CustomMatcher cusMatcher3 = new CustomMatcher("client3", 3, 2);
		CustomMatcher cusMatcher4 = new CustomMatcher("client4", 3, 2);
		System.out.println(MemoryMonitor.getInfo());
		matchingQueue.put(cusMatcher1);
		//delay(5000);
		matchingQueue.put(cusMatcher2);
		//delay(5000);
		matchingQueue.put(cusMatcher3);
		//delay(5000);
		matchingQueue.put(cusMatcher4);
	}
	
	private String getRandomName(boolean mf) {
		// t male , f female
		final String[] mfname = {"김", "이", "박", "최", "조", "문", "류", "경", "주", "정"};
		final String[] mlname = {"건우", "낙형", "우현", "준성", "한솔", "대환", "광호", "철수", "재인", "진호"};
		final String[] flname = {"수빈", "서연", "채영", "주은", "지혜", "민주", "수지", "주아", "설아", "선아"};
		Random rand = new Random();
		String name = "";
		name += mfname[rand.nextInt(mfname.length)];
		if(mf) 
			name += mlname[rand.nextInt(mlname.length)];
		else 
			name += flname[rand.nextInt(flname.length)];
		return name;
	}
	
	private void receiveThread(MatchingQueue<CoupleMatcher> matchingQueue) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					CoupleMatcher recv = matchingQueue.take();
					CoupleMatcher cm = (CoupleMatcher) recv;
					CoupleMatcher peer = null;
					String text = "";
					text += "도착] " + cm.getName() + " ♥ ";
					for(Matcher pnode : recv.getPeer()) {
						peer = (CoupleMatcher)pnode;
						text += peer.getName();
					}
					text += "\n[" + cm + "] // [" + peer + "]";
					System.out.println(text);
				}
			}
		}).start();
	}
	
	
	@SuppressWarnings("unused")
	private void delay(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
