package ca.uottawa.okorol.bioinf.ModuleInducer.properties;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;

/* Singleton class that manages local and global properties 
 * This class is ment to keep track of all the settings in the system while it's running
 */
public class SystemVariables {
	private static SystemVariables instance = null;
	
	private static final String BUNDLE_NAME = "ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables"; //$NON-NLS-1$
	
	private static ResourceBundle resourceBundle;	
	private static Locale currentLocale;
	private String experimentNotes; // This string will be printed in the ilp theory file
	private double positivePatserCutOffScore;  
	private double negativePatserCutOffScore;
	
	//constants
	public static final String SYNTHETIC_REG_REGION_PREFIX = "synthetic_";


	public double getPositivePatserCutOffScore() {
		return Double.parseDouble(this.getString("patser.score.cutoff.positive.examples"));
	}

	public void setPositivePatserCutOffScore(double positivePatserCutOffScore) {
		this.positivePatserCutOffScore = positivePatserCutOffScore;
	}

	public double getNegativePatserCutOffScore() {
		return Double.parseDouble(this.getString("patser.score.cutoff.negative.examples"));
	}

	public void setNegativePatserCutOffScore(double negativePatserCutOffScore) {
		this.negativePatserCutOffScore = negativePatserCutOffScore;
	}

	private SystemVariables() throws DataFormatException {
		
		//currentLocale = new Locale("Mac", "OK");
		
		if ("/Users/okoro103".equals(System.getProperty("user.home"))){ // Oksana's Mac //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("Mac", "OK");
		} else if ("/Users/induce".equals(System.getProperty("user.home"))){ // Marcel's Mac //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("Mac", "induce");			
		} else if ("/Users/oxy".equals(System.getProperty("user.home"))){ // Mac Mini //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("Mac", "mini");			
		} else if ("/Users/turcotte".equals(System.getProperty("user.home"))){ // Marcel's Mac //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("Mac", "MT");			
		} else if ("C:\\Users\\Oxy".equals(System.getProperty("user.home"))){   //Oksana's Windows //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("Win", "OK");			
		}else {
			//throw new DataFormatException("Unknown execution environment. If this is a first time execution of the program on this environment, note that you need a custom .properties file with PATSER install directories. Otherwise, check user.home environmental variable."); //$NON-NLS-1$
		}
		
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
	}

	public String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static SystemVariables getInstance() throws DataFormatException{
		if (instance == null){
			instance = new SystemVariables();
		}
		return instance;
	}
	
	public String getExperimentNotes() {
		return experimentNotes;
	}

	public void setExperimentNotes(String experimentNotes) {
		this.experimentNotes = experimentNotes;
	}
	public void appendToExperimentNotes(String extraNotes) {
		experimentNotes = experimentNotes + extraNotes;
	}
	
}
