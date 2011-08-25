package ca.uottawa.okorol.bioinf.ModuleInducer.services;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.DataFormatter;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FileHandling;

public class PatserRegElementServiceTest extends TestCase {
	
	private String tmpDir;
	private PatserRegElementService fixture;

	@Before
	public void setUp() throws Exception {
		tmpDir = FileHandling.createTempIlpOutputDirectory();
		File pwmDir = new File(SystemVariables.getInstance().getString("C.elegans.PWMs.dir"));
		fixture = new PatserRegElementService(pwmDir, tmpDir);
	}

	@After
	public void tearDown() throws Exception {
		FileHandling.deleteDirectory(tmpDir);
	}
	
	public void testCreateGeneFiles(){
		try {

			RegulatoryRegionService geneService = new CElegansRegRegionService(6); 
		
			ArrayList<Feature> genes;
			ArrayList<Feature> tfbsHits;
		
			genes = geneService.getPositiveRegulatoryRegions();
			tfbsHits = fixture.getRegulatoryElements(genes, 7, 0.25, 0.25);

			assertNotNull(tfbsHits);
			//assertEquals(327, tfbsHits.size()); //only tfbs matrixes
			assertEquals(357, tfbsHits.size()); //tfbs and other transcription signals

		} catch (DataFormatException e) {
			fail();
			e.printStackTrace();
		}
		
		
		
		// Check if junk file was deleted
		try {
			File tmpDir = new File(SystemVariables.getInstance().getString("temp.output.dir"));
		
			FilenameFilter filter = new FilenameFilter() { 
				public boolean accept(File dir, String name) { 
					String str = "";
					try {
						str = SystemVariables.getInstance().getString("patser.tmp.seq.output.file.name");
					} catch (DataFormatException e) {
						e.printStackTrace();
						fail("Didn't get patser prefix: patser.tmp.seq.output.file.name");
					}
					return name.startsWith(str); 
				} 
			}; 
			
			String[] children = tmpDir.list(filter);
			
			assertNotNull(children);
			assertEquals(0, children.length);

		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
	public void testGetPssms(){
		String anotherTmpDir = "";
		try {
			anotherTmpDir = FileHandling.createTempIlpOutputDirectory();
			File pwmDir = new File(SystemVariables.getInstance().getString("workspace.dir") + "ModuleInducer/data/testData/pwms/");
			fixture = new PatserRegElementService(pwmDir, anotherTmpDir);
			
			String actual;
			
			Hashtable<String, int[][]> pssmsHash = fixture.getPssms();
			//ArrayList<RegulatoryElementPWM> pwmObjects = fixture.getRegulatoryElementsPWMs();
			
			assertEquals(4, pssmsHash.size());
			

			String ma0003 = "A\t|\t0\t0\t0\t22\t19\t55\t53\t19\t9\nC\t|\t0\t185\t185\t71\t57\t44\t30\t16\t78\nG\t|\t185\t0\t0\t46\t61\t67\t91\t137\t79\nT\t|\t0\t0\t0\t46\t48\t19\t11\t13\t19";			
			actual = DataFormatter.formatPssm(pssmsHash.get("MA0003.1"));
//			System.out.println("\n\nExpected:\n" + ma0003);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0003.equals(actual));
			
			
			String ma0004 = "A\t|\t4\t19\t0\t0\t0\t0\nC\t|\t16\t0\t20\t0\t0\t0\nG\t|\t0\t1\t0\t20\t0\t20\nT\t|\t0\t0\t0\t0\t20\t0";
			actual = DataFormatter.formatPssm(pssmsHash.get("MA0004.1"));
//			System.out.println("\n\nExpected:\n" + ma0004);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0004.equals(actual));
			
			
			String ma0036 = "A\t|\t13\t0\t52\t0\t25\nC\t|\t13\t5\t0\t0\t7\nG\t|\t18\t48\t1\t0\t15\nT\t|\t9\t0\t0\t53\t6";
			actual = DataFormatter.formatPssm(pssmsHash.get("MA0036.1"));
//			System.out.println("\n\nExpected:\n" + ma0036);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0036.equals(actual));
			
			actual = DataFormatter.formatPssm(pssmsHash.get("MA0036.1diffFormat")); // different format, but the same content as ma0036
//			System.out.println("\n\nExpected:\n" + ma0036);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0036.equals(actual));
			
			 
		
		
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		} finally {
			FileHandling.deleteDirectory(anotherTmpDir);
		}
	}
	
	
	public void testGetRegulatoryElementsPWMs(){
		String anotherTmpDir = "";
		try {
			anotherTmpDir = FileHandling.createTempIlpOutputDirectory();
			File pwmDir = new File(SystemVariables.getInstance().getString("workspace.dir") + "ModuleInducer/data/testData/pwms/");
			fixture = new PatserRegElementService(pwmDir, anotherTmpDir);
			
			String actual;
			
			ArrayList<RegulatoryElementPWM> pwmObjects = fixture.getRegulatoryElementsPWMs();
			
			assertEquals(4, pwmObjects.size());
			

			String ma0003 = "A\t|\t0\t0\t0\t22\t19\t55\t53\t19\t9\nC\t|\t0\t185\t185\t71\t57\t44\t30\t16\t78\nG\t|\t185\t0\t0\t46\t61\t67\t91\t137\t79\nT\t|\t0\t0\t0\t46\t48\t19\t11\t13\t19";			
			actual = pwmObjects.get(0).getPwmString();
//			System.out.println("\n\nExpected:\n" + ma0003);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0003.equals(actual));
			
			
			String ma0004 = "A\t|\t4\t19\t0\t0\t0\t0\nC\t|\t16\t0\t20\t0\t0\t0\nG\t|\t0\t1\t0\t20\t0\t20\nT\t|\t0\t0\t0\t0\t20\t0";
			actual = pwmObjects.get(1).getPwmString();
//			System.out.println("\n\nExpected:\n" + ma0004);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0004.equals(actual));
			
			
			String ma0036 = "A\t|\t13\t0\t52\t0\t25\nC\t|\t13\t5\t0\t0\t7\nG\t|\t18\t48\t1\t0\t15\nT\t|\t9\t0\t0\t53\t6";
			actual = pwmObjects.get(2).getPwmString();
//			System.out.println("\n\nExpected:\n" + ma0036);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0036.equals(actual));
			
			actual = pwmObjects.get(3).getPwmString();	// different format, but the same content as ma0036
//			System.out.println("\n\nExpected:\n" + ma0036);
//			System.out.println("\nActual:\n"+ actual);
			assertTrue(ma0036.equals(actual));
			
			 
		
		
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		} finally {
			FileHandling.deleteDirectory(anotherTmpDir);
		}
	}

}
