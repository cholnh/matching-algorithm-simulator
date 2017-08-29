package simulator.control.util;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

/**
 * UITimer
 * 
 * @version 1.0 [2017. 8. 3.]
 * @author Choi
 */
public class UITimer {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("UITimer");
	
	private volatile boolean isDone; 
	private volatile boolean isInterrupted;
	private Integer waitingTime = 0, min = 0, max = 10;
	@SuppressWarnings("unused")
	private Display curDidplay;
	
	public UITimer(Display didplay) {
		this.curDidplay = didplay;
		this.isDone = false;
		this.isInterrupted = false;
	}
	
	public UITimer(Display didplay, Integer millis) {
		this(didplay);
		this.waitingTime = millis;
	}
	
	public void start() {
		this.isInterrupted = false;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					System.out.printf("Mytimer] wait %.3f secs ... ", (float)waitingTime/1000);
					Thread.sleep(waitingTime);
					System.out.printf("Done(%.3f)\n",(float)waitingTime/1000);
					//curDidplay.wake();
				} catch (InterruptedException e) {
					//e.printStackTrace();
					System.out.println("interrupted!");
					isDone = true;
					isInterrupted = true;
				}
				isDone = true;
			}
		}).start();
		//int count = 0;
		while(!isDone) {
			//System.out.println(""+isDone + (count++));
			/*
			if(!curDidplay.readAndDispatch()) {
				System.out.println("sleep...");
				curDidplay.sleep();
				System.out.println("sleep...wake");
			}
			*/
		}
	}
		
	public void start(Integer millis) {
		this.waitingTime = millis;
		start();
	}
	
	public void randomStart() {
		Random rand = new Random();
		Integer randNum = rand.nextInt((max*1000) - (min*1000) + 1) + (min*1000);	// default range : 0 ~ 10
		start(randNum);
	}
	
	public void randomStart(Integer min, Integer max) {
		this.min = min;
		this.max = max;
		randomStart();
	}
	
	public boolean isDone() {
		return isDone;
	}
	
	public boolean isInterrupted() {
		return isInterrupted;
	}
}
