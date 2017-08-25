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
	 * 
	 * @param waitingTime	for set the node's timeout-time.
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
			
			/* MatchingQueue Core loop */
			while(true) {
				try {
					/* block - queue */
					T node = waitingQueue.take();
					
					/* block - lock */
					lock.lock();
					
					try {
						/* set timer to each node */
						timer(node, waitingTime);
						
						/* search logic */
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
	
	/**
	 * Set the scheduled timer.
	 * The timer schedule timeout handler (specified task for execution).
	 * timeout handler is executed after the time specified in delay.
	 * 
	 * @param node	node to be scheduled	
	 * @param delay	delay in milliseconds before task is to be executed.
	 */
	private void timer(T node, long delay) {
		Timer timer = new Timer();
		node.setTimer(timer);
		timer.schedule(new TimerTask() {
			
			/**
			 * Timeout handler
			 */
			@Override @SuppressWarnings("unchecked")
			public void run() {
				remove(node);
				put((T) node.loosen());
			}
		}, delay);
	}
	
	/**
	 * Branch the node via match-logic.
	 * 
	 * @param node	node to be matched
	 */
	private void examine(T node) {
		if(node.getNeedPeerCount() == 0) {
			complete(node);
			return;
		}
		
		for(T wnode : waitingList) {
			if(node.match(wnode)) {
				/* Node into wnode's peer */
				wnode.setPeerNode(node);
				
				if(wnode.isPeerFull()) {
					/* Match complete */
					waitingList.remove(wnode);
					complete(wnode);
					return;
				} else {
					waitingList.remove(node);
					return;
				}
			}
		}
		/* New registration in list */
		insert(node);
	}
	
	/**
	 * Remove the node from this queue and list.
	 * and handle registered peer node or parent node.
	 * 
	 * @param node	node to be removed from this queue, list if present.
	 */
	public void remove(T node) {
		lock.lock();
		try {
			waitingQueue.remove(node);	// Remove if it in waitingQueue
			waitingList.remove(node);	// Remove if it in waitingList
			
			/* If node is registered as a peer somewhere, remove it */
			Matcher parentNode = node.getParent();
			if(parentNode != null) {
				parentNode.removePeerNode(node);
				node.setParent(null);
			}
			
			/* If the peer is registered, it re-makes the peers */
			List<Matcher> plist = node.getPeer();
			T firstNode = null;
			for(int i=0; i<plist.size(); i++) {
				@SuppressWarnings("unchecked")
				T pnode = (T) plist.get(i);
				if(i==0) {
					firstNode = pnode;
					firstNode.setParent(null);
					insert(firstNode);
				}
				else {
					/* remaining peers register on the first */
					firstNode.setPeerNode(pnode);
				}
			}
			
			node.setPeerClear();
		} finally {
			lock.unlock();
		}		
	}
	
	/**
	 * Insert in this waitingList at index just below the next higher node.
	 * 
	 * @param node	node to be inserted in this waitingList.
	 */
	private void insert(T node) {
		Integer idx = waitingList.size();
		Integer nNeedPeerCnt = node.getNeedPeerCount();
		
		/* below the next higher node */
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
	
	/**
	 * Completed node handler
	 * 
	 * @param node	the matched node completely
	 */
	private void complete(T node) {
		/* timer cancel */
		node.getTimer().cancel();
		for(Matcher pnode : node.getPeer()) 
			pnode.getTimer().cancel();
		
		try {
			/* put only node (node's peers are not transmitted) */
			resultQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receive incoming node from outside.
	 * and put the received node into the waitingQueue.
	 * 
	 * @param node	node to be matched
	 */
	public void put(T node) {
		/* put from outside */
		try {
			waitingQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send result node to outside.
	 * 
	 * @return	the matched node
	 */
	public T take() {
		/* take to outside */
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
