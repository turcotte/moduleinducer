package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.BufferedWriter;
import java.io.File;
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
				//TODO: Production: use the commented exception below
				throw new DataFormatException("Could not create a temporary directory "+ dirName);
				//throw new DataFormatException("Could not create a temporary directory for the job.");
			}
		}
		
		return dirName;
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
	
	
	
	


}
