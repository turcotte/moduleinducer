package ca.uottawa.okorol.bioinf.ModuleInducer.tools;


import java.util.ArrayList;

import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;


public class DataFormatterTest extends TestCase {
	
	public void testExtractRegElementFromGff(){
		
		ArrayList<Feature> expected = new ArrayList<Feature>();
		expected.add(new Feature("polypeptide_motif", "ASAGGAAG", "jur_1", "R", 413, 420, 1.47E-5));
		expected.add(new Feature("polypeptide_motif", "GTGGBTW", "jur_1", "R", 351, 357, 5.35E-5));
		expected.add(new Feature("polypeptide_motif", "ASAGGAAG", "jur_10", "D", 85, 92, 6.51E-5));
		expected.add(new Feature("polypeptide_motif", "CAGGTGB", "jur_10", "R", 267, 273, 8.78E-5));
		expected.add(new Feature("polypeptide_motif", "ACCRCAD", "jur_100", "R", 69, 75, 5.35E-5));
		expected.add(new Feature("polypeptide_motif", "ACCRCAD", "jur_100", "D", 143, 149, 5.35E-5));
		
		ArrayList<Feature> actual = DataFormatter.extractRegElementFromGff("./data/testData/fimo_short.gff");
		//FeaturesTools.printFeatures(actual);
		//FeaturesTools.printFeatures(expected);
		
		assertTrue(expected.containsAll(actual));
		assertTrue(actual.containsAll(expected));
		
		assertTrue(expected.equals(actual));
		
	}
	
	public void testExtractRegElementsFromXml(){
		ArrayList<Feature> expected = new ArrayList<Feature>();
		expected.add(new Feature("TF_binding_site", "motif_5", "jur_76", "D", 120, 127, 6.5e-05));
		expected.add(new Feature("TF_binding_site", "motif_2", "jur_76", "R", 175, 182, 1.5e-05));
		expected.add(new Feature("TF_binding_site", "motif_3", "jur_76", "R", 205, 212, 4.4e-05));
		expected.add(new Feature("TF_binding_site", "motif_3", "jur_35", "R", 88, 95, 4.4e-05));
		expected.add(new Feature("TF_binding_site", "motif_4", "jur_133", "D", 143, 150, 4.4e-05));
		
		
		ArrayList<Feature> actual = DataFormatter.extractRegElementsFromXml("./data/testData/mast_short.xml");
		//System.out.println("Number of features : " + actual.size());
		//FeaturesTools.printFeatures(actual);
		//FeaturesTools.printFeatures(expected);
		
		assertTrue(expected.containsAll(actual));
		assertTrue(actual.containsAll(expected));
		
		assertTrue(expected.equals(actual));
	}
	
	
	
}
