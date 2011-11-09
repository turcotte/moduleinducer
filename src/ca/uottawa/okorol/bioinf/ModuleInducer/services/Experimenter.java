package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataFormatter;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataModeller;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;

public class Experimenter {
	
	private Explorer explorer;
	
	public Experimenter(Explorer explorer){
		this.explorer = explorer;
	}
	
	//Randomly divide dataset (pos+neg together) in half; repeat 5 times
	//No stratification.
	public void fiveByTwoTest() throws DataFormatException{
		double testSetPercent = 0.5;
		int numberOfRuns = 5;
		
		double recallSum = 0.0;
		double specificitySum = 0.0;
		double precisionSum = 0.0;
		double wRAccSum = 0.0;  //weighted relative accuracy as in lavrac1999
		double fMeasureSum = 0.0;
		
		//TODO: make switch to train/test
	
		for (int i = 0; i < numberOfRuns; i++) {
			String alephOutput = explorer.induceRulesWithTestSet(testSetPercent);
			
			// [TP, FP, FN, TN]
			int[] confMatr = DataFormatter.extractTestConfusionMatrix(alephOutput);
			int totalNum = confMatr[0] + confMatr[1] + confMatr[2] + confMatr[3]; //total number of examples
			//TODO deal with NaN
			double recall = (double)confMatr[0] / (confMatr[0] + confMatr[2]); //also sensitivity
			double specificity = (double)confMatr[3] / (confMatr[3] + confMatr[1]);
			double precision = (double)confMatr[0] / (confMatr[0] + confMatr[1]);
			double wRAcc = ((double)(confMatr[0]*confMatr[3] - confMatr[1]*confMatr[2]))/(totalNum*totalNum);
			double fMeasure = 2 * recall * precision / (recall + precision);
			
			System.out.println("For the current run of the 5 by 2 cross validation: ");
			System.out.println("\tConfusion array [TP, FP, FN, TN] = [" + confMatr[0] + ", " + confMatr[1] + ", " + confMatr[2] + ", " + confMatr[3] +"]");
			System.out.println("\tRecall = " + recall + "\t Specificity = " + specificity + "\tPrecision = " + precision + 
					"\tF-measure = " + fMeasure + "\tWeighted Relative Acc = " + wRAcc);
			
			recallSum = recallSum + recall;
			specificitySum = specificitySum + specificity;
			precisionSum = precisionSum + precision;
			fMeasureSum = fMeasureSum + fMeasure;
			wRAccSum = wRAccSum + wRAcc;
		}
		
		System.out.println("\n Summary of the 5 by 2 cross validation experiment:");
		System.out.println("\tMean recall = " + recallSum / numberOfRuns);
		System.out.println("\tMean specificity = " + specificitySum / numberOfRuns);
		System.out.println("\tMean precision = " + precisionSum / numberOfRuns);
		System.out.println("\tMean F-measure = " + fMeasureSum / numberOfRuns);
		System.out.println("\tMean Weighted Relative Acc = " + wRAccSum / numberOfRuns);
	}
	
	
	
