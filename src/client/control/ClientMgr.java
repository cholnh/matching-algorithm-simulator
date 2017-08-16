package client.control;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;

import net.sourceforge.groboutils.junit.v1.TestRunnable;
import server.control.log.LogMgr;

/**
 * ClientMgr
 * 
 * @author Choi
 */
public class ClientMgr {
	
	/** 로그 */
	static Logger logger = LogMgr.getInstance("Client");

	/** 연결할 서버 IP */
	private String SERVERIP = "127.0.0.1";
	
	/** 연결할 서버 포트 */
	private Integer SERVERPORT = 5000;
	
	/** 랜덤 딜레이 최소/최대 값 */
	private final Integer DELAY_MINIMUM = 1;
	private final Integer DELAY_MAXIMUM = 4;
	
	/** 이미지 정보(경로, 이름, 확장자) */
	private static final String TEST_IMAGE_PATH = "asset\\img";
	private String TEST_IMAGE_NAME = "testimage.gif";
	private static final String SAVE_IMAGE_PATH = "asset\\client_side";
	private static final String IMAGE_EXTANSION = "gif";

	public ClientMgr() {}

	public ClientMgr(final String SERVERIP, final Integer SERVERPORT) {
		this();
		this.SERVERIP = SERVERIP;
		this.SERVERPORT = SERVERPORT;
	}

	static void log(final String text) {
		logger.info(text);
	}

	static void log(final String text, final Exception e) {
		log(text);
		e.printStackTrace();
	}

	/**
	 * 클라이언트 핸들러<br>
	 * 클라이언트 운영에 대한 전반적인 흐름을 정의하는 부분
	 */
	public void start() {
		/** 서버로 소켓 연결 요청 */
		Socket socket = connectToServer();

		/**
		 * 연결 핸들러 생성 <br>
		 * <code>ConnectionToServer</code> (연결 핸들러)는 여러 stream 처리를 수행
		 * 
		 * @see ClientMgr.ConnectionToServer
		 */
		ConnectionToServer conn = getConnectionToServer(socket);

		/** 전송할 이미지 */
		File imgFile = new File("asset\\img\\testimage.gif");
		BufferedImage bimg;
		try {
			/** 보낼 이미지 로드 */
			bimg = ImageIO.read(imgFile);
			
			/** 서버로 이미지 버퍼 전송 */
			conn.imageWrite(bimg);
		} catch (IOException e) {
			log("start error", e);
		}
		
		/** 서버로 부터 echo된 이미지 읽기 */
		if((bimg = conn.imageRead()) != null) {
			
			/** 이미지 저장 */
			conn.saveFile(bimg, "testimage");
		}
	}

	public ClientStringHandler getClientStringHandler() {
		return new ClientStringHandler();
	}

	public ClientImageHandler getClientImageHandler() {
		return new ClientImageHandler();
	}

	public ClientImageHandler getClientImageHandler(final Integer index) {
		return new ClientImageHandler(index);
	}

	/**
	 * 클라이언트 이미지 핸들러<br>
	 * 서버로 이미지를 보내고 echo받는 핸들러<br>
	 * 해당 핸들러는 <code>TestRunnable</code>을 상속받아<br>
	 * <code>ClientTest</code>의 멀티스레딩 테스트가 가능함
	 * 
	 * @author Choi
	 */
	class ClientImageHandler extends TestRunnable {
		
		/** 서버 연결 스트림 핸들러 */
		private ConnectionToServer conToServer;
		
		/** 이미지 파일 이름 작명에 쓰임 */
		private String clientName = "anonymous";

		ClientImageHandler() {
			Socket socket = connectToServer();
			conToServer = new ConnectionToServer(socket);
		}

		ClientImageHandler(Integer index) {
			this();
			clientName = "client[" + index + "]";
		}

		/** Thread 실행부 */
		@Override
		public void runTest() throws Throwable {
			
			/** 전송할 이미지 파일 */
			File imgFile = new File(TEST_IMAGE_PATH + "\\" + TEST_IMAGE_NAME);

			/** 이미지 파일을 버퍼 이미지로 읽어옴 */
			BufferedImage bimg = ImageIO.read(imgFile);

			/** 1~4초 대기 */
			Thread.sleep((new Random().nextInt(DELAY_MAXIMUM)+DELAY_MINIMUM)*1000);

			/** 서버로 이미지 전송 */
			conToServer.imageWrite(bimg);
			
			/** 서버로부터 echo된 이미지 읽기 */
			if((bimg = conToServer.imageRead()) != null) {
				
				/** 이미지 저장 */
				conToServer.saveFile(bimg, clientName);
			}
		}
	}
	
	/**
	 * 클라이언트 스트링 핸들러<br>
	 * 서버로 문자열을 보내고 echo받는 핸들러<br>
	 * 해당 핸들러는 <code>TestRunnable</code>을 상속받아<br>
	 * <code>ClientTest</code>의 멀티스레딩 테스트가 가능함
	 * 
	 * @author Choi
	 */
	class ClientStringHandler extends TestRunnable {
		
		/** 서버 연결 스트림 핸들러 */
		private ConnectionToServer conToServer;

		ClientStringHandler() {
			Socket socket = connectToServer();
			conToServer = new ConnectionToServer(socket);
		}

