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

public class PatserRegElementService implements RegulatoryElementService {
	
	private String patserInstallDirName = SystemVariables.getInstance().getString("patser.install.dir"); 
	private String matrixFilesDir; 
	private String tempPatserOutputDir;
	private Hashtable<String, Double> pssmMatchingStats; //keeps track of number of PSSM matches in all sequences / by # of sequences
	private Hashtable<String, int[][]> pssms; // hashed by name
	
	
	/*
	 * @param pwmDir	name of the directory, which contains one or more pwm files
	 */
	public PatserRegElementService(File pwmDir, String tempJobDir) throws DataFormatException{
		this.tempPatserOutputDir = FileHandling.createTempPatserOutputDirectory(tempJobDir);
		this.matrixFilesDir = pwmDir.getAbsolutePath(); //TODO
	}
	
	/* Parses a string with multople PWMs and writes each pwm in an individual, Patser-approved file 
	 * in a standard pwm directory, created inside a temporary directory specified.
	 * 
	 * @param pmws	String with one or more PWMs. The format of the String is:
	 * 				> pmwName
	 * 				A | 10 20 1		 or		A [10 20 1]
	 * 				C | 0 22 ...			C [0 22 ...]
	 * 
	 * @param tempJobDir	name of a temporary directory for the whole Module Inducer run. 
	 * 						A new directory will be created inside this directory to hold 
	 * 						all the pwm files.
	 */
	public PatserRegElementService(String pwms, String tempJobDir) throws DataFormatException, IOException{		
		
		this.tempPatserOutputDir = FileHandling.createTempPatserOutputDirectory(tempJobDir);
		
		this.matrixFilesDir = FileHandling.createTempPwmDirectory(tempPatserOutputDir);
		savePwmFiles(matrixFilesDir, pwms);
		//matrixFilesDir = (new File(matrixFilesDir)).getAbsolutePath(); //TODO stupid windows hack
	}
	
	
	private String createSequencesFile(ArrayList<Feature> regRegions) throws DataFormatException{
		if (regRegions == null){
			throw new DataFormatException("Can not create regulatory sequences file for PATSER. No regulatory regions were supplied."); 
		}
		
		String seqFileName = tempPatserOutputDir + SystemVariables.getInstance().getString("patser.tmp.seq.output.file.name")
										 + System.currentTimeMillis();
		
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(seqFileName));
			
			for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
				Feature gene = (Feature) iterator.next();
				
