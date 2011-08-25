package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.interfaces.RegulatoryRegionService;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;

/* 
 */
public class CustomDataRegRegionService implements RegulatoryRegionService {
	
	private ArrayList<Feature> positiveRegRegions;
	private ArrayList<Feature> negativeRegRegions;
	
	
	public CustomDataRegRegionService(InputStream posExIn, InputStream negExIn) throws DataFormatException, IOException{
		positiveRegRegions = this.readRegulatoryRegions(posExIn);
		negativeRegRegions = this.readRegulatoryRegions(negExIn);
		
		validateRegions(positiveRegRegions, negativeRegRegions);
		
		//DataModeller.generateSimulatedRegulatoryRegions(positiveRegRegions, negExMultiplicationFactor, NEGATIVE_REGION_PREFIX);
	}
	
	public CustomDataRegRegionService(String posSequeces, String negSequences) throws DataFormatException, IOException{
		positiveRegRegions = this.formatRegulatoryRegions(posSequeces);
		negativeRegRegions = this.formatRegulatoryRegions(negSequences);
		
		validateRegions(positiveRegRegions, negativeRegRegions);
		
		//DataModeller.generateSimulatedRegulatoryRegions(positiveRegRegions, negExMultiplicationFactor, NEGATIVE_REGION_PREFIX);
	}
	
	public CustomDataRegRegionService(String posSequeces, int negExMultiplicationFactor) throws DataFormatException, IOException{
		positiveRegRegions = this.formatRegulatoryRegions(posSequeces);
		negativeRegRegions = FeaturesTools.generateSimulatedRegulatoryRegions(positiveRegRegions, negExMultiplicationFactor, "sim_");
		
		validateRegions(positiveRegRegions, negativeRegRegions);
		
		//DataModeller.generateSimulatedRegulatoryRegions(positiveRegRegions, negExMultiplicationFactor, NEGATIVE_REGION_PREFIX);
	}
	
	
	private void validateRegions(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions) throws DataFormatException{
		
		int maxSeqNum = Integer.parseInt(SystemVariables.getInstance().getString("regRegion.max.num.of.sequences"));
		
		if (positiveRegRegions.size() > maxSeqNum || negativeRegRegions.size() > maxSeqNum){
			
			throw new DataFormatException("Number of sequences has exceeded the limit of " +
					maxSeqNum + " sequences.");
		}
		
		if (!FeaturesTools.areNamesDistinct(positiveRegRegions, negativeRegRegions)){
			throw new DataFormatException("Positive and negative sequences should have unique names.");
		}
	}
	
	@Override
	public ArrayList<Feature> getPositiveRegulatoryRegions(){
		return positiveRegRegions;
	}
	@Override
	public void setPositiveRegulatoryRegions(ArrayList<Feature> regRegions) {
		positiveRegRegions = regRegions;
	}
	
	@Override
	public ArrayList<Feature> getNegativeRegulatoryRegions(){
		return negativeRegRegions;
	}
	@Override
	public void setNegativeRegulatoryRegions(ArrayList<Feature> regRegions) {
		negativeRegRegions = regRegions;
	}

