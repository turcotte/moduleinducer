package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.OccurrencePerLocationMatrix;

public class StatAnalyser {
	
	private ArrayList<Feature> hits;
	
	// "Lazy evaluation" vars. Below vars are set once the appropriate methods are called.
	// Afterwards, they don't need to be recalculated
	private ArrayList<Integer> tfbsDistances;
	private double tfbsMean = - 99d; 
	
	
	public StatAnalyser(ArrayList<Feature> tfbsHits){
		this.hits = tfbsHits;
		Collections.sort(this.hits);
	}
	
	private void setTfbsDistances(){
		tfbsDistances = new ArrayList<Integer>();
		
		//Since the list of hits is sorted, we need to get to the point where TFBS start
		int tfbsStartPosition = 0;
		for (Iterator<Feature> iterator = hits.iterator(); iterator.hasNext();) {
			Feature hit = (Feature) iterator.next();
			if (Feature.TYPE_TFBS.equals(hit.getType())) break;
			tfbsStartPosition++;
		}
		
		String currGeneId = "";
		
		for (int i = tfbsStartPosition; i < hits.size(); i++) {
			if (!Feature.TYPE_TFBS.equals(hits.get(i).getType())){ //reached the end of tfbs chunk
				break;
			}
			
			if (!currGeneId.equals(hits.get(i).getParent())) {
				currGeneId = hits.get(i).getParent();
			}
			
			int j = i + 1;
			
			while(j < hits.size() && currGeneId.equals(hits.get(j).getParent()) ){
				tfbsDistances.add(Math.abs(hits.get(i).getStartPosition() - hits.get(j).getStartPosition()));
				j++;
			}
		}
	}
	
	public double getMean(){
		
		if (tfbsMean < 0) { //mean was not set yet

			if (tfbsDistances == null) setTfbsDistances();
			
			
			int sumDist = 0;
			
			for (Iterator<Integer> iterator = tfbsDistances.iterator(); iterator.hasNext();) {
				Integer dist = (Integer) iterator.next();
				sumDist = sumDist + dist;
			}
			
			tfbsMean = (double) sumDist / tfbsDistances.size();
		}
		
		return tfbsMean;
	}
	
	
	public double getStandardDeviation(){

		if (tfbsDistances == null) setTfbsDistances();
		getMean(); //make sure mean is set
		
		double sumSqr = 0;
		for (Iterator<Integer> iterator = tfbsDistances.iterator(); iterator.hasNext();) {
			Integer dist = (Integer) iterator.next();
			sumSqr = sumSqr + Math.pow(tfbsMean - dist, 2);
		}
		
		return Math.sqrt(sumSqr / tfbsDistances.size());
	}
	
	/*
	 * @param lengthOfSection	length of section in which upstream sequence will be divided; 
	 * 							probability is calculated per section
	 * @param lengthOfSequence	length of the upstream region sequence
	 * @param numOfSequences	number of upstream regions
	 * @return	matrix with location probabilities of PWM occurrences in regulatory regions
	 * 
	 * Note: StartPositions are converted to abs values
	 */
	public OccurrencePerLocationMatrix getLocationalProbabilities(int lengthOfSection, int lengthOfSequence, int numOfSequences){
		int numOfSections = lengthOfSequence / lengthOfSection; //we get only the whole part - last sequence could be longer
		ArrayList<String> tfbsNames = FeaturesTools.getUniqueNameList(hits);
		double[][] probabilities = new double[numOfSections][tfbsNames.size()];
		
		
		int i = 0;
		int j = 0;
		for (Iterator<Feature> iterator = hits.iterator(); iterator.hasNext();) {
			Feature hit = (Feature) iterator.next();
			j = tfbsNames.indexOf(hit.getName());
			i = Math.abs(hit.getStartPosition()) / lengthOfSection;
			//special boundary case
			if (i == numOfSections) i--;
			probabilities[i][j] = probabilities[i][j] + 1.0 / numOfSequences;
			//TODO take care of same tfbs occurring in the same section twice
		}
		
		return new OccurrencePerLocationMatrix(probabilities, tfbsNames, lengthOfSection);
	}



}
