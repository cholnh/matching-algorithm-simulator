package simulator.view;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import simulator.view.components.ClientComp;
import simulator.view.components.ServerComp;



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
		serverComp.setBounds(0, 0, 1584, 493);
		
		return shell;
	}
}
