package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.RegulatoryElementPWM;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.services.PatserRegElementService;


public class JasparPwmTools {
	
	public static void main(String[] args) {
		
		try {
			String pwmDir = SystemVariables.getInstance().getString("workspace.dir") + "ModuleInducer/data/Jaspar/allMatrices/";
			//String allPwmsFile = SystemVariables.getInstance().getString("workspace.dir") + "ModuleInducer/data/Jaspar/matrix_only.txt";
			
			// *** Create separate pwm file in a format, accepted by Patser service (only need to do it once)
			//FileInputStream inStream = new FileInputStream(allPwmsFile);
			//FileHandling.saveAndConvertPwmFiles(pwmDir, inStream);
			
			// *** Load matrix info into array of objects
			File pwmDirFile = new File(pwmDir);	
			String tmpJobDir = SystemVariables.getInstance().getString("temp.output.dir"); //TODO create a proper tmp job dir for experiments
			
			PatserRegElementService serv = new PatserRegElementService(pwmDirFile, tmpJobDir);
			ArrayList<RegulatoryElementPWM> pwmObjects = serv.getRegulatoryElementsPWMs();
			
			
			// *** Do the matching to strings from the artice
			
			int numMatchesToPrint =8;
			
			String runx = "VCCACA";
			pwmObjects = getMatchScoreForSequence(runx, pwmObjects);
			Collections.sort(pwmObjects);
			System.out.println("----------------------------------------------------");
			System.out.println("  Top highest scoring matrices for Runx (" + runx + "):");
			for (int i = 0; i < numMatchesToPrint; i++){
				System.out.println("   " + pwmObjects.get(i).getName() + "  (score = "+pwmObjects.get(i).getScore()+")");
			}

			
			//String gata = "CTTATCW";
			String gata = "GATAA"; // = "WNGATAA";
			pwmObjects = getMatchScoreForSequence(gata, pwmObjects);
			Collections.sort(pwmObjects);
			System.out.println("----------------------------------------------------");
			System.out.println("  Top highest scoring matrices for Gata (" + gata + "):");
			for (int i = 0; i < numMatchesToPrint; i++){
				System.out.println("   " + pwmObjects.get(i).getName() + "  (score = "+pwmObjects.get(i).getScore()+")");
			}

			
			String ets = "CAGGAARY";
			pwmObjects = getMatchScoreForSequence(ets, pwmObjects);
			Collections.sort(pwmObjects);
			System.out.println("----------------------------------------------------");
			System.out.println("  Top highest scoring matrices for Ets (" + ets + "):");
			for (int i = 0; i < numMatchesToPrint; i++){
				System.out.println("    " + pwmObjects.get(i).getName() + "  (score = "+pwmObjects.get(i).getScore()+")");
			}

			
			String eBox = "CAGVTG";
			pwmObjects = getMatchScoreForSequence(eBox, pwmObjects);
			Collections.sort(pwmObjects);
			System.out.println("----------------------------------------------------");
			System.out.println("  Top highest scoring matrices for E-Box (" + eBox + "):");
			for (int i = 0; i < numMatchesToPrint; i++){
				System.out.println("    " + pwmObjects.get(i).getName() + "  (score = "+pwmObjects.get(i).getScore()+")");
			}

			System.out.println("----------------------------------------------------");
		
		} catch (DataFormatException e) {
			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		}
	}
	
	
	public static ArrayList<RegulatoryElementPWM> getMatchScoreForSequence(String sequence, ArrayList<RegulatoryElementPWM> pwmObjects) throws DataFormatException{
		for (int i = 0; i < pwmObjects.size(); i++) {
			
			double score = DataModeller.matchSequenceToPwm(sequence, pwmObjects.get(i));
			
			pwmObjects.get(i).setScore(score);

		}
		
		return pwmObjects;
	}

}
