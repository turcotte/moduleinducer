package ca.uottawa.okorol.bioinf.ModuleInducer.tools;


import java.util.ArrayList;

import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;


public class DataFormatterTest extends TestCase {
	
	
	public void testExtractRegElementsFromXml(){
		ArrayList<Feature> expected = new ArrayList<Feature>();
		expected.add(new Feature("TF_binding_site", "motif_5", "jur_76", "D", 120, 0, 6.5e-05));
		expected.add(new Feature("TF_binding_site", "motif_2", "jur_76", "R", 175, 0, 1.5e-05));
		expected.add(new Feature("TF_binding_site", "motif_3", "jur_76", "R", 205, 0, 4.4e-05));
		expected.add(new Feature("TF_binding_site", "motif_3", "jur_35", "R", 88, 0, 4.4e-05));
		expected.add(new Feature("TF_binding_site", "motif_4", "jur_133", "D", 143, 0, 4.4e-05));
		
		
		ArrayList<Feature> actual = DataFormatter.extractRegElementsFromXml("./data/testData/mast_short.xml");
		
		assertTrue(expected.equals(actual));
	}
	
	
	
}
