package server.view;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import server.control.log.LogMgr;
import server.control.manager.UIMgr;

/**
 * MainFrame
 * 
 * @version 1.0 [2017. 8. 16.]
 * @author Choi
 */
public class MainFrame {

	/** MainFrame INSTANCE */
	private static class Singleton {
		/** Initialization On Demand Holder Idiom */
		private static final MainFrame INSTANCE = new MainFrame();
	}
	
	private MainFrame() {
	}
	
	/**
	 * MainFrame INSTANCE 반환
	 * 
	 * @return MainFrame	싱글톤 정적 인스턴스 반환 (MainFrame INSTANCE)
	 */
	public static synchronized MainFrame getInstance() {
		return Singleton.INSTANCE;
	}
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("MainFrame");
	
	protected Shell shell;
	private Table table;
	private Table table_1;
	private Label sl;
	private Label cl;
	
	/**
	 * 
	 */
	public static void main(String...args) {
		try {
			MainFrame main = new MainFrame();
			main.createContents();
			main.open();
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		//createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 * Create contents of the window.
	 */
	public Shell createContents() {
		shell = new Shell();
		shell.setSize(1600, 900);
		shell.setText("SWT Application");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(0, 0, 1584, 861);
		
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setTouchEnabled(true);
		table.setHeaderVisible(true);
		table.setBounds(10, 38, 1564, 451);
		
		TableColumn tblclmnStatus_1 = new TableColumn(table, SWT.CENTER);
		tblclmnStatus_1.setWidth(103);
		tblclmnStatus_1.setText("Status");
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Name");
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_1.setWidth(353);
		tblclmnNewColumn_1.setText("Option");
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_2.setText("Total Peer Count");
		tblclmnNewColumn_2.setWidth(102);
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_3.setWidth(624);
		tblclmnNewColumn_3.setText("Peer");
		
		TableColumn tblclmnTime = new TableColumn(table, SWT.CENTER);
		tblclmnTime.setWidth(94);
		tblclmnTime.setText("Current Count");
		
		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn_4.setWidth(83);
		tblclmnNewColumn_4.setText("Parent");
		
		TableColumn tblclmnEtc = new TableColumn(table, SWT.CENTER);
		tblclmnEtc.setWidth(100);
		tblclmnEtc.setText("Remarks");
		
		table_1 = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table_1.setTouchEnabled(true);
		table_1.setHeaderVisible(true);
		table_1.setBounds(10, 525, 1564, 326);
		
		TableColumn tblclmnStatus = new TableColumn(table_1, SWT.CENTER);
		tblclmnStatus.setWidth(103);
		tblclmnStatus.setText("Status");
		
		TableColumn tableColumn_1 = new TableColumn(table_1, SWT.CENTER);
		tableColumn_1.setWidth(76);
		tableColumn_1.setText("Name");
		
		TableColumn tableColumn_2 = new TableColumn(table_1, SWT.CENTER);
		tableColumn_2.setWidth(252);
		tableColumn_2.setText("Option");
		
		TableColumn tblclmnTotalPeerCount = new TableColumn(table_1, SWT.CENTER);
		tblclmnTotalPeerCount.setWidth(102);
		tblclmnTotalPeerCount.setText("Total Peer Count");
		
		
		TableColumn tblclmnNewColumn_5 = new TableColumn(table_1, SWT.CENTER);
		tblclmnNewColumn_5.setWidth(1026);
		tblclmnNewColumn_5.setText("Team");
		
		Label lblServer = new Label(composite, SWT.NONE);
		lblServer.setFont(SWTResourceManager.getFont("맑은 고딕", 14, SWT.NORMAL));
		lblServer.setBounds(10, 10, 58, 22);
		lblServer.setText("Server");
		
		Label lblClient = new Label(composite, SWT.NONE);
		lblClient.setText("Client");
		lblClient.setFont(SWTResourceManager.getFont("맑은 고딕", 14, SWT.NORMAL));
		lblClient.setBounds(10, 495, 58, 22);
		
		sl = new Label(composite, SWT.NONE);
		sl.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.NORMAL));
		sl.setText("0");
		sl.setBounds(72, 10, 39, 22);
		
		cl = new Label(composite, SWT.NONE);
		cl.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.NORMAL));
		cl.setText("0");
		cl.setBounds(72, 497, 39, 22);
		
		Button button = new Button(composite, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UIMgr.getInstance().serverStart();
			}
		});
		button.setText("start");
		button.setBounds(117, 7, 76, 25);
		
		Button button_1 = new Button(composite, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UIMgr.getInstance().clientStart();
			}
		});
		button_1.setText("start");
		button_1.setBounds(117, 495, 76, 25);

		return shell;
	}
	
	// server
	//	0		1		2			3			4		5		6
	// status	name	option		total		peer	cur		parent
	
	// client
	//	0		1		2		3			4
	// status	name	option	total		team
	
	public synchronized void setServerTableNode(String status, String name, String option, String total, String peer, String cur, String parent) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem tableItem = new TableItem(table, SWT.NONE);
					tableItem.setText(0, status);
					tableItem.setText(1, name);
					tableItem.setText(2, option);
					tableItem.setText(3, total);
					tableItem.setText(4, peer);
					tableItem.setText(5, cur);
					tableItem.setText(6, parent);
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setClientTableNode(String status, String name, String option, String total, String peer) {
		table_1.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table_1.isDisposed()) {
					TableItem tableItem = new TableItem(table_1, SWT.NONE);
					tableItem.setText(0, status);
					tableItem.setText(1, name);
					tableItem.setText(2, option);
					tableItem.setText(3, total);
					tableItem.setText(4, peer);
					
					table_1.getParent().layout();
				}
			}
		});
	}
	
	public synchronized void setClientStatus(String status, String name) {
		table_1.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table_1.isDisposed()) {
					TableItem item = getClientTableItem(name);
					if(item != null)
						item.setText(0, status);
					
					table_1.getParent().layout();
				}
			}
		});
	}
	public synchronized void setClientTeam(String team, String name) {
		table_1.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table_1.isDisposed()) {
					System.out.println("**************등록중 " + name + " // " + team);
					TableItem item = getClientTableItem(name);
					if(item != null)
						item.setText(4, team);
					
					table_1.getParent().layout();
				}
			}
		});
	}
	
	public synchronized void setServerStatus(String status, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(0, status);
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setServerPeer(String peer, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(4, peer);
					
					table.getParent().layout();
				}
			}
		});
	}

	public synchronized void setServerCur(String cur, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(5, cur);
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setServerParent(String parent, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(6, parent);
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setServerRemarks(String remarks, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(7, remarks);
					
					table.getParent().layout();
				}
			}
		});
	}
	
	public synchronized void completedServerNode(String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					for(int i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(name)) {
							items[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
							items[i].setText(0, "완료");
						}
					}
					table.getParent().layout();
				}
				
			}
		});
	}
	
	public synchronized void setExamining(String name, boolean tf) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					for(int i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(name)) {
							if(tf)
								items[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
							else
								items[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
						}
					}
					table.getParent().layout();
				}
				
			}
		});
	}
	
	public TableItem getServerTableItem(String name) {
		TableItem[] items = table.getItems();
		for(TableItem item : items) {
			if(item.getText(1).equals(name)) {
				return item;
			}
		}
		return null;
	}
	public TableItem getClientTableItem(String name) {
		TableItem[] items = table_1.getItems();
		for(TableItem item : items) {
			if(item.getText(1).equals(name)) {
				return item;
			}
		}
		return null;
	}
	
	public synchronized void setSl(Integer count) {
		sl.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!sl.isDisposed()) {
					sl.setText(count+"");
					
					sl.getParent().layout();
				}
				
			}
		});
	}	
	public synchronized void setCl(Integer count) {
		cl.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!cl.isDisposed()) {
					cl.setText(count+"");
					
					cl.getParent().layout();
				}
				
			}
		});
	}
	
	public synchronized void removeServerTableNode(String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					for(int i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(name)) {
							System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + name);
							items[i].setText(1, "삭제");
							table.remove(i);
							return;
						}
					}
				}
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + name + " 삭제 실패");
			}
		});
	}
	/*
	public synchronized void setServerTablePeer(String mas, String peer, String peerOpt) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					String tmp = "";
					int i, j = 0;
					
					for(i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(peer)) {
							tmp += items[i].getText(4);
							break;
						}
					}
					
					items = table.getItems();
					for(j = 0; j<items.length; j++) {
						String nm = items[j].getText(1);
						if(nm.equals(mas)) {
							items[j].setText(4, items[j].getText(4) + " " + peer + " (" + peerOpt + ") " + tmp);
							//System.out.println(nm + "에 "+ peer +"등록 : [" + items[j].getText(4) + "]");
							table.remove(i);
							//System.out.println(nm + "제거");
							break;
						}
					}
					table.getParent().layout();
				}
			}
		});
	}
	*/
}
