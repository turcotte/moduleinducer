package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

/* This regulatory service employs two MEME suite tools: DREME and MAST.
 * DREME tool discoveres motifs in the positive sequences, contrasted with negative sequences.
 * MAST tool locates motifs, discovered by DREME, in both positive and negative sequences.
 * 
 */
public class MemeSuiteRegElementService implements RegulatoryElementService {
	

	private String dremeOutputFileName = SystemVariables.getInstance().getString("dreme.output.file.name");
	private String memeInstallDirName = SystemVariables.getInstance().getString("meme.install.dir"); 
	private String tempMemeOutputDir;
	private Hashtable<String, Double> pssmMatchingStats; //keeps track of number of PSSM matches in all sequences / by # of sequences
	private Hashtable<String, int[][]> pssms; // hashed by name
	
	
	
	public MemeSuiteRegElementService(String tempJobDir) throws DataFormatException{		
		
		this.tempMemeOutputDir = FileHandling.createTempMemeOutputDirectory(tempJobDir);
		
	}
	
	
	private String createSequencesFile(ArrayList<Feature> regRegions) throws DataFormatException{
		if (regRegions == null){
			throw new DataFormatException("Can not create regulatory sequences file for MEME. No regulatory regions were supplied."); 
		}
		
		String seqFileName = tempMemeOutputDir + SystemVariables.getInstance().getString("meme.tmp.seq.output.file.name")
										 + System.currentTimeMillis();
		
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(seqFileName));
			
			for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
				Feature gene = (Feature) iterator.next();
				
				writer.write("> "+ gene.getId() + "\n" + gene.getSequence() + "\n\n"); 
				
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return seqFileName;
		
	}
	
	/* Finds regulatory elements in regulatory sequences
	 * @param regRegions - list of regulatory regions in which to look for the reg. elements
	 * @param backgroundRegRegions - list of regulatory regions to be used as background sequences in the search for reg. elements
	 * @param cutOffScore - minimum score cut off (-ls option in Patser). Matches with lower
	 * 						score will not be accepted
	 */
	public ArrayList<Feature> getRegulatoryElements(ArrayList<Feature> regRegions, ArrayList<Feature> backgroundRegRegions, double cutOffScore) throws DataFormatException {
		
		
		ArrayList<Feature> tfbsHits = new ArrayList<Feature>();

		// *** Write sequences into a fasta file
		final String seqFileName = createSequencesFile(regRegions);
		final String bkgrSeqFileName = createSequencesFile(backgroundRegRegions);
		
		
		pssmMatchingStats = new Hashtable<String, Double>();


		try {
			Runtime rt = Runtime.getRuntime();
			// Process pr = rt.exec("cmd /c dir");
			//Process pr = rt.exec("pwd");
			
			Process pr;
			
			if (System.getProperty("os.name").startsWith("Mac")){ 
				
				// *** Discover motifs using DREME 
				final String dremeCmd = "./dreme -p " + seqFileName + " -n " + bkgrSeqFileName + " > " + tempMemeOutputDir + dremeOutputFileName; 
//				final String dremeCmd = "./dreme -p " + seqFileName + " -n " + bkgrSeqFileName; 
				
				System.out.println(dremeCmd + "\n");
				System.out.println("tempDir:" + tempMemeOutputDir);
				
				pr = rt.exec(new String[] { "/bin/sh", "-c", dremeCmd }, null, new File(memeInstallDirName)); 

				// 0 -success
				// 1-127 - job itself called exit()
				// 129-255 - job terminated by Unix
				int exitVal = pr.waitFor();
				System.out.println("DREME exit code: " + exitVal);

				
				// *** Locate motifs (discovered by DREME) using MAST
				final String mastCmd = "./mast  " + tempMemeOutputDir + dremeOutputFileName + " " + seqFileName + " -o "+ tempMemeOutputDir + "mastTestDir"; 
				
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
			
			

			
			// *** Parse MAST output
			
			// pssmMatchingStats.put(pssmName, (double)hits / regRegions.size());
			
	
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}

		
		//File seqFile = new File(seqFileName);
		//seqFile.delete(); 

		return tfbsHits;
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
