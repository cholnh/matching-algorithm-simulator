package server.control.manager;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * ServerMgr
 * 
 * @author Choi
 */
public class ServerMgr {
	
	/** ServerMgr INSTANCE */
	private static class Singleton {
		/** Initialization On Demand Holder Idiom */
		private static final ServerMgr INSTANCE = new ServerMgr();
	}

	/** 로그 */
	static Logger logger = LogMgr.getInstance("Server");
	
	/** 
	 * 스레드 풀 (WorkStealingPool)<br>
	 * <code>newWorkStealingPool</code>파라미터는  parallelism level(병렬화 수준)을 설정<br>
	 */
	public static final ExecutorService mExecutorService 
		= Executors.newWorkStealingPool(1000);
	//	= Executors.newFixedThreadPool(1000);	// Runtime.getRuntime().availableProcessors()
	
	/** 서버 소켓 */
	private static ServerSocket serverSoc = null;
	
	/** 서버 소켓 포트 (기본 : 5000) */
	private static Integer PORT = 5000;

	private ServerMgr() {/*Singleton*/}

	/**
	 * ServerMgr INSTANCE 반환
	 * 
	 * @return ServerMgr	싱글톤 정적 인스턴스 반환 (ServerMgr INSTANCE)
	 */
	public static synchronized ServerMgr getInstance() {
		return Singleton.INSTANCE;
	}

	/**
	 * PORT설정 후 ServerMgr INSTANCE 반환
	 * 
	 * @param PORT			서버 소켓 포트
	 * @return ServerMgr	싱글톤 정적 인스턴스 반환 (ServerMgr INSTANCE)
	 */
	public static synchronized ServerMgr getInstance(final Integer PORT) {
		ServerMgr.PORT = PORT;
		return getInstance();
	}

