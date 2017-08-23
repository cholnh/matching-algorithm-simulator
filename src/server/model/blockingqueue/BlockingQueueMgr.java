package server.model.blockingqueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.control.manager.UIMgr;
import server.view.MainFrame;

/**
 * BlockingQueueMgr
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class BlockingQueueMgr {
	
	/** INSTANCE */
	private static class Singleton {
		/** Initialization On Demand Holder Idiom */
		private static final BlockingQueueMgr INSTANCE = new BlockingQueueMgr();
	}
	public static synchronized BlockingQueueMgr getInstance() {
		return Singleton.INSTANCE;
	}
	private BlockingQueueMgr() {/** Singleton */}
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("BlockingQueueMgr");
	static void log(final String text) {
		logger.info(text);
	}
	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}
	
	/** Field */
	private Lock lock = new ReentrantLock();
	private MainFrame ui = UIMgr.getInstance().getMainFrame();
	private final Thread coreThread = new Thread(new CoreHandler());
	private final BlockingQueue<BlockingQueueNode> waitingQueue = new LinkedBlockingQueue<BlockingQueueNode>();
	private final List<BlockingQueueNode> waitingList = Collections.synchronizedList(new ArrayList<BlockingQueueNode>());
	
	
	public BlockingQueue<BlockingQueueNode> getWaitingQueue() {
		return waitingQueue;
	}
	
	public void put(BlockingQueueNode node) {
		ui.setServerTableNode("대기열검색중", node);
		
		try {
			waitingQueue.put(node);
		} catch (InterruptedException e) {
			/* blocking queue - full */
			log("put InterruptedException", e);
		}
	}
	
	public BlockingQueueNode take() {
		BlockingQueueNode node = null;
		try {
			node = waitingQueue.take();
		} catch (InterruptedException e) {
			log("BlockingQueueNode take InterruptedException", e);
			coreStop();
		}
		return node;
	}
	
	public void coreStart() {
		if(coreThread.isAlive()) return;
		if(!coreThread.isInterrupted()) {
			coreThread.start();
		}
	}
	
	public void coreStop() {
		coreThread.interrupt();
	}
	
	class CoreHandler implements Runnable {

		@Override
		public void run() {
			while(true) {
				try {
					
					/* block - queue */
					BlockingQueueNode node = waitingQueue.take();
					
					/* block - lock */
					lock(node);
					
					try {
						/* UI */
						locking(node);
						
						/* CORE LOGIC */
						examine(node);
						
					} finally {
						unlock(node);
					}
	
				} catch (InterruptedException e) {
					log("CORE STOP", e);
					break;
				}	
			}
		}
		
	}
	
	private void lock(BlockingQueueNode node) {
		ui.setServerRemarks("lockwait", node.getClientName());
		lock.lock();
	}
	private void locking(BlockingQueueNode node) {
		ui.setServerRemarks("locking", node.getClientName());
	}
	private void unlock(BlockingQueueNode node) {
		ui.setServerRemarks("unlock", node.getClientName());
		lock.unlock();
	}
	
	private void examine(BlockingQueueNode node) {
		if(node.getNeedPeerCount() == 0) {
			complete(node);
			ui.setServerPeer("no peer", node.getClientName());
			return;
		}
		
		for(BlockingQueueNode wnode : waitingList) {
			if(wnode.isSimilarNode(node)) {
				/* peer 등록 */
				ui.removeServerTableNode(node.getClientName());
				wnode.setPeerNode(node);
				ui.setServerPeer(wnode.getPeerText(), wnode.getClientName());
				ui.setServerCur(wnode.getCurPeerCount()+"", wnode.getClientName());
				ui.setServerOption(wnode.getOptionText()+wnode.getRepText(), wnode.getClientName());
				
				if(wnode.isPeerFull()) {
					/* 등록결과 : 매칭 성공 */
					waitingList.remove(wnode);
					complete(wnode);
					return;
				}
				else {
					/* 등록 결과 : 다른 peer로 들어가서 대기 */
					waitingList.remove(node);
					return;
				}
			}
		}
		
		/* 탐색 실패 -> 신규등록 */
		insert(node);
		ui.setServerStatus("신규등록", node.getClientName());
	}
	
	private void complete(BlockingQueueNode node) {
		try {
			ui.completedServerNode(node.getClientName());
			
			/* 결과 반환 */
			node.getQueue().put(node);
			for(BlockingQueueNode pnode : node.getPeer()) {
				pnode.getQueue().put(node);
			}
		} catch (InterruptedException e) {
			log("complete InterruptedException", e);
		}
	}
	
	public void remove(BlockingQueueNode node) {
		lock(node);
		try {
			locking(node);
			waitingQueue.remove(node);	// 큐에 대기중이라면 제거
			waitingList.remove(node);	// 리스트에 있다면 제거
			
			/* 어딘가의 peer로 속해있다면 제거 */
			BlockingQueueNode parentNode = node.getParent();
			if(parentNode != null) {
				parentNode.removePeerNode(node);
				ui.setServerPeer(parentNode.getPeerText(), parentNode.getClientName());
				node.setParent(null);
			}
			
			/* peer 정리 */
			List<BlockingQueueNode> plist = node.getPeer();
			BlockingQueueNode firstNode = null;
			for(int i=0; i<plist.size(); i++) {
				BlockingQueueNode pnode = plist.get(i);
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
			
			if(firstNode != null)
				ui.setServerTableNode("신규 등록", firstNode);
			
			ui.removeServerTableNode(node.getClientName());
			node.setPeerClear();
		} finally {
			unlock(node);
		}		
	}
	
	private void insert(BlockingQueueNode node) {
		Integer idx = waitingList.size();
		Integer nNeedPeerCnt = node.getNeedPeerCount();
		
		/* 자신보다 한 단계 높은 노드의 바로 아래 인덱스 */
		for(int i=0; i<waitingList.size(); i++) {
			BlockingQueueNode wnode = waitingList.get(i);
			Integer wNeedPeerCnt = wnode.getNeedPeerCount();
			
			if(nNeedPeerCnt < wNeedPeerCnt) {
				idx = waitingList.indexOf(wnode);	// Not i
				break;
			}
		}
		waitingList.add(idx, node);
	}	
}
