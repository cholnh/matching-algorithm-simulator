package server.model.blockingqueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * BlockingQueueMgr
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class BlockingQueueMgr {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("BlockingQueueMgr");
	@SuppressWarnings("unused")
	private static void log(String text) {
		System.out.println(text);
	}
	
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
	private final Map<Integer, Node> waitingMap = Collections.synchronizedMap(new HashMap<Integer, Node>());
	
	public void examine(Node node) {
		
		if(node.getTotalPeerCount() <= 0) {
			complete(node);
			return ;
		}
		
		Integer emptyIdx = waitingMap.size();
		Integer nodeCnt = node.getCurPeerCount() + 1;
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		for(int i=0; i<waitingMap.size(); i++) {
			Node wnode = waitingMap.get(i);
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
		
		Integer minVal = -1;
		Iterator<Entry<Integer, Integer>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			Integer needPeerCount = iterator.next().getValue();
			if(minVal == -1) {
				minVal = needPeerCount;
				continue;
			} 
			if(minVal > needPeerCount) {
				minVal = needPeerCount;
			}
		}
		
		if(minVal != -1) {
			// Into peer 
			Node wnode = waitingMap.get(minVal);
			wnode.setPeerNode(node);
		} 
		else {
			// New
			waitingMap.put(emptyIdx, node);
		}
		return;
	}
	
	public void insert(Node node) {
		examine(node);
	}
	
	public void insert(Node node, Integer Idx) {
		if(waitingMap.containsKey(Idx)) {
			waitingMap.remove(Idx);
		}
		insert(node);
	}
	
	public void complete(Node node) {
		try {
			node.getQueue().put(node);
		
			for(Node pnode : node.getPeer()) {
				pnode.getQueue().put(node);
			}
			
			if(node.getIdx() != null)
				waitingMap.remove(node.getIdx());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
