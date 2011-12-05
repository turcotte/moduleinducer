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
					
					writer.write(">"+ gene.getId() + "\n" + gene.getSequence() + "\n\n"); 
					
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
		
		// File names are relative to  tempMemeOutputDir (providing full names causes production version (MacMini_induce) of script runs to fail due to long file names
		final String seqFileName = SystemVariables.getInstance().getString("meme.tmp.seq.output.file.name.prefix") + "Pos" + System.currentTimeMillis();
		final String bkgrSeqFileName = SystemVariables.getInstance().getString("meme.tmp.seq.output.file.name.prefix") + "Neg" + System.currentTimeMillis();
		final String dremeOutputFileName = "dreme_out/dreme.txt";;
		final String fimoOutputDir = "fimo_out" + System.currentTimeMillis() + "/";
		
		// *** Write sequences into a fasta file
		createSequencesFile(regRegions, tempMemeOutputDir + seqFileName);
		createSequencesFile(backgroundRegRegions, tempMemeOutputDir + bkgrSeqFileName);

		try {
			Runtime rt = Runtime.getRuntime();
			// Process pr = rt.exec("cmd /c dir");
			//Process pr = rt.exec("pwd");
			
			Process pr;
			
			if (System.getProperty("os.name").startsWith("Mac")){ 
				int exitVal;

				
				// *** Discover motifs using DREME 
				//		(see http://meme.sdsc.edu/meme/doc/overview.html or docs in the meme install dir)
				if (backgroundRegRegions != null) { //i.e. if we already have the results of a DREME run in a temp dir
					
					// 2>1 1>/dev/null - merge error output stream with normal output stream; then scrap normal output. Reason: 
					//		not to flood the input streams, which causes the process to run away (i.e. wait for the response from the 
					//		caller (java), which never comes)
					final String dremeCmd = memeInstallDirName + "dreme -e 100 -p " + seqFileName + " 2>1  1>/dev/null"; 
					

					System.out.println("Starting DREME execution. DREME command:\n"+ dremeCmd + "\n");
					
					// Execute in the temp job directory with relative paths to the file; providing full names causes production version (MacMini_induce)
					// to fail due to long file names
					pr = rt.exec(new String[] { "/bin/sh", "-c", dremeCmd }, null, new File(tempMemeOutputDir)); 


					// 0 -success
					// 1-127 - job itself called exit()
					// 129-255 - job terminated by Unix
					exitVal = pr.waitFor();
					System.out.println("DREME exit code: " + exitVal);
					

					if (FileHandling.fileContains(tempMemeOutputDir + dremeOutputFileName, "0 motifs with E-value < ")){
						throw new DataFormatException("DREME did not find any motifs. ");
					}
					
					
				}
				
				
				// *** Locate motifs, discovered by Dreme, using FIMO  
				//		(see http://meme.sdsc.edu/meme/doc/overview.html or docs in the meme install dir)
				
					
				// 2>1 1>/dev/null - merge error output stream with normal output stream; then scrap normal output. Reason: 
				//		not to flood the input streams, which causes the process to run away (i.e. wait for the response from the 
				//		caller (java), which never comes)
				final String fimoCmd = memeInstallDirName+ "fimo " + "-o "+ fimoOutputDir + " " + dremeOutputFileName + " " + seqFileName + " 2>1 1>/dev/null"; 
							
				System.out.println("Starting FIMO execution. FIMO command:\n" + fimoCmd+"\n");
				
				pr = rt.exec(new String[] { "/bin/sh", "-c", fimoCmd }, null, new File(tempMemeOutputDir)); 

				exitVal = pr.waitFor();
				System.out.println("FIMO exit code: " + exitVal);

				
			} else { // i.e. Windows
				throw new DataFormatException("MEME suite execution is not awailable on Windows yet.");
			}
			
			
			
//			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			String line;
//			while ((line = input.readLine()) != null) {
//				System.out.println(line);
//			}
			
			
	
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		//return parsed FIMO output
		return DataFormatter.extractRegElementFromGff(tempMemeOutputDir+fimoOutputDir + "fimo.gff");
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
