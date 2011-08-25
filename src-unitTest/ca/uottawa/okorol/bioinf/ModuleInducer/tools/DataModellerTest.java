package ca.uottawa.okorol.bioinf.ModuleInducer.tools;


import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;


public class DataModellerTest extends TestCase {
	
	public void testMatchRegex(){
		//simple regex test
		assertTrue("CAAATGAAAAAAAAAAGATA".matches("CA..TG.{8,10}GATA"));
		
		//method test
		String regex1 = "[c,C][a,A]..[t,T][g,G].{8,10}[g,G][a,A][t,T][a,A]";
		String seq1 = "ACTGACTCAAATGAAAAAAAAAAGATAACTGACTA";
		assertEquals(1, DataModeller.getNumberOfMatches(regex1, seq1));
		
		seq1 = "cAAATGAAAAAAAAGATAACTGACTCAAAtgAAAAAAAAAAGATAACTGACTACAAATGAAAAAAAAAGATA";
		assertEquals(3, DataModeller.getNumberOfMatches(regex1, seq1));
		
		seq1 = "CAAATGAAAAAAAGATAACTGACTCAAATGAAAAAAAAAAGATAACTGACTACAATGAAAAAAAAAGATA";
		assertEquals(1, DataModeller.getNumberOfMatches(regex1, seq1));
		
		
	}
	
