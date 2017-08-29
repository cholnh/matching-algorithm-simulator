
/**
 * package-info
 * 
 * Matching Queue는 제너릭으로 설계되어 UI와의 연결이 매끄럽지 못하다.
 * BlockingQueueMgr(구체클래스)는 Matching Queue를 구체화 시킨 것이고,  
 * Node를 상속받아 만들어진 BlockingQueueNode는 Matcher를 구체화 시킨것이다.
 * 기본적인 설계 스키마는 MatchingQueue, Matcher를 따른다.
 * 따라서 simulator는 MatchingQueue와 Matcher의 구체클래스에 해당되는 
 * BlockingQueue를 예시로 시뮬레이션하며, 서버-클라이언트 관계로 동작한다.
 * 
 * UI는 UIMgr 내 main 메서드에서 시작되며, swt 컴포넌트들로 구성되어 있고 모든 UI처리는 UIMgr을 거친다.
 * 클라이언트 - 서버관계로 동작하며, 이 둘은 모델에 해당되는 BlockingQueueNode를 주고받는다.
 * 서버 내부 로직은 BlockingQueueMgr의 CoreHandler를 수행하게 되는데, MatchingQueue와는 
 * 조금 다르게 설계 되어있다. (MatchingQueue가 갖는 queue특성인 put(), take()가 없음 - 캡슐화 실패)
 * 
 * @version 1.0 [2017. 8. 29.]
 * @author Choi
 */
package simulator;