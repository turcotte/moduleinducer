package ca.uottawa.okorol.bioinf.ModuleInducer.tools;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.OccurrencePerLocationMatrix;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;


public class DataModeller {

	private double[] transitionMM0;   //transition matrix for Markov Model order 0
	private double[][] transitionMM1; //transition matrix for Markov Model order 1
	private final static int alphabetLength = 4; //a,c,g,t
	
	public void setTransitionMM0(double[] tmm0){
		
	}
	
	
	public DataModeller(String modelData){
		int[] modelArr;
		try {
			modelArr = convertDnaString(modelData);
		
			int dataLength = modelArr.length;
			
			transitionMM0 = new double[alphabetLength];
			transitionMM1 = new double[alphabetLength][alphabetLength];
			
			for (int i = 0; i < dataLength; i++) {
				//int currPos = modelArr[i];
				transitionMM0[modelArr[i]] = transitionMM0[modelArr[i]] + (double)1/dataLength;
				if (i>0){
					transitionMM1[modelArr[i-1]][modelArr[i]] = transitionMM1[modelArr[i-1]][modelArr[i]] + (double)1/dataLength;
				}
			}
		
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
	}
	
	/* Determines A C G T composition of the sequence
	 * @param sequence - DNA sequence, consisting of a,c,g,t letters (both small and capital).
	 * 					 The sequence can contain other characters, but only a,c,g,t will be counted
	 * return array of frequencies in the following order: [0] - a
	 * 													   [1] - c
	 * 													   [2] - g
	 * 													   [3] - t
	 * 
	 */
	public static double[] getNucleotideComposition(String sequence){
		//count individual nucleotides
		double[] ntFrequencies = getNucleotideNumber(sequence);
		
		// normalize the counts
		for (int i = 0; i < ntFrequencies.length; i++) {
			ntFrequencies[i] = (double)ntFrequencies[i] / sequence.length();
		}
		
		return ntFrequencies;
	}
	
	/* Given a nucleotide string, count the number of As, Cs, ... and place it into an array of length 4
	 * return double[] - for convenience and later reuse; the counts themselves are integers
	 */
	public static double[] getNucleotideNumber(String sequence){
		double[] ntNumberArray = new double[4]; //[a][c][g][t]
		
		sequence = sequence.toLowerCase();
		
		// count nucleotides
		for (int i = 0; i < sequence.length(); i++) {
			switch (sequence.charAt(i)) {
			case 'a':
				ntNumberArray[0]++;
				break;
			case 'c':
				ntNumberArray[1]++;
				break;
			case 'g':
				ntNumberArray[2]++;
				break;
			case 't':
				ntNumberArray[3]++;
				break;
			default:
				break;
			}
		}
		
		return ntNumberArray;
	}
	
	/* Probabilistic approach. Randomly models a sequence based on the likelihood
	 * specified in the position specific score matrix (PSSM).
	 * PSSM is NOT a matrix of probabilities or PWM. Each PSSM column adds up to the same number, 
	 * but the numbers are not expected to be the same from matrix to matrix.
	 */
	public static char[] getPssmSequenceArray(int[][] pssm) throws DataFormatException{
		if (pssm[0].length != 4){
			throw new DataFormatException("Supplied matrix is not in PSSM format.");
		}
		char[] seqArray = new char[pssm.length];
		char[] alphabet = {'A', 'C', 'G', 'T'};
		
		int columnSum = 0;
		for (int j = 0; j < pssm[0].length; j++){
			columnSum = columnSum + pssm[0][j];
		}
		
		Random rand = new Random();
		int randNum = rand.nextInt(columnSum) + 1;
		
		int currColSum = 0;
		for (int i = 0; i < pssm.length; i++){
			int letterCode = -1;
			for (int j = 0; j < pssm[0].length; j++){
				currColSum = currColSum + pssm[i][j];
				if (randNum <= currColSum){
					letterCode = j;
					break;
				}
			}
			
			if (letterCode < 0){
				throw new DataFormatException("Can not get PSSM sequence. Invalid PSSM format.");
			}
			seqArray[i] = alphabet[letterCode];
			currColSum = 0;
		}
		
		
		return seqArray;
	}
	
	public static String getPssmSequence(int[][] pssm) throws DataFormatException{
		return new String(getPssmSequenceArray(pssm));
	}
	
	/* Non-probabilistic approach. Returns a most likely sequence,
	 * i.e. the letter with the highest value in the PSSM is added
	 * to the sequence. 
	 */
	public static String getPssmSequenceMax(int[][] pssm){
		String sequence = "";
		String[] alphabet = {"A", "C", "G", "T"};
		
		for (int i = 0; i < pssm.length; i++){
			int letterCode = -1;
			int maxValue = -1;
			for (int j = 0; j < pssm[0].length; j++){
				if (maxValue < pssm[i][j]){
					maxValue = pssm[i][j];
					letterCode = j;
				}
			}
			sequence = sequence.concat(alphabet[letterCode]);
		}
		
		
		return sequence;
	}
	
	/* Generates a random int in a range
	 * @param int from inclusive
	 * @param int to inclusive
	 */
	public static int generateRandomInt(int from, int to) throws DataFormatException{
		if (from > to){
			throw new DataFormatException("Generating random integer in the interval: First value should be less than second.");
		}
		Random rand = new Random();
		int randNum = rand.nextInt(to - from + 1);
		return randNum + from;
	}
	
	/* Randomly generates an event. 
	 * Returns true/false if the event has happened based on the probability
	 * @param double probability of the event to occur. Range is from 0.0 to 1.0
	 */
	public static boolean generateRandomEvent(double probability) throws DataFormatException{
		if (Double.compare(probability, 0) < 0 || Double.compare(probability, 1.0) > 0){
			throw new DataFormatException("Probability should be from 0.0 to 1.0");
		}
		Random rand = new Random();
		double randNum = rand.nextDouble();
		if (Double.compare(randNum, probability) <= 0){
			return true;
		}
		return false;
	}
	
	/* Calculates the number of times regular expression regex is found
	 * in the provided sequence.
	 */
	public static int getNumberOfMatches(String regex, String sequence){
		int numberOfMatches = 0;
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(sequence);
		
		while (matcher.find()){
			numberOfMatches++;
		}
		
		return numberOfMatches;
	}
	
	static String generateSimulatedSequenceWithPositionalPSSMs(OccurrencePerLocationMatrix probMtxObj, 
			ArrayList<RegulatoryElementPWM> regElPWMs, 
			int sequenceLength, double[] baseSeqTransitMM0) throws DataFormatException {
		
		String sequence = "";
		//final int lengthOfSection = 50;
		Random rand = new Random();
		int plantCtr = 0;

		// Generate probabilities based on model data

//		OccurrencePerLocationMatrix probMtxObj = stat.getLocationalProbabilities(lengthOfSection, sequenceLength,
//						numberOfSeq);
		double[][] pm = probMtxObj.getProbabilityMatrix();

		// int currPos = 0; //position till which the sequence has been built
		for (int i = 0; i < pm.length; i++) {

			ArrayList<String> regElementNames = new ArrayList<String>();
			ArrayList<Double> probabilities = new ArrayList<Double>();
			for (int j = 0; j < pm[i].length; j++) {
				if (Double.compare(pm[i][j], 0.0) > 0) {
					probabilities.add(pm[i][j]);
					regElementNames.add(probMtxObj.getNamesOfTfbs().get(j));
				}
			}

			while (!probabilities.isEmpty()) {
				if (sequence.length() >= (i + 1) * probMtxObj.getSectionLength()) {
					break;
				}

				int randomProbabilPosition = rand.nextInt(probabilities.size());
				double currProb = probabilities.get(randomProbabilPosition)/2; //TODO probability over 2 tuning 

				if (DataModeller.generateRandomEvent(currProb)) {  
					int[][] pwm = getPssmByName(regElementNames.get(randomProbabilPosition), regElPWMs);
					String regElementSeq = DataModeller.getPssmSequence(pwm);
					int randomPwmPosition = rand.nextInt((i + 1)
							* probMtxObj.getSectionLength() - sequence.length());
					String randSeq = "";
					if (randomPwmPosition > 0) {
						randSeq = DataModeller.getRandomSequenceMC0(randomPwmPosition, baseSeqTransitMM0);
					}
					sequence = sequence.concat(randSeq);
					sequence = sequence.concat(regElementSeq);
					plantCtr++;
				}

				regElementNames.remove(randomProbabilPosition);
				probabilities.remove(randomProbabilPosition);
			}

			// fill what's left in a section with a random sequence
			if (sequence.length() < (i + 1) * probMtxObj.getSectionLength()) {
				int len = (i + 1) * probMtxObj.getSectionLength() - sequence.length();
				String seq = DataModeller.getRandomSequenceMC0(len, baseSeqTransitMM0);
				sequence = sequence.concat(seq);
			}

		}
		sequence = sequence.substring(0, sequenceLength);

		// TODO print
		//System.out.println("=== Number of PWMs planted in this sequence: "	+ plantCtr);

		return sequence;
	}
	
	
	private static int[][] getPssmByName(String tfbsName, ArrayList<RegulatoryElementPWM> pwms){
		for (Iterator<RegulatoryElementPWM> iterator = pwms.iterator(); iterator.hasNext();) {
			RegulatoryElementPWM tfbsPSSM = (RegulatoryElementPWM) iterator.next();
			if (tfbsPSSM.getName().equals(tfbsName)){
				return tfbsPSSM.getPwm();
			}
		}
		return null;
	}


	
	/* Plants a PSSM (i.e. its corresponding sequence) into the supplied sequence.
	 * If the planting position is so cllose to the end of the sequence that PSSM
	 * doesn't fit entirely, the PSSM sequence will be trunkated to fit (i.e it's 
	 * caller's responsibility to generate a proper planting position to fit whole
	 * PSSM)
	 * 
	 * @param String sequence that will have a pwm sequence inserted
	 * @param int position starting from which the pwm sequence will be inserted;
	 * 				assume that sequence numbering starts at 1.
	 * @param int[][] pssm, sequence of which will be inserted
	 * return new sequence with planted PSSM
	 */
	public static String plantPSSM(String sequence, int startPosition, int[][] pssm) throws DataFormatException{
		
		return new String(plantPSSM(sequence.toCharArray(), startPosition, pssm));
	}
	
	public static char[] plantPSSM(char[] sequenceArray, int startPosition, int[][] pssm) throws DataFormatException{
		if (startPosition <= 0){
			throw new DataFormatException("Invalid PWM insert position. Position should be >= 1");
		}
		
		char[] pssmSeqArray = getPssmSequenceArray(pssm);

		
		int numOfCharsToPlant = pssmSeqArray.length;
		
		if (startPosition > sequenceArray.length - pssmSeqArray.length){
			numOfCharsToPlant = sequenceArray.length - startPosition + 1; 
		}
		
		
		for (int i = 0; i < numOfCharsToPlant; i++){
			sequenceArray[startPosition - 1 + i] = pssmSeqArray[i];
		}
		
		return sequenceArray;
	}
	
	
	
	/* Returns a random string of ACGT alphabet
	 *@param int length of the desired string
	 *@param double Pa probability of A
	 *@param double Pc probability of C
	 *@param double Pg probability of G
	 *@param double Pt probability of T
	 * 
	 */
	public static String getRandomSequence(int length, double Pa, double Pc, double Pg, double Pt){
		double[] transitMM0 = {Pa, Pc, Pg, Pt};
		return getRandomSequenceMC0(length, transitMM0);
	}
	
	/* Based on the model sequence, generates a random sequence. 
	 * One character probabilities are assumed (Markov Chain order 0)
	 */
	public static String getRandomSequenceMC0(int length, String modelSequence) throws DataFormatException{
		double[] transitMM0 = getMM0TransitionMatrix(modelSequence);
		return getRandomSequenceMC0(length, transitMM0);
	}
	
	/* Based on the model sequence, generates a random sequence. 
	 * Two character probabilities are assumed (Markov Chain order 1)
	 */
	public static String getRandomSequenceMC1(int length, String modelSequence) throws DataFormatException{
		double[][] transitMM1 = getMM1TransitionMatrix(modelSequence);
		return getRandomSequenceMC1(length, transitMM1);
	}
	
	
	/* Generates a random string of alphabet {A,C,G,T} based on Markov Chain model order 0
	 * 
	 * @param length	length of a sequence to be generated
	 * @param transitMM0	transition matrix for Markov Chain Order 0, i.e. probabilities
	 * 						of {A,C,G,T} respectively
	 */
	public static String getRandomSequenceMC0(int length, double[] transitMM0){
		return new String(getRandomSequenceArrayMC0(length, transitMM0));
	}
	
	public static char[] getRandomSequenceArrayMC0(int length, double[] transitMM0){
		Random rand = new Random();
		char[] alphabet = {'A', 'C', 'G', 'T'};
		char[] seqArr = new char[length];
		
		for (int i = 0; i < length; i++){
			double randNum = rand.nextDouble();
			int index = 0;
			double sum = 0.0;
			for (int j=0; j < transitMM0.length; j++){
				sum = sum + transitMM0[j];
				if (Double.compare(randNum, sum) <= 0){
					index = j; break;
				}
			}
			seqArr[i] = alphabet[index];
			
		}
		
		return seqArr;
	}
	
	/* Generates a random string of alphabet {A,C,G,T} based on Markov Chain model order 1
	 * 
	 * @param length	length of a sequence to be generated
	 * @param transitMM1	transition matrix for Markov Chain Order 1; directional probabilities
	 * 						go from j to i; i.e sum of the row is == 1.
	 */
	public static String getRandomSequenceMC1(int length, double[][] transitMM1) throws DataFormatException{
		Random rand = new Random();
		int[] seqArr = new int[length]; 
		
		//generate first nt with equal probability
		seqArr[0] = generateRandomInt(0, 3);
		
		for (int l = 1; l < length; l++){
			double randNum = rand.nextDouble();
			
			// find which row of the transition matrix to take probabilities from
			int trMtRow = seqArr[l-1];
			
			int index = 0;
			double sum = 0.0;
			for (int i=0; i < transitMM1.length; i++){
				sum = sum + transitMM1[i][trMtRow];
				if (Double.compare(randNum, sum) <= 0){
					index = i; break;
				}
			}
			seqArr[l] = index;
		}

		return convertDnaArray(seqArr);
	}
	
	
	/* Calculates a transition matrix of Markov Chain model order 0
	 * based on a supplied string
	 */
	public static double[] getMM0TransitionMatrix(String modelData) throws DataFormatException {
		if (modelData == null || modelData.isEmpty()){
			throw new DataFormatException("Transition matrix can not be built with empty model data.");
		}
		
		int[] modelArr = convertDnaString(modelData);
		
		int dataLength = modelArr.length;
		
		double[] tranMM0 = new double[alphabetLength];
		
		double likelihoodBit = (double)1/dataLength;
		for (int i = 0; i < dataLength; i++) {
			tranMM0[modelArr[i]] = tranMM0[modelArr[i]] + likelihoodBit;
		}
		
		return tranMM0;
	}
	
	/* Calculates a transition matrix of Markov Chain model order 0
	 * for all the sequences in the supplied set of regulatory regions
	 * @deprecated - updateMM0TransitionMatrix is not working
	 */
	public static double[] getMM0TransitionMatrix(ArrayList<Feature> regRegions) throws DataFormatException {
		//TODO: fix 
		double[] tranMM0 = new double[alphabetLength];
		for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			tranMM0 = updateMM0TransitionMatrix(feature.getSequence(), tranMM0);
		}
		return tranMM0;
	}
	
