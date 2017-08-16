package server.view;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import server.control.log.LogMgr;
import server.control.manager.UIMgr;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

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
		shell.setSize(1027, 539);
		shell.setText("SWT Application");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(0, 0, 1011, 500);
		
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setHeaderVisible(true);
		table.setBounds(10, 10, 991, 451);
		
		TableColumn clientColumn = new TableColumn(table, SWT.NONE);
		clientColumn.setWidth(100);
		clientColumn.setText("Connect");
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Name");
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(216);
		tblclmnNewColumn_1.setText("Option");
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setText("Num");
		tblclmnNewColumn_2.setWidth(43);
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_3.setWidth(452);
		tblclmnNewColumn_3.setText("Peer");
		
		TableColumn tblclmnTime = new TableColumn(table, SWT.NONE);
		tblclmnTime.setWidth(53);
		tblclmnTime.setText("Time");
		
		TableCursor tableCursor = new TableCursor(table, SWT.NONE);
		
		Button btnStart = new Button(composite, SWT.NONE);
		btnStart.setBounds(20, 467, 76, 25);
		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				/** completionService */
				UIMgr.getInstance().setCompletionService();
			}
		});
		btnStart.setText("start");

		return shell;
	}
	
	public synchronized void setTableNode(String con, String name, String option, String num, String peer, String time) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem tableItem = new TableItem(table, SWT.NONE);
					tableItem.setText(0, con);
					tableItem.setText(1, name);
					tableItem.setText(2, option);
					tableItem.setText(3, num);
					tableItem.setText(4, peer);
					tableItem.setText(5, time);
					table.getParent().layout();
				}
			}
		});
	}
	
	public synchronized void completedNode(String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					for(int i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(name)) {
							items[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
						}
					}
					table.getParent().layout();
				}
				
			}
		});
	}
	
	public synchronized void setTablePeer(String mas, String peer, String peerOpt) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					String tmp = "";
					String exNm = "";
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
							System.out.println(nm + "에 "+ peer +"등록 : [" + items[j].getText(4) + "]");
							table.remove(i);
							System.out.println(nm + "제거");
							break;
						}
					}
					
					table.getParent().layout();
				}
			}
		});
	}
	/*
	public synchronized void removeTableNode(String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem[] items = table.getItems();
					for(int i=0; i<items.length; i++) {
						if(items[i].getText(1).equals(name)) {
							table.remove(i);
						}
					}
					table.getParent().layout();
				}
				
			}
		});
	}
	*/
}
