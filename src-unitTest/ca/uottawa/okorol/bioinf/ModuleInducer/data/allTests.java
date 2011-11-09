package ca.uottawa.okorol.bioinf.ModuleInducer.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class allTests extends TestCase {
	

		public static Test suite(){
			TestSuite suite = new TestSuite();
			
			suite.addTestSuite(FeatureTest.class);
			
			return suite;
		}

}
