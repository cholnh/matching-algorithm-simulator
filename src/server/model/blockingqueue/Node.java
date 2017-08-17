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
	protected volatile List<BlockingQueueNode> peer;	// synchronizedList 
	protected volatile Integer totalPeerCount = 0;
	
	protected String[] option;
	protected Integer categoryIndex;
	protected Integer minusError = 0;
	protected Integer plusError = 0;
	
	public Node(String clientName, Integer totalPeerCount, Integer...args) {
		this.clientName = clientName;
		this.totalPeerCount = totalPeerCount;
		this.peer = Collections.synchronizedList(new ArrayList<BlockingQueueNode>(totalPeerCount));
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
	public String getPeerText() {
		String text = "";
		for(int i=0; i<peer.size(); i++) {
			text += peer.get(i);
			if(i != peer.size() - 1)
				text += " ";
		}
		return text;
	}
	
	protected void setOpt(Integer...args) {
		final String[] CATEGORY = {"Red", "Orange", "Yellow", "Grean", "Blue", "Navy", "Purple"};

		if((categoryIndex = args[0]) < 0 || categoryIndex > CATEGORY.length-1) {
			// error
			//log("setOpt args error");
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
			//log("setOpt args error");
			return;
		}
			
		if((minusError = categoryIndex - minusError) < 0 || minusError > categoryIndex) {
			// warning
			//log("setOpt warning - Set minusError-value to 0");
			minusError = 0;
		}
		if((plusError = categoryIndex + plusError) >= CATEGORY.length || plusError < categoryIndex) {
			// warning
			//log("setOpt warning - Set plusError-value to " + (CATEGORY.length-1));
			plusError = CATEGORY.length-1;
		}
		
		this.option = new String[plusError-minusError+1];
		
		for(int i=minusError, j=0; i<plusError+1; i++,j++) {
			this.option[j] = CATEGORY[i];
		}
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
