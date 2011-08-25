package ca.uottawa.okorol.bioinf.ModuleInducer.tools;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;



public class FastaExtractor {
	private String fastaFileName;
	
	public FastaExtractor(String fastaFileName){
		this.fastaFileName = fastaFileName;
	}
	
	/* This method deals with a file that has more than one chromosome in it
	 * String is numbered starting from 1;
	 * For the sequence actgactg beginIndex = 1 and endIndex 4 will return substring actg 
	 */
	public String extractSubSequence(String chromosome, int beginIndex, int endIndex){
		 String line = "";
		 String resultString = "";
		 
		 if (beginIndex > endIndex) return "error";
		 beginIndex--; //to adjust to java numbering of chars in the string
		 endIndex--;
		 
		 int chrLen = 0;
		 
		try {
			BufferedReader br = new BufferedReader(new FileReader(fastaFileName));
			
			boolean readIsOn = false; //turns on when we reach the chromosome we need to read
			while ((line = br.readLine()) != null){
				if (readIsOn && line.startsWith(">")){
					readIsOn = false;
					
				}
				
				if (readIsOn){
					chrLen = chrLen + line.length();

					if (beginIndex >= 0 && beginIndex < line.length()){
						if (endIndex < line.length()){
							resultString = line.substring(beginIndex, endIndex +1);
							break;
						} else {
							resultString = line.substring(beginIndex);
						}
					} else if (beginIndex < 0){
						if (endIndex < line.length()){
							resultString = resultString + line.substring(0, endIndex + 1);
							break;
						} else {
							resultString = resultString + line;
						}
					}
					beginIndex = beginIndex - line.length();
					endIndex = endIndex - line.length();

				}
				
				//if (line.startsWith(">" + chromosome)){
				if (line.startsWith(">")){
					line = line.substring(1); //remove >
					String[] lineTokens = line.split("[\\s\\t]");
					for (int i = 0; i < lineTokens.length; i++) {
						if (lineTokens[i].equals(chromosome)){
							readIsOn = true;
							break;
						}
					}
				}
				
			}

			//System.out.println("ChrLen: " + chrLen);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultString;
	}
	
	/* This method deals with a fasta file that has only one chromosome per file (like human genome). fastaFileName is a name of the directory that contains genome files.
	 * @param ArrayList<Feature> a set of features that have the chromosome name (which is also a name of a file containing a sequence for this chromosome) and begin and end index of the subsequence
	 * @return same set of features, but with the sequence information filled in
	 */
	public ArrayList<Feature> extractSubSequence(ArrayList<Feature> features){
		
		 
		try {
			String currChromosome =  features.get(0).getNote();
			BufferedReader br = new BufferedReader(new FileReader(fastaFileName + currChromosome + ".fa"));
			int currReadingPos = 0; //should start with 1
			
			
			for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
				Feature feature = (Feature) iterator.next();
				String resultString = "";
				
				if (!currChromosome.equals(feature.getNote())){
					br.close();
					currChromosome = feature.getNote();
					br = new BufferedReader(new FileReader(fastaFileName + currChromosome + ".fa"));	
					currReadingPos = 0;
				}
				
				
				int chCode;
				while((chCode = br.read()) != -1){
					char ch = (char)chCode;
					//skip line with annotation
					if (ch == '>'){
						br.readLine(); 
					} else {
					
						if (ch != ' ' && ch != '\t' && ch != '\n') { // no tabs, spaces, or \n
							currReadingPos++;
							
							if (currReadingPos >= feature.getStartPosition()){
								resultString = resultString + ch;
								//System.out.println(chCode +"   "+ ch);

								if (currReadingPos == feature.getEndPosition()) break;
							}
						}
					}
				}
				
				feature.setSequence(resultString);
			
			}
			
			br.close();
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return features;
		
	}	


}
