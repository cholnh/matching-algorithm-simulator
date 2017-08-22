package server.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.model.blockingqueue.BlockingQueueMgr;
import server.model.blockingqueue.BlockingQueueNode;
import server.model.blockingqueue.Node;

/**
 * WaitingListTest
 * 
 * @version 1.0 [2017. 8. 22.]
 * @author Choi
 */
public class WaitingListTest {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("WaitingListTest");
	List<BlockingQueueNode> waitingList = Collections.synchronizedList(new ArrayList<BlockingQueueNode>());
	BlockingQueue<BlockingQueueNode> waitingQueue = new LinkedBlockingQueue<BlockingQueueNode>();
	private final Thread coreThread = new Thread(new CoreHandler());
	
	public static void main(String...args) {
		WaitingListTest test = new WaitingListTest();
		BlockingQueueMgr queueMgr = BlockingQueueMgr.getInstance();
		queueMgr.coreStart();
		
		
//		nodes[0] = new BlockingQueueNode("client1", 3, 2);
//		nodes[1] = new BlockingQueueNode("client2", 3, 1);
//		nodes[2] = new BlockingQueueNode("client3", 3, 1);
//		nodes[3] = new BlockingQueueNode("client4", 3, 1);
		
		
		BlockingQueueNode[] nodes = new BlockingQueueNode[10];
		for(int i=0; i<nodes.length; i++) {
			nodes[i] = test.getRandomNode(i);
		}

		for(BlockingQueueNode node : nodes) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					BlockingQueueMgr queueMgr = BlockingQueueMgr.getInstance();
					BlockingQueue<BlockingQueueNode> waitingQueue = queueMgr.getWaitingQueue();
					
					try {
						while(true) {
							waitingQueue.put(node);
							Node recv = node.getQueue().poll(10, TimeUnit.SECONDS);
							System.out.println("poll] " + node.getClientName() + " 결과 : "+recv);
							System.out.println("queue 내부");
							System.out.println(waitingQueue);
							if(recv == null) {
								// Time out!
								node.setLoosenNode();
								//node.getQueue().clear();
								queueMgr.removeNode(node);
								continue;
							}
							System.out.println(node.getClientName()+"] " + test.teamPrint(recv));
							
							break;
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	private String teamPrint(Node node) {
		List<BlockingQueueNode> peer = node.getPeer();
		String text = " Team 완성 [ ";
		
		text += node.getClientName();
		if(peer.size() > 0)
			text += " - ";
		for(int i=0; i<peer.size(); i++) {
			BlockingQueueNode pnode = peer.get(i);
			text += pnode.getClientName();
			if(i != peer.size()-1)
				text += " - ";
		}
		text += " ]";
		return text;
	}
	
	public void coreStart() {
		if(coreThread.isAlive()) return;
		if(!coreThread.isInterrupted()) {
			coreThread.start();
		}
	}
	
	class CoreHandler implements Runnable {

		@Override
		public void run() {
			while(true) {
				try {
					BlockingQueueNode node = waitingQueue.take();	// block
					System.out.println("waitingQueue] " + node.getClientName() + " 받음");
					examine(node);
	
				} catch (InterruptedException e) {
					System.err.println("\n[core Stop]");
					e.printStackTrace();
					break;
				}	
			}
		}
		
	}
	
	private void insertTest() {
		for(int i=0; i<50; i++) {
			insert(getRandomNode(i));
		}
		
		for(BlockingQueueNode node : waitingList) 
			System.out.println(node.toString());
	}
	
	private void containTest() {
		BlockingQueueNode node = new BlockingQueueNode("client1", 3, 1);
		try {
			waitingQueue.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(isContain(node));
	}
	
	private void threadTest() {
		System.out.println("start");
		coreThread.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		coreThread.interrupt();
		System.out.println("done");
	}
	
	private void setOptTest() {
		BlockingQueueNode node = new BlockingQueueNode("client1", 3, 3);
		System.out.println(node);
		System.out.println(node.setLoosenNode());
		System.out.println(node.setLoosenNode());
		System.out.println(node.setLoosenNode());
		System.out.println(node.setLoosenNode());
		System.out.println(node.setLoosenNode());
		
	}
	
	private boolean isContain(BlockingQueueNode node) {
		return waitingQueue.contains(node);
	}
	
	private void examine(BlockingQueueNode node) {
		if(node.getNeedPeerCount() == 0) {
			complete(node);
			return;
		}
		
		for(BlockingQueueNode wnode : waitingList) {
			if(isSameTPC(node, wnode) && wnode.isSimilarNode(node)) {
				// peer 등록
				wnode.setPeerNode(node);
				
				if(wnode.isPeerFull()) {
					// Full Peer
					System.out.println("complete] " + node.getClientName());
					complete(wnode);
					return;
				}
				else {
					// node -> wnode`s peer
					System.out.println("node -> wnode`s peer] " + node.getClientName());
					return;
				}
			}
		}
		
		// 탐색 실패 -> 신규등록
		System.out.println("신규등록] "+node.getClientName());
		insert(node);
	}
	
	private void complete(BlockingQueueNode node) {
		try {
	
			node.getQueue().put(node);
		
			for(BlockingQueueNode pnode : node.getPeer()) {
				pnode.getQueue().put(node);
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void insert(BlockingQueueNode node) {
		Integer idx = waitingList.size();
		Integer nNeedPeerCnt = node.getNeedPeerCount();
		
		for(int i=0; i<waitingList.size(); i++) {
			BlockingQueueNode wnode = waitingList.get(i);
			Integer wNeedPeerCnt = wnode.getNeedPeerCount();
			
			if(nNeedPeerCnt < wNeedPeerCnt) {
				idx = waitingList.indexOf(wnode);	// Not i
				break;
			}
		}
		
		waitingList.add(idx, node);
	}
	
	private boolean isSameTPC(BlockingQueueNode arg1, BlockingQueueNode arg2) {
		// Total Peer Count
		return arg1.getTotalPeerCount() == arg2.getTotalPeerCount();
	}


	private BlockingQueueNode getRandomNode(Integer clientIdx) {
		BlockingQueueNode sendNode;
		/** Node 생성 */
		Random rand = new Random();
		int opt = rand.nextInt(8);
		if(opt > 4)
			sendNode = new BlockingQueueNode("Client"+clientIdx, 
					rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
		else 
			sendNode = new BlockingQueueNode("Client"+clientIdx, 
					rand.nextInt(5), opt);
		return sendNode;
	}
}
