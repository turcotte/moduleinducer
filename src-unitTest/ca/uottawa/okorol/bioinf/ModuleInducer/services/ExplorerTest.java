package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;


public class ExplorerTest extends TestCase{
	Explorer syntheticFixture;
	Explorer cElegansFixture;
	
	private String tmpDirName1;
	private String tmpDirName2;
	
	
	@Before
	public void setUp() throws Exception {
		RegulatoryRegionService syntheticRegRegionServ = new SyntheticRegRegionService(20, 1);

		RegulatoryRegionService cElegansRegRegionServ = new CElegansRegRegionService(1); 
		
		File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
		
		tmpDirName1 = FileHandling.createTempIlpOutputDirectory();
		
		PatserRegElementService patserRegElService = new PatserRegElementService(pwmDir, tmpDirName1);
		syntheticFixture = new Explorer(syntheticRegRegionServ, patserRegElService, tmpDirName1);

		tmpDirName2 = FileHandling.createTempIlpOutputDirectory();
		patserRegElService = new PatserRegElementService(pwmDir, tmpDirName2);
		cElegansFixture = new Explorer(cElegansRegRegionServ, patserRegElService, tmpDirName2);
		
		
	}

	@After
	public void tearDown() throws Exception {
		FileHandling.deleteDirectory(tmpDirName1);
		FileHandling.deleteDirectory(tmpDirName2);
		
	}
	
	public void testInduceRules(){
		try {
			String theory = syntheticFixture.induceRules();
			assertNotNull(theory);
			assertFalse(theory.isEmpty());
			assertTrue(theory.contains("[theory]"));

//			theory = cElegansFixture.induceRules();
//			assertNotNull(theory);
//			assertFalse(theory.isEmpty());
//			assertTrue(theory.contains("[theory]"));
//			
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}

	//TODO this test has to be moved down
/*	
	public void testSetNegativeExamplesMultiplicationFactor(){

		assertEquals(cElegansFixture.getRealSequences().size(), cElegansFixture.getRandomSequences().size());
		cElegansFixture.setNegativeExamplesMultiplicationFactor(5);
		assertEquals(cElegansFixture.getRealSequences().size()*5, cElegansFixture.getRandomSequences().size());
		cElegansFixture.setNegativeExamplesMultiplicationFactor(2);
		assertEquals(cElegansFixture.getRealSequences().size()*2, cElegansFixture.getRandomSequences().size());

		
		assertEquals(syntheticFixture.getRealSequences().size(), syntheticFixture.getRandomSequences().size());
		syntheticFixture.setNegativeExamplesMultiplicationFactor(5);
		assertEquals(syntheticFixture.getRealSequences().size()*5, syntheticFixture.getRandomSequences().size());
		syntheticFixture.setNegativeExamplesMultiplicationFactor(2);
		assertEquals(syntheticFixture.getRealSequences().size()*2, syntheticFixture.getRandomSequences().size());
		
	}
*/
	
}
