package ca.uottawa.okorol.bioinf.ModuleInducer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.CElegansRegRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.Experimenter;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.Explorer;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.PatserRegElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

public class MainEngine {


	/* This method currently substitutes for UI. 
	 * I.e. all the application modules are plugged in here
	 * for manual testing 
	 */
	public static void main(String[] args) {
		/* General way of how to run Module Inducer
		 * 
		 * Following input is required:
		 * 	- Positive and/or negative sequences. These are the main data in RegulatoryRegionService interface. Some pre-filled classes are available (like C.elegans, Synthetics, etc.)
		 * 	- PWMs. Could either be supplied as a string, or  saved in a directory. PatserRegElService is the only implementation of this RegulatoryElementService interface.
		 * 	- temporary directory, where all the temporary files will be written. Use FileHandling.createTempIlpOutputDirectory() and pass it to all the objects of the run.
		 * 
		 * Template:
		 * 	- create a temp directory (FileHandling.createTempIlpOutputDirectory());
		 * 	- create a RegulatoryRegionService (note, factory is not advisable, use direct implementations, like CustomRegRegionService, etc)
		 *  - create a RegulatoryElementService (just PatserRegElementService for now)
		 *  - create an Explorer (run induceRules() to get ILP result)
		 *  - delete a temp directory (Note that Patser files are deleted right after Patser execution, but ILP files will remain in the temp directory)
		 * 
		 */
		try {
			
			long timeBefore, timeAfter;
			String tempOutputDir;
			


			
			// *** Homo Sapiens data

	
			//Params of the run
			File pwmDir = new File(SystemVariables.getInstance().getString("homo.sapiens.PWMs.dir"));
			int negExMultFactor = 1; 
			
			
			/*		
			/////////////////////////////////////////////////// Run 1 //////////////////////////////
			
			System.out.println("===================================================================\n	Starting run: Jurkat vs erythroid.");

			timeBefore = System.currentTimeMillis();

			tempOutputDir = FileHandling.createTempIlpOutputDirectory();

			
			System.out.println("== All the data / scripts from this run will be saved in " + tempOutputDir + " directory.");

			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);

//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Jurkat vs Synthetic plain" ); 			
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.JURKAT, HomoSapiensRegRegionService.SYNTHETIC_PLAIN, negExMultFactor);
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Jurkat vs Synthetic with planted PSSMs" ); 
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.JURKAT, HomoSapiensRegRegionService.SYNTHETIC_WITH_PSSM,
//					negExMultFactor, patserRegElService);
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Jurkat vs Synthetic with positionally planted PSSMs" ); 
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.JURKAT, HomoSapiensRegRegionService.SYNTHETIC_WITH_POSITIONAL_PSSM,
//					negExMultFactor, tempOutputDir, pwmDir);
//			
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Erythroid vs Synthetic plain" ); 			
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.ERYTHROID, HomoSapiensRegRegionService.SYNTHETIC_PLAIN, negExMultFactor);
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Erythroid vs Synthetic with planted PSSMs" ); 
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.ERYTHROID, HomoSapiensRegRegionService.SYNTHETIC_WITH_PSSM,
//					negExMultFactor, patserRegElService);
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Erythroid vs Synthetic with positionally planted PSSMs" ); 
//			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
//					HomoSapiensRegRegionService.ERYTHROID, HomoSapiensRegRegionService.SYNTHETIC_WITH_POSITIONAL_PSSM,
//					negExMultFactor, tempOutputDir, pwmDir);
			
			
			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Jurkat vs Erythroid" ); 
			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
					HomoSapiensRegRegionService.JURKAT, HomoSapiensRegRegionService.ERYTHROID);
		

			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getPositiveRegulatoryRegions(), tempOutputDir + "positiveSequences_ModuleInducer.fasta");
			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getNegativeRegulatoryRegions(), tempOutputDir + "negativeSequences_ModuleInducer.fasta");
			System.out.println("== Positive and negative sequences have been written in a temp directory.");
			
			
//			String ebox_gata_regex = "[c,C][a,A]..[t,T][g,G].{8,10}[g,G][a,A][t,T][a,A][a,A]";
//			String ebox_gata_revCompl_regex = "[t,T][t,T][a,A][t,T][c,C].{8,10}[c,C][a,A]..[t,T][g,G]";
//			
//			System.out.println("Number of times CA..TG.{8,10}GATA expression is found in positive sequences: " +
//					FeaturesTools.getRegexMatchNumber(ebox_gata_regex, homoSapiensRegRegionService.getPositiveRegulatoryRegions())
//					//+ FeaturesTools.getRegexMatchNumber(ebox_gata_revCompl_regex, homoSapiensRegRegionService.getPositiveRegulatoryRegions()))		
//			);
//
//			System.out.println("Number of times CA..TG.{8,10}GATA expression is found in negative sequences: " +
//					FeaturesTools.getRegexMatchNumber(ebox_gata_regex, homoSapiensRegRegionService.getNegativeRegulatoryRegions())
//					//+ FeaturesTools.getRegexMatchNumber(ebox_gata_revCompl_regex, homoSapiensRegRegionService.getNegativeRegulatoryRegions()))
//					
//			);
			
			
			timeAfter = System.currentTimeMillis();
			//System.out.println("== Got regulatory regions. Time so far (sec): " + (timeAfter - timeBefore) / 1000);
			
			Explorer explorer = new Explorer(homoSapiensRegRegionService, patserRegElService, tempOutputDir);
			//Experimenter experimenter = new Experimenter(explorer);
			
			String theory = explorer.induceRules();
			//System.out.println(theory);
			//DataFormatter.extractTheoryAndPerformance(theory);
			
			
			//FileHandling.deleteDirectory(tempOutputDir);
			//System.out.println("== Temporary directory deleted (no re-runs of this job is possible).");
			
			timeAfter = System.currentTimeMillis();
			System.out.println("== Total execution time (sec): " + (timeAfter - timeBefore) / 1000);
			System.out.println("== Run finished. Results in directory: "+tempOutputDir);

	 		
			///////////////////////////// RUN 2 ///////////////////////////////
			
			System.out.println("===================================================================\n	Starting run: Jurkat vs positionally planted PSSMs.");

			timeBefore = System.currentTimeMillis();

			tempOutputDir = FileHandling.createTempIlpOutputDirectory();

			
			System.out.println("== All the data / scripts from this run will be saved in " + tempOutputDir + " directory.");

			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);

			
			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Jurkat vs Synthetic with positionally planted PSSMs" ); 
			HomoSapiensRegRegionService homoSapiensRegRegionService = new HomoSapiensRegRegionService(
					HomoSapiensRegRegionService.JURKAT, HomoSapiensRegRegionService.SYNTHETIC_WITH_POSITIONAL_PSSM,
					negExMultFactor, tempOutputDir, pwmDir);
			

			
			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getPositiveRegulatoryRegions(), tempOutputDir + "positiveSequences_ModuleInducer.fasta");
			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getNegativeRegulatoryRegions(), tempOutputDir + "negativeSequences_ModuleInducer.fasta");
			System.out.println("== Positive and negative sequences have been written in a temp directory.");
			
			
			Explorer explorer = new Explorer(homoSapiensRegRegionService, patserRegElService, tempOutputDir);
			
			explorer.induceRules();
			
		
			timeAfter = System.currentTimeMillis();
			System.out.println("== Total execution time (sec): " + (timeAfter - timeBefore) / 1000);
			System.out.println("== Run finished. Results in directory: "+tempOutputDir);
			
			
			
///////////////////////////// RUN 3 ///////////////////////////////
			
			System.out.println("===================================================================\n	Starting run: Erythroid vs positionally planted PSSMs.");

			timeBefore = System.currentTimeMillis();

			tempOutputDir = FileHandling.createTempIlpOutputDirectory();

			
			System.out.println("== All the data / scripts from this run will be saved in " + tempOutputDir + " directory.");

			patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);

			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: Erythroid vs Synthetic with positionally planted PSSMs" ); 
			homoSapiensRegRegionService = new HomoSapiensRegRegionService(
					HomoSapiensRegRegionService.ERYTHROID, HomoSapiensRegRegionService.SYNTHETIC_WITH_POSITIONAL_PSSM,
					negExMultFactor, tempOutputDir, pwmDir);
			
			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getPositiveRegulatoryRegions(), tempOutputDir + "positiveSequences_ModuleInducer.fasta");
			FeaturesTools.printFeaturesToFile(homoSapiensRegRegionService.getNegativeRegulatoryRegions(), tempOutputDir + "negativeSequences_ModuleInducer.fasta");
			System.out.println("== Positive and negative sequences have been written in a temp directory.");
			
			
			 explorer = new Explorer(homoSapiensRegRegionService, patserRegElService, tempOutputDir);
			
			explorer.induceRules();
			
		
			timeAfter = System.currentTimeMillis();
			System.out.println("== Total execution time (sec): " + (timeAfter - timeBefore) / 1000);
			System.out.println("== Run finished. Results in directory: "+tempOutputDir);
			
			
		*/	
			
			
			

			// *** C. elegans data
			timeBefore = System.currentTimeMillis();
			
			tempOutputDir = FileHandling.createTempIlpOutputDirectory();
			System.out.println("== All the data / scripts from this run will be saved in " + tempOutputDir + " directory.");
			
			
			pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);
			
//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: C.elegans vs Synthetic plain" ); 
//			CElegansRegRegionService cElegansRegRegionService = new CElegansRegRegionService(1); 

//			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: C.elegans vs Synthetic with planted tfbs" ); 
//			CElegansRegRegionService cElegansRegRegionService = new CElegansRegRegionService(1, patserRegElService);

			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: C.elegans vs Synthetic with positionally planted tfbs" ); 
			CElegansRegRegionService cElegansRegRegionService = new CElegansRegRegionService(1, tempOutputDir, pwmDir);
			
			
			Explorer explorer = new Explorer(cElegansRegRegionService, patserRegElService, tempOutputDir);
			//Experimenter experimenter = new Experimenter(explorer);

			System.out.println("\n\n************************ Single run of all data ***************************\n");
			String theory = explorer.induceRules();
			System.out.println(theory);
		
	
			
			//FileHandling.deleteDirectory(tempOutputDir);	
			//System.out.println(SystemVariables.getInstance().getExperimentNotes());
			
			 
			
			/*
			
			System.out.println("\n\n************************ 5 by 2  simple***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.fiveByTwoTest();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
			
			System.out.println("\n\n************************ 5 by 2 planted***************************\n");
			
			SystemVariables.getInstance().appendToExperimentNotes("\\n* Data types: C.elegans vs Synthetic with planted tfbs" ); 
			cElegansRegRegionService = new CElegansRegRegionService(1, patserRegElService);

			explorer = new Explorer(cElegansRegRegionService, patserRegElService, tempOutputDir);
			experimenter = new Experimenter(explorer);

	
			timeBefore = System.currentTimeMillis();
			experimenter.fiveByTwoTest();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			*/
			
			/*
			
			System.out.println("\n\n************************ k-fold ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.kFoldCrossValidation();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			 */
			
			/*
			timeAfter = System.currentTimeMillis();
			System.out.println("== Total execution time (sec): " + (timeAfter - timeBefore) / 1000);
			*/
			
			
			
			// *** Synthetic data	
			
			/*	
			
			tempOutputDir = FileHandling.createTempIlpOutputDirectory();
			SyntheticRegRegionService syntheticRegRegionService =  new SyntheticRegRegionService(300, 1);
			File pwmDir = new File(SystemVariables.getInstance().getString("test.PWMs.dir"));
			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);
			
			Explorer explorer = new Explorer(syntheticRegRegionService, patserRegElService, tempOutputDir);
			Experimenter experimenter = new Experimenter(explorer);
			
			
			 
			
			
			System.out.println("\n\n************************ 5 by 2 synthetic ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.fiveByTwoTest();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
			 
				
			System.out.println("\n\n************************ k-fold synthetic ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.kFoldCrossValidation();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
 			
		

			System.out.println("\n\n************************ leaveOneOutTest synthetic ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.leaveOneOutTest();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
			
			System.out.println("\n\n************************ increaseNegExamplesTest synthetic ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.increaseNegExamplesTest();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
			 */

			//experimenter.rule_before_InductionTest(100, 20, 60);


	/*		
			timeBefore = System.currentTimeMillis();
			
			System.out.println("\n\n************************ before experiment ***************************\n");

			tempOutputDir = FileHandling.createTempIlpOutputDirectory();
//			SyntheticRegRegionService syntheticRegRegionService =  new SyntheticRegRegionService(20, 1);
//			File pwmDir = new File(SystemVariables.getInstance().getString("test.PWMs.dir"));
//			PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tempOutputDir);
//			
//			Explorer explorer = new Explorer(syntheticRegRegionService, patserRegElService, tempOutputDir);
//			Experimenter experimenter = new Experimenter(explorer);
			
			experimenter.rule_before_InductionTest(200, 600, 600);
			
			timeAfter = System.currentTimeMillis();
			System.out.println("== Total execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
		*/	
			
			// *** C. elegans data
				
			/*
			
			RegulatoryRegionService cElegansRegRegionService = RegulatoryRegionServiceFactory.getService(
					RegulatoryRegionService.C_ELEGANS_DATA_SERVICE, 0, 20);
			RegulatoryElementService patserRegElService = RegulatoryElementServiceFactory.getService(
					RegulatoryElementService.PATSER_SERVICE, SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
			Explorer explorer = new Explorer(cElegansRegRegionService, patserRegElService);
			Experimenter experimenter = new Experimenter(explorer);
			
			
			System.out.println("\n\n************************ WEKA data run ***************************\n");
			timeBefore = System.currentTimeMillis();
			experimenter.wekaTest1();
			timeAfter = System.currentTimeMillis();
			System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
			
			
			ArrayList<Feature> posSeq = explorer.getRegulatoryElementService()
					.getRegulatoryElements(cElegansRegRegionService.getRegulatoryRegions());
			
			ArrayList<Feature> negSeq = explorer.getRegulatoryElementService()
					.getRegulatoryElements(cElegansRegRegionService.getSimulatedRegulatoryRegions());
			
			WekaService wekaService = new WekaService();
			
			wekaService.createWekaFile(posSeq, negSeq, 7.5, 7.5);
			
			System.out.println("Done.");
			*/



		} catch (DataFormatException e) {
			e.printStackTrace();
		}

	}
	
	public static void printRegRegionFeatures(ArrayList<Feature> features){
		for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			
			System.out.println(feature.getId() + "\t" + feature.getType() + "\t" + feature.getSequence());
		}
	}
	
	public static void printRegElementFeatures(ArrayList<Feature> hits, double minScore){
		for (Iterator<Feature> iterator = hits.iterator(); iterator.hasNext();) {
			Feature hit = (Feature) iterator.next();
			if (hit.getScore() > minScore){
				System.out.println("tfbs(" + hit.getName() + ", '" + hit.getParent() + "', " +
					hit.getStartPosition() + ", '" + hit.getStrand() + "', " + hit.getScore() + ").");
			}
		}
	}
	


}

