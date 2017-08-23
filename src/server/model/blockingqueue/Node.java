package server.model.blockingqueue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CompactNode
 * 
 * @version 1.0 [2017. 8. 17.]
 * @author Choi
 */
public class Node implements Serializable {

	private static final long serialVersionUID = 8057910237830080455L;

	/** Field */
	protected String clientName;
	protected volatile List<BlockingQueueNode> peer;
	protected volatile Integer totalPeerCount = 0;
	
	protected String[] option;
	protected Integer categoryIndex;
	protected Integer minusError = 0;
	protected Integer plusError = 0;
	
	protected String[] repOption;
	
	public Node(String clientName, Integer totalPeerCount, Integer...args) {
		this.clientName = clientName;
		this.totalPeerCount = totalPeerCount;
		this.peer = Collections.synchronizedList(new ArrayList<BlockingQueueNode>());
		this.setOpt(args);
	}

	public String getClientName() {
		return clientName;
	}
	public List<BlockingQueueNode> getPeer() {
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
	public String getRepText() {
		String text = "";
		text += " [대표 : ";
		for(int i=0; i<repOption.length; i++) {
			text += repOption[i];
			if(i != repOption.length - 1)
				text += " ";
		}
		text += "]";
		return text;
	}
	public String getPeerText() {
		String text = "";
		for(int i=0; i<peer.size(); i++) {
			BlockingQueueNode node = peer.get(i);
			text += node.clientName;
			text += "(";
			text += node.getOptionText();
			text += ")";
			if(i != peer.size() - 1)
				text += ", ";
		}
		return text;
	}
	public void setPeerClear() {
		peer.clear();
	}
	
	protected void setOpt(Integer...args) {
		//final String[] CATEGORY = {"Red", "Orange", "Yellow", "Green", "Blue", "Navy", "Purple"};
		final String[] CATEGORY = {"빨", "주", "노", "초", "파", "남", "보"};

		if((categoryIndex = args[0]) < 0 || categoryIndex > CATEGORY.length-1) {
			return;
		}
		
		if(args.length == 1) {
			option = new String[1];
			option[0] = CATEGORY[categoryIndex];
			repOption = option;
			return;
		}
		else if (args.length == 3) {
			minusError = args[1];
			plusError = args[2];
		}
		else {
			return;
		}
		
		Integer tmpMin = 0;
		Integer tmpMax = 0;
		
		/* 최소값 설정 */
		if(minusError > 0 && minusError < categoryIndex) 
			tmpMin = categoryIndex - minusError;
		else 
			tmpMin = 0;
		
		/* 최대값 설정 */
		if(plusError > 0 && plusError + categoryIndex < CATEGORY.length) 
			tmpMax = categoryIndex + plusError;
		else 
			tmpMax = CATEGORY.length - 1;
		
		/* 최소, 최대값에 따른 option 설정 */
		option = new String[tmpMax-tmpMin+1];
		for(int i=tmpMin, j=0; i<tmpMax+1; i++,j++)
			option[j] = CATEGORY[i];
		
		/* 대표 option 설정 */
		repOption = option;
	}
	
	@Override
	public String toString() {
		String text = clientName+ " (";
		for(int i=0; i<option.length; i++) {
			text += option[i];
			if(i != option.length - 1)
				text += " ";
		}
		text += ")\tPeer : " + totalPeerCount + " [";
		
		if(peer.size() == 0) 
			text += "no peer";
		else {
			for(BlockingQueueNode node : peer) {
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
}
