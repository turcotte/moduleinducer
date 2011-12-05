package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;

public class FileHandling {
	
	/* Recursively deletes all files/directories inside a directory and the directory itself
	 */
	public static boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	
	    return( path.delete() );
	    
	}
	
	public static boolean deleteDirectory(String path) {
		File dir = new File(path);
		return deleteDirectory(dir);
	}
	
	/* Creates a temp output dir. Dir name will have a standard prefix
	 * 
	 */
	public static String createDirectory(String dirName) throws DataFormatException{
		
		File dir = new File(dirName);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if (!success) {
				//TODO: Production: use the commented exception below
				throw new DataFormatException("Could not create a temporary directory "+ dirName);
				//throw new DataFormatException("Could not create a temporary directory for the job.");
			}
		}
		
		return dirName;
	}
	
	/* Writes a string to a file
	 * @param fileName - file name with the full path
	 * @param fileContents - string to write
	 */
	public static void writeFile(String fileName, String fileContents){
		BufferedWriter bw = null;
		
		try {
			
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(fileContents);
			
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static String getDefaultTempIlpOutputDirectoryName() throws DataFormatException{
		return SystemVariables.getInstance().getString("temp.output.dir") + 
						SystemVariables.getInstance().getString("job.tmp.output.dir.prefix") + System.currentTimeMillis() + "/";
	}
	
	public static String getDefaultTempWebIlpOutputDirectoryName() throws DataFormatException{
		return System.getProperty("catalina.base") + SystemVariables.getInstance().getString("path.to.work.from.catalina") + 
						SystemVariables.getInstance().getString("job.tmp.output.dir.prefix") + System.currentTimeMillis() + "/";
	}
	
	public static String getDefaultTempPatserOutputDirName(String tempJobDirName) throws DataFormatException{
		if (!tempJobDirName.endsWith("/") || tempJobDirName.endsWith("\\")){
			tempJobDirName = tempJobDirName + "/";
		}
		return 	tempJobDirName + SystemVariables.getInstance().getString("job.tmp.output.dir.prefix") +
					SystemVariables.getInstance().getString("patser.tmp.dir.name.prefix") + System.currentTimeMillis() + "/";
	}
	
	public static String getDefaultTempMemeOutputDirName(String tempJobDirName) throws DataFormatException{
		if (!tempJobDirName.endsWith("/") || tempJobDirName.endsWith("\\")){
			tempJobDirName = tempJobDirName + "/";
		}
		return 	tempJobDirName + SystemVariables.getInstance().getString("job.tmp.output.dir.prefix") +
					SystemVariables.getInstance().getString("meme.tmp.dir.name.prefix") + System.currentTimeMillis() + "/";
	}
	
	public static String getDefaultTempPatserPwmDirName(String patserOutputDir) throws DataFormatException{
		if (!patserOutputDir.endsWith("/") || patserOutputDir.endsWith("\\")){
			patserOutputDir = patserOutputDir + "/";
		}
		
		return 	patserOutputDir + SystemVariables.getInstance().getString("job.tmp.output.dir.prefix") +
					SystemVariables.getInstance().getString("patser.tmp.pwm.output.dir.name") ;
	}
	
	public static String createTempIlpOutputDirectory() throws DataFormatException{
		String tmpDirName = getDefaultTempIlpOutputDirectoryName();
		
		return createDirectory(tmpDirName);
	}
	
	public static String createTempWebIlpOutputDirectory() throws DataFormatException{
		String tmpDirName = getDefaultTempWebIlpOutputDirectoryName();

		return createDirectory(tmpDirName);
	}
	
	/* Creates an index.html file, which tells that the results will be there shortly.
	 * Also creates a header and footer files for the script to re-write the index.html
	 * when the results are ready.
	 * @param jobDir - directory created for the job, where the results will be written; 
	 * 				  should end with "/"
	 * @return name of the result page (without full path)
	 */
	public static String createPreliminaryResultsWebPage(String jobDir) throws DataFormatException {
		
		String htmlResultsFileName = jobDir + SystemVariables.getInstance().getString("html.results.file.name");
		String htmlHeaderFileName = jobDir + SystemVariables.getInstance().getString("html.header.file.name");
		String htmlFooterFileName = jobDir + SystemVariables.getInstance().getString("html.footer.file.name");
		
		
		//** Write initial htlm results page
		writeFile(htmlResultsFileName, getHTMLResultsHeader() + getHTMLResultsTempBody() +getHTMLResultsFooter());
		//writeInitialResultsPage(htmlResultsFileName);
		
		
		//** Write header and footer files for a script to display final result
		writeFile(htmlHeaderFileName, getHTMLResultsHeader());
		writeFile(htmlFooterFileName, getHTMLResultsFooter());
		
		return SystemVariables.getInstance().getString("html.results.file.name");
	}
	
	/* Creates a directory for the pwms in the temporary job directory specified.
	 */
	public static String createTempPwmDirectory(String tempPatserOutputDir) throws DataFormatException{
		String tmpPwmDirName =  getDefaultTempPatserPwmDirName(tempPatserOutputDir);
		
		return createDirectory(tmpPwmDirName);
	}
	
	public static String createTempPatserOutputDirectory(String tempJobDir) throws DataFormatException{
		String tmpDirName =  getDefaultTempPatserOutputDirName(tempJobDir);
		
		return createDirectory(tmpDirName);
	}
	
	
	public static String createTempMemeOutputDirectory(String tempJobDir) throws DataFormatException{
		String tmpDirName =  getDefaultTempMemeOutputDirName(tempJobDir);
		
		return createDirectory(tmpDirName);
	}
	
	
	private static String getHTMLResultsHeader(){
		String str = "<html><head>	<title>Module Inducer: Results</title>	<meta http-equiv=\"Refresh\" content=\"5\" /></head><body><div style=\"border-style:solid; border-width:1px; border-color: #348017;  font-family: Verdana, Geneva, sans-serif; text-align: center; font-size: 9pt;\">" +
				"	<h2 style=\"text-align: center;  font-family: Verdana, Geneva, sans-serif; font-size: 13pt;\">Module Inducer</h2>	Extract knowledge from biological data.<br/>	Oksana Korol and Marcel Turcotte, University of Ottawa	<br/>&nbsp;</div>	" +
				"<div style=\"margin: 2em 2em 2em 2em; font-family: Verdana, Geneva, sans-serif; \"><pre>";
		
		return str;
	}
	
	private static String getHTMLResultsTempBody(){
		String str = "<br/>Your results will appear on this page. <br/><br/>" +
				"The time that it will take for the results to appear can vary greatly: <br/>" +
				"from several minutes (for small number of sequences with supplied motif PSSMs) <br/>" +
				"to more than a day, if the dataset is large and/or motifs need to be discovered. <br/><br/>" +
				"Please save the link to this page to return to it later. <br/><br/>" +
				"Note: if the results do not appear in more than a day, your data might be too <br/>" +
				"large to run through the web interface. Please contact okoro103 at uottawa.ca or <br/>" +
				"turcotte at eecs.uottawa.ca to arrange for an off-line run.<br/>";
		
		return str;
	}
	
	private static String getHTMLResultsFooter(){
		String str = "</pre></div></body></html>";
		
		return str;
	}
	
	
	public static boolean writeLogFile(String fileName) throws IOException, DataFormatException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(SystemVariables.getInstance().getString("temp.output.dir") + fileName ));
		writer.write(""); 
		writer.close();
		
		return true;
	}
	
	public static boolean writeLogFile(String fileName, String info) throws IOException, DataFormatException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(SystemVariables.getInstance().getString("temp.output.dir") + fileName ));
		writer.write(info); 
		writer.close();
		
		return true;
	}
	
	
	public static boolean fileContains(String fileName, String containingSequence) throws DataFormatException{
		
		File file = new File(fileName);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while ((line = br.readLine()) != null){
				if (line.contains(containingSequence)) return true;
			}
		
			br.close();
		}catch (NumberFormatException e){
			throw new DataFormatException("Can not parse data in " + file.getAbsolutePath() + " file.");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	


}
