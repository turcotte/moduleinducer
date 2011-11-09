package ca.uottawa.okorol.bioinf.ModuleInducer.data;

import java.util.ArrayList;

/* Kind of PWM, but for probabilities of occurrence of a 
 * transcription factor binding site per section of sequence
 * 
 */
public class OccurrencePerLocationMatrix {
	
	private ArrayList<String> namesOfTfbs;
	private int sectionLength;
	
	// double[i][j] - 
	//   i: each successive section of a sequence; 
	//      i.e. if sequence length is 15 and section length is 5 then
	//      i==3 and represents sections [0..4][5..9][10..14] of a sequence
	//   j: names of the PWMs
	// => each cell contains a probability of a particular PWM  
	//    occuring in a particular section of a sequence
	private double[][] probabilityMatrix;
	
	public OccurrencePerLocationMatrix(double[][] probabilityMatrix, ArrayList<String> namesOfTfbs, int sectionLength){
		this.probabilityMatrix = probabilityMatrix;
		this.namesOfTfbs = namesOfTfbs;
		this.sectionLength = sectionLength;
	}
	
	public double[][] getProbabilityMatrix() {
		return probabilityMatrix;
	}

	public void setProbabilityMatrix(double[][] probabilityMatrix) {
		this.probabilityMatrix = probabilityMatrix;
	}

	public ArrayList<String> getNamesOfTfbs() {
		return namesOfTfbs;
	}

	public void setNamesOfTfbs(ArrayList<String> namesOfTfbs) {
		this.namesOfTfbs = namesOfTfbs;
	}
	
	public int getSectionLength() {
		return sectionLength;
	}

	public void setSectionLength(int sectionLength) {
		this.sectionLength = sectionLength;
	}
	
	public void printData(){
		for (int j = 0; j < probabilityMatrix[0].length; j++){
			System.out.print(namesOfTfbs.get(j) + "  |  ");
			for (int i = 0; i < probabilityMatrix.length; i++){
				System.out.print(probabilityMatrix[i][j] +"\t");
			}
			System.out.println();
		}
	}

}
