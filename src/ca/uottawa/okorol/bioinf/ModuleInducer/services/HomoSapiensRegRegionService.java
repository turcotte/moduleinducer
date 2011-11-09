package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FastaExtractor;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;

public class HomoSapiensRegRegionService implements RegulatoryRegionService {
	
	public static final String JURKAT = "JURKAT";
	public static final String ERYTHROID = "ERYTHROID";
	public static final String SYNTHETIC_PLAIN = "SYNTHETIC_PLAIN";
	public static final String SYNTHETIC_WITH_PSSM = "SYNTHETIC_WITH_PSSM";
	public static final String SYNTHETIC_WITH_POSITIONAL_PSSM = "SYNTHETIC_WITH_POSITIONAL_PSSM";

	private ArrayList<Feature> positiveRegRegions;
	private ArrayList<Feature> negativeRegRegions;
	
	private static String humanGenomeDir;
	private static String jurkatSeqFullFileName;
	private static String erythroidSeqFullFileName;

	
	private static String jurkatBedFileName = "GSM614003_jurkat.tal1.bed";
	private static String erythroidBedFileName = "GSM614004_erythroid.tal1.bed";
	
	
	private void fileNamesSetUp() throws DataFormatException{
		humanGenomeDir = SystemVariables.getInstance().getString("human.genome.dir");
		
		jurkatSeqFullFileName = humanGenomeDir + "mi_GSM614003_jurkat.tal1.txt";
		erythroidSeqFullFileName = humanGenomeDir + "mi_GSM614004_erythroid.tal1.txt";
		
		jurkatBedFileName = "GSM614003_jurkat.tal1.bed";
		erythroidBedFileName = "GSM614004_erythroid.tal1.bed";
	}
	
	
	/* Can be used for Jurkat or Erythroid constants only
	 * 
	 */
	public HomoSapiensRegRegionService(String positiveRegionsConstant, String negativeRegionsConstant) throws DataFormatException{
		
		
		if (positiveRegionsConstant.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Positive and negative regulatory regions should be different.");
		}
		
		if ( ! (JURKAT.equals(positiveRegionsConstant) || ERYTHROID.equals(positiveRegionsConstant) ||
			    JURKAT.equals(negativeRegionsConstant) || ERYTHROID.equals(negativeRegionsConstant) )){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. This initializer can only be used for Jurkat or Erythroid sequences.");
		}
		
		
		// Start initializing
		fileNamesSetUp();
		positiveRegRegions = getRegRegions(positiveRegionsConstant);
		negativeRegRegions = getRegRegions(negativeRegionsConstant);
	}
	
