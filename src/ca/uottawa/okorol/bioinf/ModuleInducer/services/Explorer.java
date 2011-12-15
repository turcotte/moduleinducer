package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

public class Explorer{
	private RegulatoryRegionService regulatoryRegionService;
	private RegulatoryElementService regulatoryElementService;
	private String tempIlpJobDirName;
	
	//*** Experimental: collection of vars from different "depths"
	//***				that are relevant to the experiment

	//TODO: inline
	//min score of the feature to be included in the experiment;
	// i.e. only features with higher score will be included in the rule induction
	private double positiveCutOffScore;
	private double negativeCutOffScore;
	
	private double posATcomposition;
	private double posCGcomposition;
	private double negATcomposition;
	private double negCGcomposition;
	
	
/*	
	public Explorer(RegulatoryRegionService regRegionService, RegulatoryElementService regElService)throws DataFormatException{
		this.regulatoryRegionService = regRegionService;
		this.regulatoryElementService = regElService;
		this.tempIlpJobDirName = FileHandling.createTempIlpOutputDirectory();
		//TODO see if I need to clean it up
		//cleanUp = true; //if we create a tmp dir, we need to clean it up after the job
		//then later
		//if (cleanUp){
		//	FileHandling.deleteDirectory(ilpDirName);
		//}
	}
*/


	public Explorer(RegulatoryRegionService regRegionService, RegulatoryElementService regElService, String tempIlpJobDirName) throws DataFormatException{
		this.regulatoryRegionService = regRegionService;
		this.regulatoryElementService = regElService;
		this.tempIlpJobDirName = tempIlpJobDirName;
		
		positiveCutOffScore = SystemVariables.getInstance().getPositivePatserCutOffScore();
		negativeCutOffScore = SystemVariables.getInstance().getNegativePatserCutOffScore();
		
		///////  Statistics
		SystemVariables.getInstance().cleanStatistics(); //clean old;
		
		SystemVariables.getInstance().setPosSeqNum(regulatoryRegionService.getPositiveRegulatoryRegions().size());
		SystemVariables.getInstance().setNegSeqNum(regulatoryRegionService.getNegativeRegulatoryRegions().size());
		
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Number of positive sequences: " + regulatoryRegionService.getPositiveRegulatoryRegions().size()); 
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Number of negative sequences: "+ regulatoryRegionService.getNegativeRegulatoryRegions().size());

		SystemVariables.getInstance().appendToExperimentNotes("\\n* Patser cut-off score for positive sequences: " +
				positiveCutOffScore);
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Patser cut-off score for negative sequences: " +
				negativeCutOffScore);
		
		double[] posNtComposition = FeaturesTools.getNucleotideComposition(regulatoryRegionService.getPositiveRegulatoryRegions());
		posATcomposition = posNtComposition[0] + posNtComposition[3];
		posCGcomposition = posNtComposition[1] + posNtComposition[2];
		SystemVariables.getInstance().appendToExperimentNotes("\\n* A:T and C:G composition of positive sequences: " + posATcomposition + " " + posCGcomposition);

		SystemVariables.getInstance().setPosATcomposition(posATcomposition);
		SystemVariables.getInstance().setPosCGcomposition(posCGcomposition);
		
		double[] negNtComposition = FeaturesTools.getNucleotideComposition(regulatoryRegionService.getNegativeRegulatoryRegions());
		negATcomposition = negNtComposition[0] + negNtComposition[3];
		negCGcomposition = negNtComposition[1] + negNtComposition[2];
		SystemVariables.getInstance().appendToExperimentNotes("\\n* A:T and C:G composition of negative sequences: " + negATcomposition + " " + negCGcomposition);
		
