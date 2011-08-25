package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.text.DecimalFormat;
import java.util.ArrayList;

import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.OccurrencePerLocationMatrix;

public class StatAnalyserTest extends TestCase {
	private StatAnalyser fixture;
	
 //Unfortunately, the StatAnalyser that this test was created for got deleted...	
 
	public void testGetMean(){
		
		ArrayList<Feature> collection = new ArrayList<Feature>();
		DecimalFormat formatter = new DecimalFormat("#.####");
		
		//*** Test 1 ***

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneC", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneC", "D", -12, -5, 5));

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneB", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", -14, -30, 5));

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneA", "D", -13, -20, 5));
		
		fixture = new StatAnalyser(collection);
		
		assertTrue(Double.compare(3.0, fixture.getMean()) == 0);
		
		double actual = Double.valueOf(formatter.format(fixture.getStandardDeviation()));
		assertTrue(Double.compare(0.8165, actual) == 0);

		
		//*** Test 2 ***
		
		collection = new ArrayList<Feature>();

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneC", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneC", "D", -13, -5, 5));

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneB", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", -110, -30, 5));
		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneA", "D", -30, -20, 5));

		
		fixture = new StatAnalyser(collection);
		
		assertTrue(Double.compare(41.0, fixture.getMean()) == 0);

		actual = Double.valueOf(formatter.format(fixture.getStandardDeviation()));
		assertTrue(Double.compare(42.2926, actual) == 0);
		
		
		//*** Test 3 ***
		
		collection = new ArrayList<Feature>();

		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneC", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneC", "D", -12, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneC", "D", -14, -5, 5));

		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneB", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", -13, -30, 5));
		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", -10, -5, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneA", "D", -14, -20, 5));

		
		fixture = new StatAnalyser(collection);
		
		assertTrue(Double.compare(3.0, fixture.getMean()) == 0);

		actual = Double.valueOf(formatter.format(fixture.getStandardDeviation()));
		assertTrue(Double.compare(0.8944, actual) == 0);

		
		 
	}
	
	public void testGetLocationalProbabilities(){
		ArrayList<Feature> collection = new ArrayList<Feature>();
		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneA", "D", -3, 0, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF1", "geneC", "D", -2, 0, 5));

		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneC", "D", -5, 0, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneB", "D", -7, 0, 5));
		collection.add(new Feature(Feature.TYPE_TFBS, "TF2", "geneD", "D", -9, 0, 5));
		
		collection.add(new Feature(Feature.TYPE_TFBS, "TF3", "geneD", "D", -9, 0, 5));
		
		fixture = new StatAnalyser(collection);
		OccurrencePerLocationMatrix pm = fixture.getLocationalProbabilities(5, 10, 4);
		
		double[][] expected = {{0.5, 0.0, 0.0},{0.0, 0.75, 0.25}};
		//pm.printData();
		double[][] actual = pm.getProbabilityMatrix();
		
		for (int i = 0; i < expected.length; i++){
			for (int j = 0; j < expected[0].length; j++) {
				assertTrue(Double.compare(expected[i][j], actual[i][j]) == 0);
			}
		}
	}


}
