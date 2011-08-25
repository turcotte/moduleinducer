package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class allTests extends TestCase {
	

		public static Test suite(){
			TestSuite suite = new TestSuite();
			
			suite.addTestSuite(PatserRegElementServiceTest.class);
			suite.addTestSuite(IlpServiceTest.class);
			suite.addTestSuite(ExperimenterTest.class);
			suite.addTestSuite(ExplorerTest.class);
			suite.addTestSuite(HomoSapiensRegRegionServiceTest.class);
			
			return suite;
		}
}
