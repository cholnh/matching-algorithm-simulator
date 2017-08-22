package server.control.manager;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import client.control.ClientMgr;
import server.control.log.LogMgr;
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
	@SuppressWarnings("unused")
	private static void log(String text) {
		System.out.println(text);
	}
	
	private MainFrame mainFrame;
	private Shell mainShell;
	
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
		
	}

	public void serverStart() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ServerMgr.getInstance().start();
			}
		}).start();
	}
	
	private static final Integer HOW_MANY_CLIENT = 10;
	
	public void clientStart() {
		ClientMgr clientMgr = new ClientMgr();
		for (int i = 0; i < HOW_MANY_CLIENT; i++) {
			mainFrame.setCl(HOW_MANY_CLIENT - i - 1);
			
			new Thread(clientMgr.getClientHandler(i)).start();
		}
	}
	
	class ClientHandler implements Runnable {
		ClientMgr clientMgr;
		int i;
		
		public ClientHandler (ClientMgr clientMgr, int i) {
			this.clientMgr = clientMgr;
			this.i = i;
		}
		
		@Override
		public void run() {
			clientMgr.getClientHandler(i);
			
		}
	}
	
}
