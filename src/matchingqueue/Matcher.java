package matchingqueue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

/**
 * Matcher
 * This class is the default scheme to be applied to the matching queue.
 * When extends this, it must be overridden the match(), loosen() abstract method.
 * The starvation problems can occur depending on the overridden method content.
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public abstract class Matcher implements Serializable {
	
	private static final long serialVersionUID = -8490918913541247088L;

	/**
	 * If matcher is registered as a peer for another, it registers for parent.
	 */
	protected Matcher parent;
	
	/**
	 * The matcher initially registers the maximum of peers it will match.
	 */
	protected Integer totalPeerCount;
	
	/**
	 * Matched matchers are stored. 
	 */
	protected List<Matcher> peer;
	
	/**
	 * The scheduled timer defined in the matching queue.
	 */
	transient protected Timer timer;
	
	/**
	 * Creates a new Matcher.
	 * 
	 * @param totalPeerCount	The total number of nodes to match. 
	 */
	public Matcher(Integer totalPeerCount) {
		this.totalPeerCount = totalPeerCount;
		peer = Collections.synchronizedList(new ArrayList<Matcher>());
	}
	
	/**
	 * Override this method to describe the match algorithm here.
	 * A basic scheme is a type that takes the specified node to be matched
	 * and returns a result of the matching algorithm.
	 * 
	 * @param node	node to be matched with this.
	 * @return		returns a result satisfying the matching condition.
	 */
	public abstract boolean match(Matcher node);
	
	/**
	 * Override this method to solve the starvation problem of matching algorithm here.
	 * The matching algorithm can cause starvation problem, 
	 * which is the phenomenon of node isolation for a long time if matching fails.
	 * Therefore, it needs to solve the starvation problem through the loosen method.
	 * 
	 * @return	returns itself(this) with condition changed.
	 */
	public abstract Matcher loosen();
	
	/**
	 * Timer getter
	 * 
	 * @return	The instance of timer .
	 */
	public Timer getTimer() {
		return timer;
	}
	
	/**
	 * Timer setter
	 * 
	 * @param timer	The timer to be set.
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	/**
	 * Parent getter
	 * 
	 * @return	The instance of parent node.
	 */
	public Matcher getParent() {
		return parent;
	}
	
	/**
	 * Parent setter
	 * 
	 * @param parent	The node to be registered as a parent.
	 */
	public void setParent(Matcher parent) {
		this.parent = parent;
	}
	
	/**
	 * Total peer count getter
	 * 
	 * @return	The total number of peers.
	 */
	public Integer getTotalPeerCount() {
		return totalPeerCount;
	}
	
	/**
	 * Peer list getter
	 * 
	 * @return	The list of peers.
	 */
	public List<Matcher> getPeer() {
		return peer;
	}
	
	/**
	 * Computes and returns the number of peers it currently needs.
	 * 
	 * @return	The number of peers currently required.
	 */
	public Integer getNeedPeerCount() {
		return totalPeerCount - peer.size();
	}
	
	/**
	 * Returns whether the peer is full.
	 * That is, it indicates whether or not the matching is successful.
	 * 
	 * @return	true if the number of peers currently registered more than or equal the total number of peers.
	 */
	public boolean isPeerFull() {
		return peer.size() >= totalPeerCount;
	}

	/**
	 * Removes the node from the list of peers.
	 * 
	 * @param node	node to be removed from this list, if present.
	 * @return		true if this list contained the specified node.
	 */
	public boolean removePeerNode(Matcher node) {
		return peer.remove(node);
	}
	
	/**
	 * Appends the specified node to the end of this peers.
	 * Then registers the parent of this node.
	 * 
	 * @param node	node to be appended to this peer list
	 */
	public void setPeerNode(Matcher node) {
		/* peer에 node 삽입 */
		peer.add(node);
		node.setParent(this);
	}
	
	/**
	 * Removes all of the nodes from this peer list.
	 */
	public void setPeerClear() {
		peer.clear();
	}
}