	// 10-fold stratified cross-validation
	public void kFoldCrossValidation() throws DataFormatException{
		double testSetPercent = 0.1;
		int numberOfRuns = 10;
		
		double recallSum = 0.0;
		double specificitySum = 0.0;
		double precisionSum = 0.0;
		double fMeasureSum = 0.0;
		double wRAccSum = 0.0;  //weighted relative accuracy as in lavrac1999
	
		for (int i = 0; i < numberOfRuns; i++) {
			String alephOutput = explorer.induceRulesWithTestSet(testSetPercent, i);
			//System.out.println(alephOutput);

			// [TP, FP, FN, TN]
			int[] confMatr = DataFormatter.extractTestConfusionMatrix(alephOutput);
			int totalNum = confMatr[0] + confMatr[1] + confMatr[2] + confMatr[3]; //total number of examples
			
			if (totalNum == 0){ // this safeguard is needed, because examples can't usually be divided into equal testSetPercent sections,
				break;			// we divide the examples rounding to a larger number, therefore one or two last runs may be executed on the 
			}					// empty test set and we don't want this to skew our results.
			
			double recall = (double)confMatr[0] / (confMatr[0] + confMatr[2]);
			double specificity = (double)confMatr[3] / (confMatr[3] + confMatr[1]);
			double precision = (double)confMatr[0] / (confMatr[0] + confMatr[1]);
			double fMeasure = 2 * recall * precision / (recall + precision);
			double wRAcc = ((double)(confMatr[0]*confMatr[3] - confMatr[1]*confMatr[2]))/(totalNum*totalNum);
			
			System.out.println("For the current run of the k-fold cross validation: ");
			System.out.println("\tConfusion array [TP, FP, FN, TN] = [" + confMatr[0] + ", " + confMatr[1] + ", " + confMatr[2] + ", " + confMatr[3] +"]");
			System.out.println("\tRecall = " + recall + "\t Specificity = " + specificity + "\tPrecision = " + precision + 
					"\tF-measure = " + fMeasure + "\tWeighted Relative Acc = " + wRAcc);
			
			recallSum = recallSum + recall;
			specificitySum = specificitySum + specificity;
			precisionSum = precisionSum + precision;
			fMeasureSum = fMeasureSum + fMeasure;
			wRAccSum = wRAccSum + wRAcc;
		}
		
		System.out.println("\n Summary of the k-fold cross validation experiment:");
		System.out.println("\tMean recall = " + recallSum / numberOfRuns);
		System.out.println("\tMean specificity = " + specificitySum / numberOfRuns);
		System.out.println("\tMean precision = " + precisionSum / numberOfRuns);
		System.out.println("\tMean F-measure = " + fMeasureSum / numberOfRuns);
		System.out.println("\tMean Weighted Relative Acc = " + wRAccSum / numberOfRuns);
		
	}
	
	/* Testing on the positive examples
	 * Remove some percent of positive and induce
	 * Compare to the full set
	 * return double - mean similarity of the experiment
	 */
	@SuppressWarnings("unchecked")
	public double leaveOneOutTest() throws DataFormatException{
		double testSetPercent = 0.1;
		
		String benchmarkResult = explorer.induceRules();
		ArrayList<String[]> benchmarkTheory = DataFormatter.extractTheoryByRules(benchmarkResult);
		
		ArrayList<Feature> realSequences = explorer.getRegulatoryRegionService().getPositiveRegulatoryRegions();

		//get the number of runs and the size of the step
		int testSetNum = (int) (realSequences.size() * testSetPercent);
		int numOfRuns = realSequences.size() / testSetNum;
		//int numOfRuns = 10;
		
		double ruleSimilSum = 0.0;
		double termSimilSum = 0.0;
		
		for (int i = 0; i < numOfRuns; i++){
			ArrayList<Feature> testSet = (ArrayList<Feature>) realSequences.clone();
			
			for (int j = i * testSetNum ; j < testSetNum; j++){				
				testSet.remove(j);
				System.out.println("--- removed "+j);
			}
			System.out.println("---");
			
			explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(testSet);
			String intermediateResult = explorer.induceRules();
			ArrayList<String[]> intermediateTheory = DataFormatter.extractTheoryByRules(intermediateResult);
			System.out.println("*** Intermediate theory (multFact = "+i+") ***");
			printTheory(intermediateTheory);
			
			double curRuleSimilarity = Experimenter.compareTheoriesByRules(benchmarkTheory, intermediateTheory);
			ruleSimilSum = ruleSimilSum + curRuleSimilarity;
			System.out.println("Rule similarity for the current 'leave-one-out' test: " + curRuleSimilarity);
			
			double curTermSimilarity = Experimenter.compareTheoriesByTerms(benchmarkTheory, intermediateTheory);
			System.out.println("Term similarity for the current 'leave-one-out' test: " + curTermSimilarity);
			termSimilSum = termSimilSum + curTermSimilarity;
			
			explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(realSequences);
		}
		
		double meanRuleSimilarity = ruleSimilSum / numOfRuns;
		System.out.println("Mean rule similarity for the 'leave-one-out' experiment: " + meanRuleSimilarity);
		
		double meanTermSimilarity = termSimilSum / numOfRuns;
		System.out.println("Mean term similarity for the 'leave-one-out' experiment: " + meanTermSimilarity);
		
		return meanRuleSimilarity;
	}
	
