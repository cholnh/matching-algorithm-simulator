package server.control.manager;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import server.control.log.LogMgr;
import server.model.waitingqueue.WaitingNode;
import server.model.waitingqueue.WaitingQueue;
import server.view.MainFrame;

/**
 * UIMgr
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public enum UIMgr {
	INSTANCE;
	public static UIMgr getInstance() {
		return INSTANCE;
	}
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("UIMgr");
	private static void log(String text) {
		System.out.println(text);
	}
	
	private MainFrame mainFrame;
	private Shell mainShell;
	private ExecutorService executorService;
	private CompletionService<WaitingNode> completionService;
	private WaitingQueue waitingQueue;
	
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	public Shell getMainShell() {
		return mainShell;
	}

	public void open() {
		if(mainFrame == null)
			init();
		mainFrame.open();
		
		
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIMgr.getInstance().open();
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	public void init() {
		
		/** main frame */
		mainFrame = MainFrame.getInstance();
		mainShell = mainFrame.createContents();
		
		/** waitingQueue*/
		waitingQueue = WaitingQueue.getInstance();
		
		/** executorService */
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
	private void setClient() {
		class ClientThread implements Callable<WaitingNode> {

			private int i;
			private WaitingQueue wq;
			
			public ClientThread(int i, WaitingQueue wq) {
				this.i = i;
				this.wq = wq;
			}
			
			@Override
			public WaitingNode call() {
				WaitingNode node;
				WaitingNode completedNode;
				Random rand = new Random();
				int opt = rand.nextInt(8);
				if(opt == 7) {
					node = new WaitingNode("client"+i, rand.nextInt(5), rand.nextInt(7), rand.nextInt(3), rand.nextInt(3));
					mainFrame.setTableNode("",  node.getClientName(), node.getOption(), ""+node.getPeerCount(), node.getPeerNode(), ""+node.getTimer());
					
					completedNode = wq.setWaitingNode(node);
				}
				else {
					node = new WaitingNode("client"+i, rand.nextInt(5), opt);
					mainFrame.setTableNode("",  node.getClientName(), node.getOption(), ""+node.getPeerCount(), node.getPeerNode(), ""+node.getTimer());
					
					completedNode = wq.setWaitingNode(node);
				}
				
				wq.addConnectCount();
				return completedNode;
			}
		}
		
		for(int i=0; i<50; i++) {
			/** 랜덤 딜레이 최소/최대 값 */
			//final Integer DELAY_MINIMUM = 1;
			//final Integer DELAY_MAXIMUM = 4;
			
			/** 1~4초 대기 */
			//UITimer timer = new UITimer(Display.getDefault());
			//timer.start(1000);
			
			completionService.submit(new ClientThread(i, waitingQueue));
			log("reg]\tclient"+i);
		}
		
	}
	
	public void setCompletionService() {
		completionService = new ExecutorCompletionService<WaitingNode>(executorService);
		
		/* 결과값 받는 코드 
		Future<Integer> result = executorService.submit(new Callable<Integer>() {
			
			@Override
			public Integer call() throws Exception {
				int result = 0;
				
				
					int count = 0;
					
				
				return result;
			}
		});
		*/
		
		
		/** client */
		setClient();
		try {
			
			while(waitingQueue.getConnectCount() > waitingQueue.getCompleteCount()) {
				Future<WaitingNode> future = completionService.take(); // take : 여러 FutureTask가 있는 블록 큐를 돌면서 끝난 녀석이 있는지 확인
				
				WaitingNode node = future.get();
				System.out.println("while] " + node.getClientName() + " : " +node.getTimer() + "초");
				// 다른 곳에 Peer로 등록된 노드 : ignore
				/*
				if(node == null) {
					continue;
				}
					
				if(node.isPeerFull()) {
					// Full Peer
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							complete(node);
						}
					}).start();
				}
				else {
					// Time out : setLoosen
					System.out.println(node.getClientName() + " : " +node.getTimer() + "초 loosen");
					if(node.getTimer() != 0) {
						System.out.println("loosen play!");
						
						completionService.submit(new Callable<WaitingNode>() {
							
							@Override
							public WaitingNode call() throws Exception {
								return waitingQueue.setWaitingNode(node.setLoosen());
							}
						});
					}
				}
				*/
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println("END");
	}
	
	private void complete(WaitingNode completedNode) {
		log("Donec] " + completedNode.toString());
	}
}
