package server.model.matchingqueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

/**
 * Matcher
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class Matcher {
	
	protected Matcher parent;
	protected Integer totalPeerCount;
	protected List<Matcher> peer;
	protected Timer timer;
	
	public Matcher(Integer totalPeerCount) {
		this.totalPeerCount = totalPeerCount;
		peer = Collections.synchronizedList(new ArrayList<Matcher>());
	}
	
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	public void setParent(Matcher parent) {
		this.parent = parent;
	}
	public Matcher getParent() {
		return parent;
	}
	public Integer getTotalPeerCount() {
		return totalPeerCount;
	}
	public void setTotalPeerCount(Integer totalPeerCount) {
		this.totalPeerCount = totalPeerCount;
	}
	public List<Matcher> getPeer() {
		return peer;
	}
	public void setPeer(List<Matcher> peer) {
		this.peer = peer;
	}
	public Integer getCurPeerCount() {
		return peer.size();
	}
	public Integer getNeedPeerCount() {
		return totalPeerCount - peer.size();
	}
	public void setPeerClear() {
		peer.clear();
	}
	
	public boolean isPeerFull() {
		/* 피어가 꽉 찼는지에 대한 여부 반환 = 매칭 성공여부 */
		return peer.size() >= totalPeerCount;
	}

	public boolean removePeerNode(Matcher node) {
		/* 현재 피어에서 node 제거 후 결과 성공여부 반환*/
		return peer.remove(node);
	}
	
	public void setPeerNode(Matcher node) {
		/* peer에 node 삽입 */
		peer.add(node);
		node.setParent(this);
	}
	
	public boolean match(Matcher node) {
		return false;
	}
	
	public Matcher loosen() {
		return this;
	}
}
