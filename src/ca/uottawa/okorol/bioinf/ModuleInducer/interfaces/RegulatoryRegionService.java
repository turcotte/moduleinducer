package ca.uottawa.okorol.bioinf.ModuleInducer.interfaces;

import java.util.ArrayList;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;

public interface RegulatoryRegionService {
	
	final String SYNTHETIC_DATA_SERVICE = "SYNTHETIC_DATA_SERVICE";
	final String C_ELEGANS_DATA_SERVICE = "C_ELEGANS_SERVICE";
	final String HOMO_SAPIENS_DATA_SERVICE = "HOMO_SAPIENS_SERVICE";
	final String CUSTOM_DATA_SERVICE = "CUSTOM_DATA_SERVICE";
	
	public ArrayList<Feature> getPositiveRegulatoryRegions() throws DataFormatException;
	public void setPositiveRegulatoryRegions(ArrayList<Feature> regRegions);

	public ArrayList<Feature> getNegativeRegulatoryRegions();
	public void setNegativeRegulatoryRegions(ArrayList<Feature> regRegions);
	
	public void updateNumberOfPositiveRegRegions(int num) throws DataFormatException ;
	public void updateNumberOfNegativeRegRegions(int multiplicationFactor) throws DataFormatException;
	
	//public ArrayList<Feature> generateSimulatedRegulatoryRegions(int numOfRegulatoryRegions, int regRegionLength, String regionNamePrefix);


}
