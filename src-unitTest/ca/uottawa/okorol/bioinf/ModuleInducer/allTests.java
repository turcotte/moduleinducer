package ca.uottawa.okorol.bioinf.ModuleInducer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class allTests extends TestCase {
	

		public static Test suite(){
			TestSuite suite = new TestSuite();
			
			suite.addTest(ca.uottawa.okorol.bioinf.ModuleInducer.data.allTests.suite());
			suite.addTest(ca.uottawa.okorol.bioinf.ModuleInducer.services.allTests.suite());
			suite.addTest(ca.uottawa.okorol.bioinf.ModuleInducer.tools.allTests.suite());
			
			return suite;
		}

}
