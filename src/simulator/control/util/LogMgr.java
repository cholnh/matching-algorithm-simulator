package simulator.control.util;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * LogMgr<br>
 * <code>org.apache.log4j.Logger</code> properties를 동적으로 관리<br>
 * 
 * @author Choi
 */
public class LogMgr {
	
	/** LOGGER MAP INSTANCE */
	private static HashMap<String, Logger> loggerMap;
	
	private LogMgr() {}

	/**
	 * 받은 switchFileName에 해당하는 Logger 인스턴스 반환<br>
	 * HashMap 인스턴스는 <code>getInstance</code>가 처음 불릴 때 한번 생성되며 (Singleton Ptn)<br>
	 * pattern이나 log file 이름등 log4j properties 정의 또한 초기에 정해짐<br>
	 * 
	 * @param switchFileName	동적으로 생성할 로그 파일이름
	 * @return logger			switchFileName에 해당하는 logger 인스턴스 반환
	 */
	public synchronized static Logger getInstance(String switchFileName) {
		if(loggerMap == null)
			loggerMap = new HashMap<>();
		
		/** Logger 등록 */
		if(!loggerMap.containsKey(switchFileName)) {
			// ┌────────────────────────────┐	┌───────────────────────────────────┐
			// │	Ex)	/log/default.log	│	│	Ex)	/log/{switchFileName}.log	│
			// └────────────────────────────┘	└───────────────────────────────────┘
			final String DEFAULT_PATH 		= "log";
			final String DEFAULT_NAME 		= "default";
			final String DEFAULT_EXTENSION 	= "log";
			final String PATTERN 			= "[%p] " + switchFileName + " %d{yyyy-MM-dd HH:mm:ss}%n\t%m%n";
			final String datePattern 		= ".yyyyMMdd";

			/** Logger INSTANCE 얻어옴 */
			Logger logger = Logger.getLogger(switchFileName);
			
			/** 기본 log4j.properties 파일의 위치 재정의 */
			PropertyConfigurator.configure( "./conf/log4j.properties" );

			/** pattern 생성 */
			PatternLayout layout = new PatternLayout(PATTERN);

			/** log file rename 설정 */
			if(switchFileName == null) switchFileName = DEFAULT_NAME;
			String fileName = DEFAULT_PATH + "/" + switchFileName + "." + DEFAULT_EXTENSION;

			/** console appender 설정 */
			ConsoleAppender cAppender = new ConsoleAppender(layout);
			logger.addAppender(cAppender);

			/** file appender 설정 */
			DailyRollingFileAppender fAppender = null;
			try {
				fAppender = new DailyRollingFileAppender(layout, fileName, datePattern);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.addAppender(fAppender);
			
			loggerMap.put(switchFileName, logger);
		}
		
		return loggerMap.get(switchFileName);
	}
}
