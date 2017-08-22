package server.model.blockingqueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	/** Field */
	private final Thread coreThread = new Thread(new CoreHandler());
	private final BlockingQueue<BlockingQueueNode> waitingQueue = new LinkedBlockingQueue<BlockingQueueNode>();
	private final List<BlockingQueueNode> waitingList = Collections.synchronizedList(new ArrayList<BlockingQueueNode>());
	private Lock lock = new ReentrantLock();
	MainFrame ui = UIMgr.getInstance().getMainFrame();
	
	public BlockingQueue<BlockingQueueNode> getWaitingQueue() {
		return waitingQueue;
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
					BlockingQueueNode node = waitingQueue.take();	// block
					System.out.println("waitingQueue] " + node.getClientName() + " 받음");
					
					ui.setServerRemarks("lockwait", node.getClientName());
					lock.lock();
					try {
						ui.setServerRemarks("locking", node.getClientName());
						ui.setExamining(node.getClientName(), true);
						
						examine(node);
						
						if(node.getNeedPeerCount() != 0)
							ui.setExamining(node.getClientName(), false);
					} finally {
						ui.setServerRemarks("unlock", node.getClientName());
						lock.unlock();
					}
	
				} catch (InterruptedException e) {
					System.err.println("\n[core Stop]");
					e.printStackTrace();
					break;
				}	
			}
		}
		
	}
	
	private void examine(BlockingQueueNode node) {
		if(node.getNeedPeerCount() == 0) {
			ui.setServerPeer("no peer", node.getClientName());
			complete(node);
			return;
		}
		
		for(BlockingQueueNode wnode : waitingList) {
			if(isSameTPC(node, wnode) && wnode.isSimilarNode(node)) {
				// peer 등록
				wnode.setPeerNode(node);
				ui.setServerPeer(wnode.getPeerText(), wnode.getClientName());
				
				if(wnode.isPeerFull()) {
					// Full Peer
					System.out.println("complete] " + wnode.getClientName());
					
					waitingList.remove(wnode);
					//ui.removeServerTableNode(wnode.getClientName());
					
					complete(wnode);
					return;
				}
				else {
					// node -> wnode`s peer
					System.out.println("node -> wnode`s peer] " + node.getClientName() + " -> " + wnode.getClientName());
					
					waitingList.remove(node);
					ui.removeServerTableNode(node.getClientName());
					
					return;
				}
			}
		}
		
		// 탐색 실패 -> 신규등록
		System.out.println("신규등록] "+node.getClientName());
		
		ui.setServerStatus("신규등록", node.getClientName());
		
		insert(node);
	}
	
	public void timeOut(BlockingQueueNode timeOutNode) {
		timeOutNode.setLoosenNode();
		removeNode(timeOutNode);
	}
	
	private boolean isSameTPC(BlockingQueueNode arg1, BlockingQueueNode arg2) {
		// Total Peer Count
		return ((int)arg1.getTotalPeerCount() == (int)arg2.getTotalPeerCount());
	}
	
	private void complete(BlockingQueueNode node) {
		try {
			ui.completedServerNode(node.getClientName());
			node.getQueue().put(node);
			System.out.println("########################## node 보냄 " + node.getClientName());
			for(BlockingQueueNode pnode : node.getPeer()) {
				System.out.println("########################## pnode 보냄 " + pnode.getClientName());
				//ui.completedServerNode(pnode.getClientName());
				pnode.getQueue().put(node);
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void removeNode(BlockingQueueNode node) {
		ui.setServerRemarks("lockwait", node.getClientName());
		System.out.print("lock wait " + node.clientName + "...");
		try {
			if(lock.tryLock(5, TimeUnit.SECONDS)) {
				ui.setServerRemarks("locking", node.getClientName());
				System.out.println("done");
				try {
//					System.out.println("\n********************************************");
//					System.out.println("removeNode("+node.getClientName()+")");
//					System.out.println("제거 전 waitingList");
//					System.out.println(waitingList);
//					System.out.println(node.getClientName() + "제거 결과 - " + waitingList.remove(node));
					
					
					waitingQueue.remove(node);	// 큐에 대기중이라면 제거
					waitingList.remove(node);	// 리스트에 있다면 제거
					
					// 어딘가의 peer로 속해있다면 제거
					BlockingQueueNode parentNode = node.getParent();
					if(parentNode != null) {
						parentNode.removePeerNode(node);
						ui.setServerPeer(parentNode.getClientName(), parentNode.getPeerText());
						node.setParent(null);
						//return;
					}
					
					// 자신에게 peer가 등록되어있다면 제거 후 peer들 끼리 재등록 
					
					/*
					for(int i=0; i<plist.size(); i++) {
						BlockingQueueNode pnode = plist.get(i);
						pnode.setParent(null);
						
						insert(pnode);
					}
					node.setPeer(null);
					*/
					List<BlockingQueueNode> plist = node.getPeer();
					BlockingQueueNode firstNode = null;
					for(int i=0; i<plist.size(); i++) {
						BlockingQueueNode pnode = plist.get(i);
						if(i==0) {
							// 첫번째 피어노드
							firstNode = pnode;
							firstNode.setParent(null);
							insert(firstNode);
						}
						else {
							// 나머지
							firstNode.setPeerNode(pnode);
						}
					}
					
					if(firstNode != null) {
						ui.setServerTableNode("신규 등록", 
											firstNode.getClientName(), 
											firstNode.getOptionText(), 
											firstNode.getTotalPeerCount()+"", 
											firstNode.getPeerText(), 
											firstNode.getCurPeerCount()+"", 
											node.getClientName() + " 에서 이전");
					}
					
					ui.removeServerTableNode(node.getClientName());
					node.setPeerClear();
					
//					System.out.println("제거 후 waitingList");
//					System.out.println(waitingList);
//					System.out.println("\n********************************************");
				} finally {
					ui.setServerRemarks("unlock", node.getClientName());
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void insert(BlockingQueueNode node) {
		Integer idx = waitingList.size();
		Integer nNeedPeerCnt = node.getNeedPeerCount();
		
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
	
	/*
	public synchronized void examine(BlockingQueueNode node) {
			if(node.getTotalPeerCount() <= 0 || node.isPeerFull()) {
				complete(node);
				return ;
			}
			
			Integer emptyIdx = waitingMap.size();
			Integer nodeCnt = node.getCurPeerCount() + 1;
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			
			for(int i=0; i<waitingMap.size(); i++) {
				BlockingQueueNode wnode = waitingMap.get(i);
				if(wnode == null) emptyIdx = i;
				else {
					if(wnode.isSimilarPeer(node)) {
						Integer needCnt = wnode.getNeedPeerCount();
						if(needCnt == nodeCnt) {
							// 바로 등록 FP
							if(wnode.setPeerNode(node)) {
								complete(wnode);
								return;
							}
						}
						map.put(i, needCnt);
					}
				}
			}
			
			Integer IdxminVal = -1;
			Integer tmp = -1;
			List<Integer> minList = new ArrayList<Integer>();
			Iterator<Entry<Integer, Integer>> iterator = map.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Integer, Integer> entry = iterator.next();
				Integer idx = entry.getKey();
				Integer needPeerCount = entry.getValue();
				
				minList.add(idx);
				if(tmp == -1) {
					tmp = needPeerCount;
					continue;
				}
				if(tmp > needPeerCount) {
					tmp = needPeerCount;
					IdxminVal = idx;
				}
			}
			
			if(IdxminVal != -1) {
				// Into peer 
				BlockingQueueNode wnode = waitingMap.get(IdxminVal);
				if(wnode == null) {
					System.out.println("wnode Null!! minVal : " + IdxminVal);
					for(Integer idx : minList) {
						if((wnode = waitingMap.get(idx)) != null) {
							System.out.println("So minVal set - " + idx);
							break;
						}
					}
				}
				if(wnode == null) return;
				wnode.setPeerNode(node);		// peer로 등록
				ui.setServerPeer(wnode.getPeerText(), wnode.getClientName());
				System.out.println(node.clientName + "] Into peer - " + wnode.clientName + "  //  getTotalPeerCount" + wnode.getTotalPeerCount());
				
				Integer nodeIdx;
				synchronized (this) {
					if((nodeIdx = node.getIdx()) != null) {	// 신규node가 아닐 경우
						waitingMap.remove(nodeIdx);	// waitingMap에서 node 삭제
						node.setIdx(null);			// node의 Idx 초기화
						ui.removeServerTableNode(node.getClientName());
					}
				}
			} 
			else {
				// New
				System.out.println(node.clientName + "] New  //  getTotalPeerCount : " + node.getTotalPeerCount());
				synchronized (this) {
					node.setIdx(emptyIdx);			// node의 Idx 초기화
				}
				waitingMap.put(emptyIdx, node);	// waitingMap에 node 추가
				ui.setServerStatus("대기열등록", node.getClientName());
				ui.setServerIdx(emptyIdx+"", node.getClientName());
			}
		
		return;
	}
	public void insert(BlockingQueueNode node) {
		Integer Idx;
		synchronized (this) {
			if((Idx = node.getIdx()) != null) {
				if(waitingMap.containsKey(Idx)) {
					//ui.removeServerTableNode(node.getClientName());
					waitingMap.remove(Idx);
				}
			}
		}
		examine(node);
	}

	public void complete(BlockingQueueNode node) {
		try {
			synchronized (this) {
				if(node.getIdx() != null) {
					waitingMap.remove(node.getIdx());
				}
				ui.completedServerNode(node.getClientName());
			}
			node.getQueue().put(node);
		
			for(BlockingQueueNode pnode : node.getPeer()) {
				pnode.getQueue().put(node);
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public BlockingQueueNode getNode(String clientName) {
		for(BlockingQueueNode wnode : waitingList) {
			if(wnode.getClientName().equals(clientName)) {
				return wnode;
			}
		}
		return null;
	}
	*/
	
	
}
