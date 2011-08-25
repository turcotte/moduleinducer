package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.OccurrencePerLocationMatrix;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataModeller;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.StatAnalyser;

public class SyntheticRegRegionService implements RegulatoryRegionService {
	
	private ArrayList<Feature> positiveRegRegions;
	private ArrayList<Feature> negativeRegRegions;
	
	private static String POSITIVE_REGION_PREFIX = "posExSeq_";
	private static String NEGATIVE_REGION_PREFIX = "negExSeq_";
	
	// nucleotide probabilities of the generated negative sequences and 
	//	base of the positive sequences (where PSSMs will be planted)
	private static double[] sequenceNtProbabilities = new double[]{0.25, 0.25, 0.25, 0.25};
	
	// Based on this service, the synthetic sequences will be generated. I.e the probabilities
	// of planted PWMs and the sequence lengths will be generated based on this service
	private RegulatoryRegionService modelRegRegionService;
	private RegulatoryElementService modelRegElService;
	//matches the length of the model sequence; same for positive and negative sequences
	private int regulatorySequenceLength = 0;
	
	public SyntheticRegRegionService(int numOfPositiveRegRegions, int negExMultiplicationFactor) throws DataFormatException{
		
		modelRegRegionService = new CElegansRegRegionService(0);
		
		File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
		String tmpJobDir = SystemVariables.getInstance().getString("temp.output.dir"); //TODO create a proper tmp job dir for experiments
		
		modelRegElService = new PatserRegElementService(pwmDir, tmpJobDir);
		
		
		if (modelRegRegionService.getPositiveRegulatoryRegions().size() > 0){
			regulatorySequenceLength = modelRegRegionService.getPositiveRegulatoryRegions().get(0).getSequence().length(); 
		} else {
			throw new DataFormatException("Model regulatory regions are empty. Can not model synthetic regions based on an empty regions.");
		}
		
		//TODO: remove - just for testing
		regulatorySequenceLength = 25;
		negativeRegRegions = FeaturesTools.generateSimulatedRegulatoryRegions(
				negExMultiplicationFactor * numOfPositiveRegRegions, 
				regulatorySequenceLength, NEGATIVE_REGION_PREFIX, sequenceNtProbabilities);

		positiveRegRegions = FeaturesTools.generateSimulatedRegulatoryRegionsWithPositionalPSSMs(numOfPositiveRegRegions, regulatorySequenceLength, 
				sequenceNtProbabilities, SystemVariables.getInstance().getPositivePatserCutOffScore(), POSITIVE_REGION_PREFIX, 
				modelRegRegionService.getPositiveRegulatoryRegions(), modelRegElService);
			//this.generateRegulatoryRegions(numOfPositiveRegRegions, );
		
	}
	
	@Override
	public ArrayList<Feature> getPositiveRegulatoryRegions(){
		return positiveRegRegions;
	}
	@Override
	public void setPositiveRegulatoryRegions(ArrayList<Feature> regRegions) {
		positiveRegRegions = regRegions;
	}
	
	@Override
	public ArrayList<Feature> getNegativeRegulatoryRegions(){
		return negativeRegRegions;
	}
	@Override
	public void setNegativeRegulatoryRegions(ArrayList<Feature> regRegions) {
		negativeRegRegions = regRegions;
	}
	
