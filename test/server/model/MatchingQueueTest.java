package server.model;
import java.util.Random;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.control.util.MemoryMonitor;
import server.model.matchingqueue.CustomMatcher;
import server.model.matchingqueue.Matcher;
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
	
	
	
	public static void main(String...args) {
		new MatchingQueueTest().customTest_1();
	}
	
	public void customTest_1() {
		MatchingQueue<CoupleMatcher> matchingQueue = new MatchingQueue<CoupleMatcher>();
		matchingQueue.setWaitingTime(10000);
		
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
	
	class CoupleMatcher extends Matcher {

		private String name;
		private boolean sex;	// t : male , f : female
		private boolean isQueer;
		private Integer age;
		private Integer needMaxAge;
		private Integer needMinAge;
		
		public String getName() {return name;}
		public Integer getAge() {return age;}
		public Integer getNeedMaxAge() {return needMaxAge;}
		public Integer getNeedMinAge() {return needMinAge;}
		public boolean isSex() {return sex;}
		public void setName(String name) {this.name = name;}
		public void setAge(Integer age) {this.age = age;}
		public void setSex(boolean sex) {this.sex = sex;}
		public void setNeedMaxAge(Integer needMaxAge) {this.needMaxAge = needMaxAge;}
		public void setNeedMinAge(Integer needMinAge) {this.needMinAge = needMinAge;}

		public CoupleMatcher(Integer totalPeerCount, String name, boolean sex, boolean isQueer, Integer age, Integer needMaxAge, Integer needMinAge) {
			super(totalPeerCount);
			this.name = name;
			this.sex = sex;
			this.isQueer = isQueer;
			this.age = age;
			
			if(needMaxAge < needMinAge)
				needMaxAge = needMinAge;
				
			this.needMaxAge = needMaxAge;
			this.needMinAge = needMinAge;
		}
		
		@Override
		public boolean match(Matcher node) {
			CoupleMatcher cnode = (CoupleMatcher) node;
			
			// 성별조건
			// 남-여
			if(this.sex != cnode.sex) {
				// 성소수자일 경우 실패
				if(this.isQueer || cnode.isQueer) return false;
				
				// 만족하는 나이조건
				// min <= age <= max 
				if(this.needMinAge <= cnode.age && cnode.age <= this.needMaxAge) {
					// 매칭성공
					return true;
				}
			} else {
				// 성소수자가 아닐 경우 실패
				if(!this.isQueer || !cnode.isQueer) return false;
				
				if(this.needMinAge <= cnode.age && cnode.age <= this.needMaxAge) {
					// 매칭성공
					return true;
				}
			}
			return false;
		}
		
		@Override
		public Matcher loosen() {
			if((needMaxAge - needMinAge) < 40) {
				if(needMinAge != 19) {
					// 눈을 낮춰본다.
					Random rand = new Random();
					if(rand.nextBoolean()) 
						needMaxAge++;
					else
						needMinAge--;
				} else {
					// 부모동의없이 19살 미만과 결혼은 범죄행위이다.
					needMaxAge++;
				}
			} else {
				// 게이나 레즈나 이성애자가 되는 길을 택한다.
				isQueer = !isQueer;
			}
			
			return this;
		}
		
		@Override
		public String toString() {
			String text = "이름 : " + name + "(" + (sex?("남"+(isQueer?" 게이":"")):("여"+(isQueer?" 레즈":""))) + ", " + age + ") +" + (needMaxAge-age) + "살 -" + (age-needMinAge) + "살";
			return text;
		}
	}
	
	public void customTest_2() {
		MemoryMonitor.print();
		MatchingQueue<CustomMatcher> matchingQueue = new MatchingQueue<CustomMatcher>();
		
		//receiveThread(matchingQueue);
		
		CustomMatcher cusMatcher1 = new CustomMatcher("client1", 3, 2);
		CustomMatcher cusMatcher2 = new CustomMatcher("client2", 3, 2);
		CustomMatcher cusMatcher3 = new CustomMatcher("client3", 3, 2);
		CustomMatcher cusMatcher4 = new CustomMatcher("client4", 3, 2);
		MemoryMonitor.print();
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
	
	private <T extends Matcher> void receiveThread(MatchingQueue<T> matchingQueue) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					T recv = matchingQueue.take();
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
