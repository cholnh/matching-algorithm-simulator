package client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import client.control.ClientMgr;
import client.control.ClientMgr.ConnectionToServer;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

/**
 * ClientTest<br>
 * <code>ClientMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ClientTest {
	/** 생성할 클라이언트 수 */
	private static final Integer HOW_MANY_CLIENT = 1100;

	/**
	 * 스트링 테스트 (싱글스레드)<br>
	 * 서버로 부터 연결을 요청하고, 연결 성공 시 문자열을 주고 받음
	 */
	@Ignore @Test
	public void stringTest_sThread() {
		ClientMgr clientMgr = new ClientMgr();
		
		/** 서버로 소켓 연결 요청 */
		Socket socket = clientMgr.connectToServer();
		Assert.assertNotNull(socket);

		/**
		 * 연결 핸들러 생성 <br>
		 * <code>ConnectionToServer</code> (연결 핸들러)는 여러 stream 처리를 수행
		 * 
		 * @see ClientMgr.ConnectionToServer
		 */
		ConnectionToServer conn = clientMgr.getConnectionToServer(socket);
		Assert.assertNotNull(conn);

		String sendMessage = "test message";
		String recvMessage = "";

		/** 서버로 문자열 버퍼 전송 */
		conn.stringWrite(sendMessage);
		
		/** 서버로 부터 echo된 문자열 버퍼 읽기 */
		recvMessage = conn.stringRead();
		Assert.assertEquals(sendMessage, recvMessage);
	}

	/**
	 * 스트링 테스트 (멀티스레드)<br>
	 * 서버로 부터 연결을 요청하고, 연결 성공 시 문자열을 주고 받음<br>
	 * 클래스 변수 <code>HOW_MANY_CLIENT</code> 수 만큼의 클라이언트 핸들러 생성 <code>ClientStringHandler</code>
	 * 
	 * @throws Throwable	MultiThreadedTestRunner
	 */
	@Ignore @Test
	public void stringTest_mThread() throws Throwable {
		TestRunnable[] tests = new TestRunnable[HOW_MANY_CLIENT];
		ClientMgr clientMgr = new ClientMgr();

		for (int i = 0; i < tests.length; i++) {
			tests[i] = clientMgr.getClientStringHandler();
		}
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tests);
		mttr.runTestRunnables();
	}

	/**
	 * 이미지 테스트 (싱글스레드)<br>
	 * 서버로 부터 연결을 요청하고, 연결 성공 시 이미지를 주고 받음
	 */
	@Ignore @Test
	public void imageTest_sThread() {
		ClientMgr clientMgr = new ClientMgr();
		
		/** 서버로 소켓 연결 요청 */
		Socket socket = clientMgr.connectToServer();
		Assert.assertNotNull(socket);

		/**
		 * 연결 핸들러 생성 <br>
		 * <code>ConnectionToServer</code> (연결 핸들러)는 여러 stream 처리를 수행
		 * 
		 * @see ClientMgr.ConnectionToServer
		 */
		ConnectionToServer conn = clientMgr.getConnectionToServer(socket);
		Assert.assertNotNull(conn);

		/** 전송할 이미지 */
		File imgFile = new File("asset\\img\\testimage.gif");
		BufferedImage bimg;
		try {
			/** 보낼 이미지 로드 */
			bimg = ImageIO.read(imgFile);
			
			/** 서버로 이미지 버퍼 전송 */
			conn.imageWrite(bimg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/** 서버로 부터 echo된 이미지 읽기 */
		if((bimg = conn.imageRead()) != null) {
			conn.saveFile(bimg, "testimage");
		}
	}

	/**
	 * 이미지 테스트 (멀티스레드)<br>
	 * 서버로 부터 연결을 요청하고, 연결 성공 시 이미지를 주고 받음<br>
	 * 클래스 변수 <code>HOW_MANY_CLIENT</code> 수 만큼의 클라이언트 핸들러 생성 <code>ClientImageHandler</code>
	 * 
	 * @throws Throwable MultiThreadedTestRunner
	 */
	@Test
	public void imageTest_mThread() throws Throwable {
		TestRunnable[] tests = new TestRunnable[HOW_MANY_CLIENT];
		ClientMgr clientMgr = new ClientMgr();

		for (int i = 0; i < tests.length; i++) {
			tests[i] = clientMgr.getClientImageHandler(i);
		}
		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tests);
		mttr.runTestRunnables();
	}
}