	/* Generates regulatory regions for ILP positive examples. 
	 * Base of the sequence is random ACGT 0.25 : 0.25 : 0.25 : 0.25 composition,
	 * cut-off score for finding PSSMs in model sequences is a default positive cut-off
	 * C.elegans pwm are planted in the base sequence at random positions.
	 * 
	 * @param int number of regions to be generates
	 * @param String generic prefix for the name of regulatory sequence
	 */
	private ArrayList<Feature> generateRegulatoryRegions(int numOfRegions, String regionNamePrefix) throws DataFormatException{		
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		if (numOfRegions <= 0){
			return regRegions;
		}
		
		// Generate probabilities based on C.elegans data
		//RegulatoryRegionService regRegionService = 
		//	RegulatoryRegionServiceFactory.getService(RegulatoryRegionService.C_ELEGANS_DATA_SERVICE, 0, 0);
		
		ArrayList<Feature> cElegansRegRegions = modelRegRegionService.getPositiveRegulatoryRegions();
		if (cElegansRegRegions == null){
			throw new DataFormatException("SyntheticRegRegionService: Unable to retrieve C.elegans regulatory regions to generate positive sequences.");
		}
		
		
		File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
		String tmpJobDir = SystemVariables.getInstance().getString("temp.output.dir"); //TODO create a proper tmp job dir for experiments
		
		PatserRegElementService regElService = new PatserRegElementService(pwmDir, tmpJobDir);
		
		double cutOffScore = SystemVariables.getInstance().getPositivePatserCutOffScore(); 
		double atComposition = sequenceNtProbabilities[0] + sequenceNtProbabilities[3];
		double cgComposition = sequenceNtProbabilities[1] + sequenceNtProbabilities[2];
		ArrayList<Feature> regElements = regElService.getRegulatoryElements(cElegansRegRegions, cutOffScore, atComposition, cgComposition);
		
		ArrayList<RegulatoryElementPWM> regElementPWMs = regElService.getRegulatoryElementsPWMs();
		
		StatAnalyser stat = new StatAnalyser(regElements);
		
		
		for (int i = 0; i < numOfRegions; i++){
			String sequence = generatePosExSequence(stat, regElementPWMs, 
					cElegansRegRegions.get(0).getSequence().length(), cElegansRegRegions.size());
			regRegions.add(new Feature(regionNamePrefix + i, "gene", null, 0, 0, sequence, 0.0));	
		}
		
		return regRegions;
	}

/*
	public ArrayList<Feature> generateSimulatedRegulatoryRegions(int numOfRegulatoryRegions, int regRegionLength, String regionNamePrefix) {
		ArrayList<Feature> genes = new ArrayList<Feature>();
		
		for (int i = 0; i < numOfRegulatoryRegions; i++){
			genes.add(new Feature(regionNamePrefix + i, "gene", null, 0, 0, DataModeller.getRandomSequence(regRegionLength, 0.25, 0.25, 0.25, 0.25), 0.0));	
		}
		
		return genes;
	}
*/
	
	private String generatePosExSequence(StatAnalyser stat, ArrayList<RegulatoryElementPWM> regElPWMs, 
										int sequenceLength, int numberOfSeq) throws DataFormatException {
		String sequence = "";
		final int lengthOfSection = 50;
		Random rand = new Random();
		int plantCtr = 0;
		
		// Generate probabilities based on C.elegans data
		/*
		GeneService geneService = GeneServiceFactory.getService(GeneService.C_ELEGANS_DATA_SERVICE);
		ArrayList<Feature> cElegansGenes = geneService.getGenes();
		if (cElegansGenes == null){
			throw new DataFormatException("SyntheticGeneService: Unable to retrieve C.elegans genes to generate positive sequences.");
		}
		TfbsService tfbsService = TfbsServiceFactory.getService(TfbsService.PATSER_SERVICE);
		ArrayList<Feature> tfbsHits = tfbsService.getTfbsHits(cElegansGenes);
		
		ArrayList<TfbsPWM> tfbsPWMs = tfbsService.getTfbsPWMs();
		
		StatAnalyser stat = new StatAnalyser(tfbsHits);
	*/	
		//OccurrencePerLocationMatrix probMtxObj = stat.getLocationalProbabilities(lengthOfSection, cElegansGenes.get(0).getUpstreamSequence().length(), cElegansGenes.size());
		OccurrencePerLocationMatrix probMtxObj = stat.getLocationalProbabilities(lengthOfSection, sequenceLength, numberOfSeq);
		double[][] pm = probMtxObj.getProbabilityMatrix();
		
		//int currPos = 0; //position till which the sequence has been built
		for (int i = 0; i < pm.length; i++){ 
			
			ArrayList<String> regElementNames = new ArrayList<String>();
			ArrayList<Double> probabilities = new ArrayList<Double>();
			for (int j = 0; j < pm[i].length; j++){
				if (Double.compare(pm[i][j], 0.0) > 0){
					probabilities.add(pm[i][j]);
					regElementNames.add(probMtxObj.getNamesOfTfbs().get(j));
				}
			}
		
			while (!probabilities.isEmpty()){
				if (sequence.length() >= (i+1)*lengthOfSection){
					break;
				}
				
				int randomProbabilPosition = rand.nextInt(probabilities.size());
				double currProb = probabilities.get(randomProbabilPosition);
				
				if (DataModeller.generateRandomEvent(currProb)){
					int[][] pwm = getPwmByName(regElementNames.get(randomProbabilPosition), regElPWMs);
					String regElementSeq = DataModeller.getPssmSequence(pwm);
					int randomPwmPosition = rand.nextInt((i+1)*lengthOfSection - sequence.length());
					String randSeq = "";
					if (randomPwmPosition > 0){
						randSeq = DataModeller.getRandomSequence(randomPwmPosition, 0.25, 0.25, 0.25, 0.25);
					}
					sequence = sequence.concat(randSeq);
					sequence = sequence.concat(regElementSeq);
					plantCtr++;
				}
				
				regElementNames.remove(randomProbabilPosition);
				probabilities.remove(randomProbabilPosition);
			}
			
			//fill what's left in a section with a random sequence
			if (sequence.length() < (i+1)*lengthOfSection){
				int len = (i+1)*lengthOfSection - sequence.length();
				String seq = DataModeller.getRandomSequence(len, 0.25, 0.25, 0.25, 0.25);
				sequence = sequence.concat(seq);
			}
		
		}
		sequence = sequence.substring(0, sequenceLength);
		
		//TODO print
		System.out.println("=== Number of PWMs planted in this sequence: " + plantCtr);
		
		return sequence;
	}
	
