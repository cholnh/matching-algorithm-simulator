package network_simulator;
import java.util.ArrayList;

import matchingqueue.Matcher;

/**
 * CustomMatcher
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class CustomMatcher extends Matcher {

	private static final long serialVersionUID = 6136630519147175854L;
	
	private String clientName;
	private String[] option;
	private Integer categoryIndex;
	private Integer minusError = 0;
	private Integer plusError = 0;
	private String[] repOption;
	
	public CustomMatcher (String clientName, Integer totalPeerCount, Integer...args) {
		super(totalPeerCount);
		this.clientName = clientName;
		this.setOpt(args);
	}
	
	public String getClientName() {
		return clientName;
	}
	public String[] getOption() {
		return option;
	}
	public String getParentText() {
		return parent == null ? "" : ((CustomMatcher) parent).getClientName();
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
			CustomMatcher node = (CustomMatcher) peer.get(i);
			text += node.getClientName();
			text += "(";
			text += node.getOptionText();
			text += ")";
			if(i != peer.size() - 1)
				text += ", ";
		}
		return text;
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
			for(Matcher node : peer) {
				if(node == null) continue;
				CustomMatcher cnode = (CustomMatcher)node;
				text += cnode.getClientName() + " (";
				for(int i=0; i<cnode.option.length; i++) {
					text += cnode.option[i];
					if(i != cnode.option.length - 1)
						text += " ";
				}
				text += ") ";
			}
		}
		text += "]";
		return text;
	}
	
	@Override
	public void setPeerNode(Matcher node) {
		super.setPeerNode(node);
		
		/* 교집합을 구하여 대표 Option으로 설정 */
		repOption = capOpt((CustomMatcher) node);
	}
	
	@Override
	public boolean match(Matcher node) {
		CustomMatcher cnode = (CustomMatcher) node;
		
		/* Matching Condition */
		if(!isSameTPC(cnode)) return false;
		if(isContain(this.repOption, cnode.getOption()))
			return true;
		return false;		
	}
	
	@Override
	public Matcher loosen() {
		/* 주어진 Option에 대한 제한을 느슨하게 하는 커스텀 핸들러 */
		
		if(plusError + minusError >= 6) {
			/* 총 피어 수를 줄임 */
			totalPeerCount--;
		}
		else {
			/* Option 제한을 넓힘 */
			minusError++;
			plusError++;
			setOpt(categoryIndex, minusError, plusError);
		}
		return this;
	}
	
	private boolean isSameTPC(CustomMatcher node) {
		/* Total Peer Count */
		return ((int)totalPeerCount == (int)node.getTotalPeerCount());
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
	
	private String[] capOpt(CustomMatcher node) {
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
}