	private ArrayList<Feature> readRegulatoryRegions(InputStream in) throws IOException, DataFormatException{
	
		ArrayList<Feature> regRegion = new ArrayList<Feature>();
		
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		//regRegion.add(new Feature("B0304.1", Feature.TYPE_REGULATORY_REGION, "II", 4519057, 4522425, "cttatttcaggaaaattttttcaaaactgtaaaacaaaaaccatttttcacagaatctaagggtatctgaaagcttaaaataacttcagaaagatatcaattccagctgtttagtacctgaactgtctgtaaacgtttcttctcgaattatagaaaattttccactttttcaagttcaggttttcaagaaaccccacgatttccactcatcgtttccaatgtccaatttcccatccaatttgccgcactctgacccaatgacttgttcctttgccaatcaatgctacctaataaatttaaaagtttaacacgcatccaattgacacacaggtaaccgccctttcttcttttacataattcggaaacttcaaagagccgaaggtgtcggttgtagcagcagcggaggaacggatgccaattgcgcaactctcggctcaactcttttagtgctccgagagcaggaagaagaatactgttgggttgtaataaagacggatgtttttgttcagagtagattagctcgtgtttgattggatttgaccggatcaagaggggaatgtcctggtggaattaaattttattagaataaattgtatttggtgtttaaattcgaatcaataatatttatgagctttaatgaataagtgttagattatataattctataatttttgaacaagcaattcaaaaagaaaacaaattttagtaacctccgaaatcaagctgggtggcctctagaagttttgaaaaaactttttttatattctgttggagtttttttaagttttataattataggttaatctttctaatttgtaagctttttcttaaccaattttttttgttaacatttttttggaattatgctatatgacctatacctaaaacagtttaaaagtttaaaaaaattttctatatttttcacttcgtattgaacctcctgggtacatatattgacagcacatatcttgtttgtctcagattttatcaaataagtttaacaagtaaaccatgcaccaaatatttttctaggtctctgtagttaggaaatatttaataaaaataaaaataaccgagatatgagccatcaaagtagatcaattaaggcacaggaaaaaagatctgaataaaatcgaagttcttaaaaatataaatcaaacaaaattttttccagaatttcagccgagaatttccagccgatttgtttatattttccacatcactccccacacttctctcacacaaacacgataaaatcttgagaagcaattagcgccaatcaactcaacacaaaaacgaaaagccaacgaaaagctaaagctatcatcgttgtcgcgtctatgagcaactcaatcgttcatcatcctcatcttcagagtgctcaaacctaccgtaacccgaattgggcggagccaaagggtccgaaacagtgcaccagggcggggagggaccctgagaaacgagagggaagtgagcaattgttgaagtgtcagttgtgctcatcgaggtccgatgaagagacgcgcctgctcacctacacaactgacttcccccatatacctttttctagaatttccttttttagatttatacggtcaggtaaaaaggtagagttttacagtgtagaaattaggaaattgctcaaaaagccgagcagaatgcatataagaagtaccatagccccaaagattcgattttccagggttcaatcaatttttgtactttgacagcgtatatctcagttttctttgattttatcaaaaactagtcaactgacaaaatacttgaaaagtattcctttatattttggtagctgaccattgtttgttaaaatataagggaatcgaaatgtcggttatcaaagtagaacctaacctaaatcgctatatatgctatttttcaaaaaaaaaaacacgttttactttgtctcaacttattaatattctttaatattttttctatttctaccattttccaaattttccaataattttccagaa", 0.0));

		String line = null;			
		String sequenceName = "";

		while ((line = input.readLine()) != null) {

			if (!(line.matches("\\s*"))){ //ignore blank lines
				
				if (sequenceName.isEmpty()) { // expecting and annotated line

					StringTokenizer st = new StringTokenizer(line, " \t");
					String token = st.nextToken();
					
					// Check for the line to start with ">" and read the first token after it. The rest of the line is ignored
					if (!">".equals(token)){
						throw new DataFormatException("Sequence file is not in the correct format. Each sequence should be preceded by a line starting with \"> sequenceName\"");
					}
					
					if (st.hasMoreTokens()){
						sequenceName = st.nextToken();
					}else {
						throw new DataFormatException("Sequence file is not in the correct format. Sequence name should follow \">\".");
					}
					
				} else { //expecting a sequence line

					//TODO temp restriction on sequence length
					int maxSeqLen = Integer.parseInt(SystemVariables.getInstance().getString("regRegion.max.sequence.length"));
					if (line.length() > maxSeqLen){						
						throw new DataFormatException("Sequence format exception: individual sequence length is temporarily limited to "+maxSeqLen+" nucleotides.");
					}
					
					if (line.matches("[ACGTacgt]*")){
						regRegion.add(new Feature(sequenceName, Feature.TYPE_REGULATORY_REGION, "", 0, 0, line, 0.0));
						sequenceName = "";
					} else {
						throw new DataFormatException("Some of the provided sequences are not in the correct format. Expecting {A,C,G,T,a,c,g,t}.");
					}
				}
				
			}
		}
		
		return regRegion;
	}

	private ArrayList<Feature> formatRegulatoryRegions(String regSequences) throws IOException, DataFormatException{
		
		ArrayList<Feature> regRegion = new ArrayList<Feature>();
		
		StringTokenizer stMain = new StringTokenizer(regSequences, "\n\r");
		String line = null;			
		String sequenceName = "";

		while (stMain.hasMoreTokens()) {
			line = stMain.nextToken().trim();

			if (!(line.matches("\\s*"))){ //ignore blank lines
				
				if (sequenceName.isEmpty()) { // expecting and annotated line
					
					if (!line.startsWith(">")){
						throw new DataFormatException("Sequence file is not in the correct format. Each sequence should be preceded by a line starting with \"> sequenceName\"");
					}
					
					line = line.substring(1); //remove > from the beginning of the line

					// Tockenize the string and read the first token as a sequence name
					StringTokenizer st = new StringTokenizer(line, " \t");
					
					if (st.hasMoreTokens()){
						sequenceName = st.nextToken().trim();
					}else {
						throw new DataFormatException("Sequence file is not in the correct format. Sequence name should follow \">\".");
					}
					
				} else { //expecting a sequence line

					//TODO temp restriction on sequence length
					int maxSeqLen = Integer.parseInt(SystemVariables.getInstance().getString("regRegion.max.sequence.length"));
					if (line.length() > maxSeqLen){						
						throw new DataFormatException("Sequence format exception: individual sequence length is limited to "+maxSeqLen+" nucleotides.");
					}
					
					if (line.matches("[ACGTacgt]+")){
						regRegion.add(new Feature(sequenceName, Feature.TYPE_REGULATORY_REGION, "", 0, 0, line, 0.0));
						sequenceName = "";
					} else {
						throw new DataFormatException("Some of the provided sequences are not in the correct format. Expecting {A,C,G,T,a,c,g,t}.");
					}
				}
				
			}
		}
		
		return regRegion;
	}


	@Override
	public void updateNumberOfPositiveRegRegions(int num) throws DataFormatException {
		throw new DataFormatException("Can not update the number of positive regulatory regions. The regions are as supplied.");
		
	}

	@Override
	public void updateNumberOfNegativeRegRegions(int multiplicationFactor) throws DataFormatException{
		throw new DataFormatException("Can not update the number of negative regulatory regions. The regions are as supplied.");
	}



}

