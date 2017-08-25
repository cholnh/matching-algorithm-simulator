package server.model.matchingqueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MatchingQueue
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class MatchingQueue <T extends Matcher> {
	
	/**
	 * The Core Thread for core-logic.
	 */
	private final Thread core = new Thread(new CoreHandler());
	
	/**
	 * BlockingQueue is used for implement Producer–consumer pattern.
	 * The waitingQueue wait for node to come in from outside.
	 * The node what taken from waitingQueue is handled at examine in CoreHandler.
	 */
	private final BlockingQueue<T> waitingQueue = new LinkedBlockingQueue<T>();
	
	/**
	 * BlockingQueue is used for implement Producer–consumer pattern.
	 * The resultQueue wait for node what completed core-logic.
	 * The completed Node is passed to the outside via take().
	 */
	private final BlockingQueue<T> resultQueue = new LinkedBlockingQueue<T>();
	
	/**
	 * The waitingList manage waiting nodes.
	 * The list is used to search in the examine().
	 */
	private final List<T> waitingList = Collections.synchronizedList(new ArrayList<T>());
	
	/**
	 * Each node have waitingTime.
	 * The timer schedule timeout handler (specified task for execution).
	 * timeout handler is executed after the time specified in waitingTime.
	 */
	private volatile long waitingTime;
	
	/**
	 * The lock for synchronize core-logic and remove-logic.
	 */
	private final Lock lock = new ReentrantLock();
	
	/**
	 * Set waitingTime.
	 * @param millis for set the node's timeout-time
	 */
	public void setWaitingTime(long millis) {this.waitingTime = millis;}
	
	/**
	 * Creates a new MatchingQueue and start core Thread immediately.
	 * Default waitingTime is 30secs.
	 */
	public MatchingQueue () {
		waitingTime = 30000;
		core.start();
	}
	
	/**
	 * Creates a new MatchingQueue and start core Thread immediately.
	 * @param waitingTime for set the node's timeout-time.
	 */
	public MatchingQueue (long waitingTime) {
		this.waitingTime = waitingTime;
		core.start();
	}
	
	/**
	 * CoreHandler
	 * core logic handler
	 * 
	 * @version 1.0 [2017. 8. 25.]
	 * @author Choi
	 */
	class CoreHandler implements Runnable {

		@Override
		public void run() {
			while(true) {
				try {
					/* block - queue */
					T node = waitingQueue.take();
					
					/* block - lock */
					lock.lock();
					
					try {
						/* CORE LOGIC */
						timer(node, waitingTime);
						examine(node);
					} finally {
						lock.unlock();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}	
			}
		}
	}
	
	private void timer(T node, long delay) {
		Timer timer = new Timer();
		node.setTimer(timer);
		timer.schedule(new TimerTask() {
			
			@Override @SuppressWarnings("unchecked")
			public void run() {
				/* 제거 후 재등록 */
				remove(node);
				put((T) node.loosen());
			}
		}, delay);
	}
	
	private void examine(T node) {
		if(node.getNeedPeerCount() == 0) {
			complete(node);
			return;
		}
		
		for(T wnode : waitingList) {
			if(node.match(wnode)) {
				/* peer 등록 */
				wnode.setPeerNode(node);
				
				if(wnode.isPeerFull()) {
					/* 등록결과 : 매칭 성공 */
					waitingList.remove(wnode);
					complete(wnode);
					return;
				} else {
					/* 등록 결과 : 다른 peer로 들어가서 대기 */
					waitingList.remove(node);
					return;
				}
			}
		}
		/* 탐색 실패 -> 신규등록 */
		insert(node);
	}
	
	public void remove(T node) {
		lock.lock();
		try {
			waitingQueue.remove(node);	// 큐에 대기중이라면 제거
			waitingList.remove(node);	// 리스트에 있다면 제거
			
			/* 어딘가의 peer로 속해있다면 제거 */
			Matcher parentNode = node.getParent();
			if(parentNode != null) {
				parentNode.removePeerNode(node);
				node.setParent(null);
			}
			
			/* peer 정리 */
			List<Matcher> plist = node.getPeer();
			T firstNode = null;
			for(int i=0; i<plist.size(); i++) {
				@SuppressWarnings("unchecked")
				T pnode = (T) plist.get(i);
				if(i==0) {
					/* 첫 번째 peer */
					firstNode = pnode;
					firstNode.setParent(null);
					insert(firstNode);
				}
				else {
					/* 나머지 peer -> 첫 번째peer에 등록시킴 */
					firstNode.setPeerNode(pnode);
				}
			}
			
			node.setPeerClear();
		} finally {
			lock.unlock();
		}		
	}
	
	private void insert(T node) {
		Integer idx = waitingList.size();
		Integer nNeedPeerCnt = node.getNeedPeerCount();
		
		/* 자신보다 한 단계 높은 노드의 바로 아래 인덱스 */
		for(int i=0; i<waitingList.size(); i++) {
			T wnode = waitingList.get(i);
			Integer wNeedPeerCnt = wnode.getNeedPeerCount();
			
			if(nNeedPeerCnt < wNeedPeerCnt) {
				idx = waitingList.indexOf(wnode);	// Not i
				break;
			}
		}
		waitingList.add(idx, node);
	}	
	
	private void complete(T node) {
		/* timer 종료 */
		node.getTimer().cancel();
		for(Matcher pnode : node.getPeer()) 
			pnode.getTimer().cancel();
		
		try {
			resultQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void put(T node) {
		/* External put */
		try {
			waitingQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public T take() {
		/* External take */
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
