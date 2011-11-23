package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.util.ArrayList;

import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;


public class FeaturesToolsTest  extends TestCase {

	public void testTfbsDistance(){
		ArrayList<Feature> collection = new ArrayList<Feature>();
		
		//*** Test 1 ***

		collection.add(new Feature(Feature.TYPE_TFBS, "TF4", "geneG", "D", 3, 6, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF5", "geneG", "D", 5, 9, 5));
		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", 3, 6, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneA", "D", 5, 9, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneA", "D", 9, 10, 5));

		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", 3, 6, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneB", "D", 8, 10, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneB", "D", 9, 10, 5));
		
		ArrayList<SeqDist> distances = FeaturesTools.getRegElDistances("TF1", "TF2", collection);
		
//		for (Iterator iterator = distances.iterator(); iterator.hasNext();) {
//			SeqDist seqDist = (SeqDist) iterator.next();
//			System.out.println(seqDist.getDistance() + ", " + seqDist.getRegRegionName());
//		}
		
		assertEquals(2, distances.size());
		assertEquals(2, distances.get(0).getDistance());
	}
	
	public void testAreNamesDistinct(){
		
		ArrayList<Feature> regRegions1 = new ArrayList<Feature>();
		ArrayList<Feature> regRegions2 = new ArrayList<Feature>();
		

		regRegions1.add(new Feature("id1", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions1.add(new Feature("id2", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions1.add(new Feature("id3", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions1.add(new Feature("id4", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));

		regRegions2.add(new Feature("id10", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions2.add(new Feature("id11", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions2.add(new Feature("id12", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions2.add(new Feature("id13", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		regRegions2.add(new Feature("id14", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACTGCATCGA", 0.7));
		
		assertTrue(FeaturesTools.areNamesDistinct(regRegions1, regRegions2));

		
		regRegions1.add(new Feature("id4", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACCCCCCCCCCCCCCA", 0.8));		
		FeaturesTools.areNamesDistinct(regRegions1, regRegions2);
		assertFalse(FeaturesTools.areNamesDistinct(regRegions1, regRegions2));
		
		
		regRegions1.remove(4);
		FeaturesTools.areNamesDistinct(regRegions1, regRegions2);
		assertTrue(FeaturesTools.areNamesDistinct(regRegions1, regRegions2));
		
		regRegions2.add(new Feature("id2", Feature.TYPE_REGULATORY_REGION, "", 0,0, "ACCCCCTCGA", 0.3));
		FeaturesTools.areNamesDistinct(regRegions1, regRegions2);
		assertFalse(FeaturesTools.areNamesDistinct(regRegions1, regRegions2));
		
	}
	
	public void testNumSequencesContainingAll(){
		ArrayList<Feature> regEls = new ArrayList<Feature>();
		
		//*** Test 1 ***
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneG", "D", 3, 6, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF4", "geneG", "D", 3, 6, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF5", "geneG", "D", 5, 9, 5));
		
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", 3, 6, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneA", "D", 5, 9, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneA", "D", 9, 10, 5));

		regEls.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", 3, 6, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneB", "D", 8, 10, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneB", "D", 9, 10, 5));
		regEls.add(new Feature(Feature.TYPE_TFBS, "TF4", "geneB", "D", 3, 6, 5));
		
		assertEquals(2, FeaturesTools.numSequencesContainingAll(new String[] {"TF3", "TF1"}, regEls));
		assertEquals(3, FeaturesTools.numSequencesContainingAll(new String[] {"TF1"}, regEls));
		assertEquals(0, FeaturesTools.numSequencesContainingAll(new String[] {"TF3", "TF5", "TF1"}, regEls));
	}
	
	public void testGenerateSimulatedRegulatoryRegions(){
		ArrayList<Feature> model = new ArrayList<Feature>();
		model.add(new Feature("s1", Feature.TYPE_REGULATORY_REGION, "", 0, 0, "ACTGACTGACTGACTAGCTAGCTATATC", 7.5));
		model.add(new Feature("s2", Feature.TYPE_REGULATORY_REGION, "", 0, 0, "AAAAAAAAAGACTGACTGACTAGCTAGCTATATC", 7.5));
		model.add(new Feature("s3", Feature.TYPE_REGULATORY_REGION, "", 0, 0, "ACCTAGCTAGCTATATC", 7.5));
		try {
			ArrayList<Feature> actual = FeaturesTools.generateSimulatedMC1RegulatoryRegions(model, 2, "d");
			
			assertEquals(6, actual.size());
			assertEquals(model.get(0).getSequence().length(), actual.get(0).getSequence().length());
			assertEquals(model.get(0).getSequence().length(), actual.get(1).getSequence().length());
			assertEquals(model.get(1).getSequence().length(), actual.get(2).getSequence().length());
			assertEquals(model.get(1).getSequence().length(), actual.get(3).getSequence().length());
			assertEquals(model.get(2).getSequence().length(), actual.get(4).getSequence().length());
			assertEquals(model.get(2).getSequence().length(), actual.get(5).getSequence().length());
			
			// make sure the ids are unique
			assertFalse(actual.get(0).getId().equals(actual.get(1).getId()));
			assertFalse(actual.get(0).getId().equals(actual.get(2).getId()));
			assertFalse(actual.get(0).getId().equals(actual.get(3).getId()));
			assertFalse(actual.get(0).getId().equals(actual.get(4).getId()));
			assertFalse(actual.get(0).getId().equals(actual.get(5).getId()));
			assertFalse(actual.get(1).getId().equals(actual.get(2).getId()));
			assertFalse(actual.get(1).getId().equals(actual.get(3).getId()));
			assertFalse(actual.get(1).getId().equals(actual.get(4).getId()));
			assertFalse(actual.get(1).getId().equals(actual.get(5).getId()));
			assertFalse(actual.get(2).getId().equals(actual.get(3).getId()));
			assertFalse(actual.get(2).getId().equals(actual.get(4).getId()));
			assertFalse(actual.get(2).getId().equals(actual.get(5).getId()));
			assertFalse(actual.get(3).getId().equals(actual.get(4).getId()));
			assertFalse(actual.get(3).getId().equals(actual.get(5).getId()));
			assertFalse(actual.get(4).getId().equals(actual.get(5).getId()));
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
}
