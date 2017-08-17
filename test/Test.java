import org.apache.log4j.Logger;

import server.control.log.LogMgr;
import server.model.blockingqueue.BlockingQueueNode;
import server.model.blockingqueue.Node;

/**
 * Test
 * 
 * @version 1.0 [2017. 8. 18.]
 * @author Choi
 */
public class Test {

	/** 로그 */
	static Logger logger = LogMgr.getInstance("Test");

	@org.junit.Test
	public void test() {
		//Node node = new Node("ss", 2, 1);
		
		//BlockingQueueNode bq = (BlockingQueueNode)node;
		
		Parent p = new Child("p");
		Child c = (Child)p;
		
	}
	class Parent {
		private String name;
		public Parent(String nm) {
			name = nm;
		}
	}
	class Child extends Parent {
		public String cname;
		public Child(String nm) {
			super(nm);
		}
		
	}
}
