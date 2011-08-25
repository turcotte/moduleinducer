package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import junit.framework.TestCase;


public class ArrayToolsTest  extends TestCase{
	public void testApproxEqual(){
		double[] m1 = {0.1, 0.2, 0.3, 0.4};
		double[] m2 = {0.15, 0.15, 0.34, 0.38};
		
		assertTrue(ArrayTools.approxEqual(m1, m2, 0.06));
		assertFalse(ArrayTools.approxEqual(m1, m2, 0.04));
		
	}
	
	public void testRegEx(){
		// Empty line (carriage return, tab, space...)
		String emptyLineRegEx = "\\s*";
		
		assertTrue("".matches(emptyLineRegEx));
		assertTrue(" ".matches(emptyLineRegEx));
		assertTrue("\n".matches(emptyLineRegEx));
		assertTrue("\t".matches(emptyLineRegEx));
		assertTrue("\r".matches(emptyLineRegEx));
		assertTrue("\n  \t ".matches(emptyLineRegEx));
		assertTrue("  \n \t \n\n\n  ".matches(emptyLineRegEx));
		assertFalse(" . \n ".matches(emptyLineRegEx));
		
		// space or tab any number of times
		String spaceTabRegEx = "[ \t]*";
		
		assertTrue("".matches(spaceTabRegEx));
		assertTrue(" ".matches(spaceTabRegEx));
		assertTrue("\t".matches(spaceTabRegEx));
		assertTrue("  ".matches(spaceTabRegEx));
		assertTrue("\t\t".matches(spaceTabRegEx));
		assertTrue(" \t \t \t  ".matches(spaceTabRegEx));
		assertFalse("\n".matches(spaceTabRegEx));
		assertFalse(" l ".matches(spaceTabRegEx));
		assertFalse("\t\n".matches(spaceTabRegEx));
		
		
		//Nucleotide sequence
		String ntRegEx = "[ACGTacgt]+";
		
		assertTrue("CAGTATGCATACC".matches(ntRegEx));
		assertTrue("CTTATTTCAGGAAAATTTTTTCAAAACTGTAAAACAAAAACCATTTTTCACAGAATCTAAGGGTATCTGAAAGCTTAAAATAACTTCAGAAAGATATCAATTCCAGCTGTTTAGTACCTGAACTGTCTGTAAACGTTTCTTCTCGAATTATAGAAAATTTTCCACTTTTTCAAGTTCAGGTTTTCAAGAAACCCCACGATTTCCACTCATCGTTTCCAATGTCCAATTTCCCATCCAATTTGCCGCACTCTGACCCAATGACTTGTTCCTTTGCCAATCAATGCTACCTAATAAATTTAAAAGTTTAACACGCATCCAATTGACACACAGGTAACCGCCCTTTCTTCTTTTACATAATTCGGAAACTTCAAAGAGCCGAAGGTGTCGGTTGTAGCAGCAGCGGAGGAACGGATGCCAATTGCGCAACTCTCGGCTCAACTCTTTTAGTGCTCCGAGAGCAGGAAGAAGAATACTGTTGGGTTGTAATAAAGACGGATGTTTTTGTTCAGAGTAGATTAGCTCGTGTTTGATTGGATTTGACCGGATCAAGAGGGGAATGTCCTGGTGGAATTAAATTTTATTAGAATAAATTGTATTTGGTGTTTAAATTCGAATCAATAATATTTATGAGCTTTAATGAATAAGTGTTAGATTATATAATTCTATAATTTTTGAACAAGCAATTCAAAAAGAAAACAAATTTTAGTAACCTCCGAAATCAAGCTGGGTGGCCTCTAGAAGTTTTGAAAAAACTTTTTTTATATTCTGTTGGAGTTTTTTTAAGTTTTATAATTATAGGTTAATCTTTCTAATTTGTAAGCTTTTTCTTAACCAATTTTTTTTGTTAACATTTTTTTGGAATTATGCTATATGACCTATACCTAAAACAGTTTAAAAGTTTAAAAAAATTTTCTATATTTTTCACTTCGTATTGAACCTCCTGGGTACATATATTGACAGCACATATCTTGTTTGTCTCAGATTTTATCAAATAAGTTTAACAAGTAAACCATGCACCAAATATTTTTCTAGGTCTCTGTAGTTAGGAAATATTTAATAAAAATAAAAATAACCGAGATATGAGCCATCAAAGTAGATCAATTAAGGCACAGGAAAAAAGATCTGAATAAAATCGAAGTTCTTAAAAATATAAATCAAACAAAATTTTTTCCAGAATTTCAGCCGAGAATTTCCAGCCGATTTGTTTATATTTTCCACATCACTCCCCACACTTCTCTCACACAAACACGATAAAATCTTGAGAAGCAATTAGCGCCAATCAACTCAACACAAAAACGAAAAGCCAACGAAAAGCTAAAGCTATCATCGTTGTCGCGTCTATGAGCAACTCAATCGTTCATCATCCTCATCTTCAGAGTGCTCAAACCTACCGTAACCCGAATTGGGCGGAGCCAAAGGGTCCGAAACAGTGCACCAGGGCGGGGAGGGACCCTGAGAAACGAGAGGGAAGTGAGCAATTGTTGAAGTGTCAGTTGTGCTCATCGAGGTCCGATGAAGAGACGCGCCTGCTCACCTACACAACTGACTTCCCCCATATACCTTTTTCTAGAATTTCCTTTTTTAGATTTATACGGTCAGGTAAAAAGGTAGAGTTTTACAGTGTAGAAATTAGGAAATTGCTCAAAAAGCCGAGCAGAATGCATATAAGAAGTACCATAGCCCCAAAGATTCGATTTTCCAGGGTTCAATCAATTTTTGTACTTTGACAGCGTATATCTCAGTTTTCTTTGATTTTATCAAAAACTAGTCAACTGACAAAATACTTGAAAAGTATTCCTTTATATTTTGGTAGCTGACCATTGTTTGTTAAAATATAAGGGAATCGAAATGTCGGTTATCAAAGTAGAACCTAACCTAAATCGCTATATATGCTATTTTTCAAAAAAAAAAACACGTTTTACTTTGTCTCAACTTATTAATATTCTTTAATATTTTTTCTATTTCTACCATTTTCCAAATTTTCCAATAATTTTCCAGAA".matches(ntRegEx));
		assertTrue("cagtagta".matches(ntRegEx));
		assertTrue("aCgtTG".matches(ntRegEx));
		assertFalse("a CgtTG".matches(ntRegEx));
		assertFalse("aCXtTG".matches(ntRegEx));
		assertFalse("a.CgtTG".matches(ntRegEx));
		assertFalse("a1CgtTG".matches(ntRegEx));
		assertFalse("aNCgtTG".matches(ntRegEx));
		assertFalse("AGTATAATGACAACTTACAAAzTTGGGAAATCTGGAAAACCGAGC".matches(ntRegEx));
		
		//PWM line 
		// unicode for | is \u007C
		// space: \u0020
		
		
		System.out.println("\u007C");

		String pwmRexEx = "[ACGT][\\t ]+\\|([\\t ]+[0-9]+)+";
		
		assertTrue("G	|	12 	2	 2343".matches(pwmRexEx));
		assertTrue("A | 1 2 3".matches(pwmRexEx));
		assertTrue("C | 11 2234 3".matches(pwmRexEx));
		assertTrue("T |	1	12	3123".matches(pwmRexEx));
		assertTrue("A	|	49	0	288	26	77	67	45	50".matches(pwmRexEx));
		assertTrue("A\t|\t49\t0\t288\t26\t77\t67\t45\t50".matches(pwmRexEx));
		assertFalse("a | 1 2 3".matches(pwmRexEx));
		assertFalse("A |\n 1 2 3".matches(pwmRexEx));
		assertFalse("A 1 2 3".matches(pwmRexEx));
		assertFalse("A |	1 C 3".matches(pwmRexEx));
		assertFalse("C |1 2 3".matches(pwmRexEx));
		
		
		// fasta sequence name
		
		String fastaNameSeqRegEx = ">[ \t]*\\w+.*"; 
		
		assertTrue("> name ".matches(fastaNameSeqRegEx));
		assertTrue(">name".matches(fastaNameSeqRegEx));
		assertTrue(">\t name\t".matches(fastaNameSeqRegEx));
		assertTrue(">    name123  stuf\t12342 \t".matches(fastaNameSeqRegEx));
		assertTrue(">check sda".matches(fastaNameSeqRegEx));
		assertTrue("> negExSeq_0_B0304.1".matches(fastaNameSeqRegEx));
		
		assertFalse("".matches(fastaNameSeqRegEx));
		assertFalse(" > name ".matches(fastaNameSeqRegEx));
		assertFalse(">\tname\t\n".matches(fastaNameSeqRegEx));
		assertFalse(">\t".matches(fastaNameSeqRegEx));
		
		
		//fasta file sequence
		
		String oneFastaSection = "\\s*" + fastaNameSeqRegEx + "\\s*\n+" + "[ACGTacgt]+" ;
		String fastaSeqRegEx = "(" + oneFastaSection + "[ \t\r]*\n" + ")*" + oneFastaSection + "\\s*" ;
		
		assertTrue("> Y105E8B.1c\nacgtacgtacggttacCTTACAAAATTGGGAAATCTGGAAAACCGAGC".matches(fastaSeqRegEx));
		assertTrue("> negExSeq_0_B0304.1\nCCACGCTTGATATATGGA\n".matches(fastaSeqRegEx));
		assertTrue("> negExSeq_0_B0304.1\nCCACGCTTGATATATGGAAGCGTACAACAGGCATTATTCCATC\n>C02B8.4\nCTTATTTCAGGAAAATTTTTTCAAA".matches(fastaSeqRegEx));
		assertTrue("\t\t\t\t\t\n> negExSeq_0_B0304.1\nCCACGCTTGATATATGGAAGCGTACAACAGGCATTATTCCATC\n\t\t\t\t\n> C02B8.4\nCTTATTTCAGGAAAATTTTTTCAAAA\n\t\t  \n  \t".matches(fastaSeqRegEx));
		assertTrue("\t\t\t\t\r\n> negExSeq_0_B0304.1\r\nCCACGCTTGATATATGGAAGCGTACAACAGGCATTATTCCATC\n\t\t\t\t\n> C02B8.4\nCTTATTTCAGGAAAATTTTTTCAAAA\n\t\t  \n  \t".matches(fastaSeqRegEx));
		String realOutput = "\t\t\t\t\t\r\n> B0304.1\r\nCTTATTTCAGGGAA\r\n\r\n> F29F11.5\r\nGCCACATGG\r\n\r\n\t\t\t";
		assertTrue(realOutput.matches(fastaSeqRegEx));
	
		
		
		assertFalse("".matches(fastaSeqRegEx));
		assertFalse("> Y105E8B.1c\n".matches(fastaSeqRegEx));
		assertFalse("> Y105E8B.1c\nAGTATAATGACAACTTACAAAzTTGGGAAATCTGGAAAACCGAGC".matches(fastaSeqRegEx));
		assertFalse("> Y105E8B.1c acgtacgtacggttacCTTACAAAATTGGGAAATCTGGAAAACCGAGC".matches(fastaSeqRegEx));
		assertFalse("> negExSeq_0_B0304.1\nCCACGCTTGATATATGGAAGCGTACAACAGGCATTATTCCATC >C02B8.4\nCTTATTTCAGGAAAATTTTTTCAAA".matches(fastaSeqRegEx));
		assertFalse("> negExSeq_0_B0304.1\nCCACGCTTGATATATGGAAGCGTACAACAGGCATTATTCCATC\n\nCTTATTTCAGGAAAATTTTTTCAAA".matches(fastaSeqRegEx));
		assertFalse("".matches(fastaSeqRegEx));
		
		
		// pwm line: 

		String pwmLineRegEx1 = "[ACGTacgt][ \t]*\\|([ \t]*\\d+)+[ \t]*";
		
		assertTrue("A | 2 26 7".matches(pwmLineRegEx1));
		assertTrue("A |2 26 7".matches(pwmLineRegEx1));
		assertTrue("A| 2 26 7".matches(pwmLineRegEx1));
		assertTrue("A\t| \t2 \t\t\t26 7\t\t ".matches(pwmLineRegEx1));

		assertFalse("".matches(pwmLineRegEx1));
		assertFalse(" \t \t ".matches(pwmLineRegEx1));
		assertFalse("abra-cadabra".matches(pwmLineRegEx1));
		assertFalse("> test A | 1 2 3".matches(pwmLineRegEx1));
		assertFalse("A 2 26 7".matches(pwmLineRegEx1));
		assertFalse("A | 2 A 7".matches(pwmLineRegEx1));
		assertFalse("A 2 26 7".matches(pwmLineRegEx1));
		assertFalse("A [ 2 26 7]".matches(pwmLineRegEx1));
		
		
		String pwmLineRegEx2 = "[ACGTacgt][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*";

		assertTrue("A [ 2 26 7]".matches(pwmLineRegEx2));
		assertTrue("A\t[\t2\t26\t7\t]".matches(pwmLineRegEx2));
		assertTrue("A[ 2 26 7]".matches(pwmLineRegEx2));
		
		assertFalse("".matches(pwmLineRegEx2));
		assertFalse(" \t \t ".matches(pwmLineRegEx2));
		assertFalse("abra-cadabra".matches(pwmLineRegEx2));
		assertFalse("> test A | 1 2 3".matches(pwmLineRegEx2));
		assertFalse("A 2 26 7".matches(pwmLineRegEx2));
		assertFalse("2 26 7".matches(pwmLineRegEx2));
		assertFalse("A | 2 A 7".matches(pwmLineRegEx2));
		assertFalse("A 2 26 7".matches(pwmLineRegEx2));
		assertFalse("A 2 26 7".matches(pwmLineRegEx2));
		assertFalse("A [ 2 26 7".matches(pwmLineRegEx2));
		assertFalse("A [ ]".matches(pwmLineRegEx2));
		assertFalse("A | 2 26 7".matches(pwmLineRegEx2));
		
		String pwmRegEx1 = "[Aa][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
				"[Cc][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
				"[Gg][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*\n" +
				"[Tt][ \t]*\\|([ \t]*\\d+)+[ \t]*\\s*";
		
		assertTrue("A | 2 26 7\nC | 2 26 7\nG | 2 26 7\nT | 2 26 7".matches(pwmRegEx1));
		assertTrue("A | 2 26 7\nC | 2 26 7	\nG | 2 26 7\nT |	2		26 7\r\n".matches(pwmRegEx1));

		assertFalse("".matches(pwmRegEx1));
		assertFalse("A | 2 26 7\nC | 2 26 7\nG | 2 26 7".matches(pwmRegEx1));
		assertFalse("A  2 26 7\nC | 2 26 7	\nG | 2 26 7\nT |	2		26 7\r\n".matches(pwmRegEx1));

	
		String pwmRegEx2 = "[Aa][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Cc][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Gg][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*\n" +
		"[Tt][ \t]*\\[([ \t]*\\d+)+[ \t]*\\][ \t]*\\s*";

		assertTrue("A [ 2 26 7]\r\nC [ 2 26 7]\r\nG [ 2 26 7]\nT [ 2 26 7]".matches(pwmRegEx2));
		assertTrue("A [\t2 26 7]\r\nC [ 2 26 7]\r\nG [ 2 26 7]\nT [ 2 26 7]\n".matches(pwmRegEx2));

		assertFalse("".matches(pwmRegEx2));
		assertFalse("A [ 2 26 7]\r\nC [ 2 26 7]\r\nG [ 2 26 7]\nT [ 2 26 7".matches(pwmRegEx2));
		assertFalse("A [ 2 26 7]\r\nC [ 2 26 7]G [ 2 26 7]\nT [ 2 26 7]".matches(pwmRegEx2));
		assertFalse("A [ 2 26 7]\r\nC [ 2 26 7]\r\nG [ 2 26 7]\nA [ 2 26 7]".matches(pwmRegEx2));
		assertFalse("\tA [ 2 26 7]\r\nC [ 2 26 7]\r\nG [ 2 26 7]\nT [ 2 26 7]".matches(pwmRegEx2));
		
	}
}
