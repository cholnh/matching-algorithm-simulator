package simulator.view.components;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import simulator.control.UIMgr;
import simulator.model.BlockingQueueNode;
import simulator.view.MainFrame;

/**
 * ServerTableComp
 * 
 * @version 1.0 [2017. 8. 29.]
 * @author Choi
 */
public class ServerComp extends Composite {

	private Table table;
	private Button serverButton;
	private Label sl;
	
	public Label getSl() {
		return sl;
	}
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ServerComp(Composite parent, int style) {
		super(parent, style);

		/* Server Table */
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
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
		
		//	0		1		2		3		4		5		6
		// status	name	option	total	peer	cur		parent
		columns[0].setText("Status");
		columns[1].setText("Name");
		columns[2].setText("Option");
		columns[3].setText("Total Peer Count");
		columns[4].setText("Peer");
		columns[5].setText("Current Count");
		columns[6].setText("Parent");
		columns[7].setText("Remarks");
		
		/* Count Label */
		Label lblServer = new Label(this, SWT.NONE);
		lblServer.setFont(SWTResourceManager.getFont("맑은 고딕", 14, SWT.NORMAL));
		lblServer.setBounds(10, 10, 58, 22);
		lblServer.setText("Server");
		
		sl = new Label(this, SWT.NONE);
		sl.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.NORMAL));
		sl.setText("OFF");
		sl.setBounds(72, 10, 39, 22);
		
		serverButton = new Button(this, SWT.NONE);
		serverButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				UIMgr.getInstance().serverStart();
				serverButton.setEnabled(false);
				MainFrame.getInstance().getClientComp().getClientButton().setEnabled(true);
			}
		});
		serverButton.setText("start");
		serverButton.setBounds(117, 7, 76, 25);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

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
	private TableItem getServerTableItem(String name) {
		TableItem[] items = table.getItems();
		for(TableItem item : items) {
			if(item.getText(1).equals(name)) {
				return item;
			}
		}
		return null;
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
}
