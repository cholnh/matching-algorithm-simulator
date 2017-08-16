package server.control;


import java.net.Socket;

import org.junit.Ignore;
import org.junit.Test;

import server.control.manager.ServerMgr;

/**
 * ServerTest<br>
 * <code>ServerMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ServerTest {

	/**
	 * 스트링 서버 테스트 <br>
	 * 문자열을 echo하는 서버 생성
	 */
	@SuppressWarnings("static-access")	// mExecutorService
	@Ignore @Test
	public void stringTest() {
		
		/**
		 * 싱글톤으로 구현된 <code>ServerMgr</code>의 인스턴스를 얻음 <code>getInstance()</code>
		 */
		ServerMgr server = ServerMgr.getInstance();
		Socket conn;

		/** 서버 소켓 생성 */
		server.makeServerSocket();
		
		/**
		 * 클라이언트로 부터의 연결을 기다림<br>
		 * <code>waitForClient()</code> slow system call
		 */
		while((conn = server.waitForClient()) != null) {
			
			/**
			 * 스레드풀에 <code>ServerStringHandler</code> 워커스레드 추가
			 */
			server.mExecutorService.execute(server.getServerStringHandler(conn));
		}
		
		/** 서버 소켓 종료 */
		server.close();
	}

	/**
	 * 이미지 서버 테스트 <br>
	 * 이미지를 echo하는 서버 생성
	 */
	@SuppressWarnings("static-access")	// mExecutorService
	@Test
	public void imageTest() {
		
		/**
		 * 싱글톤으로 구현된 <code>ServerMgr</code>의 인스턴스를 얻음 <code>getInstance()</code>
		 */
		ServerMgr server = ServerMgr.getInstance();
		Socket conn;
		Integer index = 0;

		/** 서버 소켓 생성 */
		server.makeServerSocket();
		
		/**
		 * 클라이언트로 부터의 연결을 기다림<br>
		 * <code>waitForClient()</code> slow system call
		 */
		while((conn = server.waitForClient()) != null) {
			
			/**
			 * 스레드풀에 <code>ServerImageHandler</code> 워커스레드 추가
			 */
			server.mExecutorService.execute(server.getImageHandler(conn, index++));
		}
		
		/** 서버 소켓 종료 */
		server.close();
	}
}
