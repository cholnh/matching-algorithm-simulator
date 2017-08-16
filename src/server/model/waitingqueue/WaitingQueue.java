package server.model.waitingqueue;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import server.control.log.LogMgr;
import server.control.manager.UIMgr;
import server.control.util.UITimer;

/**
 * WaitingQueue
 * 
 * @version 1.0 [2017. 8. 15.]
 * @author Choi
 */
public class WaitingQueue {

	/** Log */
	static Logger logger = LogMgr.getInstance("WaitingQueue");
	private static void log(String text) {
		System.out.println(text);
	}
	
	/** INSTANCE */
	private static class Singleton {
		/** Initialization On Demand Holder Idiom */
		private static final WaitingQueue INSTANCE = new WaitingQueue();
	}
	public static synchronized WaitingQueue getInstance() {
		return Singleton.INSTANCE;
	}
	private WaitingQueue() {/** Singleton */}
	
	/** Field */
	private final List<WaitingNode> waitingList = new ArrayList<WaitingNode>();
	private volatile Integer completeCount = 0;
	private volatile Integer connectCount = 0;
	
	public synchronized void addConnectCount() {
		this.connectCount++;
	}
	
	public Integer getConnectCount() {
		return connectCount;
	}
	
	public Integer getCompleteCount() {
		return completeCount;
	}
	
	public WaitingNode setWaitingNode(WaitingNode node) {
		if(node.getPeerCount() <= 0) {
			complete(node);
			return node;
		}
		
		for(WaitingNode wNode : waitingList) {
			if(wNode.getClientName().equals(node.getClientName())) continue;
			int needPeerNum = wNode.getPeerCount() - wNode.getCurPeerNumber();	// 수용량
			System.out.println("wnode : " + wNode.getClientName() + " node : " + node.getClientName() + " // " +wNode.getPeerCount() + " - " + wNode.getCurPeerNumber() + " = needPeerNum :  " + needPeerNum + " // node.getCurPeerNumber() + 1 : " + (node.getCurPeerNumber() + 1));
			if(needPeerNum < node.getCurPeerNumber() + 1) continue;
			if(wNode.isSimilarPeer(node)) {
				System.out.println("등록");
				// peer 등록
				if(wNode.setPeerNode(node)) {
					// 성공
					log("Reg]\t" + node.toString() + " [Go In " + wNode.getClientName() + "]");
					//Control.getInstance().getMainFrame().removeTableNode(node.getClientName());
					UIMgr.getInstance().getMainFrame().setTablePeer(wNode.getClientName(), node.getClientName(),node.getOption());
					if(wNode.isPeerFull()) {
						// Full Peer
						complete(wNode);
						wNode.resetTimer();
						return wNode;
					}
					node.resetTimer();
					return null;
				} 
			}
		}
		

		int wait = 5;
		synchronized (this) {
			node.setTimer(wait);
			waitingList.add(node);
		}
		
		UITimer timer = new UITimer(UIMgr.getInstance().getMainShell().getDisplay());
		timer.start(wait*1000);
		log("Reg]\t" + node.toString());
		System.out.println(node.getClientName() + " : " +node.getTimer() + "초!!");
		if(node.getTimer() != 0)
			return setWaitingNode(node.setLoosen());
		else
			return node;
	}
	
	public void printWaitingList() {
		for(WaitingNode wNode : waitingList) {
			log("Print]\t" + wNode.toString());
		}
	}
	
	private void complete(WaitingNode completedNode) {
		
		synchronized (this) {
			waitingList.remove(completedNode);
			completeCount += completedNode.getCurPeerNumber() + 1;
		}
		UIMgr.getInstance().getMainFrame().completedNode(completedNode.getClientName());
		//log("Done]\t" + completedNode.toString());
	}
	
}
