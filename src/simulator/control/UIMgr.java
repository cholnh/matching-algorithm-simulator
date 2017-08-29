package simulator.control;

import org.eclipse.swt.widgets.Shell;

import simulator.model.BlockingQueueNode;
import simulator.view.MainFrame;

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
		/* main frame */
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
	
	public void clientStart(Integer HOW_MANY_CLIENT) {
		ClientMgr clientMgr = new ClientMgr();
		
		for (int i = 0; i < HOW_MANY_CLIENT; i++) {
			setCl(HOW_MANY_CLIENT - i - 1);
			
			new Thread(clientMgr.getClientHandler(i)).start();
		}
	}
	
	/* server handler */
	public void setServerTableNode(String status, BlockingQueueNode node) {
		mainFrame.getServerComp().setServerTableNode(status, node);
	}
	public void setServerTableNode(String status, String name, String option, String total, String peer, String cur, String parent) {
		mainFrame.getServerComp().setServerTableNode(status, name, option, total, peer, cur, parent);
	}
	public void setServerStatus(String status, String name) {
		mainFrame.getServerComp().setServerStatus(status, name);
	}
	public void setServerPeer(String peer, String name) {
		mainFrame.getServerComp().setServerPeer(peer, name);
	}
	public void setServerOption(String opt, String name) {
		mainFrame.getServerComp().setServerOption(opt, name);
	}
	public void setServerCur(String cur, String name) {
		mainFrame.getServerComp().setServerCur(cur, name);
	}
	public void setServerParent(String parent, String name) {
		mainFrame.getServerComp().setServerParent(parent, name);
	}
	public void setServerRemarks(String remarks, String name) {
		mainFrame.getServerComp().setServerRemarks(remarks, name);
	}
	public void completedServerNode(String name) {
		mainFrame.getServerComp().completedServerNode(name);
	}		
	public void removeServerTableNode(String name) {
		mainFrame.getServerComp().removeServerTableNode(name);
	}
	public void setSl(Integer count) {
		if(mainFrame == null) return;
		mainFrame.getServerComp().setSl(count);
	}
	
	/* client handler */
	public void setClientTableNode(String status, String name, String option, String total, String peer) {
		mainFrame.getClientComp().setClientTableNode(status, name, option, total, peer);
	}
	public void setClientStatus(String status, String name) {
		mainFrame.getClientComp().setClientStatus(status, name);
	}
	public void setClientTeam(String team, String name) {
		mainFrame.getClientComp().setClientTeam(team, name);
	}
	public void setCl(Integer count) {
		mainFrame.getClientComp().setCl(count);
	}
}