	/* Hypothesis 2
	 * return double - mean similarity of the experiment
	 */
	public double increaseNegExamplesTest() throws DataFormatException{
		int startMultFactor = 11;
		int step = 2;
		
		explorer.getRegulatoryRegionService().updateNumberOfNegativeRegRegions(startMultFactor); 
		
		String benchmarkResult = explorer.induceRules();
		ArrayList<String[]> benchmarkTheory = DataFormatter.extractTheoryByRules(benchmarkResult);
		System.out.println("*** Benchmark theory (multFact = " + startMultFactor + ") ***");
		printTheory(benchmarkTheory);
		
		
		double similByRulesSum = 0.0;
		double similByTermsSum = 0.0;
		
		int numOfRuns = 0;
		for (int i = startMultFactor - step ; i > 0; i = i - step){
			numOfRuns++;
			explorer.getRegulatoryRegionService().updateNumberOfNegativeRegRegions(i);
			String intermediateResult = explorer.induceRules();
			ArrayList<String[]> intermediateTheory = DataFormatter.extractTheoryByRules(intermediateResult);
			System.out.println("*** Intermediate theory (multFact = "+i+") ***");
			printTheory(intermediateTheory);
			
			double curSimilarityByRules = Experimenter.compareTheoriesByRules(benchmarkTheory, intermediateTheory);
			System.out.println("Similarity by rules for the current 'increase negative examples' test: " + curSimilarityByRules);
			similByRulesSum = similByRulesSum + curSimilarityByRules;
			
			double curSimilarityByTerms = Experimenter.compareTheoriesByTerms(benchmarkTheory, intermediateTheory);
			System.out.println("Similarity by terms for the current 'increase negative examples' test: " + curSimilarityByTerms);
			similByTermsSum = similByTermsSum + curSimilarityByTerms;
		}
		
		double meanSimilarityByRules = similByRulesSum / numOfRuns;
		System.out.println("Mean similarity by rules for the 'increase negative examples' experiment: " + meanSimilarityByRules);
		
		double meanSimilarityByTerms = similByTermsSum / numOfRuns;
		System.out.println("Mean similarity by terms for the 'increase negative examples' experiment: " + meanSimilarityByTerms);
		
		return meanSimilarityByRules;
	}
	
	
	/* 
	 * In this test we plant PWMs specifically to test each ILP rule.
	 * NOTE: make sure that PatserService has a dir with 2 matrixes
	 * @param int the length of the generated sequences
	 */
//TODO: make static
	public void rule_before_InductionTest(int sequenceLength, int numOfPositiveSequences, int numOfNegativeSequences) throws DataFormatException{
		/* Positive example sequences are generated per each test and PWMs are planted in them
		 * 		according to the rule that is being tested.
		 * Negative example sequences stay the same throughout all tests
		 */
		
		Random rand = new Random();
		// nucleotide probabilities of the generated negative sequences and 
		//	base of the positive sequences (where PWMs will be planted)
		double[] sequenceProbabilities = new double[]{0.25, 0.25, 0.25, 0.25};
		
		ArrayList<Feature> negativeSequences = FeaturesTools.generateSimulatedRegulatoryRegions(
				numOfNegativeSequences, sequenceLength,	"negExSeq_", sequenceProbabilities);
		explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(negativeSequences);
		
		
		//***** Test "before" rule
		// two matrixes that have shown to be infrequent in equal composition
		int [][] first_pssm = new int[][] { //m2
				{0,53,0,1},
				{2,5,8,39},
				{1,3,1,49},
				{0,50,1,3},
				{1,4,10,39},
				{2,9,2,41},
				{4,42,2,6},
				{7,10,7,30},
				{1,1,1,51},
				{0,49,1,4},
				{4,1,1,48},
				{4,11,2,37},
				{1,46,1,6},
		};
		
		int [][] second_pssm = new int[][] { //gata1_m0080_transf
				{23,0,0,4},
				{0,0,27,0}, 
				{27,0,0,0},
				{0,2,0,25},
				{26,0,0,1},
				{27,0,0,0},
				{0,0,27,0},
				{27,0,0,0}, 
				{0,0,0,27},
				{26,0,0,1},
				{19,1,4,3},
		};
		
		RegulatoryElementPWM firstPWM = new RegulatoryElementPWM("first_pssm", first_pssm);
		RegulatoryElementPWM secondPWM = new RegulatoryElementPWM("second_pssm", second_pssm);
		int firstPwmLength = firstPWM.getPwm().length;
		int secondPwmLength = secondPWM.getPwm().length;
		
		// Base composition for positive example sequences is the same as the negative
		ArrayList<Feature> positiveSequences = FeaturesTools.generateSimulatedRegulatoryRegions(
				numOfPositiveSequences, sequenceLength,	"posExSeq_", sequenceProbabilities);
		
		// Here we plant PWMs in positive sequence according to the rule: before
		for (int i = 0; i < positiveSequences.size(); i++){
			
			Feature seqFeature = positiveSequences.get(i);
			String seqStr = seqFeature.getSequence();
			
			// Generate position to plant first and second PWM sequence so that
			//   the positions are random, but first PWM sequence is always located
			//   before the second.
			int pos1 = rand.nextInt(seqStr.length() - 2 * (firstPwmLength + secondPwmLength)) + 1;
			int pos2 = DataModeller.generateRandomInt(pos1 + firstPwmLength + 1, 
								seqStr.length() - secondPwmLength -1);
			
			seqStr = DataModeller.plantPSSM(seqStr, pos1, firstPWM.getPwm());
			seqStr = DataModeller.plantPSSM(seqStr, pos2, secondPWM.getPwm());
			
			seqFeature.setSequence(seqStr);
			positiveSequences.set(i, seqFeature);
		}
		
		// induce rules
		explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(positiveSequences);
		//explorer.setPositiveCutOffScore(6.0); 
		//explorer.setNegativeCutOffScore(7.0); 
		
		String alephOutput = explorer.induceRules();
		
		//analyse the result
		//System.out.println("\n\n" + alephOutput + "\n");
		
		ArrayList<String[]> theory = DataFormatter.extractTheoryByRules(alephOutput);
		
		//print theory
		System.out.println(DataFormatter.extractTheoryAndPerformance(alephOutput));
		
		String expectedTerm = "before(A, " + firstPWM.getName() + ", " + secondPWM.getName() + ")";
		boolean containsTerm = Experimenter.containsTerm(theory, expectedTerm);
		System.out.println("Induction of _before_: Contains term? : " + containsTerm);

		boolean containsRule = Experimenter.containsRule(theory, "positive(A) :- " + expectedTerm + ".");
		System.out.println("Induction of _before_: Contains rule? : " + containsRule);
		
	}
	
	
	public void rule_has_tfbs_InductionTest(int sequenceLength, int numOfPositiveSequences, int numOfNegativeSequences) throws DataFormatException{
		/* Positive example sequences are generated per each test and PWMs are planted in them
		 * 		according to the rule that is being tested.
		 * Negative example sequences stay the same throughout all tests
		 */
		
		Random rand = new Random();
		// nucleotide probabilities of the generated negative sequences and 
		//	base of the positive sequences (where PWMs will be planted)
		double[] sequenceProbabilities = new double[]{0.25, 0.25, 0.25, 0.25};

		ArrayList<Feature> negativeSequences = FeaturesTools.generateSimulatedRegulatoryRegions(numOfNegativeSequences,
				sequenceLength, "negExSeq_", sequenceProbabilities);
		explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(negativeSequences);
		
		
		// Get PWMs to plant into positive example sequences
		ArrayList<RegulatoryElementPWM> pwms = explorer.getRegulatoryElementService().getRegulatoryElementsPWMs();
		
		
		//***** Test "has_tfbs" rule
		
		RegulatoryElementPWM plantedPWM = pwms.get(rand.nextInt(pwms.size()));
		//System.out.println("Planting PMW name: "+ plantedPWM.getName());
		
		// Base composition for positive example sequences is the same as the negative
		ArrayList<Feature> positiveSequences = FeaturesTools.generateSimulatedRegulatoryRegions(
				numOfPositiveSequences, sequenceLength,	"posExSeq_", sequenceProbabilities);
		
		// Here we plant PWMs in positive sequence according to the rule: before
		for (int i = 0; i < positiveSequences.size(); i++){
			
			Feature seqFeature = positiveSequences.get(i);
			String seqStr = seqFeature.getSequence();
			
			// Generate position to plant first and second PWM sequence so that
			//   the positions are random, but first PWM sequence is always located
			//   before the second.
			int pos1 = rand.nextInt(seqStr.length() - plantedPWM.getPwm().length) + 1;
			
			seqStr = DataModeller.plantPSSM(seqStr, pos1, plantedPWM.getPwm());
			
			seqFeature.setSequence(seqStr);
			positiveSequences.set(i, seqFeature);
		}
		
		// induce rules
		explorer.getRegulatoryRegionService().setPositiveRegulatoryRegions(positiveSequences);
		explorer.setNegativeCutOffScore(10.5);
		String alephOutput = explorer.induceRules();
		
		//analyse the result
		//System.out.println("\n\n" + alephOutput + "\n");
		
		ArrayList<String[]> theory = DataFormatter.extractTheoryByRules(alephOutput);
		
		String expectedTerm = "has_tfbs(A, " + plantedPWM.getName() + ")";
		boolean containsTerm = Experimenter.containsTerm(theory, expectedTerm);
		System.out.println("Induction of _has_tfbs_: Contains term? : " + containsTerm);

		boolean containsRule = Experimenter.containsTerm(theory, "has_crm(A) :- " + expectedTerm + ".");
		System.out.println("Induction of _has_tfbs_: Contains rule? : " + containsRule);
		
	}

	
	
	
	
