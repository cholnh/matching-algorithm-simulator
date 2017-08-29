package simulator.view;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import simulator.control.util.MemoryMonitor;
import simulator.view.components.ClientComp;
import simulator.view.components.ServerComp;
import org.eclipse.wb.swt.SWTResourceManager;



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
	private ClientComp clientComp;
	private ServerComp serverComp;
	private ProgressBar progressBar;
	private Label systemInfo;
	private Label lblMemoryInfo;
	
	public ClientComp getClientComp() {
		return clientComp;
	}
	public ServerComp getServerComp() {
		return serverComp;
	}
	
	public void open() {
		Display display = Display.getDefault();
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
		shell.setText("simulator");
		
		clientComp = new ClientComp(shell, SWT.NONE);
		clientComp.setBounds(0, 499, 1584, 362);
		
		serverComp = new ServerComp(shell, SWT.NONE);
		serverComp.setBounds(0, 0, 1584, 457);
		
		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(10, 470, 1564, 17);
		
		systemInfo = new Label(serverComp, SWT.NONE);
		systemInfo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		systemInfo.setAlignment(SWT.CENTER);
		systemInfo.setBounds(987, 10, 587, 23);
		
		lblMemoryInfo = new Label(serverComp, SWT.NONE);
		lblMemoryInfo.setFont(SWTResourceManager.getFont("맑은 고딕", 9, SWT.BOLD));
		lblMemoryInfo.setAlignment(SWT.RIGHT);
		lblMemoryInfo.setBounds(910, 10, 74, 23);
		lblMemoryInfo.setText("Memory Info");
		
		Thread updateThread = new Thread() {
			public void run() {
				while (true) {
					systemInfo.getDisplay().syncExec(new Runnable() {

						@Override
						public void run() {
							systemInfo.setText(MemoryMonitor.getInfo());
						}
					});

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		// background thread
		updateThread.setDaemon(true);
		updateThread.start();
		
		return shell;
	}
	
	public synchronized void setProgress(int value) {
		progressBar.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (!progressBar.isDisposed()) {
					System.out.println(value);
					progressBar.setSelection(value);
					progressBar.getParent().layout();
				}
			}
		});
	}
}
