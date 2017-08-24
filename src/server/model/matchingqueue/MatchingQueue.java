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

import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * MatchingQueue
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class MatchingQueue <T extends Matcher> {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("MatchingQueue");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	private Lock lock = new ReentrantLock();
	private final Thread coreThread = new Thread(new CoreHandler());
	private final BlockingQueue<T> waitingQueue = new LinkedBlockingQueue<T>();
	private final BlockingQueue<T> resultQueue = new LinkedBlockingQueue<T>();
	private final List<T> waitingList = Collections.synchronizedList(new ArrayList<T>());
	
	public MatchingQueue () {
		coreThread.start();
	}
	
	class CoreHandler implements Runnable {

		@Override
		public void run() {
			while(true) {
				try {
					
					/* block - queue */
					T node = waitingQueue.take();
					log(node.hashCode() + "] 입장");
					
					/* block - lock */
					lock(node);
					
					try {
						locking(node);
						
						/* CORE LOGIC */
						timer(node, 30000);
						examine(node);
						
					} finally {
						unlock(node);
					}
	
				} catch (InterruptedException e) {
					//log("CORE STOP", e);
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
				log(node.hashCode() + "] 재등록");
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
					log(wnode.hashCode() + "] 완성");
					complete(wnode);
					return;
				} else {
					/* 등록 결과 : 다른 peer로 들어가서 대기 */
					waitingList.remove(node);
					log(node.hashCode() + "] peer로 등록 - " + wnode.hashCode());
					return;
				}
			}
		}
		/* 탐색 실패 -> 신규등록 */
		insert(node);
		log(node.hashCode() + "] new");
	}
	
	public void remove(T node) {
		lock(node);
		try {
			locking(node);
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
			unlock(node);
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
		for(Matcher pnode : node.getPeer()) {
			pnode.getTimer().cancel();
		}
		
		try {
			resultQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void put(T node) {
		try {
			waitingQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public T take() {
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void lock(T node) {
		lock.lock();
	}
	private void locking(T node) {
	}
	private void unlock(T node) {
		lock.unlock();
	}

}