	/*****************************************************************************************
	 * 									Helper methods
	 *****************************************************************************************/
	
	
	/*
	 * return double similarity
	 */
	public static double compareTheories(ArrayList<String[]> expectedRules, String actualAlephOutput) throws DataFormatException{
		if (actualAlephOutput == null || actualAlephOutput.isEmpty()){
			throw new DataFormatException("Aleph output is empty.");
		}
		ArrayList<String[]> actualRules = DataFormatter.extractTheoryByRules(actualAlephOutput);
		return compareTheoriesByRules(expectedRules, actualRules);
	}
	
	public static double compareTheoriesByRules(ArrayList<String[]> expectedRules, ArrayList<String[]> actualRules) throws DataFormatException{
		
		if (expectedRules == null || expectedRules.isEmpty() ||
				actualRules == null || actualRules.isEmpty()){			
			throw new DataFormatException("No rules were extracted from Aleph's output.");
		}
		
		int numMatched = compareRules(expectedRules, actualRules);
		double similarity = (double) numMatched / expectedRules.size();
		
		//System.out.println("Number of matched rules: " + numMatched);		
		//System.out.println("Accuracy: "+ accuracy);
		
		return similarity;
	}
	
	public static double compareTheoriesByTerms(ArrayList<String[]> expectedRules, ArrayList<String[]> actualRules) throws DataFormatException{
		
		if (expectedRules == null || expectedRules.isEmpty() ||
				actualRules == null || actualRules.isEmpty()){			
			throw new DataFormatException("No rules were extracted from Aleph's output.");
		}
		
		int numMatched = compareTerms(expectedRules, actualRules);
		double similarity = (double) numMatched / getNumberOfTerms(expectedRules);;
		
		//System.out.println("Number of matched rules: " + numMatched);		
		//System.out.println("Accuracy: "+ accuracy);
		
		return similarity;
	}
	