	/* Updates a given transition matrix for Markov Chain order 0 with information
	 * from a new string
	 * 
	 * @param modelData	sequence of {A,C,T,G} alphabet, nt frequences of which will be used to
	 * 					update given transition matrix
	 * @param tranMM0	transition matrix to be updated
	 * 
	 * @return	updates transition matrix
	 * @deprecated - not working as it should
	 */
	public static double[] updateMM0TransitionMatrix(String modelData, double[] tranMM0) throws DataFormatException {
		if (modelData == null || modelData.isEmpty()){
			throw new DataFormatException("Transition matrix can not be built with empty model data.");
		}
		
		int[] modelArr = convertDnaString(modelData);
		
		int dataLength = modelArr.length;
		
		if (tranMM0 == null){ //case for transit matrix for one string only
			tranMM0 = new double[alphabetLength];
		}
		
		double likelihoodBit = (double)1/dataLength;
		for (int i = 0; i < dataLength; i++) {
			tranMM0[modelArr[i]] = tranMM0[modelArr[i]] + likelihoodBit;
		}
		
		return tranMM0;
	}
	
	public static double[][] getMM1TransitionMatrix(String modelData) throws DataFormatException{
		
		if (modelData == null || modelData.isEmpty()){
			throw new DataFormatException("Transition matrix can not be built with empty model data.");
		}
		
		int[] modelArr = convertDnaString(modelData);
		
		int dataLength = modelArr.length;
		
		
		//count individual nucleotides
		double[] likelihoodBits = getNucleotideNumber(modelData);
		//convert to likelihood bits for transition from each nucleotide
		for (int i = 0; i < likelihoodBits.length; i++) {
			likelihoodBits[i] = 1.0 / likelihoodBits[i];
		}
		
		
		double[][] transitionMM1 = new double[alphabetLength][alphabetLength];
		
		for (int i = 0; i < dataLength; i++) {
			if (i < dataLength -1) {
				transitionMM1[modelArr[i+1]][modelArr[i]] = transitionMM1[modelArr[i+1]][modelArr[i]] + likelihoodBits[modelArr[i]];
			} else { //in the case of the last nt in the sequence, always add one likelihood bit to itself
				transitionMM1[modelArr[i]][modelArr[i]] = transitionMM1[modelArr[i]][modelArr[i]] + likelihoodBits[modelArr[i]];
			}
		}
		return transitionMM1;
	}
	
	
	