		/** Thread 실행부 */
		@Override
		public void runTest() throws Throwable {
			
			/** 보낼 문자열 (길이 5인 무작위 문자열로 구성) */
			String sendmsg = RandomString.random(5);
			String response = "";

			/** 1~4초 대기 */
			Thread.sleep((new Random().nextInt(DELAY_MAXIMUM)+DELAY_MINIMUM)*1000);

			/** 서버로 문자열 전송 */
			conToServer.stringWrite(sendmsg);
			
			/** 서버로부터 echo된 문자열 읽기 */
			if ((response = conToServer.stringRead()) != null) {
				
				/** echo된 문자열 일치 판별 */
				Assert.assertEquals(sendmsg, response);
			}
		}
	}

	public ConnectionToServer getConnectionToServer(final Socket socket) {
		return new ConnectionToServer(socket);
	}

	/**
	 * 연결 스트림 핸들러<br>
	 * 서버와 연결된 소켓의 <code>InputStream</code>과 <code>OutputStream</code>을 통하여<br>
	 * 주고 받는 여러 stream 처리를 수행
	 * 
	 * @author Choi
	 */
	public class ConnectionToServer {
		private Socket socket;
		private InputStream is;
		private OutputStream os;
		private BufferedReader br;
		private ObjectInputStream ois;
		private PrintWriter pw;

		ConnectionToServer(final Socket socket) {
			this.socket = socket;
			try {
				is = socket.getInputStream();
				os = socket.getOutputStream();
				br = new BufferedReader(new InputStreamReader(is));
				pw = new PrintWriter(os);
				ois = new ObjectInputStream(is);
			} catch (Exception e) {
				log("ConnectionToServer Error", e);
			}
		}

		/**
		 * 연결된 소켓(서버)으로 문자열을 전송
		 * 
		 * @param obj	전송할 문자열
		 */
		public void stringWrite(final String obj) {
			try {
				pw.println(obj);
				pw.flush();
				log("send to " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + obj.toString() + "\"");
			} catch (Exception e) {
				log("write Error", e);
			}
		}

		/**
		 * 연결된 소켓(서버)으로 부터 전송된 문자열을 얻음<br>
		 * <code>stringRead()</code> slow system call
		 * 
		 * @return 읽어들인 문자열 반환
		 */
		public String stringRead() {
			try {
				String msg = (String) ois.readObject();
				log("receive from " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")\t msg : \"" + msg + "\"");
				return msg;
			} catch (Exception e) {
				log("read Error", e);
				return null;
			}
		}

		/**
		 * 연결된 소켓(서버)으로 이미지 버퍼 전송
		 * 
		 * @param bimg	전송할 이미지 버퍼
		 */
		public void imageWrite(final BufferedImage bimg) {
			try {
				ImageIO.write(bimg, IMAGE_EXTANSION, os);
				log("send image to " + socket.getInetAddress().getHostAddress() + 
						" (" + Thread.currentThread().getName() + ")");
			} catch (IOException e) {
				log("imageWrite Error", e);
			}
		}

		/**
		 * 연결된 소켓(서버)으로 부터 전송된 이미지를 얻음<br>
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
		 * @param		bimg		저장할 이미지의 버퍼
		 * @param		fileName	파일 이름
		 */
		public void saveFile(final BufferedImage bimg, final String fileName) {
			final String FULLPATH = SAVE_IMAGE_PATH + "\\" + fileName + "." + IMAGE_EXTANSION;

			FileOutputStream fout;
			try {
				fout = new FileOutputStream(FULLPATH);
				ImageIO.write(bimg, IMAGE_EXTANSION, fout);
				log("save image (" + FULLPATH + ")");
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
				ois.close();
				pw.close();
			} catch (IOException e) {
				log("Connection close Error", e);
			}
		}
	}

	/**
	 * 서버 소켓 연결 요청 
	 * 
	 * @return 연결된 서버 소켓 반환
	 */
	public Socket connectToServer() {
		Socket conn = null;
		try {
			conn = new Socket(this.SERVERIP, this.SERVERPORT);
			log("connect to server");
		} catch (IOException e) {
			log("connectToServer Error", e);
		} 
		return conn;
	}

	public Socket connectToServer(final String IP, final Integer PORT) {
		this.SERVERIP = IP; 
		this.SERVERPORT = PORT;
		return connectToServer();
	}

	/**
	 * 랜덤 문자열생성 클래스
	 */
	static class RandomString {
		private static final char[] chars;

		static {
			StringBuffer buffer = new StringBuffer();
			for(char ch = 'a'; ch <= 'z'; ++ch)
				buffer.append(ch);
			for(char ch = 'A'; ch <= 'Z'; ++ch)
				buffer.append(ch);
			chars = buffer.toString().toCharArray();	
		}

		/**
		 * 핸덤 문자열 생성
		 * 
		 * @param length	문자열 길이
		 * @return	무작위의 문자열 반환
		 */
		public static String random(final int length){
			if(length < 1)
				throw new IllegalArgumentException("length < 1: " + length);

			StringBuilder randomString = new StringBuilder();
			Random random = new Random();

			for(int i=0; i < length; i++){
				randomString.append(chars[random.nextInt(chars.length)]);			
			}
			return randomString.toString();
		}	
	}
}