	public static double compareTheories(String expectedAlephOutput, String actualAlephOutput) throws DataFormatException{
		if (expectedAlephOutput == null || expectedAlephOutput.isEmpty() ||
				actualAlephOutput == null || actualAlephOutput.isEmpty()){
			throw new DataFormatException("Aleph output is empty.");
		}
		ArrayList<String[]> expectedRules = DataFormatter.extractTheoryByRules(expectedAlephOutput);
		ArrayList<String[]> actualRules = DataFormatter.extractTheoryByRules(actualAlephOutput);
	
		return compareTheoriesByRules(expectedRules, actualRules);
	}
	
	/*
	public static double compareTheories(String expectedAlephOutput, String actualAlephOutput) throws DataFormatException{
		if (expectedAlephOutput == null || expectedAlephOutput.isEmpty() ||
				actualAlephOutput == null || actualAlephOutput.isEmpty()){
			throw new DataFormatException("Aleph output is empty.");
		}
		ArrayList<String[]> expectedRules = Experimenter.extractTheory(expectedAlephOutput);
		ArrayList<String[]> actualRules = Experimenter.extractTheory(actualAlephOutput);
		
		if (expectedRules == null || expectedRules.isEmpty() ||
				actualRules == null || actualRules.isEmpty()){			
			throw new DataFormatException("No rules were extracted from Aleph's output.");
		}
		
		int numMatched = compareRules(expectedRules, actualRules);
		double accuracy = (double) numMatched / expectedRules.size();
		
		//System.out.println("Number of matched rules: " + numMatched);		
		//System.out.println("Accuracy: "+ accuracy);
		
		return accuracy;
	}
	*/
	
