package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
				//throw new DataFormatException("Could not create a temporary directory "+ dirName);
				throw new DataFormatException("Could not create a temporary directory for the job.");
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
		writeFile(htmlResultsFileName, getHTMLResultsHeader(true) + getHTMLResultsTempBody() +getHTMLResultsFooter(false,false));
		//writeInitialResultsPage(htmlResultsFileName);
		
		
		//** Write header and footer files for a script to display final result
		writeFile(htmlHeaderFileName, getHTMLResultsHeader(false));
		writeFile(htmlFooterFileName, getHTMLResultsFooter(false, true));
		
		return SystemVariables.getInstance().getString("html.results.file.name");
	}
	
	/* Over-writes an ilp pretty print result file with error message
	 * this file is then inserted between header and footer html of the results page by a script
	 * (E.g. created when DREME returned no results) 
	 * @param jobDir - directory created for the job, where the results will be written; 
	 * 				  should end with "/"
	 * @param errorMsg - message to be written on the page
	 * @return name of the result page (without full path)
	 */
	public static void createErrorResultsWebPage(String jobDir, String errorMsg) throws DataFormatException {
		
//		String ilpPprintFileName = jobDir + SystemVariables.getInstance().getString("ilp.pprint.result.file.name");
		String fileName = jobDir + SystemVariables.getInstance().getString("html.results.file.name");
		
		String htmlMsg = "</pre> \n<p class=\"error-message\"> " +
				"ModuleInducer was unable to extract a theory from the specified data. The reason: <br/>"
				+ errorMsg + " </p>\n";
		
		htmlMsg = htmlMsg +"<pre>\n";
		
		writeFile(fileName, getHTMLResultsHeader(false) + htmlMsg + getHTMLResultsFooter(false, true));
		
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
	
	
	private static String getHTMLResultsHeader(boolean needsRefresh){
		String str = "<html><head> \n\n" +
				"<title>Module Inducer: Results</title>	";
		
		if (needsRefresh){
			str = str + "<meta http-equiv=\"Refresh\" content=\"5\" />";
		}
		
		str = str + "\n\n<style> <!--\n" +
				"		* {font-family: Verdana, Geneva, sans-serif; font-size: 9pt; }\n" +
				"		.main {width: 700px; margin: 25px 25px 25px 25px; }\n" +
				"		.title {font-size: 13pt; font-weight:bold;	}\n" +
				"		.header {background-color: white; padding: 10px; margin: 10px; border-style:solid; border-width:2px; border-color: #348017; text-align: center; }\n" +
				"		.mi_results_body {background-color: white; padding: 10px; margin: 10px;}\n" +
				"		.info-message {background-color: #F8F8F8; padding: 4px; text-align: left; }\n" +
				"		.error-message { text-align: left; padding: 10px; background-color: #FFF380}\n" +
				"		.data-entry-title {font-size: 11pt; font-weight:bold;}\n" +
				"		.help-header-text{color:#FF9900; font-weight:bold; }\n" +
				"--> </style>\n\n" +
				"</head>\n\n" +
				"<body>\n\n" +
				"<div class=\"main\">\n" +
				"<div class=\"header\"> <div class=\"title\">ModuleInducer ::</div>:: results </div>\n" +
				"<br/>\n" +
				"<div class=\"mi_results_body\">\n" +
				"<pre>\n";

		
		return str;
	}
	
	
	
	private static String getHTMLResultsTempBody(){
		String str = "<br/>Your results will appear on this page. <br/><br/>" +
				"The time that it will take for the results to appear can vary greatly: <br/>" +
				"from several minutes (for small number of sequences with supplied motif PSSMs) <br/>" +
				"to more than a day, if the dataset is large and/or motifs need to be discovered. <br/><br/>" +
				"Please save the link to this page to return to it later. <br/><br/>" +
				"Note: if the results do not appear in more than a day, your data might be too <br/>" +
				"large to run through the web interface. Please contact Module.Inducer@site.uOttawa.ca <br/>" +
				"to arrange for an off-line run.<br/>";
		
		return str;
		
	}
	
	private static String getRuleDescriptionHtml(){
		String str = "<p class=\"info-message\">\n" +
				"	<font class=\"help-header-text\">How to read the theory </font> <br/><br/>\n\n" +
				"	The theory consists of several rules that describe experiment data. You can see how significant the rule is by the number of positive (experiment) and negative (control) examples covered ([Pos cover = 12 Neg cover = 0]). In ideal case, the positive cover will be the total number of experiment sequences and the negative cover will be 0, which means that one rule describes all the experiment and none of the control sequences.<br/><br/> \n\n" +
				"	Example of reading a rule.\n\n" +
				"	<br/><br/>&nbsp;&nbsp;positive(A) <font style=\"background-color: #C3FDB8;\">:-</font><br/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;has_feature(A,m2)	<font style=\"background-color: #C3FDB8;\">, </font>before(A,m7,m12).<br/><br/>\n\n" +
				"	Sequence A is an experiment sequence<font style=\"background-color: #C3FDB8;\"> if </font>it contains a feature (biological marker or transcription factor binding site) m2 <font style=\"background-color: #C3FDB8;\">and</font> biological marker m7 is located before m12.<br/>\n" +
				"	<br/><br/>\n" +
				"	<font class=\"help-header-text\">Description of possible theory rules (m1, m2 are assumed to be biological markers):</font>\n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>has_feature(A, m1)</i> - sequence A contains m1. By contains we mean that the motif correspondig to m1 was found in A.\n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>has_feature(A, m1, 'R')</i> - sequence A contains m1 as a reverse match (see \"Other parameters\" below for more details). \n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>before(A, m1, m2)</i> - regulatory element m1 is located before m2 in sequence A.\n" +
//				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>chromosome(A, chr1)</i> - sequence A is found on the chromosome chr1. Only relevant when the chromosome information was supplied.\n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>distance_interval(A, m1, m2, 10, 2)</i> - the distance between regulatory element m1 and m2 is 10 +/- 2 nucleotides in sequence \n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>pos_lteq(A, m1, -35)</i> - m1 is located at or less than the index -35 inside sequence A. The positions inside the input sequences are numbered with 0 in the middle, increasing the indexes to the right and decreasing to the left (i.e. [ -3, -2, -1, 0, 1, 2, 3 ]). This numbering extra information on the relative location of the motif inside the sequence. This information is especially useful when analyzing ChIP-Seq data, because it shows how close the motif is located from the peak.\n\n" +
				"	<br/><br/>&nbsp;&nbsp; <b>::</b> <i>pos_gteq(A, m1, 57)</i> - m1 is located at or more than the index -35 inside sequence A (similar to above).<br/><br/>\n" +
				"	<font class=\"help-header-text\">Other parameters:</font>\n" +
				"	<br/>&nbsp;&nbsp; <b>::</b> <i>'D'</i> - the match of the biological marker was found on a direct strand (5' to 3')\n" +
				"	<br/>&nbsp;&nbsp; <b>::</b> <i>'R'</i> - the match of the biological marker was found on a reverse strand (3' to 5')\n" +
				"</p>";
		return str;
	}
	
	public static String getHTMLResultsFooter(boolean hasRuleDescription, boolean hasJobDescription) throws DataFormatException {
		String str = "</pre>\n";
		
		if (hasRuleDescription) {
			str = str + getRuleDescriptionHtml();
		}
		
		if (hasJobDescription){
			str = str + "<div class=\"data-entry-title\">Run information</div>\n" +
						getStatisticsTableHtml();
					
			String relPathToDreme = SystemVariables.getInstance().getRelativePathToDreme();
			if (relPathToDreme != null && !relPathToDreme.isEmpty()){
				str = str + "<br/><p> Results of DREME execution can be found here: <a href=\""+relPathToDreme+"\">DREME results.</a></p>\n";
			}
			
		}		
		
		
		str = str +	"</div>\n" +
				"</div>\n" +
				"</body></html>";
		
		return str;
	}
	
	
	private static String getStatisticsTableHtml() throws DataFormatException{
		String str = "";
		
		str = "<table border=\"1\" width=\"100%\">\n" +
				"	<tr>\n" +
				"		<td/>\n" +
				"		<td><strong>Experiment</strong></td>\n" +
				"		<td><strong>Control</strong></td>\n" +
				"	</tr>\n" +
				"	<tr>\n" +
				"		<td>Number of sequences</td>\n" +
				"		<td>" + SystemVariables.getInstance().getPosSeqNum() + "</td>\n" +
				"		<td>"+ SystemVariables.getInstance().getNegSeqNum() +"</td>\n" +
				"	</tr>\n" +
				"	<tr>\n" +
				"		<td>A:C composition</td>\n" +
				"		<td>" + SystemVariables.getInstance().getPosATcomposition() + "</td>\n" +
				"		<td>" + SystemVariables.getInstance().getNegATcomposition() + "</td>\n" +
				"	</tr>\n" +
				"	<tr>\n" +
				"		<td>G:C composition</td>\n" +
				"		<td>" + SystemVariables.getInstance().getPosCGcomposition() + "</td>\n" +
				"		<td>" + SystemVariables.getInstance().getNegCGcomposition() + "</td>\n" +
				"	</tr>\n" +
				"	<tr>\n" +
				"		<td>Total number of motif matches</td>\n" +
				"		<td>" + SystemVariables.getInstance().getPosSeqRelElMatchesNum() + "</td>\n" +
				"		<td>" + SystemVariables.getInstance().getNegSeqRelElMatchesNum() + "</td>\n" +
				"	</tr>\n" +
				"</table>\n";
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
