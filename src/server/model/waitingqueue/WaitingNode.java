package server.model.waitingqueue;
import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * WaitingNode
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class WaitingNode {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("WaitingNode");

	private static void log(String text) {
		System.out.println(text);
	}
	
	private String clientName;
	private String[] option;
	private volatile WaitingNode[] peerNode;
	private volatile Integer peerCurNumber = 0;
	private Integer index;
	private Integer minusError = 0;
	private Integer plusError = 0;
	private Integer waitingTime = 0;
	
	public WaitingNode(String clientName, Integer peerCount, Integer...args) {
		this.clientName = clientName;
		peerNode = new WaitingNode[peerCount];
		setOpt(args);
	}
	
	public String getClientName() {
		return this.clientName;
	}
	
	public Integer getCurPeerNumber() {
		return peerCurNumber;
	}
	
	public Integer getPeerCount() {
		return peerNode.length;
	}
	
	public String getOption() {
		String text = "";
		for(String opt : option)
			text += opt + " ";
		return text;
	}
	
	public String getPeerNode() {
		String text = "";
		for(int i=0; i<peerCurNumber; i++)
			text += peerNode[i] + " ";
		return text;
	}
	
	public void setTimer(Integer secs) {
		this.waitingTime = secs;
	}
	
	public Integer getTimer() {
		return waitingTime;
	}
	
	public void resetTimer() {
		this.waitingTime = 0;
	}
	
	@Override
	public String toString() {
		String text = "name : " + clientName+ " (";
		for(String opt : option) {
			text += opt + " ";
		}
		text += ") Peer : ";
		if(peerCurNumber != 0) {
			text += "["+peerCurNumber + "] ";
			for(WaitingNode node : peerNode) {
				if(node == null) continue;
				text += "name : " + node.clientName + " (";
				for(String opt : node.option) {
					text += opt + " ";
				}
				text += ") ";
			}
		}
		else
			text += "no peer";
		return text;
	}
	
	public void setOpt(Integer...args) {
		final String[] CATEGORY = {"Red", "Orange", "Yellow", "Grean", "Blue", "Navy", "Purple"};

		if((index = args[0]) < 0 || index > CATEGORY.length-1) {
			// error
			log("setOpt args error");
			return;
		}
		
		if(args.length == 1) {
			this.option = new String[1];
			this.option[0] = CATEGORY[index];
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
			
		if((minusError = index - minusError) < 0 || minusError > index) {
			// error
			log("setOpt error - Set minusError-value to 0");
			minusError = 0;
		}
		if((plusError = index + plusError) >= CATEGORY.length || plusError < index) {
			// error
			log("setOpt error - Set plusError-value to " + (CATEGORY.length-1));
			plusError = CATEGORY.length-1;
		}
		
		this.option = new String[plusError-minusError+1];
		
		for(int i=minusError, j=0; i<plusError+1; i++,j++) {
			this.option[j] = CATEGORY[i];
		}
	}
	
	public boolean isSimilarPeer(WaitingNode node) {
		// this option
		if(!isContain(this.option, node.option)) return false;
		
		// peer option
		for(int i=0; i<peerCurNumber; i++) {
			WaitingNode peer = peerNode[i];
			if(!isContain(peer.option, node.option)) return false;
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
	
	public synchronized boolean setPeerNode(WaitingNode node) {
		if(isPeerFull()) {
			log("setPeerNode isPeerFull Warning!!");
			//complete(node);
			return false;
		}
		peerNode[peerCurNumber++] = node;
		return true;
	}
	
	public synchronized boolean isPeerFull() {
		return peerCurNumber >= peerNode.length;
	}
	
	public WaitingNode setLoosen() {
		if(plusError + minusError > 7) { // 7 is CATEGORY length
			WaitingNode[] newWN = new WaitingNode[peerNode.length - 1];
			for(int i=0; i<newWN.length; i++) {
				newWN[i] = peerNode[i];
			}
			peerNode = newWN;
		}
		else
			setOpt(index, ++minusError, ++plusError);
		return this;
	}
}
