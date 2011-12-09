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
	
	// execution parameters
	private double positivePatserCutOffScore;  
	private double negativePatserCutOffScore;
	
	// statistics and results variables
	private String experimentNotes; // This string will be printed in the ilp theory file
	private String relativePathToDreme;
	private int posSeqNum;
	private int negSeqNum;
	private double posATcomposition;
	private double posCGcomposition;
	private double negATcomposition;
	private double negCGcomposition;
	private int posSeqRelElMatchesNum;
	private int negSeqRelElMatchesNum;
	
	
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
			currentLocale = new Locale("MacMini", "induce");			
		} else if ("/Users/oxy".equals(System.getProperty("user.home"))){ // Mac Mini //$NON-NLS-1$ //$NON-NLS-2$
			currentLocale = new Locale("MacMini", "OK");			
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
	
	
	
	// ********* Runtime Getters and Setters  ***********
	public int getPosSeqNum() {
		return posSeqNum;
	}
	public void setPosSeqNum(int posSeqNum) {
		this.posSeqNum = posSeqNum;
	}

	public int getNegSeqNum() {
		return negSeqNum;
	}
	public void setNegSeqNum(int negSeqNum) {
		this.negSeqNum = negSeqNum;
	}

	public int getPosSeqRelElMatchesNum() {
		return posSeqRelElMatchesNum;
	}
	public void setPosSeqRelElMatchesNum(int posSeqRelElMatchesNum) {
		this.posSeqRelElMatchesNum = posSeqRelElMatchesNum;
	}

	public int getNegSeqRelElMatchesNum() {
		return negSeqRelElMatchesNum;
	}
	public void setNegSeqRelElMatchesNum(int negSeqRelElMatchesNum) {
		this.negSeqRelElMatchesNum = negSeqRelElMatchesNum;
	}
	
	public String getRelativePathToDreme() {
		return relativePathToDreme;
	}
	public void setRelativePathToDreme(String relativePathToDreme) {
		this.relativePathToDreme = relativePathToDreme;
	}
	
	public double getPosATcomposition() {
		return posATcomposition;
	}
	public void setPosATcomposition(double posATcomposition) {
		this.posATcomposition = posATcomposition;
	}

	public double getPosCGcomposition() {
		return posCGcomposition;
	}
	public void setPosCGcomposition(double posCGcomposition) {
		this.posCGcomposition = posCGcomposition;
	}

	public double getNegATcomposition() {
		return negATcomposition;
	}
	public void setNegATcomposition(double negATcomposition) {
		this.negATcomposition = negATcomposition;
	}

	public double getNegCGcomposition() {
		return negCGcomposition;
	}
	public void setNegCGcomposition(double negCGcomposition) {
		this.negCGcomposition = negCGcomposition;
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
