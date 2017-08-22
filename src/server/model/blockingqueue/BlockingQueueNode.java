package server.model.blockingqueue;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Node
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class BlockingQueueNode extends Node {

	private static final long serialVersionUID = 1L;
	
	/** Field */
	private BlockingQueue<Node> queue;
	private BlockingQueueNode parent;

	public BlockingQueueNode(String clientName, Integer totalPeerCount, Integer...args) {
		super(clientName, totalPeerCount, args);
		queue = new ArrayBlockingQueue<Node>(1);
	}
	
	public BlockingQueue<Node> getQueue() {
		return queue;
	}
	public void setParent(BlockingQueueNode parent) {
		this.parent = parent;
	}
	public BlockingQueueNode getParent() {
		return parent;
	}
	public String getParentText() {
		return parent == null ? null : parent.getClientName();
	}
	
	public boolean isSimilarNode(BlockingQueueNode node) {
		// this option
		if(!isContain(this.option, node.getOption())) return false;
		
		// this peer option
		for(int i=0; i<peer.size(); i++) {
			BlockingQueueNode pnode = peer.get(i);
			if(!isContain(pnode.option, node.option)) return false;
		}
		return true;
	}
	
	public boolean isSimilarPeer(BlockingQueueNode node) {
		// this option
		if(!isContain(this.option, node.getOption())) return false;
		
		// this peer option
		for(int i=0; i<peer.size(); i++) {
			BlockingQueueNode pnode = peer.get(i);
			if(!isContain(pnode.option, node.option)) return false;
		}
		
		// node peer option
		List<BlockingQueueNode> plist = node.getPeer();
		if(!plist.isEmpty()) {
			for(int i=0; i<plist.size(); i++) {
				// this option
				BlockingQueueNode pnode = plist.get(i);
				if(!isContain(this.option, pnode.option)) return false;
				
				// this peer option
				for(int j=0; j<peer.size(); j++) {
					BlockingQueueNode thisPnode = peer.get(j);
					if(!isContain(thisPnode.option, pnode.option)) return false;
				}
			}
			
		}
		return true;
	}
	
	private boolean isContain(String[] strArr1, String[] strArr2) {
		for(String thisOpt : strArr1) {
			for(String nodeOpt : strArr2) {
				if(thisOpt.equals(nodeOpt)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPeerFull() {
		return peer.size() >= totalPeerCount;
	}
	

	public boolean removePeerNode(BlockingQueueNode node) {
		return peer.remove(node);
		/*
		for(BlockingQueueNode pnode : peer) {
			if(pnode.clientName.equals(node.getClientName())) {
				peer.remove(pnode);
				return true;
			}
		}
		
		return false;
		 */
	}
	
	public void setPeerNode(BlockingQueueNode node) {
		// try node into -> this`s peer
		
		if(isPeerFull()) {
			return;
		}
		peer.add(node);
		node.setParent(this);
		
		/*
		if(isPeerFull()) {
			return false;
		}
		Integer needCnt = getNeedPeerCount();
		if(needCnt > 0) {
			peer.add(node);			// 노드 등록
			node.setParent(this);	// 노드의 부모로 등록
			System.out.println(clientName + "] " + node.clientName + "가 " + clientName + " 에 등록됨");
			// node`s peer
			List<BlockingQueueNode> plist;
			while(!(plist = node.getPeer()).isEmpty() && (needCnt = getNeedPeerCount()) > 0) {
				for(int i=0; i<plist.size(); i++) {
					BlockingQueueNode pnode = plist.get(i);
					if(pnode != null) {
						peer.add(pnode);			// 노드 등록
						pnode.setParent(this);		// 노드의 부모로 등록
						break;
					}
				}
			}
			System.out.println(clientName+ "] 최종결과\t" + toString());
			plist.clear();
			return true;
		}
		return false;
		*/
	}
	
	public BlockingQueueNode setLoosenNode() {
		
		if(plusError + minusError >= 6) { // 7 is CATEGORY length
			totalPeerCount--;
			System.out.println(clientName + "] loosen\tplusError : "+plusError+" minusError : " + minusError + " totalPeerCount : "+totalPeerCount);
			
		}
		else {
			minusError++;
			plusError++;
			System.out.println(clientName + "] loosen\tplusError : "+plusError+" minusError : " + minusError + " totalPeerCount : "+totalPeerCount);
			
			super.setOpt(categoryIndex, minusError, plusError);
			
		}
		return this;
	}
}