	/* Based on the transitionMM0 (Markov Chain order 0) generates a random sequence of length length
	 * 
	 */
	public String getRandomSequence0(int length){
		Random rand = new Random();
		String[] alphabet = {"A", "C", "G", "T"};
		String seq = "";
		
		for (int i = 0; i < length; i++){
			double randNum = rand.nextDouble();
			int index = 0;
			double sum = 0.0;
			for (int j=0; j<transitionMM0.length; j++){
				sum = sum + transitionMM0[j];
				if (randNum <= sum){
					index = j; break;
				}
			}
			seq = seq.concat(alphabet[index]);
		}
		
		return seq;
	}
	
	/* Based on the transitionMM1 (Markov Chain order 1) generates a random sequence of length length
	 * 
	 */
	public String getRandomSequence1(int length){
		return "";
	}

	
	/* Converts a string of characters {A, C, G, T} into an integer array of {0, 1, 2, 3},
	 * where A corresponds to 0, a.s.f.
	 * If any other characters are included in the string, return null.
	 */
	private static int[] convertDnaString(String str) throws DataFormatException{
		int[] digitalStr = new int[str.length()];
		
		str = str.toUpperCase();
		
		for (int i = 0; i < digitalStr.length; i++) {
			if (str.charAt(i) == 'A'){
				digitalStr[i] = 0;
			} else if (str.charAt(i) == 'C'){
				digitalStr[i] = 1;
			} else if (str.charAt(i) == 'G'){
				digitalStr[i] = 2;
			} else if (str.charAt(i) == 'T'){
				digitalStr[i] = 3;
			} else {
				throw new DataFormatException("Unknown character encountered in DNA sequence.");
			} 
		}
		
		return digitalStr;
	} 
	
	
	
