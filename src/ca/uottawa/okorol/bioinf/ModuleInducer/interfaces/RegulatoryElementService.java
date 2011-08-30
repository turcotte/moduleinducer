package ca.uottawa.okorol.bioinf.ModuleInducer.interfaces;

import java.util.ArrayList;
import java.util.Hashtable;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;

public interface RegulatoryElementService {
	final String PATSER_SERVICE = "PATSER_SERVICE";
	final String SYNTHETIC_TFBS_SERVICE = "SYNTHETIC_TFBS_SERVICE";

	public ArrayList<Feature> getRegulatoryElements(ArrayList<Feature> regSequences, double cutOffScore, 
			double atComposition, double cgComposition) throws DataFormatException;
	public Hashtable<String, Double> getPssmMatchingStatistics();
	public Hashtable<String, int[][]> getPssms() throws DataFormatException;
//TODO: retire this one:
	public ArrayList<RegulatoryElementPWM> getRegulatoryElementsPWMs() throws DataFormatException;
	public ArrayList<Feature> getRegulatoryElements(ArrayList<Feature> regRegions, ArrayList<Feature> backgroundRegRegions, double cutOffScore) throws DataFormatException;
}
