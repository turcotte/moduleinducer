package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.Experimenter;

/* Collection of static methods that deal with formatting of the project's specific data,
 * which has a generic format. Ex. massaging Aleph's output, saved as a String, etc.
 */
public class DataFormatter {
	
	/* Extract motif hits from FIMO output (as gff)  
	 * @param gffFileName - full path to a gff file
	 */
	public static ArrayList<Feature> extractRegElementFromGff(String gffFileName){
		ArrayList<Feature> motifHits = new ArrayList<Feature>();
		
		File file = new File(gffFileName);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine(); // read the first line - it's a gff-version line; no data
			
			while ((line = br.readLine()) != null){
				motifHits.add(extractFeatureFromGff(line));
			}
		
			br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return motifHits;
	}
	
	/*
	 * Given one line from a gff file, creates and populates a Feature object with the gff data
	 */
	public static Feature extractFeatureFromGff(String gffLine){
		Feature feature = new Feature();
		
		String[] gffParams = gffLine.split("[\\s;]+");

		feature.setType(gffParams[2]);
		feature.setParent(gffParams[0]);
		feature.setStartPosition(Integer.parseInt(gffParams[3]));
		feature.setEndPosition(Integer.parseInt(gffParams[4]));
		if ("-".equals(gffParams[6])){
			feature.setStrand("R");
		} else {
			feature.setStrand("D");
		}
		feature.setName(gffParams[8].substring(11));
		// score is a p-value
		feature.setScore(Double.parseDouble(gffParams[9].substring(7)));
		
		
		return feature;
	}

	/* Extract motif hits from MAST output (as xml)  
	 * @param xmlFileName - full path to an xml file
	 */
	public static ArrayList<Feature> extractRegElementsFromXml(String xmlFileName){
		ArrayList<Feature> motifHits = new ArrayList<Feature>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(xmlFileName);
			
			//get the root element
			Element docEle = dom.getDocumentElement();

			//System.out.println ("Root element of the doc is " + docEle.getNodeName());

			NodeList sequenceElList = docEle.getElementsByTagName("sequence");
			//System.out.println("Number of sequences in the document: " + sequenceElList.getLength());
			
			if(sequenceElList != null && sequenceElList.getLength() > 0) {
				for (int i = 0 ; i < sequenceElList.getLength();i++) {
					
					//get the sequence element
					Element seqEl = (Element)sequenceElList.item(i);		
					
					NodeList hitElList = seqEl.getElementsByTagName("hit");
					
					//System.out.println("Number of hits for sequence " + i +" : " + hitElList.getLength());
					
					for (int j = 0; j < hitElList.getLength(); j++){
						Element hitEl = (Element)hitElList.item(j);
						
						//System.out.println("Tag name: "+ hitEl.getTagName()+ "   Node name: " +hitEl.getNodeName());
						
						Feature hit = new Feature("TF_binding_site");
						hit.setName(hitEl.getAttribute("motif")); //TODO: perhaps print the actual motif sequence
						hit.setParent(seqEl.getAttribute("name"));
						if ("reverse".equals(hitEl.getAttribute("strand"))){
							hit.setStrand("R");
						} else {
							hit.setStrand("D");
						}
						hit.setStartPosition(Integer.parseInt(hitEl.getAttribute("pos")));
						hit.setEndPosition(hit.getStartPosition() + 7); //TODO: fix
						hit.setScore(Double.parseDouble(hitEl.getAttribute("pvalue")));
						
						motifHits.add(hit);
					}
				}
			}



		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}

		
		return motifHits;
	}
	
	
	
	/* Extracts the rules of resulting theory from the full Aleph output.
	 * Each rule is placed separately into the ArrayList.
	 */
	public static ArrayList<String[]> extractTheoryByRules(String alephOutput){
		ArrayList<String[]> rules = new ArrayList<String[]>();
		
		StringTokenizer st = new StringTokenizer(alephOutput, "\n");
		
		boolean readingTheory = false;
		String rule = "";
		
		while (st.hasMoreTokens()){
			String alephLine = st.nextToken();
			
			if (alephLine.startsWith("[theory]")){
				// we've reached the theory
				readingTheory = true;
				continue;
			} else if (alephLine.startsWith("[Training set performance]")){
				// reached end of theory
				readingTheory = false;
				// add the last remaining rule
				if (!rule.isEmpty()){
					String[] terms = Experimenter.tokeniseAndTransform(rule);
					rules.add(terms);
				}
			}
			
			if (readingTheory){
				if (alephLine.startsWith("[Rule")){
					if (!rule.isEmpty()){
						String[] terms = Experimenter.tokeniseAndTransform(rule);
						rules.add(terms);
					}
					rule = "";
				} else {	
					rule = rule.concat(alephLine);
				}
			}
		}
		
		return rules;
	}
	
	/* Extracts only theory from the full Aleph output.
	 * Each rule is placed separately into the ArrayList.
	 */
	public static String extractTheoryAndPerformance(String alephOutput){
		String theoryStr = "";
		
		StringTokenizer st = new StringTokenizer(alephOutput, "\n");
		
		boolean readingTheory = false;
		while (st.hasMoreTokens()){
			String alephLine = st.nextToken();
			
			if (alephLine.startsWith("[theory]")){
				readingTheory = true;
			}
			
			if (readingTheory){				
				theoryStr = theoryStr + alephLine + "\n";
			}
		}
		
		return theoryStr;
		
	}

	/* Extracts results of the test run from Aleph output in the form of confusion matrix.
	 * The matrix is a 1D array of the form: [TP, FP, FN, TN]
	 */
	public static int[] extractTestConfusionMatrix(String alephOutput){
		int[] confArray = new int[4];
		
		
		StringTokenizer st = new StringTokenizer(alephOutput, "\n");
		
		
		while (st.hasMoreTokens()){
			String alephLine = st.nextToken();
			
			if (alephLine.startsWith("[Test set summary]")){
				int from = alephLine.indexOf("[[") + 2;
				int to =  alephLine.indexOf("]]");
				
				String intLine = alephLine.substring(from, to);
				StringTokenizer st2 = new StringTokenizer(intLine, ", ");
				
				int i = 0;
				while(st2.hasMoreTokens()){
					String intStr = st2.nextToken();
					confArray[i] = Integer.parseInt(intStr);
					i++;
				}
				
				break;
			}
		}	
		
		
		return confArray;
	}
	
	/* Formats an int[][] array of PSSM to a string.
	 * Tabs are placed in between the elements
	 */
	public static String formatPssm(int[][] pssm){
		String pwmStr = "";
		
		for (int j = 0; j < pssm[0].length; j++){
			
			String nt = "?";
			
			switch (j) {
			case 0:
				nt = "A"; break;
			case 1:
				nt = "C"; break;
			case 2:
				nt = "G"; break;
			case 3:
				nt = "T"; break;
			default:
				break;
			}
			
			pwmStr = pwmStr + nt + "\t|";
			
			for (int i = 0; i < pssm.length; i++){
				pwmStr = pwmStr + "\t" + pssm[i][j];
			}
			
			if (j < 3){
				pwmStr = pwmStr + "\n";
			}
			
		}
		
		return pwmStr;
	}

}
