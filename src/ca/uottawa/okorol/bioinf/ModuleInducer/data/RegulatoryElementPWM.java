package ca.uottawa.okorol.bioinf.ModuleInducer.data;

public class RegulatoryElementPWM implements Comparable<RegulatoryElementPWM>{
	private String regElementName;
	private int[][] pwm;
	private double score;
	
	//TODO: faze it out

	public RegulatoryElementPWM(){
	}
	
	public RegulatoryElementPWM(String tfbsName, int[][] pwm){
		this.regElementName = tfbsName;
		this.pwm = pwm;
	}

	
	public String getName() {
		return regElementName;
	}
	public void setName(String tfbsName) {
		this.regElementName = tfbsName;
	}

	public int[][] getPwm() {
		return pwm;
	}
	public void setPwm(int[][] pwm) {
		this.pwm = pwm;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	/*
	 * @return pwm array formatted as a String; tabs are placed in between the elements
	 */
	public String getPwmString(){
		String pwmStr = "";
		
		for (int j = 0; j < pwm[0].length; j++){
			
			String nt = "?";
			
			switch (j) {
			case 0:
				nt = "A"; break;
			case 1:
				nt = "C"; break;
			case 2:
				nt = "G"; break;
			case 3:
				nt = "T"; break;
			default:
				break;
			}
			
			pwmStr = pwmStr + nt + "\t|";
			
			for (int i = 0; i < pwm.length; i++){
				pwmStr = pwmStr + "\t" + pwm[i][j];
			}
			
			if (j < 3){
				pwmStr = pwmStr + "\n";
			}
			
		}
		
		return pwmStr;
	}

	@Override
	public int compareTo(RegulatoryElementPWM otherHit) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    
	    // first sort by score
	    if (this.score > otherHit.score) return BEFORE;
	    if (this.score < otherHit.score) return AFTER;
	    
	    // then sort by name
	    if (this.regElementName != null){
	    	if (this.regElementName.compareTo(otherHit.regElementName) < 0) return BEFORE;
	    	if (this.regElementName.compareTo(otherHit.regElementName) > 0) return AFTER;
	    }
	    
	    
	    // if all of these have passed, then the objects are equal
	    return EQUAL;
	}
	
}