	/* Converts an integer array of {0, 1, 2, 3} into a string of characters {A, C, G, T} 
	 * where A corresponds to 0, a.s.f.
	 * Unknown letter codes (not 0-3) are ignored and no letter is added
	 */
	private static String convertDnaArray(int[] dnaArray) throws DataFormatException{
		String sequence = "";
		
		for (int i = 0; i < dnaArray.length; i++) {
			if (dnaArray[i] == 0){
				sequence = sequence.concat("A");
			} else if (dnaArray[i] == 1){
				sequence = sequence.concat("C");
			} else if (dnaArray[i] == 2){
				sequence = sequence.concat("G");
			} else if (dnaArray[i] == 3){
				sequence = sequence.concat("T");
			}	
		}
		
		return sequence;
	} 
	
	
	
	/* Given a sequence and a pwm, returns the highest score for the match of the two.
	 * Note: the score is only good for sorting, since no statistical model was used in its calculation.
	 */
	public static double matchSequenceToPwm(String sequence, RegulatoryElementPWM pwmObj) throws DataFormatException{
		double matchScore = 0.0;
		sequence = sequence.toUpperCase();
		
		int[][] pwm = pwmObj.getPwm();
		
		int pwmTotal = pwm[0][0] + pwm[0][1] + pwm[0][2] + pwm[0][3];

		if (sequence.length() <= pwm.length){
			for (int i = 0; i <= pwm.length - sequence.length(); i++){
				double currMatchScore = 0.0;
				for (int k = 0; k < sequence.length(); k++){
					int higestPwmValue = getHighestNtScore(sequence.charAt(k), pwm[i+k]);
					currMatchScore = currMatchScore + (double)higestPwmValue / pwmTotal;
					//currMatchScore = currMatchScore + (double)pwm[i+k][seqArr[k]] / pwmTotal;
				}
				
				if (currMatchScore > matchScore){
					matchScore = currMatchScore;
				}
			}
		} else {
			for (int i = 0; i <= sequence.length() - pwm.length; i++){
				double currMatchScore = 0.0;
				for (int k = 0; k < pwm.length; k++){
					int higestPwmValue = getHighestNtScore(sequence.charAt(i+k), pwm[k]);
					currMatchScore = currMatchScore + (double)higestPwmValue / pwmTotal;
					//currMatchScore = currMatchScore + (double)pwm[k][seqArr[i+k]] / pwmTotal;
				}
				
				if (currMatchScore > matchScore){
					matchScore = currMatchScore;
				}
			}
		}
		
		matchScore = matchScore / sequence.length(); //normalize the score by the sequence length
		
		// complete ad hoc: bump up the score if pwm and sequence are close in length
		if (Math.abs(pwm.length - sequence.length()) < 3) matchScore = matchScore + 0.1;
		
		return matchScore;
	}
	
