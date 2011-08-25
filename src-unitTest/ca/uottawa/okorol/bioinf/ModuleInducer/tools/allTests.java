package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class allTests extends TestCase {
	

		public static Test suite(){
			TestSuite suite = new TestSuite();

			suite.addTestSuite(StatAnalyserTest.class);
			suite.addTestSuite(DataModellerTest.class);
			suite.addTestSuite(FastaExtractorTest.class);
			suite.addTestSuite(ArrayToolsTest.class);
			suite.addTestSuite(FeaturesToolsTest.class);
			
			return suite;
		}

}