				writer.write(gene.getId() + " \\" + gene.getSequence() + "\\\n"); 
				
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return seqFileName;
		
	}
	
	/* Finds regulatory elements in regulatory sequences
	 * @param regRegions - list of regulatory regions in which to look for the reg. elements
	 * @param cutOffScore - minimum score cut off (-ls option in Patser). Matches with lower
	 * 						score will not be accepted
	 */
	public ArrayList<Feature> getRegulatoryElements(ArrayList<Feature> regRegions, double cutOffScore, double atComposition, double cgComposition) throws DataFormatException {
		/* Verified:
		 * When Patser matches an "R" sequence (reverse complement), it reports a start position in the 
		 * original sequence of the reverse complement of a PSSM.
		 */
		
		///// Patser parameters :
		//double cutOffScore = SystemVariables.getInstance().getString("");
		
		ArrayList<Feature> tfbsHits = new ArrayList<Feature>();

		// *** Write genes into file acceptable by PATSER
		final String seqFileName = createSequencesFile(regRegions);

		// *** Get file names of all matrixes
		final String[] matrixFileNames = new File(matrixFilesDir).list();
		
		pssmMatchingStats = new Hashtable<String, Double>();

		for (int i = 0; i < matrixFileNames.length; i++) {
			// Only read files with .matrix extension
			if (!matrixFileNames[i].endsWith(SystemVariables.getInstance().getString("pwm.extension"))){
				continue;
			}
			// *** Run PATSER tool and extract tfbs info from its output
			try {
				Runtime rt = Runtime.getRuntime();
				// Process pr = rt.exec("cmd /c dir");
				//Process pr = rt.exec("pwd");
				File patserDir = new File(patserInstallDirName);
				
				Process pr;
				
				if (System.getProperty("os.name").startsWith("Mac")){ 

					final String cmd = "./patser-v3e -A a:t " + atComposition + " c:g " + cgComposition + " -b 1 -c -d1 -ls " + cutOffScore +
						" -p -s -m " + matrixFilesDir +"/"+ matrixFileNames[i] +" -f " + seqFileName; 
					
					pr = rt.exec(new String[] { "/bin/sh", "-c", cmd }, null, patserDir); 
					//pr = rt.exec(cmd);
					
					//pr = rt.exec( new String[] { "patser-v3e", "-A", "a:t 0.25 c:g 0.25", "-b", "1", "-c", "-d1", "-ls", "7", "-p", "-s", "-m", matrixFileNames[i], "-f", seqFileName } );
					
				} else { // i.e. Windows
					String patserCommand;
					//Stupid windows hack
					if (matrixFilesDir.endsWith("/")){
						patserCommand = "cmd /c patser-v3e -A a:t " + atComposition + " c:g " + cgComposition + " -b 1 -c -d1 -ls " + cutOffScore +
						" -p -s -m \"" + matrixFilesDir + matrixFileNames[i]  + "\" -f \"" + seqFileName + "\"";
					} else{
						patserCommand = "cmd /c patser-v3e -A a:t " + atComposition + " c:g " + cgComposition + " -b 1 -c -d1 -ls " + cutOffScore +
						" -p -s -m \"" + matrixFilesDir + "\\" + matrixFileNames[i]  + "\" -f \"" + seqFileName+"\"";
						
					}
					
//					String patserCommand = "cmd /c patser-v3e -A a:t 0.25 c:g 0.25 -b 1 -c -d1 -ls 7 -p -s" 
//						+ " -m " + matrixFilesDir + matrixFileNames[i]  + " -f " + seqFileName;
					
					
					pr = rt.exec(patserCommand, new String[] { "PATH=C:/cygwin/bin" }, patserDir); 
					
				}

				BufferedReader input = new BufferedReader(
						new InputStreamReader(pr.getInputStream()));

				String line = null;
				String pssmName = matrixFileNames[i].substring(0, matrixFileNames[i].length() - 7);
				int hits = 0;
				String lastGeneName = "";
				//TODO

				String word;
				// Read the input and filter out matrix hits data
				while ((line = input.readLine()) != null) {
					//System.out.println(line);

					if (line.contains("position=") && line.contains("score=")) { //result line 
						Feature regElement = new Feature("TF_binding_site"); 
						//Set tfbs name, but filter the .matrix extension
						regElement.setName(pssmName);
						//hit.setNameAttribute(matrixFileNames[i]);
						
						StringTokenizer st = new StringTokenizer(line);
						//first one is the name
						word = st.nextToken();
						regElement.setParent(word);
						
						while (st.hasMoreTokens()) {
							word = st.nextToken();
							if (word.equals("position=")) { 
								word = st.nextToken();
								if (word.endsWith("C")) { 
									regElement.setStrand("R"); 
									word = word.substring(0, word.indexOf("C")); 
								} else {
									regElement.setStrand("D"); 
								}
								regElement.setStartPosition(Integer.parseInt(word));
							} else if (word.equals("score=")) { 
								word = st.nextToken();
								regElement.setScore(Double.parseDouble(word));
							} else if (word.equals("sequence=")) { 
								word = st.nextToken();
								regElement.setSequence(word);
								// Now that we have TFBS sequence, we can set the end position of TFBS:
								regElement.setEndPosition(regElement.getStartPosition() + word.length());
							}
						} // end of tokenizer

						//This is to calculate statistics of PSSM hits
						//if (!lastGeneName.equals(regElement.getParent())){
							hits++;
							lastGeneName = regElement.getParent();
						//}

						tfbsHits.add(regElement);


					}
				} // end of reading patser input
				
				pssmMatchingStats.put(pssmName, (double)hits / regRegions.size());
				
			
				//int exitVal = pr.waitFor();
				//System.out.println("Exited with error code " + exitVal);

			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}

		}
		
		File seqFile = new File(seqFileName);
		seqFile.delete(); 
/*
		// add begin and end of sequence "fake" matches, or markers; like ^ and $ for begin and ed sequence in regEx
		// this is for ILP to be able to induce rules with begin and end of sequence (distance, etc.)
		for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
			Feature feature = (Feature) iterator.next();
			// markers are place before the sequence begins and after it ends
			tfbsHits.add(new Feature("generic_marker", "begin", feature.getId(), "D", 0, 0, 0.0));
			tfbsHits.add(new Feature("generic_marker", "end", feature.getId(), "D", feature.getSequence().length()+2, feature.getSequence().length()+2, 0.0));
		}
*/
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
	
	/* Returns PSSMs used in this Patser instance, keyed by name
	 */
	public Hashtable<String, int[][]> getPssms() throws DataFormatException{
		if (pssms == null || pssms.isEmpty()){
			pssms = readPssmsFromFileSystem();
		}
		
		return pssms;
	}
	
	/* Reads the PSSM directory (saved in this instance) with a number of files that contain PSSMs and creates a Hashmap of 
	 * these PSSMs keyed by name.
	 *  - Each pwm file should have a .matrix extension. "." are permitted in the name (i.e. ma.123.my.matrix is acceptable)
	 *  - Each file should have only one pwm in it. 
	 *  - No "> pmw.." lines accepted in a file. I.e nothing but the actual matrix should be present in a file
	 *  - pwms can be of 2 formats: A | 1 2 3... or A [ 1 2 3]...
	 *  - blank lines are permitted in the file
	 */
	private Hashtable<String, int[][]> readPssmsFromFileSystem() throws DataFormatException {
		Hashtable<String, int[][]> resultPssms = new Hashtable<String, int[][]>();
		
		String pssmLineRegEx1 = "[ACGTacgt][ \t]*\\|([ \t]*\\d+)+[ \t]*";
		String pssmLineRegEx2 = "[ACGTacgt][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*";
		
		
		// *** Get file names of all matrixes
		final String[] matrixFileNames = new File(matrixFilesDir).list();
		
		for (int i = 0; i < matrixFileNames.length; i++) {
			// Only read files with .matrix extension
			if (!matrixFileNames[i].endsWith(SystemVariables.getInstance().getString("pwm.extension"))){
				continue;
			}
			
			String pssmName = matrixFileNames[i].substring(0, matrixFileNames[i].length() - 7);
			
			BufferedReader bufferedReader = null;
		        
	        try {
	        	//for Mac
	            bufferedReader = new BufferedReader(new FileReader(matrixFilesDir +"/"+ matrixFileNames[i]));
	            
	        	//bufferedReader = new BufferedReader(new FileReader(matrixFilesDir + matrixFileNames[i]));
	            
	            
	            String line = null;   
	            int[][] pssmMatrix = null;
	            
	            int j = -1;
                while (null != (line = bufferedReader.readLine())){
                	
                	if (line.matches(pssmLineRegEx1) || line.matches(pssmLineRegEx2)){
                		
                		j++;
                		if (j > 3) throw new DataFormatException("Error parsing matrix file <" + pssmName+".matrix>. Unexpected line in the file. File should contain only one matrix.");
	                
                		line = line.replaceAll("[ACGTacgt\\|\\[\\]]", ""); //remove everything but the numbers
	                	
	                	StringTokenizer strTok = new StringTokenizer(line);
	                	
	                	int lengthOfPwm = strTok.countTokens(); 
	                	
			            if (pssmMatrix == null){
			            	pssmMatrix = new int[lengthOfPwm][4];
			            }
			            
			            if (lengthOfPwm != pssmMatrix.length) throw new DataFormatException("Error parsing matrix file <" + pssmName+".matrix>. Matrix is unbalanced.");
			            
			            int k = -1;
		                while (strTok.hasMoreElements()){
		                	k++;
		                	String token = strTok.nextToken();
		                	pssmMatrix[k][j] = Integer.parseInt(token);
		                }
		                
                	}
                }
                
                resultPssms.put(pssmName, pssmMatrix);
	            
	        } catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	                if (bufferedReader != null)
	                    bufferedReader.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
			
		}//matrix for
		
		return resultPssms;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryElementService#getRegulatoryElementsPWMs()
	 * 
	 * Reads a directory with a number of files that contain pwms and creates an ArrayList of pwm objects.
	 *  - Each pwm file should have a .matrix extension. "." are permitted in the name (i.e. ma.123.my.matrix is acceptable)
	 *  - Each file should have only one pwm in it. 
	 *  - No "> pmw.." lines accepted in a file. I.e nothing but the actual matrix should be present in a file
	 *  - pwms can be of 2 formats: A | 1 2 3... or A [ 1 2 3]...
	 *  - blank lines are permitted in the file
	 *  -
	 */
	//TODO: faze it out - now I use Hash as above. Make sure that the score
	public ArrayList<RegulatoryElementPWM> getRegulatoryElementsPWMs() throws DataFormatException {
		String pwmLineRegEx1 = "[ACGTacgt][ \t]*\\|([ \t]*\\d+)+[ \t]*";
		String pwmLineRegEx2 = "[ACGTacgt][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*";
		
		ArrayList<RegulatoryElementPWM> pwms = new ArrayList<RegulatoryElementPWM>();
		
		// *** Get file names of all matrixes
		final String[] matrixFileNames = new File(matrixFilesDir).list();
		
		for (int i = 0; i < matrixFileNames.length; i++) {
			// Only read files with .matrix extension
			if (!matrixFileNames[i].endsWith(SystemVariables.getInstance().getString("pwm.extension"))){
				continue;
			}
			
			RegulatoryElementPWM currPwmObj = new RegulatoryElementPWM();
			String tfbsName = matrixFileNames[i].substring(0, matrixFileNames[i].length() - 7);
			currPwmObj.setName(tfbsName);
			
			BufferedReader bufferedReader = null;
		        
	        try {
	        	//for Mac
	            bufferedReader = new BufferedReader(new FileReader(matrixFilesDir +"/"+ matrixFileNames[i]));
	            
	        	//bufferedReader = new BufferedReader(new FileReader(matrixFilesDir + matrixFileNames[i]));
	            
	            
	            String line = null;   
	            int[][] pwmArr = null;
	            
	            int j = -1;
                while (null != (line = bufferedReader.readLine())){
                	
                	if (line.matches(pwmLineRegEx1) || line.matches(pwmLineRegEx2)){
                		
                		j++;
                		if (j > 3) throw new DataFormatException("Error parsing matrix file <" + tfbsName+".matrix>. Unexpected line in the file. File should contain only one matrix.");
	                
                		line = line.replaceAll("[ACGTacgt\\|\\[\\]]", ""); //remove everything but the numbers
	                	
	                	StringTokenizer strTok = new StringTokenizer(line);
	                	
	                	int lengthOfPwm = strTok.countTokens(); 
	                	
			            if (pwmArr == null){
			            	pwmArr = new int[lengthOfPwm][4];
			            }
			            
			            if (lengthOfPwm != pwmArr.length) throw new DataFormatException("Error parsing matrix file <" + tfbsName+".matrix>. Matrix is unbalanced.");
			            
			            int k = -1;
		                while (strTok.hasMoreElements()){
		                	k++;
		                	String token = strTok.nextToken();
		                	pwmArr[k][j] = Integer.parseInt(token);
		                }
		                
                	}
                }
	            
                currPwmObj.setPwm(pwmArr);
	            
	        } catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	                if (bufferedReader != null)
	                    bufferedReader.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        pwms.add(currPwmObj);
			
		}//matrix for
		
		return pwms;
	}
	
	
	
	
	/* Parses and saves an input stream of several PWMs. Each PWM is saved 
	 * in separate file (for Patser)
	 * Expected input stream format: 
	 * > matrixName
	 * A | 10 20 30
	 * C | 20 0 ...
	 */
	public static void savePwmFiles(String dirName, InputStream in) throws DataFormatException, IOException{
		
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		
		String line = null;
		String pwmName = "";
		int pwmLineCount = 0;
		String pwmString = "";
		
		while ((line = input.readLine()) != null) { 
			
			if (!(line.matches("\\s*"))){ //ignore blank lines
				
				if (pwmName.isEmpty()) { // expecting and annotated line

					StringTokenizer st = new StringTokenizer(line, " \t");
					String token = st.nextToken();
					
					// Check for the line to start with ">" and read the first token after it. The rest of the line is ignored
					if (!">".equals(token)){
						throw new DataFormatException("PWM sequence is not in the correct format. Each PWM should be preceded by a line starting with \"> pwmName\"");
					}
					
					if (st.hasMoreTokens()){
						pwmName = st.nextToken();
					}else {
						throw new DataFormatException("PWM sequence is not in the correct format. PWM name should follow \">\".");
					}
					
				} else { //expecting a pwm line

					if (line.matches("[ACGT][\\t ]+\\|([\\t ]+[0-9]+)+")){
						
						pwmString = pwmString + line + "\n";
						pwmLineCount++;
						
					} else {
						throw new DataFormatException("At least one of the PWMs is not in the correct format. ");
					}
				}
				
				if (!pwmName.isEmpty() && pwmLineCount == 4){ // got all the info for writing a matrix
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(dirName + pwmName + ".matrix"));
					
					writer.write(pwmString); 
					
					writer.close();
					
					pwmName = "";
					pwmLineCount = 0;
					pwmString = "";
				
				}
				
			}
		}
		
	}
	
	
	
	public static void savePwmFiles(String dirName, String pwmsStr) throws DataFormatException, IOException{
		
		int maxPwmNum = Integer.parseInt(SystemVariables.getInstance().getString("regEl.max.pwm.num"));
		
		String line = null;
		String pwmName = "";
		int pwmLineCount = 0;
		String pwmString = "";
		StringTokenizer stMain = new StringTokenizer(pwmsStr, "\n\r");
		int currPwmNum = 0;

		while (stMain.hasMoreTokens()) {
			line = stMain.nextToken();
			line = line.trim();
			
			if (!(line.matches("\\s*"))){ //ignore blank lines
				
				if (pwmName.isEmpty()) { // expecting and annotated line

					StringTokenizer st = new StringTokenizer(line, " \t");
					String token = st.nextToken();
					
					// Check for the line to start with ">" and read the first token after it. The rest of the line is ignored
					if (!">".equals(token)){
						throw new DataFormatException("PWM sequence is not in the correct format. Each PWM should be preceded by a line starting with \"> pwmName\"");
					}
					
					if (st.hasMoreTokens()){
						pwmName = st.nextToken().trim();
					}else {
						throw new DataFormatException("PWM sequence is not in the correct format. PWM name should follow \">\".");
					}
					
				} else { //expecting a pwm line

					if (line.matches("[ACGT][\\t ]+\\|([\\t ]+[0-9]+)+")){
						
						pwmString = pwmString + line + "\n";
						pwmLineCount++;
						
					} else {
						throw new DataFormatException("At least one of the PWMs is not in the correct format. ");
					}
				}
				
				if (!pwmName.isEmpty() && pwmLineCount == 4){ // got all the info for writing a matrix
					
					currPwmNum++;
					
					if (currPwmNum > maxPwmNum){
						throw new DataFormatException("Number of regulatory elements (PWMs) has exceeded the limit of " +
								maxPwmNum + " elements.");
					}
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(dirName + pwmName + ".matrix"));
					
					writer.write(pwmString); 
					
					writer.close();
					
					pwmName = "";
					pwmLineCount = 0;
					pwmString = "";
				
				}
				
			}
		}
		
	}
	
	
	
	
	/* Parses and saves an input stream of several PWMs. Each PWM is saved 
	 * in separate file (for Patser)
	 * Expected input stream format: 
	 * > matrixName
	 * A [ 10 20 30 ]
	 * C [ 20 0 ...
	 * 
	 * Converted files should be of format A | 10 20 10...
	 */
	public static void saveAndConvertPwmFiles(String dirName, InputStream in) throws DataFormatException, IOException{
		
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		
		String line = null;
		String pwmName = "";
		int pwmLineCount = 0;
		String pwmString = "";
		
		while ((line = input.readLine()) != null) { 
			
			if (!(line.matches("\\s*"))){ //ignore blank lines
				
				if (pwmName.isEmpty()) { // expecting and annotated line
					
					if (!line.startsWith(">")){
						throw new DataFormatException("PWM sequence is not in the correct format. Each PWM should be preceded by a line starting with \"> pwmName\"");
					}
					
					line = line.substring(1);

					StringTokenizer st = new StringTokenizer(line, " \t");
					
					if (st.hasMoreTokens()){
						pwmName = st.nextToken();
					}else {
						throw new DataFormatException("PWM sequence is not in the correct format. PWM name should follow \">\".");
					}
					
				} else { //expecting a pwm line

					//if (line.matches("[ACGT][\\t ]+\\|([\\t ]+[0-9]+)+")){
						
						line = line.replace('[', '|');
						int len = line.length();
						line = line.substring(0, len - 1);
						pwmString = pwmString + line + "\n";
						pwmLineCount++;
						
					//} else {
					//	throw new DataFormatException("At least one of the PWMs is not in the correct format. ");
					//}
				}
				
				if (!pwmName.isEmpty() && pwmLineCount == 4){ // got all the info for writing a matrix
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(dirName + pwmName + ".matrix"));
					
					writer.write(pwmString); 
					
					writer.close();
					
					pwmName = "";
					pwmLineCount = 0;
					pwmString = "";
				
				}
				
			}
		}
		
	}

	@Override
	public ArrayList<Feature> getRegulatoryElements(
			ArrayList<Feature> regRegions,
			ArrayList<Feature> backgroundRegRegions, double cutOffScore)
			throws DataFormatException {
		throw new DataFormatException("This method is not applicable to Patser Service");
		
	}
	

}