	/* Helper method for matchSequenceToPwm(String, RegulatoryElementPWM)
	 * Given an IUPAC letter for nucleotive and a vertical slice of a pwm,
	 * match the letter to an appropriate pwm index anr return the value in the pwm.
	 * If the letter stands for more than one nt (i.e. Y = T or C), then return the
	 * sum of the scores.
	 * 
	 * @param nt	nucleotide letter in capital
	 * @param pwmFrame	vertical slice of the pwm that needs to be matched to the nt
	 */
	private static int getHighestNtScore(char nt, int[] pwmFrame) throws DataFormatException {
		int score = 0;
									///		A = 0		C = 1		G = 2		T = 3
		switch (nt) {
		case 'A':
			score = pwmFrame[0];
			break;
		case 'C':
			score = pwmFrame[1];
			break;
		case 'G':
			score = pwmFrame[2];
			break;	
		case 'T':
			score = pwmFrame[3];
			break;
		case 'M':		// A or C
			score = pwmFrame[0] + pwmFrame[1];
			break;
		case 'R':		// A or G
			score = pwmFrame[0] + pwmFrame[2];
			break;
		case 'W':		// A or T
			score = pwmFrame[0]  + pwmFrame[3];
			break;
		case 'S':		// C or G
			score = pwmFrame[1] + pwmFrame[2];
			break;
		case 'Y':		// C or T
			score = pwmFrame[1] + pwmFrame[3];
			break;
		case 'K':		// G or T
			score = pwmFrame[2] + pwmFrame[3];
			break;
		case 'V':		// A, C or G
			score = pwmFrame[0] + pwmFrame[1] + pwmFrame[2];
			break;
		case 'H':		// A, C or T
			score = pwmFrame[0] + pwmFrame[1] + pwmFrame[3];
			break;
		case 'D':		// A, G or T
			score = pwmFrame[0] + pwmFrame[2] + pwmFrame[3];
			break;
		case 'B':		// C, G or T
			score = pwmFrame[1] + pwmFrame[2] + pwmFrame[3];
			break;
		case 'N':		// any base (A,C,G, or T)
			score = pwmFrame[0] + pwmFrame[1] + pwmFrame[2] + pwmFrame[3];
			break;	
		default:
			throw new DataFormatException("Could not match a nucleotide letter <" + nt + "> to a PWM.");
		}
		
		return score;
	}
	
	/* Estimates probability of the sequence based on Markov Chain Model of order 1
	 * ex. P(GTA) = P(G)P(T|G)P(A|T) = P(G)P(GT)P(TA)
	 */
	public double getProbability(String str) throws DataFormatException{
		double probability = 0.0;
		
		int[] strArray = convertDnaString(str);
		
		probability = transitionMM0[strArray[0]];
		
		for (int i = 1; i < strArray.length; i++) {
			probability = probability * transitionMM1[strArray[i-1]][strArray[i]];
		}
		
		return probability;
	}
	
	public void printTransitionMM0(){
		System.out.println("Transition array for MM0");
		for (int i = 0; i < transitionMM0.length; i++) {
			System.out.print("	"+transitionMM0[i]);
		}
		System.out.println();
	}
	
	public void printTransitionMM1(){
		System.out.println("Transition matrix MM1");
		for (int i = 0; i < transitionMM1.length; i++) {
			for (int j = 0; j < transitionMM1[0].length; j++) {
				System.out.print("	"+transitionMM1[i][j]);
			}
			System.out.println();
		}
	}


	

}
