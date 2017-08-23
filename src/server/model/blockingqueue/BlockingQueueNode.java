package server.model.blockingqueue;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import server.control.manager.UIMgr;
import server.view.MainFrame;

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
		return parent == null ? "" : parent.getClientName();
	}
	
	public boolean isSimilarNode(BlockingQueueNode node) {
		/* Matching Condition */
		
		if(!isSameTCP(node)) return false;
		if(isContain(this.repOption, node.getOption()))
			return true;
		return false;		
	}
	
	private String[] capOpt(BlockingQueueNode node) {
		/* 중복되는 Option(교집합) 반환 */
		
		ArrayList<String> capList = new ArrayList<String>();
		for(String thisOpt : this.option)
			for(String nodeOpt : node.getOption())
				if(thisOpt.equals(nodeOpt)) {
					capList.add(thisOpt);
				}
		String[] returnArr = new String[capList.size()];
		for(int i=0; i<returnArr.length; i++) {
			returnArr[i] = capList.get(i);
		}
		return returnArr;
	}
	
	private boolean isContain(String[] strArr1, String[] strArr2) {
		/* 인자로 받은 두 스트링 배열의 값 중 중복되는 것이 하나라도 있으면 true반환, 그 외 false반환 */
		
		for(String thisOpt : strArr1) {
			for(String nodeOpt : strArr2) {
				if(thisOpt.equals(nodeOpt)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isSameTCP(BlockingQueueNode node) {
		/* Total Peer Count */
		
		return ((int)totalPeerCount == (int)node.getTotalPeerCount());
	}
	
	public Node waitComplete(long timeout, TimeUnit unit) {
		MainFrame ui = UIMgr.getInstance().getMainFrame();
		Node completedNode = null;
		try {
			/* 노드가 원하는 조건을 만족(피어를 모두 채움)할 때 까지 poll */
			completedNode = queue.poll(timeout, unit);
		} catch (InterruptedException e) {
		}	
		
		/* blocking queue의 poll TIMEOUT -> null이 반환됨 */
		if(completedNode == null) {
			ui.setServerStatus("조건변경중", clientName);
			setLoosenNode();
		}
		return completedNode;
	}
	
	public boolean isPeerFull() {
		/* 피어가 꽉 찼는지에 대한 여부 반환 = 매칭 성공여부 */
		
		return peer.size() >= totalPeerCount;
	}

	public boolean removePeerNode(BlockingQueueNode node) {
		/* 현재 피어에서 node 제거 후 결과 성공여부 반환*/
		
		return peer.remove(node);
	}
	
	public void setPeerNode(BlockingQueueNode node) {
		/* peer에 node 삽입 */
		
		peer.add(node);
		node.setParent(this);
		
		/* 교집합을 구하여 대표 Option으로 설정 */
		repOption = capOpt(node);
	}
	
	public BlockingQueueNode setLoosenNode() {
		/* 주어진 Option에 대한 제한을 느슨하게 하는 커스텀 핸들러 */
		
		if(plusError + minusError >= 6) {
			/* 총 피어 수를 줄임 */
			totalPeerCount--;
		}
		else {
			/* Option 제한을 넓힘 */
			minusError++;
			plusError++;
			super.setOpt(categoryIndex, minusError, plusError);
		}
		return this;
	}
}