	/* Helper method for matching rules.
	 * Given clause as a string, creates an ArrayList with
	 * first element being the head of the clause and
	 * subsequent elements - terms of the body of the clause.
	 * NOTE: all variables are renamed as one variable Z - this is
	 * 		an assumption of the comparison (we only take rule names and
	 * 		constants into consideration, all variables are considered
	 * 		the same).
	 */
	public static String[] tokeniseAndTransform(String clause){
		clause = Experimenter.unifyVarNames(clause);
		
		String[] clauseList = clause.split("\\s*\u003A-\\s*|\\),\\s*");
		
		for (int i = 0; i < clauseList.length; i++) {
			String currClause = clauseList[i];
			if (currClause.endsWith(".")){
				currClause = currClause.substring(0, currClause.length() - 1);
			}
			if (!currClause.endsWith(")")){
				currClause = currClause.concat(")");
			}
			clauseList[i] = currClause;
		}
		
		return clauseList;
		
		
	}
	
	/*
	 * return int number of actual rules that were matched (equal) to expected
	 */
	public static int compareRules(ArrayList<String[]> expectedRules, ArrayList<String[]> actualRules){
		int numberMatched = 0;
		
		for (Iterator<String[]> iterator = expectedRules.iterator(); iterator.hasNext();) {
			String[] expectedRule = (String[]) iterator.next();
			
			for (Iterator<String[]> iterator2 = actualRules.iterator(); iterator2.hasNext();) {
				String[] actualRule = (String[]) iterator2.next();
				if (ruleEquals(expectedRule, actualRule)){
					numberMatched++;
					//remove matched rule for faster runs
					//actualRules.remove(actualRule); 
				}
			}
		}
		
		return numberMatched;
	}
	
