package ca.uottawa.okorol.bioinf.ModuleInducer.services;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

public class IlpServiceTest extends TestCase {
	
	private IlpService fixture;
	private String tmpDirName;

	@Before
	public void setUp() throws Exception {
		//TODO run ilp in temp dir
		tmpDirName = FileHandling.createTempIlpOutputDirectory();
		fixture = new IlpService(tmpDirName);
	}

	@After
	public void tearDown() throws Exception {
		FileHandling.deleteDirectory(tmpDirName);
	}
	
	public void testRunIlp(){
		try {

			RegulatoryRegionService geneService = new CElegansRegRegionService(6); 
			
			ArrayList<Feature> genes;
			ArrayList<Feature> fakeGenes;
			ArrayList<Feature> tfbsHits;
			ArrayList<Feature> fakeTfbsHits;
		
			genes = geneService.getPositiveRegulatoryRegions();
			fakeGenes = geneService.getNegativeRegulatoryRegions();

			File pwmDir = new File( SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
			PatserRegElementService tfbsService = new PatserRegElementService(pwmDir, tmpDirName);
			
			tfbsHits = tfbsService.getRegulatoryElements(genes, 8.5, 0.25, 0.25);
			fakeTfbsHits = tfbsService.getRegulatoryElements(fakeGenes, 8.5, 0.25, 0.25);

			// Test creation of ILP files
			fixture.createIlpFiles(genes, fakeGenes, tfbsHits, fakeTfbsHits);
		
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
					
				
		File tmpDir = new File(fixture.ilpDirName);
		
		FilenameFilter filter = new FilenameFilter() { 
			public boolean accept(File dir, String name) { 
				boolean result = false;
				try {
					result = name.startsWith(SystemVariables.getInstance().getString("ilp.script.file.name.prefix"));
				} catch (DataFormatException e) {
					e.printStackTrace();
					fail();
				} 
				return result;
			} 
		}; 
		
		String[] children = tmpDir.list(filter);
		
		assertNotNull(children);
		assertEquals(3, children.length);

		// Test running of ILP files
		String theory = fixture.runILP();

		/*
		FilenameFilter filter2 = new FilenameFilter() { 
			public boolean accept(File dir, String name) { 
				return name.endsWith(".out"); 
			} 
		}; 
		
		children = tmpDir.list(filter2);
		
		assertNotNull(children);
		assertTrue(children.length > 0);
		*/
		
		assertNotNull(theory);
		assertFalse(theory.isEmpty());
		
		System.out.println("\n****** ILP theory as returned to the test *******\n");
		System.out.println(theory);
		System.out.println("\n\n****** End of test execution *******\n");
	}

}