	public void testPlantPSSM(){
		int[][] pssm = new int[][]{	{3, 0, 0, 0},
									{0, 3, 0, 0},
									{0, 0, 3, 0},
									{0, 0, 3, 0},
									{0, 0, 0, 3}};
		String seq = "aaaaaaaaaaa";
		String actual;
		try {
			
			actual = DataModeller.plantPSSM(seq, 3, pssm);
			assertEquals("aaACGGTaaaa", actual);
			
			actual = DataModeller.plantPSSM(seq, 9, pssm);
			assertEquals("aaaaaaaaACG", actual);
			
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	
	public void testGetNucleotideComposition(){
		double[] actual = DataModeller.getNucleotideComposition("aaaaccccggggtttt");
		assertEquals(0, Double.compare(actual[0], actual[1])) ;
		assertEquals(0, Double.compare(actual[2], actual[3])) ;
		assertEquals(0, Double.compare(actual[0], actual[3])) ;
		assertEquals(0, Double.compare(0.25, actual[0])) ;
		
		
		actual = DataModeller.getNucleotideComposition("aaaAaacccC");
		assertEquals(0, Double.compare(0.6, actual[0]));
		assertEquals(0, Double.compare(0.4, actual[1]));
		assertEquals(0, Double.compare(0.0, actual[2]));
		assertEquals(0, Double.compare(0.0, actual[3]));
	}
	
	public void testMatchSequenceToPwm(){
		
		int[][] pwm = new int[][]{
				{20,10,0,10},
				{0,30,0,10},
				{0,0,20,20},
				{0,0,20,20},
				{0,40,0,0},
		};
		
		RegulatoryElementPWM pwmObj = new RegulatoryElementPWM("pwm1", pwm);
		
		try {
			double actual = DataModeller.matchSequenceToPwm("ACTG", pwmObj);
			assertEquals(0, Double.compare(actual, 0.6625));
			
			actual = DataModeller.matchSequenceToPwm("AACTGA", pwmObj);
			assertEquals(0, Double.compare(actual, 0.475));
			
			actual = DataModeller.matchSequenceToPwm("AAAACTGA", pwmObj); 
			assertTrue(actual > 0.28124 && actual < 0.28126);	//0.28125
			
			actual = DataModeller.matchSequenceToPwm("GGGNBSRYTTT", pwmObj);
			assertTrue(actual > 0.35 && actual < 0.37);// 0.(36)
			
			actual = DataModeller.matchSequenceToPwm("VW", pwmObj);
			assertTrue(actual > 0.624 && actual < 0.626);//0.625
			
			actual = DataModeller.matchSequenceToPwm("TGG", pwmObj);
			assertTrue(actual > 0.515 && actual < 0.517);//0.51(6)
			
			actual = DataModeller.matchSequenceToPwm("TKD", pwmObj);
			assertTrue(actual > 0.84 && actual < 0.86);//0.85
			
			actual = DataModeller.matchSequenceToPwm("TGGH", pwmObj);
			assertTrue(actual > 0.6624 && actual < 0.6626);//0.6625
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
		
		
		
		// *** tests from real data
//		A  |  0   0   0  22  19  55  53  19   9 
//		C  |  0 185 185  71  57  44  30  16  78 
//		G  |185   0   0  46  61  67  91 137  79 
//		T  |  0   0   0  46  48  19  11  13  19
		int [][] pwm_ma0003_1 = new int[][] {
				{0, 0, 185, 0},
				{0, 185, 0, 0},
				{0, 185, 0, 0},
				{22, 71, 46, 46},
				{19, 57, 61, 48},
				{55, 44, 67, 19},
				{53, 30, 91, 11},
				{19, 16, 137, 13},
				{9, 78, 79, 19},
		};
		
		RegulatoryElementPWM pwmObj2 = new RegulatoryElementPWM("MA0003.1", pwm_ma0003_1);
		
		try {
			double actual = DataModeller.matchSequenceToPwm("CCACA", pwmObj2);
			assertTrue(actual > 0.54485 && actual < 0.54487); //0.54(486)
			
			
			actual = DataModeller.matchSequenceToPwm("VCCACA", pwmObj2);
			assertTrue(actual > 0.62071 && actual < 0.62073); //0.62(072)

		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testGetMM0TransitionMatrix(){
		try {
			DataModeller.getMM0TransitionMatrix("");
			fail("Getting transition matrix based on empty model sequence should throw an exception.");
		} catch (DataFormatException e) {
			// test passed
		}
		
		try {
			DataModeller.getMM0TransitionMatrix("ABCDEFG");
			fail("Model sequence for transition matrix should be in {A,C,G,T} alphabet.");
		} catch (DataFormatException e) {
			// test passed
		}
		
		try {
			double[] expected = new double[]{0.25,0.25,0.25,0.25};
			double[] matrix = DataModeller.getMM0TransitionMatrix("ACGTACGTACGTACGT");
			assertTrue(ArrayTools.arrayEquals(expected, matrix));
			
			expected = new double[]{0.35, 0.15, 0.15, 0.35};
			matrix = DataModeller.getMM0TransitionMatrix("aaaaaaacccgggttttttt");
			//ArrayTools.printMatrix(matrix);
			assertTrue(ArrayTools.approxEqual(expected, matrix, 0.01));
			
			expected = new double[]{0.0, 0.5, 0.5, 0.0};
			//matrix = DataModeller.getMM0TransitionMatrix("ccccggggcgcgcgccggccggcgcgcccgggcgcgcgcgccccccccggggggggccccccccggggggggcgcgcgcgccggccggccggccggcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcgcg");
			matrix = DataModeller.getMM0TransitionMatrix("cgcccgggcg");
			//ArrayTools.printMatrix(matrix);
			assertTrue(ArrayTools.arrayEquals(expected, matrix));
		} catch (DataFormatException e) {
			fail("Should return a matrix.");
		}
		
	}
	
	public void testGetMM1TransitionMatrix(){
		try {
			DataModeller.getMM1TransitionMatrix("");
			fail("Getting transition matrix based on empty model sequence should throw an exception.");
		} catch (DataFormatException e) {
			// test passed
		}
		
		try {
			DataModeller.getMM1TransitionMatrix("ABCDEFG");
			fail("Model sequence for transition matrix should be in {A,C,G,T} alphabet.");
		} catch (DataFormatException e) {
			// test passed
		}
		
		try {
			double[][] expected = new double[][]{{0.5,	0.0,	0.0,	0.0},
												 {0.5,	0.5,	0.0,	0.0},
												 {0.0,	0.5,	0.5,	0.0},
												 {0.0,	0.0,	0.5,	1.0}
			};
			double[][] matrix = DataModeller.getMM1TransitionMatrix("AACCGGTT");
			//ArrayTools.printMatrix(matrix);

			assertTrue(ArrayTools.arrayEquals(expected, matrix));
			
			
		} catch (DataFormatException e) {
			fail("Should return a matrix.");
		}
		
	}
	
	public void testPlantPWM(){
		int[][] pwm = new int[][]{{0,4,0,0}, {0,4,0,0}, {0,4,0,0}};

		try {
			String actual = DataModeller.plantPSSM("1234567890", 3, pwm);
			assertEquals("12CCC67890", actual);
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
		
		try {
			String actual = DataModeller.plantPSSM("1234567890", 9, pwm);
			assertEquals("12345678CC", actual);
			
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testGetMM0RandomSequence(){
		try {
			String modelString = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT";
			String actual = DataModeller.getRandomSequenceMC0(200, modelString);
			assertEquals(200, actual.length());
			double[] expectedTransitMatrix = new double[]{0.25,0.25,0.25,0.25};
			double[] actualTransitMatrix = DataModeller.getMM0TransitionMatrix(actual);
			
			//Attempting to test a random generation: this may sometimes fail
			assertTrue(ArrayTools.approxEqual(expectedTransitMatrix, actualTransitMatrix, 0.08));
			//System.out.println("Generated string: " + actual);
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testGetMM1RandomSequence(){
		try {
			String modelString = "AACCGGTTTTAACCGGACGGTGCGCGATCGATCGCTAGCGACGCTAGCGATAAATTCCCACTAATCGCTCTAGCTGCGCGCGCATCGCGCGCT";
			String actual = DataModeller.getRandomSequenceMC1(8000, modelString);
			assertEquals(8000, actual.length());
			double[][] expectedTransitMatrix = DataModeller.getMM1TransitionMatrix(modelString);
			
			double[][] actualTransitMatrix = DataModeller.getMM1TransitionMatrix(actual);
			
			//ArrayTools.printMatrix(expectedTransitMatrix);
			//System.out.println(actual);
			//ArrayTools.printMatrix(actualTransitMatrix);
			
			//Attempting to test a random generation: this may sometimes fail
			assertTrue(ArrayTools.approxEqual(expectedTransitMatrix, actualTransitMatrix, 0.1));
			//System.out.println("Generated string: " + actual);
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
}