		SystemVariables.getInstance().setNegATcomposition(negATcomposition);
		SystemVariables.getInstance().setNegCGcomposition(negCGcomposition);
		
	}
	
	
	

	/*
	 * Runs ILP 
	 */
	public String induceRules() throws DataFormatException {

		IlpService ilpService = createIlpFiles();
		
		System.out.println("== Created all ILP files. Starting to induce.");
		

		//
		// ==> the star of the show
		//
		ilpService.runILP();
		
		
		// Overwrite temporary result html footer with the one that explains how to read a theory
		String htmlFooterFileName = tempIlpJobDirName + SystemVariables.getInstance().getString("html.footer.file.name");
		FileHandling.writeFile(htmlFooterFileName, FileHandling.getHTMLResultsFooter(true, true));
		
		return "Done";
	}
	

	
	
	public IlpService createIlpFiles() throws DataFormatException{
		
		ArrayList<Feature> posRegElements = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getPositiveRegulatoryRegions(), positiveCutOffScore, posATcomposition, posCGcomposition);
		
		if (posRegElements == null) {
			posRegElements = regulatoryElementService.getRegulatoryElements(
					regulatoryRegionService.getPositiveRegulatoryRegions(), regulatoryRegionService.getNegativeRegulatoryRegions(), positiveCutOffScore);
		}
		
		// ***** Add statistics
		addMotifMatchingStatisticsToNotes("positive");
		SystemVariables.getInstance().setPosSeqRelElMatchesNum(posRegElements.size());

		
		
		ArrayList<Feature> negRegElements = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getNegativeRegulatoryRegions(), negativeCutOffScore, negATcomposition, negCGcomposition);
		
		if (negRegElements == null){
			negRegElements = regulatoryElementService.getRegulatoryElements(
					regulatoryRegionService.getNegativeRegulatoryRegions(), null, negativeCutOffScore);
		}
		
		// ***** Add statistics
		addMotifMatchingStatisticsToNotes("negative");
		SystemVariables.getInstance().setNegSeqRelElMatchesNum(negRegElements.size());
		

		/*	
		System.out.println("\n Number of matches of pairs of matrices in jurkat and erythroid sequences");

		String[] pwmPair = new String[]{"MA0055.1", "MA0152.1"};
		System.out.println(pwmPair[0] + " and " + pwmPair[1]+" :\t" + 
				FeaturesTools.numSequencesContainingAll(pwmPair, posRegElements) + "\t"+
				FeaturesTools.numSequencesContainingAll(pwmPair, negRegElements));
	
		System.out.println("\nDistances between Gata and Ebox for positive sequences:");
		FeaturesTools.printRegElDistances("Gata", "Ebox", posRegElements);
	*/
		
		IlpService ilpService = new IlpService(tempIlpJobDirName);
		ilpService.createIlpFiles(regulatoryRegionService.getPositiveRegulatoryRegions(),
				regulatoryRegionService.getNegativeRegulatoryRegions(),
				posRegElements, negRegElements);
		
		return ilpService;
	}


	/* Note: unsound method
	 * Based on the statistics, collected when extracting motif matches by RegElementService,
	 * collects text note of motif matching statistics, which will be added to ILP file.
	 * This method has to be called immediately after RegElementService.getRegulatoryElements call,
	 * since every such call overrides the pssmMatchStatistics in RegElementService
	 * 
	 * @param sequenceKind - can only be "positive" or "negative", depending on the kind of sequences
	 */
	private void addMotifMatchingStatisticsToNotes(String sequenceKind) throws DataFormatException {
		
		Hashtable<String, Double> pssmMatchStats = regulatoryElementService.getPssmMatchingStatistics();
		if (pssmMatchStats != null && !pssmMatchStats.isEmpty()){
			
			SystemVariables.getInstance().appendToExperimentNotes("\\n* Number of PSSM matches in " + sequenceKind + " sequences: ");
			int numberOfRegions = 0;
			if ("positive".equals(sequenceKind)){
				numberOfRegions = regulatoryRegionService.getPositiveRegulatoryRegions().size();
			} else{
				numberOfRegions = regulatoryRegionService.getNegativeRegulatoryRegions().size();
			}
			
			Enumeration<String> pssmMatchNames = pssmMatchStats.keys();
			int totalNumMatches = 0;
			String r_pssmNames = "";
			String r_matches = "";
			while(pssmMatchNames.hasMoreElements()){
				String pssmName = pssmMatchNames.nextElement();
				double pssmStat = pssmMatchStats.get(pssmName);
				int numMatches = (int)(pssmStat*numberOfRegions);
				totalNumMatches = totalNumMatches + numMatches;
				
				r_pssmNames = r_pssmNames + "\'" + pssmName+ "\', ";
				r_matches = r_matches + numMatches + ", ";
				SystemVariables.getInstance().appendToExperimentNotes("\\n\\t" + pssmName + "\\t" + numMatches);
			}
			SystemVariables.getInstance().appendToExperimentNotes("\\n Total number of matches: " + totalNumMatches);
			
//			System.out.println("In positive sequences: ");
//			System.out.println(r_pssmNames);
//			System.out.println(r_matches);
		}
	}
	

	
	public String induceRulesWithTestSet(double testSetPercent, int runNumber) throws DataFormatException {

		SystemVariables.getInstance().appendToExperimentNotes("\\n* Total number of PSSM matches in positive sequences for this run: ");
		//System.out.println("Total number of PSSM matches in positive sequences: ");
		ArrayList<Feature> posTrainingSet = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getPositiveRegulatoryRegions(), positiveCutOffScore, posATcomposition, posCGcomposition);
		
		
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Total number of PSSM matches in negative sequences for this run: ");
		//System.out.println("Total number of PSSM matches in negative sequences: ");
		ArrayList<Feature> negTrainingSet = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getNegativeRegulatoryRegions(), negativeCutOffScore, negATcomposition, negCGcomposition);
				
		IlpService formatter = new IlpService(tempIlpJobDirName);
		formatter.createIlpFilesWithTestSet(regulatoryRegionService.getPositiveRegulatoryRegions(), regulatoryRegionService.getNegativeRegulatoryRegions(),
				posTrainingSet, negTrainingSet, 
				testSetPercent, runNumber);
		
		String ilpTheory = formatter.runILP();
		
		return ilpTheory;

	}
	
	public String induceRulesWithTestSet(double testSetPercent) throws DataFormatException {

		SystemVariables.getInstance().appendToExperimentNotes("\\n* Total number of PSSM matches in positive sequences: ");
		//System.out.println("Total number of PSSM matches in positive sequences: ");
		ArrayList<Feature> posTrainingSet = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getPositiveRegulatoryRegions(), positiveCutOffScore, posATcomposition, posCGcomposition);
		
		SystemVariables.getInstance().appendToExperimentNotes("\\n* Total number of PSSM matches in negative sequences: ");
		//System.out.println("Total number of PSSM matches in negative sequences: ");
		ArrayList<Feature> negTrainingSet = regulatoryElementService.getRegulatoryElements(
				regulatoryRegionService.getNegativeRegulatoryRegions(), negativeCutOffScore, negATcomposition, negCGcomposition);
					
		IlpService formatter = new IlpService(tempIlpJobDirName);
		formatter.createIlpFilesWithTestSet(regulatoryRegionService.getPositiveRegulatoryRegions(), regulatoryRegionService.getNegativeRegulatoryRegions(),
				posTrainingSet, negTrainingSet, 
				testSetPercent);
		
		String ilpTheory = formatter.runILP();
		
		return ilpTheory;

	}


	
	public RegulatoryRegionService getRegulatoryRegionService() {
		return regulatoryRegionService;
	}
	public void setRegulatoryRegionService(RegulatoryRegionService regulatoryRegionService) {
		this.regulatoryRegionService = regulatoryRegionService;
	}
	
	public RegulatoryElementService getRegulatoryElementService() {
		return regulatoryElementService;
	}
	public void setRegulatoryElementService(
			RegulatoryElementService regulatoryElementService) {
		this.regulatoryElementService = regulatoryElementService;
	}
	
	public double getPositiveCutOffScore() {
		return positiveCutOffScore;
	}
	public void setPositiveCutOffScore(double positiveCutOffScore) {
		this.positiveCutOffScore = positiveCutOffScore;
	}

	public double getNegativeCutOffScore() {
		return negativeCutOffScore;
	}
	public void setNegativeCutOffScore(double negativeCutOffScore) {
		this.negativeCutOffScore = negativeCutOffScore;
	}
	
	public String getTempIlpJobDirName() {
		return tempIlpJobDirName;
	}
	public void setTempIlpJobDirName(String tempIlpJobDirName) {
		this.tempIlpJobDirName = tempIlpJobDirName;
	}

}