	/* Use this one if plain synthetic data is required (i.e. no synthetic with PWM)
	 * 
	 */
	public HomoSapiensRegRegionService(String positiveRegionsConstant, String negativeRegionsConstant, 
			int negExMultiplicationFactor) throws DataFormatException{
		
		
		if (positiveRegionsConstant.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Positive and negative regulatory regions should be different.");
		}
		
		if (!SYNTHETIC_PLAIN.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. This initializer can only be used for Plain Synthetic negative sequences.");
		}
		
		// Start initializing
		fileNamesSetUp();
		positiveRegRegions = getRegRegions(positiveRegionsConstant);
		negativeRegRegions = FeaturesTools.generateSimulatedRegulatoryRegions(positiveRegRegions, negExMultiplicationFactor, "simul_");
		
	}
	
	
	/* Use this one if synthetic with plain planted PWMs data is required
	 * By "plain" we mean that the probability of the hit
	 */
	public HomoSapiensRegRegionService(String positiveRegionsConstant, String negativeRegionsConstant, int negExMultiplicationFactor,
					RegulatoryElementService regElService) throws DataFormatException{
		
		
		if (positiveRegionsConstant.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Positive and negative regulatory regions should be different.");
		}
		
		if (!SYNTHETIC_WITH_PSSM.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. This initializer can only be used for Synthetic with PSSM negative sequences.");
		}

		// Start initializing
		fileNamesSetUp();
		positiveRegRegions = getRegRegions(positiveRegionsConstant);
		
		negativeRegRegions = FeaturesTools.generateSimulatedRegulatoryRegionsWithPSSMs(negExMultiplicationFactor,
				positiveRegRegions, regElService);
		
		
	}
	/* Use this one if plain synthetic with planted PWMs data is required
	 * 
	 */
	public HomoSapiensRegRegionService(String positiveRegionsConstant, String negativeRegionsConstant, 
			int negExMultiplicationFactor, String tempJobDir, File pwmDir) throws DataFormatException{
		
		if (positiveRegionsConstant.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Positive and negative regulatory regions should be different.");
		}
		
		if (!SYNTHETIC_WITH_POSITIONAL_PSSM.equals(negativeRegionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. This initializer can only be used for Synthetic with Positional PSSM negative sequences.");
		}
		
		// Start initializing
		fileNamesSetUp();
		
		positiveRegRegions = getRegRegions(positiveRegionsConstant);		
			
		int seqLen = positiveRegRegions.get(9).getSequence().length(); //just a random pick of 9
		double[] transitionMatrix = new double[]{0.25, 0.25, 0.25, 0.25};

		negativeRegRegions = FeaturesTools.generateSimulatedRegulatoryRegionsWithPositionalPSSMs(
				positiveRegRegions.size(), seqLen, transitionMatrix, SystemVariables.getInstance().getPositivePatserCutOffScore(),
				"simul_", positiveRegRegions, new PatserRegElementService(pwmDir, tempJobDir)
		);

		
		
		
	}
	
	private ArrayList<Feature>  getRegRegions(String regionsConstant) throws DataFormatException{
		File jurFile = new File(jurkatSeqFullFileName);
		File eryFile = new File(erythroidSeqFullFileName);
		
		ArrayList<Feature> regRegions;
		
		//*** Set positive regulatory regions
		if (JURKAT.equals(regionsConstant)){
			if (jurFile.exists()){
				regRegions = FeaturesTools.readRegRegionsFromFile(jurFile);
			} else {
				regRegions = this.generateRegulatoryRegions(humanGenomeDir + jurkatBedFileName, "jur_");	
				FeaturesTools.writeRegRegionsToFile(regRegions, jurFile);
			}
		} else if (ERYTHROID.equals(regionsConstant)){
			if (eryFile.exists()){
				regRegions = FeaturesTools.readRegRegionsFromFile(eryFile);
			} else {
				regRegions = this.generateRegulatoryRegions(humanGenomeDir + erythroidBedFileName, "ery_");
				FeaturesTools.writeRegRegionsToFile(regRegions, eryFile);
			}
			
		} else if (SYNTHETIC_PLAIN.equals(regionsConstant) || SYNTHETIC_WITH_PSSM.equals(regionsConstant) || SYNTHETIC_WITH_POSITIONAL_PSSM.equals(regionsConstant)){
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Generated sequences can only be used in negative examples.");
			
		} else {
			throw new DataFormatException("Failed to initialize HomoSapiensRegRegionService. Unknown constant.");
		}
		
		return regRegions;
	}
	
	
	
	private ArrayList<Feature> generateRegulatoryRegions(String bedFileName, String seqPrefix)  throws DataFormatException {
		// Data is assembled in steps.
		//
		// First we get chromosome number, sequence coordinated and sequence score 
		//		from the .bed files, supplied in GEO (from the article)
		// Then we extract actual sequences from the human genome files, using above info.
		//		Human genome is hg18 and downloaded from UCSC browser (same as in article)
		
		
		// *** Get info from the .bed files
		
		ArrayList<Feature> regRegions = new ArrayList<Feature>();
		
		regRegions = extractBedFileInfo(regRegions, bedFileName, seqPrefix); 
		
		FastaExtractor fe = new FastaExtractor(humanGenomeDir + "chromFa/");
		regRegions = fe.extractSubSequence(regRegions);
		
		return regRegions;
	}

	private ArrayList<Feature> extractBedFileInfo(ArrayList<Feature> regRegions, String bedFileName, String seqPrefix) throws DataFormatException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(bedFileName));
			String line;
			int seqCount = 1;
			while ((line = br.readLine()) != null){
				if (!line.isEmpty()){
					String[] params = line.split("[\\s\\t]");
					regRegions.add(new Feature(seqPrefix + seqCount++, Feature.TYPE_REGULATORY_REGION, params[0], 
							Integer.parseInt(params[1]), Integer.parseInt(params[2]), "", Integer.parseInt(params[3])));
					
					//TODO: remove when done testing
					//if (seqCount > 40) break; 
				}
			}
		
			br.close();
		}catch (NumberFormatException e){
			throw new DataFormatException("Can not parse data in " + jurkatBedFileName + " file.");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return regRegions;
	}


	@Override
	public ArrayList<Feature> getPositiveRegulatoryRegions() throws DataFormatException {
		return positiveRegRegions;
	}
	@Override
	public void setPositiveRegulatoryRegions(ArrayList<Feature> regRegions) {
		positiveRegRegions = regRegions;
	}
	
	@Override
	public ArrayList<Feature> getNegativeRegulatoryRegions() {
		return negativeRegRegions;
	}
	@Override
	public void setNegativeRegulatoryRegions(ArrayList<Feature> regRegions) {
		negativeRegRegions = regRegions;
	}


	@Override
	public void updateNumberOfPositiveRegRegions(int num) throws DataFormatException {
		
		throw new DataFormatException("Can not update the number of positive Homo sapiens regulatory regions. " +
				"The regions are read from a file.");
	}

	@Override
	public void updateNumberOfNegativeRegRegions(int multiplicationFactor) throws DataFormatException{
		
		throw new DataFormatException("Can not update number of negative Homo sapiens regulatory regions. " +
				"The regions are read from a file.");
	}

}