	private int[][] getPwmByName(String tfbsName, ArrayList<RegulatoryElementPWM> pwms){
		for (Iterator<RegulatoryElementPWM> iterator = pwms.iterator(); iterator.hasNext();) {
			RegulatoryElementPWM tfbsPWM = (RegulatoryElementPWM) iterator.next();
			if (tfbsPWM.getName().equals(tfbsName)){
				return tfbsPWM.getPwm();
			}
		}
		return null;
	}

	@Override
	public void updateNumberOfPositiveRegRegions(int num) throws DataFormatException {
		//TODO check for empty regions
		//TODO test this
		int deltaSeqNum = num - positiveRegRegions.size();
		
		if (deltaSeqNum < 0){ //desired number of positive sequences is less than is available now
			for (int i = 0; i > deltaSeqNum; i--){
				positiveRegRegions.remove(0);
			}
		} else { // desired number is more than available -> need to generate extra
			ArrayList<Feature> extraSimulatedGenes = FeaturesTools.generateSimulatedRegulatoryRegionsWithPositionalPSSMs(
					deltaSeqNum, regulatorySequenceLength, sequenceNtProbabilities, SystemVariables.getInstance().getPositivePatserCutOffScore(),
					POSITIVE_REGION_PREFIX, modelRegRegionService.getPositiveRegulatoryRegions(), modelRegElService);
				
				//generateRegulatoryRegions(deltaSeqNum, POSITIVE_REGION_PREFIX);

			negativeRegRegions.addAll(extraSimulatedGenes);
		}
		
	}

	@Override
	public void updateNumberOfNegativeRegRegions(int multiplicationFactor) {
		//TODO check for empty regions
		//TODO test this
		int deltaSeqNum = positiveRegRegions.size() * multiplicationFactor - negativeRegRegions.size();
		
		if (deltaSeqNum < 0){ //desired number of negative sequences is less than is available now
			for (int i = 0; i > deltaSeqNum; i--){
				negativeRegRegions.remove(0);
			}
		} else { // desired number is more than available -> need to generate extra
			ArrayList<Feature> extraSimulatedGenes = FeaturesTools.generateSimulatedRegulatoryRegions(deltaSeqNum, 
					positiveRegRegions.get(0).getSequence().length(), NEGATIVE_REGION_PREFIX, sequenceNtProbabilities);

			negativeRegRegions.addAll(extraSimulatedGenes);
		}
	}


}
