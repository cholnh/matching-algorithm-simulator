package server.model.blockingqueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private final Map<Integer, BlockingQueueNode> waitingMap = new HashMap<Integer, BlockingQueueNode>();
	MainFrame ui = UIMgr.getInstance().getMainFrame();
	
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
}
