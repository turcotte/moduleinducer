package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataFormatter;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

/* This regulatory service employs two MEME suite tools: DREME and MAST.
 * DREME tool discoveres motifs in the positive sequences, contrasted with negative sequences.
 * MAST tool locates motifs, discovered by DREME, in both positive and negative sequences.
 * 
 */
public class MemeSuiteRegElementService implements RegulatoryElementService {
	

	private String memeInstallDirName = SystemVariables.getInstance().getString("meme.install.dir"); 
	private String tempMemeOutputDir;
	private Hashtable<String, Double> pssmMatchingStats; //keeps track of number of PSSM matches in all sequences / by # of sequences
	
	
	
	public MemeSuiteRegElementService(String tempJobDir) throws DataFormatException{		
		
		this.tempMemeOutputDir = FileHandling.createTempMemeOutputDirectory(tempJobDir);
		
	}
	
	
	private String createSequencesFile(ArrayList<Feature> regRegions, String fileName) throws DataFormatException{
		
		if (regRegions != null){
		
			BufferedWriter writer = null;
	
			try {
				writer = new BufferedWriter(new FileWriter(fileName));
				
				for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
					Feature gene = (Feature) iterator.next();
					
					writer.write("> "+ gene.getId() + "\n" + gene.getSequence() + "\n\n"); 
					
				}
				
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return fileName;
		
	}
	
	/* Finds regulatory elements in regulatory sequences
	 * @param regRegions - list of regulatory regions in which to look for the reg. elements
	 * @param backgroundRegRegions - list of regulatory regions to be used as background sequences in the search for reg. elements
	 * @param cutOffScore - minimum score cut off (-ls option in Patser). Matches with lower
	 * 						score will not be accepted
	 */
	public ArrayList<Feature> getRegulatoryElements(ArrayList<Feature> regRegions, ArrayList<Feature> backgroundRegRegions, double cutOffScore) throws DataFormatException {
		
		final String seqFileName = tempMemeOutputDir + SystemVariables.getInstance().getString("meme.tmp.seq.output.file.name.prefix") + "Pos" + System.currentTimeMillis();
		final String bkgrSeqFileName = tempMemeOutputDir + SystemVariables.getInstance().getString("meme.tmp.seq.output.file.name.prefix") + "Neg" + System.currentTimeMillis();
		final String dremeOutputFileName = tempMemeOutputDir + SystemVariables.getInstance().getString("dreme.output.file.name");
		final String mastOutputDir = tempMemeOutputDir + "mastTestDir" + System.currentTimeMillis() + "/";
		
		// *** Write sequences into a fasta file
		createSequencesFile(regRegions, seqFileName);
		createSequencesFile(backgroundRegRegions, bkgrSeqFileName);
		

		try {
			Runtime rt = Runtime.getRuntime();
			// Process pr = rt.exec("cmd /c dir");
			//Process pr = rt.exec("pwd");
			
			Process pr;
			
			if (System.getProperty("os.name").startsWith("Mac")){ 
				int exitVal;

				
				// *** Discover motifs using DREME 
				if (backgroundRegRegions != null) { //i.e. if we already have the results of a DREME run in a temp dir
					final String dremeCmd = "./dreme -p " + seqFileName + " > " + dremeOutputFileName + " 2>/dev/null"; 
//					final String dremeCmd = "./dreme -p " + seqFileName + " > " + dremeOutputFileName; 
//					final String dremeCmd = "./dreme -p " + seqFileName + " -n " + bkgrSeqFileName + " > " + dremeOutputFileName; 
//					final String dremeCmd = "./dreme -p " + seqFileName + " -n " + bkgrSeqFileName; 
					
					//System.out.println(dremeCmd + "\n");
					System.out.println("Starting DREME execution.");
					pr = rt.exec(new String[] { "/bin/sh", "-c", dremeCmd }, null, new File(memeInstallDirName)); 
	

					// 0 -success
					// 1-127 - job itself called exit()
					// 129-255 - job terminated by Unix
					exitVal = pr.waitFor();
					System.out.println("DREME exit code: " + exitVal);
					
					if (FileHandling.fileContains(dremeOutputFileName, "0 motifs with E-value < ")){
						throw new DataFormatException("No motifs were found.");
					}
					
					
				}
				
				
				// *** Locate motifs (discovered by DREME) using MAST
				final String mastCmd = "./mast  " + dremeOutputFileName + " " + seqFileName + 
							" -o "+ mastOutputDir + " -mt 0.001" + " 2>/dev/null"; 
				
//				String mastFile = "";
//				if (backgroundRegRegions == null){
//					mastFile = "/Users/okoro103/workspace/ModuleInducer/tmp/mi_meme_ery_jur/mi_MemeOut/memeSeqNeg.txt";
//				} else {
//					mastFile = "/Users/okoro103/workspace/ModuleInducer/tmp/mi_meme_ery_jur/mi_MemeOut/memeSeqPos.txt";
//				}
//				final String mastCmd = "./mast  " + "/Users/okoro103/workspace/ModuleInducer/tmp/mi_meme_ery_jur/mi_MemeOut/dreme_manual.out " + mastFile + 
//				" -o "+ mastOutputDir; 
	
				
				//System.out.println(mastCmd + "\n");
				System.out.println("Starting MAST execution.");
				pr = rt.exec(new String[] { "/bin/sh", "-c", mastCmd }, null, new File(memeInstallDirName)); 

				exitVal = pr.waitFor();
				System.out.println("MAST exit code: " + exitVal);

				
			} else { // i.e. Windows
				throw new DataFormatException("MEME suite execution is not awailable on Windows yet.");
			}
			
			
			
//			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			String line;
//			while ((line = input.readLine()) != null) {
//				System.out.println(line);
//			}
			
			
	
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		//return parsed MAST output
		return DataFormatter.extractRegElementsFromXml(mastOutputDir + "mast.xml");
	}
	
	
	
	/* Statistics for the last Patser run. With every getRegulatoryElements() run it gets overwritten.
	 * 
	 * (non-Javadoc)
	 * @see ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService#getPssmMatchingStatistics()
	 */
	public Hashtable<String, Double> getPssmMatchingStatistics(){
		return pssmMatchingStats;
	}
	
	
	@Override
	public ArrayList<Feature> getRegulatoryElements(
			ArrayList<Feature> regSequences, double cutOffScore,
			double atComposition, double cgComposition)
			throws DataFormatException {

		//throw new DataFormatException("This method is not applicable to MEME Suite Service");
		return null;
	}


	@Override
	public Hashtable<String, int[][]> getPssms() throws DataFormatException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<RegulatoryElementPWM> getRegulatoryElementsPWMs()
			throws DataFormatException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
