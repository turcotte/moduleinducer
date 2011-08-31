package ca.uottawa.okorol.bioinf.ModuleInducer.data;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

public class FeatureTest extends TestCase {
	
	public void testHitsSorting(){
		
		ArrayList<Feature> testCollection = new ArrayList<Feature>();

		testCollection.add(new Feature("TF_binding_site", "TF3", "geneC", "D", -20, -10, 5));
		testCollection.add(new Feature("TF_binding_site", "TF3", "geneC", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF2", "geneC", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF1", "geneC", "D", -10, -5, 5));

		testCollection.add(new Feature("TF_binding_site", "TF2", "geneB", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF1", "geneB", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF1", "geneB", "D", -40, -30, 5));

		testCollection.add(new Feature("TF_binding_site", "TF3", "geneA", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF1", "geneA", "D", -10, -5, 5));
		testCollection.add(new Feature("TF_binding_site", "TF2", "geneA", "D", -30, -20, 5));
		testCollection.add(new Feature("TF_binding_site", "TF2", "geneA", "D", -20, -10, 5));

		
		Collections.sort(testCollection);

		ArrayList<Feature> benchmarkCollection = new ArrayList<Feature>();
		
		benchmarkCollection.add(new Feature("TF_binding_site", "TF1", "geneA", "D", -10, -5, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF2", "geneA", "D", -30, -20, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF2", "geneA", "D", -20, -10, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF3", "geneA", "D", -10, -5, 5));

		benchmarkCollection.add(new Feature("TF_binding_site", "TF1", "geneB", "D", -40, -30, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF1", "geneB", "D", -10, -5, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF2", "geneB", "D", -10, -5, 5));

		benchmarkCollection.add(new Feature("TF_binding_site", "TF1", "geneC", "D", -10, -5, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF2", "geneC", "D", -10, -5, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF3", "geneC", "D", -20, -10, 5));
		benchmarkCollection.add(new Feature("TF_binding_site", "TF3", "geneC", "D", -10, -5, 5));
		
		for (int i = 0; i < testCollection.size(); i++) {
			assertEquals(benchmarkCollection.get(i).getName(), testCollection.get(i).getName());
			assertEquals(benchmarkCollection.get(i).getParent(), testCollection.get(i).getParent());
			assertEquals(benchmarkCollection.get(i).getStrand(), testCollection.get(i).getStrand());
			assertEquals(benchmarkCollection.get(i).getStartPosition(), testCollection.get(i).getStartPosition());
			assertEquals(benchmarkCollection.get(i).getEndPosition(), testCollection.get(i).getEndPosition());
			assertEquals(benchmarkCollection.get(i).getScore(), testCollection.get(i).getScore());
		}
	
		
		/*		
		System.out.println("\n*** ");
		for (Iterator<Feature> iterator = testCollection.iterator(); iterator.hasNext();) {
			Feature hits = (Feature) iterator.next();
			System.out.println(hits.getParentAttribute() + "	" + hits.getNameAttribute() + "	" + hits.getStartPosition()
					+ "	" + hits.getEndPosition() + "	" + hits.getStrand() + "	" + hits.getScore() );
		}
		 */		
				
		
	}
	
	public void testEqual(){
		Feature f1 = new Feature();
		Feature f2 = new Feature();
		
		assertTrue(f1.equals(f2));
		assertFalse(f1==f2);
		
		f1.setId("123");
		f2.setId("123");
		
		f1.setScore(Double.parseDouble("4.4e-05"));
		f2.setScore(4.4e-05);
		
		f1.setName("test");
		f2.setName("test");

		f1.setEndPosition(3);
		f2.setEndPosition(3);
		
		assertTrue(f1.equals(f2));
		
		f1.setNote("Note");
		
		assertFalse(f1.equals(f2));
	}

}
