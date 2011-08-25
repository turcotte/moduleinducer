package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;


public class HomoSapiensRegRegionServiceTest extends TestCase  {
	
	HomoSapiensRegRegionService fixture;
	String tempDir;
	
	@Before
	public void setUp() throws Exception {
//		tempDir = FileHandling.createTempIlpOutputDirectory();
//		fixture = new HomoSapiensRegRegionService(HomoSapiensRegRegionService.JURKAT, 
//				HomoSapiensRegRegionService.ERYTHROID);
	}

	@After
	public void tearDown() throws Exception {
//		FileHandling.deleteDirectory(tempDir);
	}
	
	// Useless test
	
	public void testGenerateRegulatoryRegions(){
	/*
	 	long timeBefore, timeAfter;
		timeBefore = System.currentTimeMillis();
		try{
			
			FeaturesTools.printFeaturesToFile(fixture.getPositiveRegulatoryRegions(), tempDir+ "jurkat_features.txt");
			FeaturesTools.printFeaturesToFile(fixture.getNegativeRegulatoryRegions(), tempDir + "erythroid_features.txt");
	
		} catch (Exception e){
			fail();
		}
		timeAfter = System.currentTimeMillis();
		System.out.println("== Execution time (sec): " + (timeAfter - timeBefore) / 1000);
	 */
	}
}

	