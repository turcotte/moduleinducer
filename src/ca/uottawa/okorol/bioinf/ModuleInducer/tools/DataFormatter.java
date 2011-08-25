package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.util.ArrayList;
import java.util.StringTokenizer;

import ca.uottawa.okorol.bioinf.ModuleInducer.services.Experimenter;

/* Collection of static methods that deal with formatting of the project's specific data,
 * which has a generic format. Ex. massaging Aleph's output, saved as a String, etc.
 */
public class DataFormatter {

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
