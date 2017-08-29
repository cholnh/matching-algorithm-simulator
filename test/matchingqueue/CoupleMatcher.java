package matchingqueue;

import java.util.Random;

import matchingqueue.Matcher;

/**
 * CoupleMatcher
 * 
 * @version 1.0 [2017. 8. 29.]
 * @author Choi
 */
public class CoupleMatcher extends Matcher{

	private static final long serialVersionUID = 1L;
	private String name;
	private boolean sex;	// t : male , f : female
	private boolean isQueer;
	private boolean isChange;
	private Integer lastCount;
	private Integer age;
	private Integer needMaxAge;
	private Integer needMinAge;
	
	public String getName() {return name;}
	public Integer getAge() {return age;}
	public Integer getNeedMaxAge() {return needMaxAge;}
	public Integer getNeedMinAge() {return needMinAge;}
	public Integer getLastCount() {return lastCount;}
	public boolean isSex() {return sex;}
	public void setName(String name) {this.name = name;}
	public void setAge(Integer age) {this.age = age;}
	public void setSex(boolean sex) {this.sex = sex;}
	public void setNeedMaxAge(Integer needMaxAge) {this.needMaxAge = needMaxAge;}
	public void setNeedMinAge(Integer needMinAge) {this.needMinAge = needMinAge;}

	public CoupleMatcher(Integer totalPeerCount, String name, boolean sex, boolean isQueer, Integer age, Integer needMaxAge, Integer needMinAge) {
		super(totalPeerCount);
		this.name = name;
		this.sex = sex;
		this.isQueer = isQueer;
		this.isChange = false;
		this.lastCount = 0;
		this.age = age;
		
		if(needMaxAge < needMinAge)
			needMaxAge = needMinAge;
			
		this.needMaxAge = needMaxAge;
		this.needMinAge = needMinAge;
	}
	
	@Override
	public boolean match(Matcher node) {
		CoupleMatcher cnode = (CoupleMatcher) node;
		
		// 성별조건
		// 남-여
		if(this.sex != cnode.sex) {
			// 성소수자일 경우 실패
			if(this.isQueer || cnode.isQueer) return false;
			
			// 만족하는 나이조건
			// min <= age <= max 
			if(this.needMinAge <= cnode.age && cnode.age <= this.needMaxAge) {
				// 매칭성공
				return true;
			}
		} else {
			// 성소수자가 아닐 경우 실패
			if(!this.isQueer || !cnode.isQueer) return false;
			
			if(this.needMinAge <= cnode.age && cnode.age <= this.needMaxAge) {
				// 매칭성공
				return true;
			}
		}
		
		if(lastCount > 10 && cnode.getLastCount() > 10) return true;	// 영원히 매칭이 안되는 것을 방지
		
		return false;
	}
	
	@Override
	public Matcher loosen() {
		if((needMaxAge - needMinAge) < 40) {
			if(needMinAge != 19) {
				// 눈을 낮춰본다.
				Random rand = new Random();
				if(rand.nextBoolean()) 
					needMaxAge++;
				else
					needMinAge--;
			} else {
				// 부모동의없이 19살 미만과 결혼은 범죄행위이다.
				needMaxAge++;
			}
		} else if (!isChange) {
			// 게이나 레즈나 이성애자가 되는 길을 택한다.
			isQueer = !isQueer;
			isChange = true;
		} else {
			lastCount++;
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		String text = "이름 : " + name + "(" + (sex?("남"+(isQueer?" 게이":"")):("여"+(isQueer?" 레즈":""))) + ", " + age + ") +" + (needMaxAge-age) + "살 -" + (age-needMinAge) + "살";
		return text;
	}
}
