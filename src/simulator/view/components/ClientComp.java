package simulator.view.components;
import org.eclipse.jface.dialogs.IInputValidator;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
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

/**
 * ClientComp
 * 
 * @version 1.0 [2017. 8. 29.]
 * @author Choi
 */
public class ClientComp extends Composite {

	private Table table;
	private Button clientButton;
	private Label cl;
	
	public Label getCl() {
		return cl;
	}
	public Button getClientButton() {
		return clientButton;
	}
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ClientComp(Composite parent, int style) {
		super(parent, style);

		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setBounds(10, 31, 1564, 326);
		table.setTouchEnabled(true);
		table.setHeaderVisible(true);
		
		/* Client Table */
		TableColumn[] columns_1 = new TableColumn[5];
		for(int i=0; i<columns_1.length; i++) 
			columns_1[i] = new TableColumn(table, SWT.CENTER);
		
		columns_1[0].setWidth(103);
		columns_1[1].setWidth(76);
		columns_1[2].setWidth(252);
		columns_1[3].setWidth(102);
		columns_1[4].setWidth(1026);
		
		//	0		1		2		3		4
		// status	name	option	total	team
		columns_1[0].setText("Status");
		columns_1[1].setText("Name");
		columns_1[2].setText("Option");
		columns_1[3].setText("Total Peer Count");
		columns_1[4].setText("Team");
		
		
		Label lblClient = new Label(this, SWT.NONE);
		lblClient.setLocation(10, 3);
		lblClient.setSize(58, 22);
		lblClient.setText("Client");
		lblClient.setFont(SWTResourceManager.getFont("맑은 고딕", 14, SWT.NORMAL));
		
		cl = new Label(this, SWT.NONE);
		cl.setLocation(72, 3);
		cl.setSize(39, 22);
		cl.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.NORMAL));
		cl.setText("0");
		
		clientButton = new Button(this, SWT.NONE);
		clientButton.setLocation(117, 3);
		clientButton.setSize(76, 25);
		clientButton.setEnabled(false);
		clientButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog input = new InputDialog(parent.getShell(), "", "INPUT Client Count ", "10", new Validator());
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
	}
	
	class Validator implements IInputValidator {

		public String isValid(String newText) {
			Integer num;
			try {
				num = Integer.parseInt(newText);
			} catch (NumberFormatException e) {
				return "Only Number";
			}
			if(num < 0)
				return "Only Positive Number";
			return null;
		}
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public synchronized void setClientTableNode(String status, String name, String option, String total, String peer) {
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
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setClientStatus(String status, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getClientTableItem(name);
					if(item != null)
						item.setText(0, status);
					
					table.getParent().layout();
				}
			}
		});
	}
	public synchronized void setClientTeam(String team, String name) {
		table.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!table.isDisposed()) {
					TableItem item = getClientTableItem(name);
					if(item != null)
						item.setText(4, team);
					
					table.getParent().layout();
				}
			}
		});
	}
	private TableItem getClientTableItem(String name) {
		TableItem[] items = table.getItems();
		for(TableItem item : items) {
			if(item.getText(1).equals(name)) {
				return item;
			}
		}
		return null;
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

}
