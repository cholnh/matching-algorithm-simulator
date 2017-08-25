package server.control.util;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import server.control.log.LogMgr;

/**
 * MemoryMonitor
 * 
 * @version 1.0 [2017. 8. 25.]
 * @author Choi
 */
public class MemoryMonitor {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("MemoryMonitor");

	static Runtime r = Runtime.getRuntime();
	
	public static void print() {
		DecimalFormat format = new DecimalFormat("###,###,###.##");
		
		// JVM이 현재 시스템에 요구 가능한 최대 메모리량, 이 값을 넘으면 OutOfMemory 오류가 발생 
		long max = r.maxMemory() ;

		// JVM이 현재 시스템에 얻어 쓴 메모리의 총량
		long total = r.totalMemory();

		// JVM이 현재 시스템에 청구하여 사용중인 최대 메모리(total)중에서 사용 가능한 메모리
		long free = r.freeMemory();

		System.out.println("Max: " + format.format(max/1048576) + "MB (" + format.format(max) + "Byte) , "
						+ "Total: " + format.format(total/1048576) + "MB (" + format.format(total) + "Byte) , "
						+ "Free: "+format.format(free/1048576) + "MB (" + format.format(free) + "Byte)");          
	}
}
