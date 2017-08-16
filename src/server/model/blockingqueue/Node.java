package server.model.blockingqueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * Node
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class Node {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("Node");
	private static void log(String text) {
		System.out.println(text);
	}
	
	/** Field */
	private Integer Idx;
	private BlockingQueue<Node> queue;
	private String clientName;
	private Node parent;
	private List<Node> peer;	// synchronizedList
	private Integer totalPeerCount = 0;
	
	private String[] option;
	private Integer categoryIndex;
	private Integer minusError = 0;
	private Integer plusError = 0;
	
	public void setIdx(Integer Idx) {
		this.Idx = Idx;
	}
	public Integer getIdx() {
		return Idx;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public BlockingQueue<Node> getQueue() {
		return queue;
	}
	public String getClientName() {
		return clientName;
	}
	public List<Node> getPeer() {
		return peer;
	}
	public Integer getTotalPeerCount() {
		return totalPeerCount;
	}
	public Integer getCurPeerCount() {
		return peer.size();
	}
	public Integer getNeedPeerCount() {
		return totalPeerCount - peer.size();
	}
	public String[] getOption() {
		return option;
	}
	public String getOptionText() {
		String text = "";
		for(int i=0; i<option.length; i++) {
			text += option[i];
			if(i != option.length - 1)
				text += " ";
		}
		return text;
	}
	public String getPeerText() {
		String text = "";
		for(int i=0; i<peer.size(); i++) {
			text += peer.get(i);
			if(i != peer.size() - 1)
				text += " ";
		}
		return text;
	}
	
	public Node(BlockingQueue<Node> queue, String clientName, Integer totalPeerCount, Integer...args) {
		this.queue = queue;
		this.clientName = clientName;
		this.totalPeerCount = totalPeerCount;
		this.peer = Collections.synchronizedList(new ArrayList<Node>(totalPeerCount));
		setOpt(args);
	}
	
	@Override
	public String toString() {
		String text = clientName+ " (";
		for(int i=0; i<option.length; i++) {
			text += option[i];
			if(i != option.length - 1)
				text += " ";
		}
		text += ")\tPeer [";
		
		if(peer.size() == 0) 
			text += "no peer";
		else {
			for(Node node : peer) {
				if(node == null) continue;
				text += node.clientName + " (";
				for(int i=0; i<node.option.length; i++) {
					text += node.option[i];
					if(i != node.option.length - 1)
						text += " ";
				}
				text += ") ";
			}
		}
		text += "]";
		return text;
	}
	
	public void setOpt(Integer...args) {
		final String[] CATEGORY = {"Red", "Orange", "Yellow", "Grean", "Blue", "Navy", "Purple"};

		if((categoryIndex = args[0]) < 0 || categoryIndex > CATEGORY.length-1) {
			// error
			log("setOpt args error");
			return;
		}
		
		if(args.length == 1) {
			this.option = new String[1];
			this.option[0] = CATEGORY[categoryIndex];
			return;
		}
		else if (args.length == 3) {
			minusError = args[1];
			plusError = args[2];
		}
		else {
			// error
			log("setOpt args error");
			return;
		}
			
		if((minusError = categoryIndex - minusError) < 0 || minusError > categoryIndex) {
			// error
			log("setOpt error - Set minusError-value to 0");
			minusError = 0;
		}
		if((plusError = categoryIndex + plusError) >= CATEGORY.length || plusError < categoryIndex) {
			// error
			log("setOpt error - Set plusError-value to " + (CATEGORY.length-1));
			plusError = CATEGORY.length-1;
		}
		
		this.option = new String[plusError-minusError+1];
		
		for(int i=minusError, j=0; i<plusError+1; i++,j++) {
			this.option[j] = CATEGORY[i];
		}
	}
	
	public boolean isSimilarPeer(Node node) {
		// this option
		if(!isContain(this.option, node.getOption())) return false;
		
		// this peer option
		for(int i=0; i<peer.size(); i++) {
			Node pnode = peer.get(i);
			if(!isContain(pnode.option, node.option)) return false;
		}
		
		// node peer option
		List<Node> plist = node.getPeer();
		if(!plist.isEmpty()) {
			for(int i=0; i<plist.size(); i++) {
				// this option
				Node pnode = plist.get(i);
				if(!isContain(this.option, pnode.option)) return false;
				
				// this peer option
				for(int j=0; j<peer.size(); j++) {
					Node thisPnode = peer.get(j);
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
		return peer.size() == totalPeerCount;
	}
	
	private Lock peerLock;
	
	public boolean removePeerNode(Node node) {
		synchronized (peerLock) {
			for(Node pnode : peer) {
				if(pnode.clientName.equals(node.clientName)) {
					peer.remove(pnode);
					return true;
				}
			}
			return false;
		}
	}
	
	// 재귀 형태로 수정해야하나?
	public boolean setPeerNode(Node node) {
		synchronized (peerLock) {
			if(isPeerFull()) {
				return false;
			}
			Integer needCnt = getNeedPeerCount();
			if(needCnt > 0) {
				peer.add(node);
				List<Node> plist;
				while(!(plist = node.getPeer()).isEmpty() && (needCnt = getNeedPeerCount()) > 0) {
					for(int i=0; i<plist.size(); i++) {
						Node pnode = plist.get(i);
						if(pnode != null) {
							peer.add(pnode);
							break;
						}
					}
				}
				plist.clear();
				return true;
			}
			return false;
		}
	}
	
	public Node setLoosenNode() {
		if(parent != null)
			parent.removePeerNode(this);
		
		if(plusError + minusError > 7) { // 7 is CATEGORY length
			synchronized (peerLock) {
				totalPeerCount--;
			}
		}
		else
			setOpt(categoryIndex, ++minusError, ++plusError);
		return this;
	}
}