	/*
	 * return int number of actual rules that were matched (equal) to expected
	 */
	public static int compareTerms(ArrayList<String[]> expectedRules, ArrayList<String[]> actualRules){
		int numberMatched = 0;
		
		for (Iterator<String[]> iterator = expectedRules.iterator(); iterator.hasNext();) {
			String[] expectedRule = (String[]) iterator.next();
			
			for (int i = 0; i < expectedRule.length; i++) {
				if (containsTerm(actualRules, expectedRule[i])){
					numberMatched++;
				}
			}
			
		}
		
		return numberMatched;
	}
	
	
	/* Checks if the specified theory contains a specified term.
	 * (Term is aprt of the predicate, like before(m2, m5, A)).
	 * NOTE: variables (the ones that start with capital letter)
	 * 		 are all converted to Z.
	 */
	public static boolean containsTerm(ArrayList<String[]> theory, String term){
		term = unifyVarNames(term);
		
		for (Iterator<String[]> iterator = theory.iterator(); iterator.hasNext();) {
			String[] rule = (String[]) iterator.next();
			for (int i = 0; i < rule.length; i++) {
				if (term.equals(unifyVarNames(rule[i]))){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/* Similar to the method above, but more strict. Checks if the 
	 * whole rule is contained in the theory. 
	 * @param ArrayList<String[]> theory is expected to have all variables 
	 * 				substituted for Z (i.e. passed through tokenizeAndTransform())
	 * @param String rule is of the Aleph rule output format, 
	 * 				 ex. "head(A) :- clause1(var, var, A), clause2(var)."
	 */
	public static boolean containsRule(ArrayList<String[]> theory, String rule){
		String[] terms = tokeniseAndTransform(rule);
		
		for (Iterator<String[]> iterator = theory.iterator(); iterator.hasNext();) {
			String[] theoryTerms = (String[]) iterator.next();
			
			if (ruleEquals(terms, theoryTerms)){
				return true;
			}
		}
		
		return false;
	}
	
	
	/* Matches 2 aleph ilp rules. 
	 * @param String[] tokenized rule, i.e. sequence of terms, starting with the head
	 * 					and followed by body terms.
	 * return true if two rules are considered equivalent; false otherwise.
	 */
	public static boolean ruleEquals(String[] expectedTerms, String[] actualTerms){
		
		if (expectedTerms.length != actualTerms.length) return false;
		
		Arrays.sort(expectedTerms);
		Arrays.sort(actualTerms);
		
		for (int i = 0; i < actualTerms.length; i++) {
			if (!expectedTerms[i].equals(actualTerms[i])){
				return false;
			}
		}
		
		return true;
	}
	
	
	
	public static String unifyVarNames(String clause){
		//anything upper case: [A-Z]\w*
		Pattern p = Pattern.compile("[^\\w'][A-Z]\\w*");
        //String clause = "has_crm('ABC') :- same_strand(mA2, mB3, A), other(B).";
		Matcher m = p.matcher(clause);
        
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        
        while(result) {
            m.appendReplacement(sb, clause.charAt(m.start()) +"Z");
            result = m.find();
        }
        // Add the last segment of input to the new String
        m.appendTail(sb);
        
        return sb.toString();
	}
	
	public static void printTheory(ArrayList<String[]> theory){
		for (int i = 0; i < theory.size(); i++) {
			String[] rules = theory.get(i);
			
			System.out.print("[Rule]"+i+"]\t");
			
			for (int j = 0; j < rules.length; j++) {
				System.out.print(rules[j]);
				if (j == rules.length - 1){
					System.out.print(".");
				} else if (j == 0){
					System.out.print(" :- ");
				} else {
					System.out.print(", ");					
				}
			}
			System.out.println();
		}
	}
	
	public static int getNumberOfTerms(ArrayList<String[]> theory){
		int termsNum = 0;
		
		for (Iterator<String[]> iterator = theory.iterator(); iterator.hasNext();) {
			String[] rule = (String[]) iterator.next();
				termsNum = termsNum + rule.length;
		}
		
		return termsNum;
	}

}
