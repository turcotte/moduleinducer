package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.OccurrencePerLocationMatrix;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;

public class FeaturesTools {

	public static ArrayList<String> getIDList(ArrayList<Feature> features){
		ArrayList<String>  list = new ArrayList<String>();
		for (int i = 0; i < features.size(); i++) {
			list.add(features.get(i).getId());
		}
		return list;
	}
	
	public static ArrayList<String> getUniqueNameList(ArrayList<Feature> features){
		ArrayList<String>  list = new ArrayList<String>();
		
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			if (!list.contains(feature.getName())){
				list.add(feature.getName());
			}
		}
		return list;
	}
	
	public static ArrayList<String> getUniqueParentList(ArrayList<Feature> features){
		ArrayList<String> list = new ArrayList<String>();
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			if (!list.contains(feature.getParent())){
				list.add(feature.getParent());
			}
		}
		
		return list;
	}
	
	public static String[] getUniqueNameArray(ArrayList<Feature> features){
		ArrayList<String> list = getUniqueNameList(features);
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}
	
	/* Calculates a nucleotide (nt) composition of all the sequences in the Features array
	 * @param features - ArrayList of features to calculate a total nt composition 
	 * 					 Note: sequence parameter of the Feature has to be set;
	 * 						   sequence can be upper or lower case characters of any kind,
	 * 									however, only a,c,g, and t characters will be counted
	 * return array of frequencies in the following order: [0] - a
	 * 													   [1] - c
	 * 													   [2] - g
	 * 													   [3] - t
	 */
	public static double[] getNucleotideComposition(ArrayList<Feature> features){
		String totalSequence = "";
		
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			totalSequence = totalSequence + feature.getSequence();
		}
		
		return DataModeller.getNucleotideComposition(totalSequence);
	}
	
	
	/* Returns a number of matches of all the sequences in the features to the provided regex expression
	 * 
	 * @param regex regular expression to be matched
	 * @param features set of features in the sequences of which the match is supposed to be found
	 * 
	 * @return total number of regex matches in all the sequences of features
	 */
	public static int getRegexMatchNumber(String regex, ArrayList<Feature> features){
		int numberOfMatches = 0;

		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature f = (Feature) iterator.next();
			numberOfMatches = numberOfMatches + DataModeller.getNumberOfMatches(regex, f.getSequence());
		}
		
		return numberOfMatches;
		
	}
	
	public static void printFeatures(ArrayList<Feature> features){
		System.out.println("Id, Name, Parent, Type, Note, Start pos, End pos, Score, Strand, Sequence");
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature f = (Feature) iterator.next();
			System.out.println(f.getId() + ", " + f.getName() + ", " +  f.getParent() + ", " + f.getType() + ", " +  f.getNote() + ", " +  
							   f.getStartPosition() + ", " + f.getEndPosition() + ", " + f.getScore() + ", " + ", " + f.getStrand() + ", " + f.getSequence());
		}
		
	}
	
	public static void printRegElDistances(String regEl1, String regEl2, ArrayList<Feature> features){
		ArrayList<SeqDist> distances = getRegElDistances(regEl1, regEl2, features);
		
		for (Iterator<SeqDist> iterator = distances.iterator(); iterator.hasNext();) {
			SeqDist seqDist = (SeqDist) iterator.next();
			System.out.println("Distance:\t" + seqDist.getDistance() + "\t Sequence:\t" + seqDist.getRegRegionName());
			
		}
	}
	/* Given a set of regulatory elements, calculates the distances between two
	 * TFBS in each sequence
	 */
	public static ArrayList<SeqDist> getRegElDistances(String regEl1, String regEl2, ArrayList<Feature> features){
		Collections.sort(features); //sorts by reg region first
		
		ArrayList<SeqDist> distances = new ArrayList<SeqDist>();
		
		String currRegRegionName = "";
		ArrayList<Feature> regEl1Features = new ArrayList<Feature>(); //keeps all regEl1 features per sequence
		ArrayList<Feature> regEl2Features = new ArrayList<Feature>(); //keeps all regEl2 features per sequence
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			if (!currRegRegionName.equals(feature.getParent())){ //new sequence
				//calculate distances
				distances.addAll(getDistances(regEl1Features, regEl2Features));
				regEl1Features = new ArrayList<Feature>();
				regEl2Features = new ArrayList<Feature>();
				currRegRegionName = feature.getParent();
			}
			
			if (regEl1.equals(feature.getName())){
				regEl1Features.add(feature);
			}
			if (regEl2.equals(feature.getName())){
				regEl2Features.add(feature);
			}
		}
		//case for the last sequence
		distances.addAll(getDistances(regEl1Features, regEl2Features));
		
		Collections.sort(distances);
		
		return distances;
	}
	
	public static ArrayList<SeqDist> getDistances(ArrayList<Feature> regEls1, ArrayList<Feature> regEls2){
		ArrayList<SeqDist> distArr = new ArrayList<SeqDist>();
		
		if (regEls1 != null && regEls2 != null && !regEls1.isEmpty() && !regEls2.isEmpty()){
			for (Iterator<Feature> iterator = regEls1.iterator(); iterator.hasNext();) {
				Feature regEl1 = (Feature) iterator.next();
				
				for (Iterator<Feature> iterator2 = regEls2.iterator(); iterator2.hasNext();) {
					Feature regEl2 = (Feature) iterator2.next();
					int dist = 0;
					if (regEl1.getStartPosition() < regEl2.getStartPosition()){
						dist = regEl2.getStartPosition() - regEl1.getEndPosition();
					} else {
						dist = regEl1.getStartPosition() - regEl2.getEndPosition();
					}
					distArr.add(new SeqDist(regEl1.getParent(), dist));
				}
			}
		}
		
		return distArr;
	}
	
	/* Prints a list of features (with all the possible values)
	 * to a specified file. 
	 * This method is used for visual output check
	 */
	public static void printFeaturesToFile(ArrayList<Feature> features, String fileName){
		try {
		
		   BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		   bw.write(";;; Id, Name, Parent, Type, Note, Start pos, End pos, Score, Strand, Sequence\n");
		   for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature f = (Feature) iterator.next();
			bw.write("\n> " + f.getId() + ", " + f.getName() + ", " +  f.getParent() + ", " + f.getType() + ", " +  f.getNote() + 
					", " +  f.getStartPosition() + ", " + f.getEndPosition() + ", " + f.getScore() + ", " + ", " + 
					f.getStrand() + ", \n" + f.getSequence() + "\n");
			
		   }
		   
		   bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* This method is used to write human genome sequences to a file,
	 * to avoid re-processing in the next runs
	 */
	public static void writeRegRegionsToFile(ArrayList<Feature> features, File file){
		try {
			
			   BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			   
			   for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
					Feature f = (Feature) iterator.next();

					bw.write(f.getId() + "\n");
					bw.write(f.getType() + "\n");
					bw.write(f.getNote() + "\n");
					bw.write(f.getStartPosition() + "\n");
					bw.write(f.getEndPosition() + "\n");
					bw.write(f.getSequence() + "\n");
					bw.write(f.getScore() + "\n");
					bw.write("\n");
					
				   }
			   
			   bw.close();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Goes in pair with writeRegRegionsToFile(). 
	 * Note: no extra validation is done. The file is expected to be in the
	 * 		 format that writeRegRegionsToFile() creates
	 */
	public static ArrayList<Feature> readRegRegionsFromFile(File file) throws DataFormatException{
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = br.readLine()) != null){
				Feature regRegion = new Feature();
				
				regRegion.setId(line);
				
				line = br.readLine();
				regRegion.setType(line);
				
				line = br.readLine();
				regRegion.setNote(line);
				
				line = br.readLine();
				regRegion.setStartPosition(Integer.parseInt(line));

				line = br.readLine();
				regRegion.setEndPosition(Integer.parseInt(line));
				
				line = br.readLine();
				regRegion.setSequence(line);
				
				line = br.readLine();
				regRegion.setScore(Double.parseDouble(line));
				
				line = br.readLine(); //for the separator
				
				regRegions.add(regRegion);
			}
		
			br.close();
		}catch (NumberFormatException e){
			throw new DataFormatException("Can not parse data in " + file.getAbsolutePath() + " file.");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return regRegions;
	}
	
	/* Compares an array list of regulatory regions and returns true if the ids are equal
	 * 
	 */
	public static boolean regRegionsDistinct(ArrayList<Feature> regRegions1, ArrayList<Feature> regRegions2){
		if (regRegions1.size() != regRegions2.size()) return true;
		
		Collections.sort(regRegions1);
		Collections.sort(regRegions2);
		
		for (int i = 0; i < regRegions1.size(); i++){
			if (!regRegions1.get(i).getId().equals(regRegions2.get(i).getId()))
				return true;
		}
		
		return false;
	}
	
	/* Checks if all the names (ids) are unique in each ArayList and across the two ArrayList
	 * 
	 * @return 	true if the names in both regulatory regions are unique
	 * 			false if at least two names are the same
	 */
	public static boolean areNamesDistinct(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions){
		ArrayList<String> allIds = new ArrayList<String>();
		
		for (Iterator<Feature> iterator = posRegRegions.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			if (allIds.contains(feature.getId())){
				return false;
			} else {
				allIds.add(feature.getId());
			}
		}

		for (Iterator<Feature> iterator = negRegRegions.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			if (allIds.contains(feature.getId())){
				return false;
			} else {
				allIds.add(feature.getId());
			}
		}
		
		return true;
	}
	
	
	
	/* Should be user with regulatory elements. Find the number of sequences (genes) that have
	 * all the requested tfbs elements.
	 * 
	 * @param tfbsNames	array of regulatory elements' names (tfbs)
	 * @param regEls	regulatory elements with matches to sequences (genes)
	 * @return			number of sequences that contain all the tfbs, specified in tfbsNames
	 */
	public static int numSequencesContainingAll(String[] tfbsNames, ArrayList<Feature> regEls){
		int numSeqContaining = 0;
		
		Collections.sort(regEls); // sorts by gene name first, by tfbs name second,...
		List<String> tfbsNamesCol = Arrays.asList(tfbsNames);
		
		String currSeqName = regEls.get(0).getParent();
		ArrayList<String> currTfbsNames = new ArrayList<String>();
		
		
		for (Iterator<Feature> iterator = regEls.iterator(); iterator.hasNext();) {
			Feature regEl = (Feature) iterator.next();
			
			if (!currSeqName.equals(regEl.getParent())){
				if (currTfbsNames.containsAll(tfbsNamesCol)){
					numSeqContaining++;
				}
				currTfbsNames = new ArrayList<String>();
				currSeqName = regEl.getParent();
			}
			
			currTfbsNames.add(regEl.getName());
		}
		// case of last entry
		if (currTfbsNames.containsAll(tfbsNamesCol)){
			numSeqContaining++;
		}
		
		return numSeqContaining;
	}

	/* Generates random regulatory regions based on the composition of the provided regulatory regions.
	 * 
	 * @param modelRegRegions	set of regulatory regions, based on which the new reg regions will be simulated
	 * @param multiplicationFactor	how many random sequences per real sequence will be produced 
	 * 		  (i.e. 2 will return twice as many fake genes as the real genes)
	 * @param regionNamePrefix	prefix of the simulated reg regions names (counter will be 
	 * 			appended for each individual name)
	 * @return	set of simulated regulatory regions, where each region is modelled on one of the model sequences,
	 * 			i.e. with the same composition and length 
	 */
	public static ArrayList<Feature> generateSimulatedMC0RegulatoryRegions(ArrayList<Feature> modelRegRegions, 
									int multiplicationFactor, String regionNamePrefix) throws DataFormatException{
		
		if (multiplicationFactor <= 0) return null;
		
		ArrayList<Feature> simulatedRegRegions = new ArrayList<Feature>();
		
		for (Iterator<Feature> iterator = modelRegRegions.iterator(); iterator.hasNext();) {
			Feature realRegRegion = (Feature) iterator.next();
			
			for (int i = 0; i < multiplicationFactor; i++) {
				Feature simulRegRegion = new Feature(Feature.TYPE_REGULATORY_REGION);
				//simulRegRegion.setId("Fake" + i + "_" + realRegRegion.getId());
				simulRegRegion.setId(regionNamePrefix + realRegRegion.getId() + "_" + i  );
				simulRegRegion.setNote(realRegRegion.getNote()); //same chromosome
				simulRegRegion.setSequence(DataModeller.getRandomSequenceMC0(realRegRegion.getSequence().length(), 
						realRegRegion.getSequence()));
				simulatedRegRegions.add(simulRegRegion);
			}
		}
		
		return simulatedRegRegions;
	}
	
	/* Generates random regulatory regions using Markov chain of order 1 (currentnt depends on the previous).
	 * 
	 * @param modelRegRegions	set of regulatory regions, based on which the new reg regions will be simulated
	 * @param multiplicationFactor	how many random sequences per real sequence will be produced 
	 * 		  (i.e. 2 will return twice as many fake genes as the real genes)
	 * @param regionNamePrefix	prefix of the simulated reg regions names (counter will be 
	 * 			appended for each individual name)
	 * @return	set of simulated regulatory regions, where each region is modelled on one of the model sequences,
	 * 			i.e. with the same composition and length 
	 */
	public static ArrayList<Feature> generateSimulatedMC1RegulatoryRegions(ArrayList<Feature> modelRegRegions, 
									int multiplicationFactor, String regionNamePrefix) throws DataFormatException{
		
		if (multiplicationFactor <= 0) return null;
		
		ArrayList<Feature> simulatedRegRegions = new ArrayList<Feature>();
		
		for (Iterator<Feature> iterator = modelRegRegions.iterator(); iterator.hasNext();) {
			Feature realRegRegion = (Feature) iterator.next();
			
			for (int i = 0; i < multiplicationFactor; i++) {
				Feature simulRegRegion = new Feature(Feature.TYPE_REGULATORY_REGION);
				//simulRegRegion.setId("Fake" + i + "_" + realRegRegion.getId());
				simulRegRegion.setId(regionNamePrefix + realRegRegion.getId() + "_" + i  );
				simulRegRegion.setNote(realRegRegion.getNote()); //same chromosome
				simulRegRegion.setSequence(DataModeller.getRandomSequenceMC1(realRegRegion.getSequence().length(), 
						realRegRegion.getSequence()));
				simulatedRegRegions.add(simulRegRegion);
			}
		}
		
		return simulatedRegRegions;
	}


	/* Generates a set of regulatory regions with simulated sequences
	 * 
	 * @param numOfRegions	number of regions to be generated
	 * @param seqLength		length of the regulatory regions sequences to be generated
	 * @param transitMM0	transition matrix for Markov Chain Order 0, i.e. probabilities
	 * 						of {A,C,G,T} respectively
	 * @param regionNamePrefix	prefix of the simulated reg regions names (counter will be 
	 * 							appended for each individual name)
	 * @return	set of regulatory regions with id, type and generated sequence
	 */
	public static ArrayList<Feature> generateSimulatedRegulatoryRegions(int numOfRegions, int seqLength,
			String regionNamePrefix, double[] transitMM0){
		
		ArrayList<Feature> resultingRegRegions = new ArrayList<Feature>();
		
		for (int i = 0; i < numOfRegions; i++){
			Feature simulRegRegion = new Feature(Feature.TYPE_REGULATORY_REGION);
			simulRegRegion.setId(regionNamePrefix + i);
			simulRegRegion.setSequence(DataModeller.getRandomSequenceMC0(seqLength, transitMM0));
			
			resultingRegRegions.add(simulRegRegion);
		}
		
		return resultingRegRegions;
	}
	
	
	/* Generates a set of regulatory regions with simulated sequences with planted PSSMs
	 * 
	 * PSSMs are planted according to the following rule. PSSMs are located in the model regions
	 * and a simple statistics is collected: P =  (number of matches in all regions) / (number of regions)
	 * per each PSSM name. For each newly generated sequence, we plant each PSSM according to the collected 
	 * number P in this way: if P < 1, plant this PSSM with the probability P; if P > 1 (possible when there
	 * are multiple matches per sequence), PSSM is planted as many times as the whole number portion plus 
	 * one more time with the probability of the remainder part.
	 * 
	 * Position where to plan PSSM is generated randomly and no checking for double-planting at the same
	 * position is made.
	 * 
	 * 
	 */
	public static ArrayList<Feature> generateSimulatedRegulatoryRegionsWithPSSMs(int negExMultiplicationFactor, ArrayList<Feature> modelRegRegions,  
			RegulatoryElementService regElService) throws DataFormatException{
		
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		double[] modelComposition = FeaturesTools.getNucleotideComposition(modelRegRegions);
		double modelATcomposition = modelComposition[0] + modelComposition[3];
		double modelCGcomposition = modelComposition[1] + modelComposition[2];
		double[] modelSwappedComposition = new double[]{modelComposition[1], modelComposition[0],
				modelComposition[3], modelComposition[2]};
		double[] equalCompositionMatrix = new double[]{0.25, 0.25, 0.25, 0.25};

		
		regElService.getRegulatoryElements(modelRegRegions, SystemVariables.getInstance().getPositivePatserCutOffScore(),
				modelATcomposition, modelCGcomposition);
		
		Hashtable<String, Double> pssmMatchingStatistics = regElService.getPssmMatchingStatistics();
		Hashtable<String, int[][]> pssmMatrixes = regElService.getPssms();
		
		
		// generate sequences:
		// generate a huge sequence of lenght seqLen*numOfSequences; plant PSSMs in it and then divide into individual sequences
		// this way the overall PSSM match will be the same, while keeping true to the PSSM match distribultion
		int symSeqLen = modelRegRegions.get(DataModeller.generateRandomInt(0, modelRegRegions.size()-1)).getSequence().length();
		int numModelSeqs = modelRegRegions.size();
		int bigSeqLen = negExMultiplicationFactor * numModelSeqs * symSeqLen;
		
		char[] bigSeq = DataModeller.getRandomSequenceArrayMC0(bigSeqLen, equalCompositionMatrix);
		//String bigSeq = DataModeller.getRandomSequenceMC0(bigSeqLen, modelComposition);
		//String bigSeq = DataModeller.getRandomSequenceMC0(bigSeqLen, modelSwappedComposition);
		
		//plant PSSMs
		int plantCtr = 0;
		Enumeration<String> pssmNamesEnum = pssmMatchingStatistics.keys();
		while(pssmNamesEnum.hasMoreElements()){
			String pssmName = pssmNamesEnum.nextElement();
			double pssmStats = pssmMatchingStatistics.get(pssmName) * numModelSeqs * negExMultiplicationFactor;
			//TODO: trying to half the probability
			pssmStats = pssmStats / 2;
			//pssmStats = pssmStats;
			
			int numberOfPlantings = (int) pssmStats;
			
			for (int j = 0; j < numberOfPlantings; j++){
				int plantPos = DataModeller.generateRandomInt(1, bigSeqLen - 3); // -3 to give it a chance to plant at least 3 nt
											// we don't care if it plants completely or plants one on top of another
				
				bigSeq = DataModeller.plantPSSM(bigSeq, plantPos, pssmMatrixes.get(pssmName));
				plantCtr++;
			}
		}

	
		/*
		//V1 Split big simulated sequence into individual
		for (int i = 0; i < bigSeq.length; i = i + symSeqLen){
			String symSeq = new String(bigSeq, i, symSeqLen);
		
			// randomly pick model regulatory region to get its sequence length and chromosome info
			Feature randomModelRegion = modelRegRegions.get(DataModeller.generateRandomInt(0, modelRegRegions.size()-1));
			
			// add newly modeled regulatory region	
			regRegions.add(new Feature(SystemVariables.SYNTHETIC_REG_REGION_PREFIX + i,
					Feature.TYPE_REGULATORY_REGION, randomModelRegion.getNote(), 0, 0, symSeq, 0));

		}
	*/
		
		for (int i = 0; i < negExMultiplicationFactor * numModelSeqs; i++) {
			int currSeqLen = symSeqLen;
			if (bigSeqLen < (i+1)*symSeqLen){
				currSeqLen = bigSeqLen - i*symSeqLen;
			}
			String symSeq = new String(bigSeq, i*symSeqLen, currSeqLen);

			// randomly pick model regulatory region to get its sequence length and chromosome info
			Feature randomModelRegion = modelRegRegions.get(DataModeller.generateRandomInt(0, modelRegRegions.size()-1));
			
			// add newly modeled regulatory region	
			regRegions.add(new Feature(SystemVariables.SYNTHETIC_REG_REGION_PREFIX + i,
					Feature.TYPE_REGULATORY_REGION, randomModelRegion.getNote(), 0, 0, symSeq, 0));
		
		}
		
		return regRegions;
	}
	

	/* Generates a set of regulatory regions with simulated sequences with planted PSSMs
	 * 
	 * PSSMs are planted according to the following rule. PSSMs are located in the model regions
	 * and a simple statistics is collected: P =  (number of matches in all regions) / (number of regions)
	 * per each PSSM name. For each newly generated sequence, we plant each PSSM according to the collected 
	 * number P in this way: if P < 1, plant this PSSM with the probability P; if P > 1 (possible when there
	 * are multiple matches per sequence), PSSM is planted as many times as the whole number portion plus 
	 * one more time with the probability of the remainder part.
	 * 
	 * Position where to plan PSSM is generated randomly and no checking for double-planting at the same
	 * position is made.
	 * 
	 * TODO: removes
	 */
	public static ArrayList<Feature> OLD_generateSimulatedRegulatoryRegionsWithPSSMs(int numOfNegExamples, ArrayList<Feature> modelRegRegions,  
			RegulatoryElementService regElService) throws DataFormatException{
		
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		double[] transitionMatrix = FeaturesTools.getNucleotideComposition(modelRegRegions);
		double modelATcomposition = transitionMatrix[0] + transitionMatrix[3];
		double modelCGcomposition = transitionMatrix[1] + transitionMatrix[2];
		double[] swappedTransMatrix = new double[]{transitionMatrix[1], transitionMatrix[0],
				transitionMatrix[3], transitionMatrix[2]};
		
		regElService.getRegulatoryElements(modelRegRegions, SystemVariables.getInstance().getPositivePatserCutOffScore(),
				modelATcomposition, modelCGcomposition);
		
		Hashtable<String, Double> pssmMatchingStatistics = regElService.getPssmMatchingStatistics();
		Hashtable<String, int[][]> pssmMatrixes = regElService.getPssms();
		
		// generate sequences
		int totalNumPlanted = 0;
		for (int i = 0; i < numOfNegExamples; i++) {
			// randomly pick model regulatory region to get its sequence length and chromosome info
			Feature randomModelRegion = modelRegRegions.get(DataModeller.generateRandomInt(0, modelRegRegions.size()-1));
			int symSeqLength = randomModelRegion.getSequence().length();
			double[] bgCompositionMatrix = new double[]{0.25, 0.25, 0.25, 0.25};
			String symSequence = DataModeller.getRandomSequenceMC0(symSeqLength, bgCompositionMatrix);
			//String symSequence = DataModeller.getRandomSequenceMC0(symSeqLength, transitionMatrix);
			//String symSequence = DataModeller.getRandomSequenceMC0(symSeqLength, swappedTransMatrix);
			
			//plant PSSMs
			int plantCtr = 0;
			Enumeration<String> pssmNamesEnum = pssmMatchingStatistics.keys();
			while(pssmNamesEnum.hasMoreElements()){
				String pssmName = pssmNamesEnum.nextElement();
				double pssmStats = pssmMatchingStatistics.get(pssmName);
				//TODO: trying to half the probability
				pssmStats = pssmStats / 2;
				//pssmStats = pssmStats;
				
				int numberOfPlantings = (int) pssmStats;
				double statsFractPart = pssmStats - numberOfPlantings;
				
				if (DataModeller.generateRandomEvent(statsFractPart)){
					numberOfPlantings++;
				}
				
				for (int j = 0; j < numberOfPlantings; j++){
					int plantPos = DataModeller.generateRandomInt(1, symSeqLength - 3); // -3 to give it a chance to plant at least 3 nt
					// we don't care if it plants completely or plants one on top of another
					
					symSequence = DataModeller.plantPSSM(symSequence, plantPos, pssmMatrixes.get(pssmName));
					plantCtr++;
				}
			}
			
			// got all the info - add newly modelled regulatory region	
			regRegions.add(new Feature(SystemVariables.SYNTHETIC_REG_REGION_PREFIX + i,
					Feature.TYPE_REGULATORY_REGION, randomModelRegion.getNote(), 0, 0, symSequence, 0));
			
			// TODO print
			System.out.println("=== Number of PWMs planted in this sequence: "	+ plantCtr);
			totalNumPlanted = totalNumPlanted + plantCtr;
			
		}
		
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Total number of PSSMs planted in negative sequences: " + totalNumPlanted);
		
		
		return regRegions;
	}

	/* Generates a set of regulatory regions with simulated sequences with positionally planted PSSMs
	 * I.e. The window is moved along the set of model sequences, PSSMs are found and planted in new
	 * sequences in the same window with the probability of the model sequences
	 *
	 * @param numOfRegions	number of regions to be generated
	 * @param seqLength		length of the regulatory regions sequences to be generated
	 * @param baseSeqTransitMM0	transition matrix for Markov Chain Order 0, i.e. probabilities
	 * 							of {A,C,G,T} respectively of the base sequence, into which PWMs will be implanted
	 * @param regionNamePrefix	prefix of the simulated reg regions names (counter will be 
	 * 							appended for each individual name)
	 * @param modelRegRegions	regulatory regions, based on which the locational probabilities of the 
	 * 							PSSMs to implant will be calculated
	 * @param modelRegElementService	regulatory element service, which is used to extract PWMs for probability 
	 * 									calculations
	 */
	public static ArrayList<Feature> generateSimulatedRegulatoryRegionsWithPositionalPSSMs(int numOfRegions, int seqLength, 
			double[] baseSeqTransitMM0, double patserCutOffScore, String regionNamePrefix, ArrayList<Feature> modelRegRegions, 
			RegulatoryElementService modelRegElementService) throws DataFormatException{	
		
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		
		if (numOfRegions <= 0){
			return regRegions;
		}
		
		// Generate probabilities based on C.elegans data
		//RegulatoryRegionService regRegionService = 
		//	RegulatoryRegionServiceFactory.getService(RegulatoryRegionService.C_ELEGANS_DATA_SERVICE, 0, 0);
		
		//ArrayList<Feature> modelRegRegions = modelRegRegionService.getPositiveRegulatoryRegions();
		if (modelRegRegions == null){
			throw new DataFormatException("Unable to retrieve model regulatory regions to generate positive sequences.");
		}
		
//		RegulatoryElementService regElService = RegulatoryElementServiceFactory.getService(
//					RegulatoryElementService.PATSER_SERVICE, 
//					SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
		double[] modelNtComposition = getNucleotideComposition(modelRegRegions);
		double modelATcomposition =  modelNtComposition[0] + modelNtComposition[3];
		double modelCGcompostion = modelNtComposition[1] + modelNtComposition[2];
		ArrayList<Feature> regElements = modelRegElementService.getRegulatoryElements(modelRegRegions, patserCutOffScore, modelATcomposition, modelCGcompostion);
		
		ArrayList<RegulatoryElementPWM> regElementPSSMs = modelRegElementService.getRegulatoryElementsPWMs();
		
		StatAnalyser stat = new StatAnalyser(regElements);
		
		final int lengthOfSection = Integer.parseInt(
				SystemVariables.getInstance().getString("DataModeller.occurencePerLocation.section.length"));
		
		//Find the longest sequence in the set
		int longestSeqLen = 0;
		for (int i=0; i< modelRegRegions.size(); i++) {
			if (modelRegRegions.get(i).getSequence().length() > longestSeqLen){
				longestSeqLen = modelRegRegions.get(i).getSequence().length();
			}
		}
		
		OccurrencePerLocationMatrix probMtxObj = stat.getLocationalProbabilities(lengthOfSection, 
				longestSeqLen, modelRegRegions.size());
		
		for (int i = 0; i < numOfRegions; i++){
			String sequence = DataModeller.generateSimulatedSequenceWithPositionalPSSMs(probMtxObj, regElementPSSMs, seqLength, baseSeqTransitMM0);
			regRegions.add(new Feature(regionNamePrefix + i, Feature.TYPE_REGULATORY_REGION, null, 0, 0, sequence, 0.0));	
		}
		
		return regRegions;
	}

	
	
}

class SeqDist implements Comparable<SeqDist>{
	private String regRegionName;
	private int distance;
	
	public SeqDist(String seqName, int dist){
		this.regRegionName = seqName;
		this.distance = dist;
	}

	public String getRegRegionName() {
		return regRegionName;
	}
	public void setRegRegionName(String regRegionName) {
		this.regRegionName = regRegionName;
	}

	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}


	@Override
	public int compareTo(SeqDist o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    
	    //first sort by distance
	    if (this.distance < o.distance) return BEFORE;
	    if (this.distance > o.distance) return AFTER;
	    
	    // then sort by reg region
	    if (this.regRegionName != null){ 
	    	if (this.regRegionName.compareTo(o.regRegionName) < 0) return BEFORE;
	    	if (this.regRegionName.compareTo(o.regRegionName) > 0) return AFTER;
	    }
		return EQUAL;
	}
	
}