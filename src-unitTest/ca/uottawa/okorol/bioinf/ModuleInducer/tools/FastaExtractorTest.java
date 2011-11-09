package ca.uottawa.okorol.bioinf.ModuleInducer.tools;
import java.util.ArrayList;

import junit.framework.TestCase;
import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;


public class FastaExtractorTest extends TestCase {
	FastaExtractor fixture;
	
	public void testExtractSubSequence(){
		fixture = new FastaExtractor("data/testData/test.dna.fa");
		String expected;
		String actual;
		
		expected = "GCCTA";
		actual = fixture.extractSubSequence("ChrI", 1, 5);
		assertEquals(expected, actual);

		expected = "AGCCTAA";
		actual = fixture.extractSubSequence("1", 48, 54);
		assertEquals(expected, actual);
		
		expected = "taAGC";
		actual = fixture.extractSubSequence("chr1", 46, 50);
		assertEquals(expected, actual);
		
		
		expected = "taagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaagcctaag";
		actual = fixture.extractSubSequence("I", 148, 247);
		assertEquals(expected, actual);
		
		expected = "CCTTGCGCTC";
		actual = fixture.extractSubSequence("V", 3, 12);
		assertEquals(expected, actual);
			
	}
	
	public void testExtractSubSequence2(){
		fixture = new FastaExtractor("data/testData/");
		
		ArrayList<Feature> expected = new ArrayList<Feature>();
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr1", 1, 5, "", 0.0));
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr1", 9, 13, "", 0.0));
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr2", 6, 13, "", 0.0));
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr2", 22, 22, "", 0.0));
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr3", 1, 12, "", 0.0));
		expected.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr3", 13, 14, "", 0.0));
		
		ArrayList<Feature> actual = new ArrayList<Feature>();
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr1", 1, 5, "12345", 0.0));
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr1", 9, 13, "90123", 0.0));
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr2", 6, 13, "fghijklm", 0.0));
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr2", 22, 22, "z", 0.0));
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr3", 1, 12, "ABCDEFGHIJKL", 0.0));
		actual.add(new Feature("", Feature.TYPE_REGULATORY_REGION, "chr3", 13, 14, "MN", 0.0));
		

		expected = fixture.extractSubSequence(expected);
		
		FeaturesTools.printFeatures(actual);
		
		for (int i = 0; i < actual.size(); i++) {
			if (actual.get(i).compareTo(expected.get(i)) != 0) fail();
		}

		//assertTrue(expected.equals(actual));
		
	}
	

	// This test works, but runs for too long, so it is commented out. 
/*	
	public void testCompareToBioJava(){
		FastaExtractor myExtractor = new FastaExtractor("data/testData/c_elegans.WS214.dna.fa");
		long startTime = System.currentTimeMillis();
		String myValue = myExtractor.extractSubSequence("X", 12467223, 12475264);
		long endTime = System.currentTimeMillis();
		long myExecTime = endTime - startTime;
		
		BioJavaFastaExtractor extractor = new BioJavaFastaExtractor("data/testData/c_elegans.WS214.dna.fa");
		startTime = System.currentTimeMillis();
		String biojavaValue = extractor.extractSubSequence("CHROMOSOME_X", 12467223, 12475264);
		endTime = System.currentTimeMillis();
		long biojavaExecTime = endTime - startTime;
		
		assertEquals(myValue, biojavaValue);
		System.out.println("Execution time of my method:      " + myExecTime);
		System.out.println("Execution time of biojava method: " + biojavaExecTime);
	}
*/	
	
	// This is not a test but just a print-out.
/*	
	public void testPrintData(){
		// Test data from the article "Identification of muscle-specific regulatory modules in Caenorhabditis elegans"
		//  by Guoyan Zhao, Lawrence A. Schriefer and Gary D. Stormo
		//  from http://www.genome.org
		 
		// Organism: C.elegans (Taxonomy ID: 6239)
		// Below are the 16 genes used in the research. The program will extract 2000 nt upstream regions for each gene.
		// These regions will be used in the Patser tool (http://rsat.ulb.ac.be/rsat/patser_form.cgi) to match
		// 18 PWMs of the tfbs found by the research (PWMs are in supplementary info)
		
				
//		B0304.1
//		Genomic Position: II:4519057..4522425
//
//		C02B8.4 
//		Genomic Position:  	X:8116581..8120049 bp 
//
//		C09D1.1a 
//		Genomic Position:  	I:4035763..4090962 bp 
//
//		C36E6.5 
//		Genomic Position:  	X:17456509..17455508 bp 
//
//		F07A5.7
//		Genomic Position:  	I:7383194..7376605 bp 
//
//		F11C3.3 
//		Genomic Position:  	I:14863527..14852479 bp
//
//		F29F11.5
//		Genomic Position:  	V:10672434..10675619 bp
//
//		F40E10.3 
//		Genomic Position:  	X:14684796..14688286 bp
//
//		F55B12.1 
//		Genomic Position:  	V:13813289..13815252 bp
//
//		F58A3.2a 
//		Genomic Position:  	X:11016336..11023334 bp
//
//		K12F2.1
//		Genomic Position:  	V:12236664..12226794 bp 
//
//		R06C7.10
//		Genomic Position:  	I:7267077..7258919 bp 
//
//		T18D3.4
//		Genomic Position: X:12467223..12475264 bp 
//
//		W09B12.1
//		Genomic Position:  	X:16367147..16373955 bp
//
//		Y105E8B.1a
//		Genomic Position:  	I:14631241..14620747 bp
//
//		Y105E8B.1c
//		Genomic Position:  	I:14631241..14620747 bp

		
		FastaExtractor fixture = new FastaExtractor("data/c_elegans.WS214.dna.fa");
		String[] geneNames =		{"B0304.1",	"C02B8.4",	"C09D1.1a",	"C36E6.5",	"F07A5.7",	"F11C3.3",	"F29F11.5",	"F40E10.3",	"F55B12.1",	"F58A3.2a",	"K12F2.1",	"R06C7.10",	"T18D3.4",	"W09B12.1",	"Y105E8B.1a",	"Y105E8B.1c"};
		int[] geneStartPosition =	{4519057,	8116581,	4035763,	17456509,	7383194,	14863527,	10672434,	14684796,	13813289,	11016336,	12236664,	7267077,	12467223,	16367147,	14631241,	14631241};
		String[] geneChromosome =	{"II",	"X",	"I",	"X",	"I",	"I",	"V",	"X",	"V",	"X",	"V",	"I",	"X",	"X",	"I",	"I"};
		String upstreamRegion;
		String geneRegion;
		int usrLenght = 2000; //specified in the article
		
		for (int i = 0; i < geneNames.length; i++) {			
			upstreamRegion = fixture.extractSubSequence(geneChromosome[i], geneStartPosition[i] - usrLenght, geneStartPosition[i] - 1);
			System.out.println(">"+geneNames[i]);
			System.out.println(upstreamRegion);
		}
	}
*/
	
}
