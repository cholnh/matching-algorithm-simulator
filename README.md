# Matching-Queue-simulator


*설명*  	

매칭 조건에 일치하는 노드들을 묶어 반환하는 큐입니다.  
Client-Server 관계로 동작하며, 모델에 해당되는 Matching Queue Node를 주고 받습니다.  
Matching Queue를 여러 상황에 맞게 사용할 수 있도록 라이브러리 형태로 제작했습니다.  
 
----  
*설계*	

Node에 해당하는 Matcher 클래스와 MatchingQueue 클래스로 구성되어 있습니다.  
Matcher는 부모노드, 총인원, 피어노드, 타이머로 구성됩니다.  
사용자는 Matcher를 상속하여 내부에 정의된 추상메서드(match, loosen)를 구현해야 합니다.  
match는 매칭조건을 기술하는 부분이고, loosen은 기아현상을 방지하기 위한 Aging정책을 기술하는 부분입니다.  
Matching Queue는 Producer-Consumer 패턴으로 구현되었으며, Matcher를 상속한 클래스를 타입으로 받는 제너릭 구조입니다.  
따라서 Matcher를 입력 받아 타이머를 설정하고 기술된 match 검색을 수행합니다.  

----  
Matching Queue는 다음과 같은 특징이 있습니다.  
-	제너릭과 상속을 이용하여 확장성 높은 구조를 구현하였습니다.
-	매칭 조건이 맞지 않아 발생하는 기아현상을 Aging정책을 통해 방지할 수 있습니다.
-	명시적 락을 사용하여 동기화를 해 두었기 때문에 스레드 안전성이 높습니다.  
(명시적 락의 비선점 방식 때문에 Convoy 효과가 나타날 수 있습니다.)

----    
언어 : JAVA  
라이브러리/프레임워크 : SWT, JUnit4, GroboUtils, log4j  
기간 : 2017/09/01   
~ 2017/09/29  

----   
*Matching Queue 알고리즘*  
  
입력 : 	노드(Matcher)  
처리 : 	 

0. 노드 입력을 기다린다. (take 상태)
1. 노드 입력이 들어오면, Blocking Queue에서 노드를 꺼내온다.
2. 명시적 락을 수행한다.
3. 노드 내부에 타이머를 재 설정한다. (타임아웃 핸들러 재 시작)  
(신규 노드인 경우, 초기에 정의된 시간으로 타이머를 설정한다.)
4. 노드에 정의된 매칭조건에 맞는 노드를 Waiting List 내에서 검색한다.  
만약 조건에 부합한다면 검색한 노드의 피어로 설정한다.  
이 때, 노드에 정의된 총 인원 수가 전부 찼다면 노드의 부모노드를 Result Queue에 등록하고, 타이머를 제거한 뒤 List에서 제거한다.  
5. Waiting List를 전부 검색하지 않았다면 단계 4로 간다.
6. 노드를 Waiting List에 추가한다.
7. 명시적 락을 해제한다.
  
  
*타이머 내 타임아웃 핸들러*  

0. 명시적 락을 수행한다.
1. Blocking Queue와 내부 Waiting List에서 해당 노드를 제거한다.
2. 자신이 속한 그룹과의 연결을 제거한다.
3. 명시적 락을 해제한다.
4. 사전에 정의된 Aging을 수행한다.
5. Aging된 노드를 Blocking Queue와 Waiting List에 재 등록한다.
 