	static void log(final String text) {
		logger.info(text);
	}

	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}

	/**
	 * 서버 핸들러<br>
	 * 서버 운영에 대한 전반적인 흐름을 정의하는 부분
	 */
	public void start() {
		Socket conn;
		Integer index = 0;

		/** 서버 소켓 생성 */
		makeServerSocket();
		
		/**
		 * 클라이언트로 부터의 연결을 기다림<br>
		 * <code>waitForClient()</code> slow system call
		 */
		while((conn = waitForClient()) != null) {
			
			/**
			 * 스레드풀에 <code>ServerImageHandler</code> 워커스레드 추가
			 */
			mExecutorService.execute(getImageHandler(conn, index++));
		}
		close();
	}

	/** 서버 종료 처리 */
	public void close() {
		try {
			
			/** 서버 소켓 종료 */
			serverSoc.close();
			
			/** 스레드 풀 종료 */
			mExecutorService.shutdown();
		} catch (IOException e) {
			log("serverSoc close Error", e);
		}
	}

	public ServerStringHandler getServerStringHandler(final Socket socket) {
		return new ServerStringHandler(socket);
	}

	public ServerImageHandler getImageHandler(final Socket socket) {
		return new ServerImageHandler(socket);
	}

	public ServerImageHandler getImageHandler(final Socket socket, final Integer index) {
		return new ServerImageHandler(socket, index);
	}

	/**
	 * 서버 이미지 핸들러<br>
	 * 클라이언트로 부터 받은 이미지를 echo해주는 핸들러
	 * 
	 * @author Choi
	 */
	class ServerImageHandler extends Thread {
		private Socket socket;
		
		/** 클라이언트 연결 스트림 핸들러 */
		private ConnectionToClient conToClient;
		
		/** 이미지 파일 이름 작명에 쓰임 */
		private String clientName = "anonymous";

		ServerImageHandler(final Socket socket) {
			this.socket = socket;
			conToClient = new ConnectionToClient(socket);
		}

		ServerImageHandler(final Socket socket, final Integer index) {
			this(socket);
			clientName = "client[" + index + "]";
		}

		/** Thread 실행부 */
		public void run() {
			BufferedImage bimg;
			
			/** 클라이언트로 부터 이미지 읽기 */
			if((bimg = conToClient.imageRead()) != null) {
				
				/** 이미지 저장 */
				conToClient.saveFile(bimg, clientName);
				
				/** 이미지 echo */
				conToClient.imageWrite(bimg);
			}
			
			/** 작업이 끝났으므로 서버에 연결되어있는 클라이언트 수를 수정  */
			clientCount--;
			log("exit " + socket.getInetAddress().getHostAddress() + " [clients : " + clientCount + "명]");
		}
	}

	/**
	 * 서버 스트링 핸들러<br>
	 * 클라이언트로 부터 받은 문자열을 echo해주는 핸들러
	 * 
	 * @author Choi
	 */
	class ServerStringHandler extends Thread {
		private Socket socket;
		
		/** 클라이언트 연결 스트림 핸들러 */
		private ConnectionToClient conToClient;

		ServerStringHandler(final Socket socket) {
			this.socket = socket;
			conToClient = new ConnectionToClient(socket);
		}

		/** Thread 실행부 */
		public void run() {
			String response = "";
			
			/** 클라이언트로 부터 문자열 읽기 */
			if((response = conToClient.stringRead()) != null){
				
				/** 문자열 echo */
				conToClient.stinrgWrite(response);
			}
			
			/** 작업이 끝났으므로 서버에 연결되어있는 클라이언트 수를 수정  */
			clientCount--;
			log("exit " + socket.getInetAddress().getHostAddress() + "\n[clients : " + clientCount + "명]");
		}
	}

	/**
	 * 연결 스트림 핸들러<br>
	 * 클라이언트와 연결된 소켓의 <code>InputStream</code>과 <code>OutputStream</code>을 통하여<br>
	 * 주고 받는 여러 stream 처리를 수행
	 * 
	 * @author Choi
	 */
	class ConnectionToClient {
		private Socket socket;
		private InputStream is;
		private OutputStream os;
		private BufferedReader br;
		private ObjectOutputStream oos;

		ConnectionToClient(final Socket socket) {
			this.socket = socket;
			try {
				is = socket.getInputStream();
				os = socket.getOutputStream();
				br = new BufferedReader(new InputStreamReader(is));
				oos = new ObjectOutputStream(os);
			} catch (Exception e) {
				log("ConnectionToClient Error", e);
			}
		}

		/**
		 * 연결된 소켓(클라이언트)으로 부터 전송된 문자열을 얻음<br>
		 * <code>stringRead()</code> slow system call
		 * 
		 * @return 읽어들인 문자열 반환
		 */
		public String stringRead(){
			try {
				String msg = br.readLine();
				log("receive from " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + msg + "\"");
				return msg;
			} catch(Exception e) {
				log("stringRead Error", e);
				return null;
			}
		}

		/**
		 * 연결된 소켓(클라이언트)으로 문자열을 전송
		 * 
		 * @param obj	전송할 문자열
		 */
		public void stinrgWrite(final Object obj) {
			try {
				oos.writeObject(obj);
				oos.flush();
				log("send to " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + obj.toString() + "\"");
			} catch (Exception e) {
				log("stinrgWrite Error", e);
			}
		}

		/**
		 * 연결된 소켓(클라이언트)으로 이미지 버퍼 전송
		 * 
		 * @param obj	전송할 이미지 버퍼
		 */
		public void imageWrite(final BufferedImage bimg) {
			try {
				ImageIO.write(bimg, "gif", os);
				log("send image to " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")");
			} catch (IOException e) {
				log("imageWrite Error", e);
			}
		}

		/**
		 * 연결된 소켓(클라이언트)으로 부터 전송된 이미지를 얻음<br>
		 * <code>imageRead()</code> slow system call
		 * 
		 * @return 읽어들인 이미지 버퍼 반환
		 */
		public BufferedImage imageRead() {
			BufferedImage bimg = null;
			try {
				bimg = ImageIO.read(is);
				log("receive image from " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")");
			} catch (IOException e) {
				log("imageRead Error", e);
			}
			return bimg;
		}

		/**
		 * 파일 저장 <br>
		 * 메서드 내 <code>FULLPATH</code> 경로에 이미지 저장
		 * 
		 * @param		bimg	저장할 이미지의 버퍼
		 */
		public void saveFile(BufferedImage bimg, String fileName) {
			final String IMAGE_EXTANSION = "gif";
			final String SAVE_IMAGE_PATH = "asset\\server_side";
			final String FULLPATH = SAVE_IMAGE_PATH + "\\" + fileName + "." + IMAGE_EXTANSION;

			FileOutputStream fout;
			try {
				fout = new FileOutputStream(FULLPATH);
				ImageIO.write(bimg, IMAGE_EXTANSION, fout);
				log("save image \t PATH : " + FULLPATH);
			} catch (FileNotFoundException e) {
				log("saveFile error", e);
			} catch (IOException e) {
				log("saveFile error", e);
			}
		}
		
		/** 종료처리 */
		public void close() {
			try {
				br.close();
				oos.close();
			} catch (IOException e) {
				log("Connection close Error", e);
			}
		}
	}

	/** 서버 소켓 생성 */
	public void makeServerSocket() {
		if(serverSoc == null) {
			try {
				log("서버 생성");
				serverSoc = new ServerSocket(PORT);
			} catch (IOException e) {
				log("makeServerSocket Error", e);
			}
		}
	}

	/** 서버에 접속된 클라이언트 수 */
	private volatile Integer clientCount = 0;
	
	/**
	 * 클라이언트 연결 대기<br>
	 * 접속을 시도하는 클라이언트와의 소켓연결을 하고  해당 소켓 <code>serverSoc</code>을 받아 반환
	 * 
	 * @return 연결된 클라이언트 소켓
	 */
	public Socket waitForClient() {
		if(serverSoc == null) return null;
		log("waitting for Client...");
		try {
			Socket conn = serverSoc.accept();
			clientCount++;
			log("connect to " + conn.getInetAddress().getHostAddress() + " [clients : " + clientCount + "명]");
			return conn;
		} catch (IOException e) {
			log("Accept Error", e);
			return null;
		}
	}
}