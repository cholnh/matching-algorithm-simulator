package server.control;


import java.net.Socket;

import org.junit.Test;

import server.control.manager.ServerMgr;

/**
 * ServerTest<br>
 * <code>ServerMgr</code> 클래스에 대한 테스트 유닛 케이스
 * 
 * @author Choi
 */
public class ServerTest {
	@SuppressWarnings("static-access")	// mExecutorService
	@Test
	public void nodeTest() {
		
		ServerMgr server = ServerMgr.getInstance();
		Socket conn;

		/** 서버 소켓 생성 */
		server.makeServerSocket();
		
		/** 클라이언트로 부터의 연결을 기다림 slow system call */
		while((conn = server.waitForClient()) != null) {
			
			/** 스레드풀에 워커스레드 추가 */
			server.mExecutorService.execute(server.getServerHandler(conn));
		}
		server.close();
	}
}
