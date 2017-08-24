package server.view;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
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

import server.control.manager.UIMgr;
import server.model.blockingqueue.BlockingQueueNode;

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
	
	private MainFrame() {}
	
	/**
	 * MainFrame INSTANCE 반환
	 * 
	 * @return MainFrame	싱글톤 정적 인스턴스 반환 (MainFrame INSTANCE)
	 */
	public static synchronized MainFrame getInstance() {
		return Singleton.INSTANCE;
	}
	
	protected Shell shell;
	private Table table;
	private Table table_1;
	private Label sl;
	private Label cl;
	private Button serverButton;
	private Button clientButton;
	
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
		
		/* Server Table */
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setTouchEnabled(true);
		table.setHeaderVisible(true);
		table.setBounds(10, 38, 1564, 451);
		
		TableColumn[] columns = new TableColumn[8];
		for(int i=0; i<columns.length; i++) 
			columns[i] = new TableColumn(table, SWT.CENTER);
			
		columns[0].setWidth(103);
		columns[1].setWidth(100);
		columns[2].setWidth(353);
		columns[3].setWidth(102);
		columns[4].setWidth(624);
		columns[5].setWidth(94);
		columns[6].setWidth(83);
		columns[7].setWidth(100);
		
		columns[0].setText("Status");
		columns[1].setText("Name");
		columns[2].setText("Option");
		columns[3].setText("Total Peer Count");
		columns[4].setText("Peer");
		columns[5].setText("Current Count");
		columns[6].setText("Parent");
		columns[7].setText("Remarks");
		
		/* Client Table */
		table_1 = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table_1.setTouchEnabled(true);
		table_1.setHeaderVisible(true);
		table_1.setBounds(10, 525, 1564, 326);
		
		TableColumn[] columns_1 = new TableColumn[5];
		for(int i=0; i<columns_1.length; i++) 
			columns_1[i] = new TableColumn(table_1, SWT.CENTER);
		
		columns_1[0].setWidth(103);
		columns_1[1].setWidth(76);
		columns_1[2].setWidth(252);
		columns_1[3].setWidth(102);
		columns_1[4].setWidth(1026);
		
		columns_1[0].setText("Status");
		columns_1[1].setText("Name");
		columns_1[2].setText("Option");
		columns_1[3].setText("Total Peer Count");
		columns_1[4].setText("Team");
		
		/* Count Label */
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
		sl.setText("OFF");
		sl.setBounds(72, 10, 39, 22);
		
		cl = new Label(composite, SWT.NONE);
		cl.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.NORMAL));
		cl.setText("0");
		cl.setBounds(72, 497, 39, 22);
		
		serverButton = new Button(composite, SWT.NONE);
		serverButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				UIMgr.getInstance().serverStart();
				serverButton.setEnabled(false);
				clientButton.setEnabled(true);
			}
		});
		serverButton.setText("start");
		serverButton.setBounds(117, 7, 76, 25);
		
		clientButton = new Button(composite, SWT.NONE);
		clientButton.setEnabled(false);
		clientButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog input = new InputDialog(shell, "", "INPUT Client Count ", "10", new Validator());
				if (input.open() == Window.OK) {
					Integer clientCnt = 0;
					clientCnt = Integer.parseInt(input.getValue());
					setCl(clientCnt);
					UIMgr.getInstance().clientStart(clientCnt);
					clientButton.setEnabled(false);
				}
			}
		});
		clientButton.setText("start");
		clientButton.setBounds(117, 495, 76, 25);

		return shell;
	}
	
	class Validator implements IInputValidator {

		public String isValid(String newText) {
			try {
				Integer.parseInt(newText);
			} catch (NumberFormatException e) {
				return "Only Number";
			}
			
			return null;
		}
	}
	
	// server
	//	0		1		2		3		4		5		6
	// status	name	option	total	peer	cur		parent
	
	// client
	//	0		1		2		3		4
	// status	name	option	total	team
	
	public synchronized void setServerTableNode(String status, BlockingQueueNode node) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem tableItem = new TableItem(table, SWT.NONE);
					tableItem.setText(0, status);
					tableItem.setText(1, node.getClientName());
					tableItem.setText(2, node.getOptionText() + node.getRepText());
					tableItem.setText(3, node.getTotalPeerCount()+"");
					tableItem.setText(4, node.getPeerText());
					tableItem.setText(5, node.getCurPeerCount()+"");
					tableItem.setText(6, node.getParentText());
					
					table.getParent().layout();
				}
			}
		});
	}

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
	public synchronized void setServerOption(String opt, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getServerTableItem(name);
					if(item != null)
						item.setText(2, opt);
					
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
							table.remove(i);
							return;
						}
					}
				}
			}
		});
	}
}
