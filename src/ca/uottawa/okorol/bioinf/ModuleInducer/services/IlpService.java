package ca.uottawa.okorol.bioinf.ModuleInducer.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import ca.uottawa.okorol.bioinf.ModuleInducer.data.Feature;
import ca.uottawa.okorol.bioinf.ModuleInducer.exceptions.DataFormatException;
import ca.uottawa.okorol.bioinf.ModuleInducer.properties.SystemVariables;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.FeaturesTools;
import ca.uottawa.okorol.bioinf.ModuleInducer.tools.StatAnalyser;

public class IlpService {
	
	protected String ilpDirName; //protected for tests

	
	/*
	 * @param dirName	name of the directory where all ilp files will be created and ilp will be invoked;
	 * 					if null or empty, the default directory from properties is used
	 */
	public IlpService(String tmpDirName) throws DataFormatException{

		if (tmpDirName == null || tmpDirName.isEmpty()){
			throw new DataFormatException("Temporary ILP output directory must be specified.");
		} 
		
		File tmpDir = new File(tmpDirName);
		
		if (!tmpDir.exists()){
			throw new DataFormatException("Temporary ILP output directory " + tmpDirName + " does not exist.");
		}
		
		ilpDirName = tmpDirName;
	}
	
	/* Given positive and negative regulatory elements with their cut-off score,
	 * writes all necessary ilp files in the specified directory.
	 */
	public void createIlpFiles(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions, 
			ArrayList<Feature> positiveRegulatoryElemets, ArrayList<Feature> negativeRegulatoryElements) throws DataFormatException{
		
			
		String bFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".b";
		String nFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".n";
		String fFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".f";
		String theoryFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.theory.file.name");
		
		String macIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.mac.script.file.name");
		String winIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.win.script.file.name");
		
		String htmlResultsFileName = ilpDirName + SystemVariables.getInstance().getString("html.results.file.name");

		//** Write prolog background rules
		writeBRulesFile();
		
		//** Write the rules file .b
		writeBfile(bFileName, posRegRegions, negRegRegions, positiveRegulatoryElemets, 
				negativeRegulatoryElements, theoryFileName);
		
		//** Write positive examples file
		//TODO this takes too long. Improve
		//ArrayList<String> realRegRegionsList = FeaturesTools.getUniqueParentList(positiveRegulatoryElemets);
		writeExamplesFile(fFileName, posRegRegions);
		
		
		//** Write positive examples file
		//TODO this takes too long. Improve
		//ArrayList<String> fakeRegRegionsList = FeaturesTools.getUniqueParentList(negativeRegulatoryElements);
		writeExamplesFile(nFileName, negRegRegions);
		
		//** Write run scripts
		writeMacIlpScript(macIlpScriptFileName);
		writeWinIlpScript(winIlpScriptFileName);
		
		//** Write htlm results page
		writeInitialResultsPage(htmlResultsFileName);
		
		
	}
	
	
	
	/* Creates ILP files for training with testing. The sampling is performed in stratified manner.
	 * We select a particular percent of positive and negative examples (testSetPercent) for testing
	 * at a particular location (runNumber)
	 * 
	 *@param testSetPercent percent of the positive AND negative examples to be selected for testing
	 *@param runNumber if testSetPercent is 10 and runNumber is 1 it means that we select
	 *					second 10% from the set (i.e. from 10% to 20%).
	 */
	public void createIlpFilesWithTestSet(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions, 
			ArrayList<Feature> posRegElements, ArrayList<Feature> negRegElements, 
			double testSetPercent, int runNumber) throws DataFormatException{
		
		String bFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".b";
		String nFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".n";
		String fFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".f";
		
		String posTestSetFileName = SystemVariables.getInstance().getString("ilp.script.test.file.name.prefix") + ".f";
		String negTestSetFileName = SystemVariables.getInstance().getString("ilp.script.test.file.name.prefix") + ".n";
		
		String posTestSetFileFullName = ilpDirName + posTestSetFileName;
		String negTestSetFileFullName = ilpDirName + negTestSetFileName;
		
		String macIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.mac.script.file.name");
		String winIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.win.script.file.name");
		
		//*** Divide data set into training and test:
		//* Positive
		
		ArrayList<String> realRegRegionsList = FeaturesTools.getIDList(posRegRegions);
		int step = (int) Math.round((float)realRegRegionsList.size() * testSetPercent);
		int start = step * runNumber;
		int end = start + step;
		
		ArrayList<String> posTestSet = new ArrayList<String>();
		for (int i = 0; i < realRegRegionsList.size(); i++) {
			if (i >= start && i < end){
				posTestSet.add(realRegRegionsList.get(i));
			}
		}
		realRegRegionsList.removeAll(posTestSet);
		
		//* Negative
		
		ArrayList<String> fakeRegRegionsList = FeaturesTools.getIDList(negRegRegions);
		step = (int) Math.round((float)fakeRegRegionsList.size() * testSetPercent);
		start = step * runNumber;
		end = start + step;
		//TODO extract method
		ArrayList<String> negTestSet = new ArrayList<String>();
		for (int i = 0; i < fakeRegRegionsList.size(); i++) {
			if (i >= start && i < end){
				negTestSet.add(fakeRegRegionsList.get(i));
			}
		}
		fakeRegRegionsList.removeAll(negTestSet);
		
		//** Write training set positive examples file
		writeExamplesFile_redo(fFileName, realRegRegionsList);
		
		
		//** Write training set negative examples file
		writeExamplesFile_redo(nFileName, fakeRegRegionsList);
		
		
		//** Write test set positive examples file
		writeExamplesFile_redo(posTestSetFileFullName, posTestSet);
		
		
		//** Write training set negative examples file
		writeExamplesFile_redo(negTestSetFileFullName, negTestSet);
		
		//** Write prolog background rules
		writeBRulesFile();
		
		//** Write the rules file .b
		writeBfileWithTestSet(bFileName, posRegRegions, negRegRegions,
				posRegElements, negRegElements, 
				posTestSetFileName, negTestSetFileName);
		
		//** Write run scripts
		writeMacIlpScript(macIlpScriptFileName);
		writeWinIlpScript(winIlpScriptFileName);
	}
	
	
	/* Creates ILP files for training with testing. The sampling is performed randomly.
	 * Sampling is performed randomly from a pool of positive + negative examples (i.e. no guarantee of 
	 * ratio of positive to negative examples), except for making sure that at least one positive 
	 * and one negative example is selected
	 * 
	 *@param testSetPercent percent of the positive AND negative examples to be selected for testing
	 */
	public void createIlpFilesWithTestSet(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions, 
			ArrayList<Feature> posRegElemets, ArrayList<Feature> negRegElements, 
			double testSetPercent) throws DataFormatException{
		
		String bFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".b";
		String nFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".n";
		String fFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.script.file.name.prefix") + ".f";
		
		String posTestSetFileName = SystemVariables.getInstance().getString("ilp.script.test.file.name.prefix") + ".f";
		String negTestSetFileName = SystemVariables.getInstance().getString("ilp.script.test.file.name.prefix") + ".n";
		
		String posTestSetFileFullName = ilpDirName + posTestSetFileName;
		String negTestSetFileFullName = ilpDirName + negTestSetFileName;
		
		String macIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.mac.script.file.name");
		String winIlpScriptFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.win.script.file.name");
		
		//*** Divide data set into training and test:
		
		//TODO this takes too long. Improve
		ArrayList<String> realRegRegionsList = FeaturesTools.getUniqueParentList(posRegElemets);
		ArrayList<String> fakeRegRegionsList = FeaturesTools.getUniqueParentList(negRegElements);

		int testSetSize = (int) Math.round((float)(realRegRegionsList.size() + fakeRegRegionsList.size()) * testSetPercent);
		
		ArrayList<String> posTestSet = new ArrayList<String>();
		ArrayList<String> negTestSet = new ArrayList<String>();
		
		Random rand = new Random();
		
		int samplePos = rand.nextInt(realRegRegionsList.size());	
		posTestSet.add(realRegRegionsList.get(samplePos));

		samplePos = rand.nextInt(fakeRegRegionsList.size());
		negTestSet.add(fakeRegRegionsList.get(samplePos));
		
		for (int i = 0; i < testSetSize - 2; i++) {
			//sample from a communal pool of pos and neg
			samplePos = rand.nextInt(realRegRegionsList.size() + fakeRegRegionsList.size());
			
			//decide where to get from and put to a sampled position
			// (assume the positive examples go first)
			if (samplePos < realRegRegionsList.size()){
				posTestSet.add(realRegRegionsList.get(samplePos));
			} else {
				negTestSet.add(fakeRegRegionsList.get(samplePos - realRegRegionsList.size()));
			}
		}

		realRegRegionsList.removeAll(posTestSet);
		fakeRegRegionsList.removeAll(negTestSet);
		
		
		
		//** Write training set positive examples file
		writeExamplesFile_redo(fFileName, realRegRegionsList);
		
		
		//** Write training set negative examples file
		writeExamplesFile_redo(nFileName, fakeRegRegionsList);
		
		
		//** Write test set positive examples file
		writeExamplesFile_redo(posTestSetFileFullName, posTestSet);
		
		
		//** Write training set negative examples file
		writeExamplesFile_redo(negTestSetFileFullName, negTestSet);
		
		
		//** Write prolog background rules
		writeBRulesFile();
		
		//** Write the rules file .b
		writeBfileWithTestSet(bFileName, posRegRegions, negRegRegions, 
				posRegElemets, negRegElements, 
				posTestSetFileName, negTestSetFileName);
		
		//** Write run scripts
		writeMacIlpScript(macIlpScriptFileName);
		writeWinIlpScript(winIlpScriptFileName);
	}
	
	/*
	 * @param fileName name of the file to write positive or 
	 * 					negative examples (either .n or .f)
	 * @param regRegions ArralyList of Features containing regulatory regions
	 */
	private void writeExamplesFile(String fileName, ArrayList<Feature> regRegions){
		
		try {
			// ***** Positive examples
		   BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		   
		   for (Iterator<Feature> iterator = regRegions.iterator(); iterator.hasNext();) {
			   Feature regRegion = (Feature) iterator.next();
			   bw.write("positive('" + regRegion.getId() + "'). \n");
		   }


			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	//TODO remove this and integrate with above
	private void writeExamplesFile_redo(String fileName, ArrayList<String> regionList){
		
		try {
			// ***** Positive examples
		   BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

			for (int i = 0; i < regionList.size(); i++) {
				bw.write("positive('" + regionList.get(i) + "'). \n");
			}

			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/* Writes Aleph background knowledge file (.b)
	 * 
	 * @param fileName	name of the .b file to be written (not: .b extension should be in the file name)
	 * @param posRegulatoryElemets	positive regulatory elements
	 * @param negRegulatoryElements	negative regulatory elements
	 * @param positiveCutOffScore	cut-off score for the positive regulatory elements 
	 * 								(lower than specified will not be written into the file, and as a result 
	 * 								will not be used in the theory induction)
	 * @param negativeCutOffScore	cut-off score for the negative regulatory elements
	 * 								(lower than specified will not be written into the file, and as a result 
	 * 								will not be used in the theory induction)
	 * @param outputTheoryFileName	name of the file (with extension), where the resulting theory will be written;
	 * 								if null or empty string, theory output will not be written to a file
	 */	
	private void writeBfile(String fileName, ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions,
			ArrayList<Feature> posRegulatoryElemets, ArrayList<Feature> negRegulatoryElements, String outputTheoryFileName){
		
		BufferedWriter bw = null;
		
		try {
			
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(getBfileContents(posRegRegions, negRegRegions, posRegulatoryElemets, negRegulatoryElements, outputTheoryFileName));
			
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
	}
	
	

	private void writeBfileWithTestSet(String fileName, ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions, 
			ArrayList<Feature> posRegElemets, ArrayList<Feature> negRegElements, 
			String posExFileName, String negExFileName){
		
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			
			bw.write(":- set(test_pos, '"+ posExFileName + "').\n");
			bw.write(":- set(test_neg, '"+ negExFileName + "').\n");

			bw.write(getBfileContents(posRegRegions, negRegRegions, posRegElemets, negRegElements, "test_theory_output.txt"));
			
		
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}catch (DataFormatException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	private void writeBRulesFile() throws DataFormatException{
		
		String plFileName = ilpDirName + SystemVariables.getInstance().getString("ilp.background.rules.file.name");
		
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(plFileName));
			
			bw.write("% Background model \n\n");
			
			bw.write("chromosome(Seq, Chromosome) :- \n" +
					"	var(Chromosome), !,\n" +
					"	seq(Seq, Chromosome, _, _). \n\n" +

					"chromosome(Seq, Chromosome) :-  \n" +
					"	seq(Seq, Chromosome, _, _).\n\n"
					);

			
			bw.write("has_feature(Seq, F) :- \n" +
					"	has_feature(Seq, F, _, _, _, _). \n\n" +
					
					"has_feature(Seq, F, Strand) :- \n" +
					"	has_feature(Seq, F, _, _, Strand, _). \n\n");
			
			
			bw.write("disjoint(Seq, F1, F2) :- \n" +
					"	has_feature(Seq, F1, Pos1, Len1, Strand, _), \n" +
					"	has_feature(Seq, F2, Pos2, _, Strand, _), \n" +
					"	Pos1+Len1 < Pos2. \n\n" +
					
					"disjoint(Seq, F1, F2) :- \n" +
					"	has_feature(Seq, F1, Pos1, _, Strand, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, Strand, _),\n" +
					"	Pos2+Len2 < Pos1. \n\n");  

			
			bw.write("before(Seq, F1, F2) :- \n" +
					"	has_feature(Seq, F1, Pos1, Len1, 'D', _),\n" +
					"	has_feature(Seq, F2, Pos2, _, 'D', _),\n" +
					"	Pos1+Len1<Pos2.\n\n" +
					
					"before(Seq, F1, F2) :-\n" +
					"	has_feature(Seq, F1, Pos1, _, 'R', _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, 'R', _),\n" +
					"	Pos2+Len2<Pos1. \n\n");
			
/*			
			bw.write("connected(Seq, F1, F2) :-\n" +
					"	overlapping(Seq, F1, F2).\n\n" +
					
					"connected(Seq, F1, F2) :-\n" +
					"	contained(Seq, F1, F2).\n\n");
			
			bw.write("overlapping(Seq, F1, F2) :- \n" +
					"	has_feature(Seq, F1, Pos1, Len1, _, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, _, _),\n" +
					"	Pos1<Pos2,\n" +
					"	Pos1+Len1 >= Pos2,\n" +
					"	Pos1+Len1 =< Pos2+Len2.\n\n" +
					
					"overlapping(Seq, F1, F2) :-\n" +
					"	has_feature(Seq, F1, Pos1, Len1, _, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, _, _),\n" +
					"	Pos2 < Pos1,\n" +
					"	Pos2+Len2 >= Pos1,\n" +
					"	Pos2+Len2 =< Pos1+Len1.\n\n");
			
			bw.write("contained(Seq, F1, F2) :-\n" +
					"	has_feature(Seq, F1, Pos1, Len1, _, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, _, _),\n" +
					"	Pos1 < Pos2,\n" +
					"	Pos2+Len2 < Pos1+Len1.\n\n" +
					
					"contained(Seq, F1, F2) :-\n" +
					"	has_feature(Seq, F1, Pos1, Len1, _, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, _, _),\n" +
					"	Pos2 < Pos1,\n" +
					"	Pos1+Len1 < Pos2+Len2.\n\n");
*/			
			bw.write("distance(Seq, F1, F2, D) :-\n" +
					"	has_feature(Seq, F1, Pos1, Len1, Strand, _),\n" +
					"	has_feature(Seq, F2, Pos2, _, Strand, _),\n" +
					"	Pos1+Len1 < Pos2,\n" +
					"	D is Pos2 - Pos1 - Len1.\n\n" +
					
					"distance(Seq, F1, F2, D) :-\n" +
					"	has_feature(Seq, F1, Pos1, _, Strand, _),\n" +
					"	has_feature(Seq, F2, Pos2, Len2, Strand, _),\n" +
					"	Pos2+Len2 < Pos1,\n" +
					"	D is Pos1 - Pos2 - Len2. \n\n");
			
/*			
			
			bw.write("min_distance(Seq, F1, F2, Dist) :-\n" +
					"	var(Dist), !,\n" +
					"	distance(Seq, F1, F2, Dist).\n\n" +
					
					"min_distance(Seq, F1, F2, Dist) :-\n" +
					"	distance(Seq, F1, F2, D),\n" +
					"	D >= Dist.\n\n");
	
			
			bw.write("max_distance(Seq, F1, F2, Dist) :-\n" +
					"	var(Dist), !,\n" +
					"	distance(Seq, F1, F2, Dist).\n\n" +
					
					"max_distance(Seq, F1, F2, Dist) :-\n" +
					"	distance(Seq, F1, F2, D),\n" +
					"	D =< Dist.\n\n"); 
	*/		
			
			bw.write("distance_interval(Seq, F1, F2, D, Offset) :-\n" +
					"	var(D), !,\n" +
					"	distance(Seq, F1, F2, D),\n" +
					"	range(Offset).\n\n" +
					
					"distance_interval(Seq, F1, F2, D, Offset) :-\n" +
					"	distance(Seq, F1, F2, D1),\n" +
					"	D1 > D-Offset,\n" +
					"	D1 < D+Offset.\n\n" +
					
					"range(0).\n" +
					"range(1).\n" +
					"range(2).\n" +
					"range(3).\n" +
					"range(5).\n" +
					"range(10).\n\n");

			
			bw.write("% Position specific \n\n");
			
			
			bw.write("pos_negative(Seq, Feature) :-\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos < 0.\n\n");
			
			
			bw.write("pos_positive(Seq, Feature) :-\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos > 0.\n\n");

			
			bw.write("pos_lteq(Seq, Feature, Pos) :-\n" +
					"	var(Pos), !,\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos =< 0. \n\n" +
					
					"pos_lteq(Seq, Feature, Value) :-\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos =< Value.\n\n");

			
			bw.write("pos_gteq(Seq, Feature, Pos) :-\n" +
					"	var(Pos), !,\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos >= 0. \n\n" +
					
					"pos_gteq(Seq, Feature, Value) :-\n" +
					"	has_feature(Seq, Feature, Pos, _, _, _),\n" +
					"	Pos >= Value.\n\n");

			bw.write("% Types\n\n");

			bw.write("strand('D'). \n" +
					"strand('R').\n\n\n");
			
			bw.write("% % % % Printing functions to display the sequence information for all the positive examples \n" +
					"% % % % that cover a rule in the theory \n" +
					"% % % % To use:\n" +
					"% % % % 	- screen dispaly: call mi_print right after the theory was induced.\n" +
					"% % % % 	- write to file: call mi_pprint_to_file(FileName) right after the theory was induced.\n" +
					"% % % % \n\n"); 

			bw.write("mi_pprint_to_file(FileName) :- \n" +
					"	telling(Old),\n" +
					"	tell(FileName),\n" +
					"	mi_pprint,\n" +
					"	told,\n" +
					"	tell(Old).\n\n");

			bw.write("mi_pprint :-\n" +
					"	mi_rules_sorted(Rules),\n" +
					"	aleph_member(Cov-RuleNum,Rules),\n" +
					"	'$aleph_global'(theory,theory(RuleNum,_,(positive(A):-Body),_,_)),\n" +
					"	findall(A, Body, As),\n" +
					"	write('-----------------------------------------------------------'), nl,\n" +
					"	write('[Rule '), write(RuleNum), write('] '), write('[Coverage (pos and neg) '), write(Cov), write(']'), nl,\n" +
					"	portray_clause((positive(A):-Body)), nl,\n" +
					"	remove_duplicates(As, Bs),\n" +
					"	mi_pprint_example(Bs), nl, nl,\n" +
					"	fail.\n");
			
			bw.write("mi_pprint.\n\n\n");


			bw.write("%% SortedRls is a list of Key-Value elements where Key is the rule coverage, and Value is rule number \n");
			bw.write("mi_rules_sorted(SortedRls) :- \n" +
					"	'$aleph_global'(rules,rules(R1)),\n" +
					"	aleph_reverse(R1,R2), % don't need this, but Aleph does some magic here\n" +
					"	mi_rules_with_coverage(R2, R3),\n" +
					"	keysort(R3, R4),\n" +
					"	aleph_reverse(R4,SortedRls).\n\n\n");


			bw.write("mi_rules_with_coverage([], []).\n");
			bw.write("mi_rules_with_coverage([H|T1], [Cov-H|T2]) :- \n" +
					"	'$aleph_global'(theory,theory(H,_,(positive(A):-Body),_,_)), \n" +
					"	findall(A, Body, As),\n" +
					"	remove_duplicates(As, Bs),\n" +
					"	length(Bs, Cov),\n" +
					"	%write(Cov), write('-'), write(H),nl,\n" +
					"	mi_rules_with_coverage(T1, T2).\n\n");
				

			bw.write("mi_pprint_example([A|As]) :-\n" +
					"	seq(A, Chr, Start, Len),\n" +
					"	write(Chr), write('\t'),\n" +
					"	write(Start), write('\t'),\n" +
					"	End is Start+Len+1,\n" +
					"	write(End), write('\t'),\n" +
					"	write(A), nl,\n" +
					"	mi_pprint_example(As).\n");
			bw.write("mi_pprint_example([]) :- !.\n\n\n");

			bw.write("% The following two predicates are from Yap lists module.\n\n");

			bw.write("%   delete(List, Elem, Residue)\n" +
					"%   is true when List is a list, in which Elem may or may not occur, and\n" +
					"%   Residue is a copy of List with all elements identical to Elem deleted.\n\n");

			bw.write("delete([], _, []).\n");
			bw.write("delete([Head|List], Elem, Residue) :-\n" +
					"	Head == Elem, !,\n" +
					"	delete(List, Elem, Residue).\n");
			bw.write("delete([Head|List], Elem, [Head|Residue]) :-\n" +
					"	delete(List, Elem, Residue).\n\n");


			bw.write("%   remove_duplicates(List, Pruned) \n" +
					"%   removes duplicated elements from List.  Beware: if the List has\n" +
					"%   non-ground elements, the result may surprise you.\n\n");

			bw.write("remove_duplicates([], []).\n");
			bw.write("remove_duplicates([Elem|L], [Elem|NL]) :- \n" +
					"	delete(L, Elem, Temp),\n" +
					"	remove_duplicates(Temp, NL).\n\n");
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private String getBfileContents(ArrayList<Feature> posRegRegions, ArrayList<Feature> negRegRegions,
			ArrayList<Feature> posRegulatoryElemets, ArrayList<Feature> negRegulatoryElements, 
			String outputTheoryFileName) throws DataFormatException{
		//calculate the upper bound of negative examples to be covered in induction
		double noisePercent = 0.005; // 2% noise
		int noise = (int)(negRegulatoryElements.size() * noisePercent);
		
		String bFileContents = ":- style_check( -discontiguous ). \n\n" +
				"% Aleph directives\n\n" +
				":- set(verbosity, 1). \n" +
				":- set(nodes, 100000). \n" +
				//sets the max number of negative examples that can be covered
				":- set(noise, " + noise + "). \n" +
				":- set(minacc, 0.9). \n" +
				":- set(clauselength, 6). \n" +
				//minimum number of positive example coverage for a rule
				":- set(minpos, 3). \n" +
				":- set(experiment_notes, '"+SystemVariables.getInstance().getExperimentNotes()+"'). \n";

				// set the write-to-file directive
		if (outputTheoryFileName != null && !outputTheoryFileName.isEmpty()){
			
			bFileContents = bFileContents + 
					":- set(record, true). \n" +     // will write the output to a file 
					":- set(recordfile, '" + outputTheoryFileName + "'). \n\n";
		}

		bFileContents = bFileContents + 
			":- modeh(1, positive(+seq)). \n" +
			":- modeb(*, before(+seq, #feature, #feature)).  \n" +
			":- modeb(*, has_feature(+seq, #feature)). \n" +
			":- modeb(*, has_feature(+seq, #feature, #strand)). \n" +
			":- modeb(*, chromosome(+seq,#chr)). \n" +
			":- modeb(*, distance_interval(+seq, #feature, #feature, #number, #number)). \n" +
			":- modeb(*, pos_lteq(+seq, #feature, #number)). \n" +
			":- modeb(*, pos_gteq(+seq, #feature, #number)). \n" + 

			//*** These were found redundant of too slow to include in the theory construction
//			":- modeb(*, distance(+seq, #feature, #feature, #number)). \n" + // no need, distance_interval takes care of this
//			":- modeb(*, min_distance(+seq, #feature, #feature, #number)). \n" +
//			":- modeb(*, max_distance(+seq, #feature, #feature, #number)). \n" +
//			":- modeb(*, pos_negative(+seq, #feature)). \n" +
//			":- modeb(*, pos_positive(+seq, #feature)). \n";
		
			" \n" +
			":- determination(positive/1, before/3). \n" +
			":- determination(positive/1, has_feature/2). \n" +
			":- determination(positive/1, has_feature/3).\n" +
			":- determination(positive/1, chromosome/2). \n" +
			":- determination(positive/1, distance_interval/5). \n" +
			":- determination(positive/1, pos_lteq/3). \n" +
			":- determination(positive/1, pos_gteq/3). \n" +

			//*** These were found redundant of too slow to include in the theory construction
//			":- determination(positive/1, distance/4). \n" +
//			":- determination(positive/1, min_distance/4). \n" +
//			":- determination(positive/1, max_distance/4). \n" +
//			":- determination(positive/1, pos_negative/2). \n" +
//			":- determination(positive/1, pos_positive/2). \n" +
			
			" \n";
		
		// Types
		bFileContents = bFileContents + 
			"% has_feature(<regSeq name>, <regEl name>, <start position>, <end position>, <strand>, <score>) \n" +
			"%   <regEl name>: name of the regulatory element to\n" +
			"%   <regSeq name>: name of the regulatory sequence this regEl belongs to\n" +
			"%   <start position>: position of the first nucleotide (inclusive) of the regEl in regSeq. \n" +
			"%          Position numbering in regulatory sequence starts at 1.  \n" +
			"%   <length>: position of the last nucleotide (exclusive) of the regEl in regSeq\n" +
			"%          Example: \"aaaCCCaaa\". Start position of \"CCC\" = 4; end position = 7  \n" +
			"%   <strand>: either a direct match to a PWM (\"D\"), or reverse complement match (\"R\")\n" +
			"%   <score>: score of the match, as reported by PATSER\n" +
			" \n";

		//Positive examples
		
		Hashtable<String, Integer> regRegionsLengths = new Hashtable<String, Integer>();
		for (Iterator<Feature> iterator = posRegRegions.iterator(); iterator.hasNext();) {
			Feature regRegion = (Feature) iterator.next();
			
			regRegionsLengths.put(regRegion.getId(), regRegion.getSequence().length());
			
			bFileContents = bFileContents + 
				
				//seq(jur10, chr1, 10914832, 371).
				"seq('" + regRegion.getId() + "', '" + regRegion.getNote() + "', " + regRegion.getStartPosition() + ", " + regRegion.getSequence().length() + "). \n" +
				
				//seq(jur414).
				"seq('" + regRegion.getId() + "'). \n";
		}
		
		for (Iterator<Feature> iterator = posRegulatoryElemets.iterator(); iterator.hasNext();) {
			Feature regElement = (Feature) iterator.next();
			//if (Feature.TYPE_TFBS.equals(hit.getType())){
					
				// the location of TFBS inside the region is reported in a ChIP-Seq manner:
				// the sequence numbering starts in the middle (to correspond with the ChIP-Seq peak),
				// and not at the beginning of the sequene (ex. -3 -2 -1 0 1 2 for the sequence of length 6)
				int regRegionLength = regRegionsLengths.get(regElement.getParent()); 
				int pos = regElement.getStartPosition() - (regRegionLength / 2 + 1); 
				
				bFileContents = bFileContents + 
					"has_feature('" + regElement.getParent() + "', '" + regElement.getName() + "', " +
						pos + ", " + (regElement.getEndPosition() - regElement.getStartPosition()) + 
					", '" + regElement.getStrand() + "', " + regElement.getScore() + "). \n";
			//}
		}
		
		
		//Negative examples
		
		regRegionsLengths = new Hashtable<String, Integer>();
		
		for (Iterator<Feature> iterator = negRegRegions.iterator(); iterator.hasNext();) {
			Feature regRegion = (Feature) iterator.next();
			
			regRegionsLengths.put(regRegion.getId(), regRegion.getSequence().length());
			
			bFileContents = bFileContents + 
				//seq(jur10, chr1, 10914832, 371).
				"seq('" + regRegion.getId() + "', '" + regRegion.getNote() + "', " + regRegion.getStartPosition() + ", " + regRegion.getSequence().length() + "). \n" +
			
				//seq(jur414).
				"seq('" + regRegion.getId() + "'). \n";
		}

		for (Iterator<Feature> iterator = negRegulatoryElements.iterator(); iterator.hasNext();) {
			Feature regElement = (Feature) iterator.next();
			//if (Feature.TYPE_TFBS.equals(hit.getType())){
					
				// the location of TFBS inside the region is reported in a ChIP-Seq manner:
				// the sequence numbering starts in the middle (to correspond with the ChIP-Seq peak),
				// and not at the beginning of the sequene (ex. -3 -2 -1 0 1 2 for the sequence of length 6)
				int regRegionLength = regRegionsLengths.get(regElement.getParent()); 
				int pos = regElement.getStartPosition() - (regRegionLength / 2 + 1); 
				
				bFileContents = bFileContents + 
					"has_feature('" + regElement.getParent() + "', '" + regElement.getName() + "', " +
					pos + ", " + (regElement.getEndPosition() - regElement.getStartPosition()) + ", '" + 
					regElement.getStrand() + "', " + regElement.getScore() + "). \n";
			//}
		}
		
		return bFileContents;
	}
	
	
	
	
	private void writeMacIlpScript(String fileName) throws DataFormatException{
		BufferedWriter bw = null;
		
		String alephDir = SystemVariables.getInstance().getString("ilp.install.dir");
		String swiplPath = SystemVariables.getInstance().getString("swipl.install.dir");
		
		try {
			File scriptFile = new File(fileName);
			
			
			bw = new BufferedWriter(new FileWriter(scriptFile));
			
			bw.write("% Background model \n\n");
			
			bw.write("#!/bin/bash\n"); 
			bw.write("export PATH=" + swiplPath + ":$PATH\n"); 
			//bw.write("echo Start\n");
			bw.write("swipl <<EOF\n");
			bw.write("['" + alephDir + "aleph'].\n");	
			bw.write("[ilpBackground].\n");
			bw.write("read_all(moduleInducer), induce, halt.\n");
			bw.write("EOF\n");
			//bw.write("echo Done!\n");
		
			bw.close();
			
			//System.out.println("File exists: " + scriptFile.exists());

			scriptFile.setExecutable(true,true);
			
			//System.out.println("Can execute file: " + scriptFile.canExecute());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * Writes a script that is used to run ilp on windows
	 * Note: aleph.pl has to be in one directory up
	 */
	private void writeWinIlpScript(String fileName) throws DataFormatException{
		BufferedWriter bw = null;
		
		String alephDir = SystemVariables.getInstance().getString("ilp.install.dir");
		
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			
			bw.write("go :-\n");
			bw.write("\t ['" + alephDir + "aleph'],['ilpBackground'],read_all(moduleInducer),induce.\n");
			
//			bw.write("%go :-\n");
//			bw.write("%	[aleph],[ilpBackground],write('grrrrrr'), nl.\n");
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void writeInitialResultsPage(String fileName){
		BufferedWriter bw = null;
		
		try {
			File htmlFile = new File(fileName);
			
			
			bw = new BufferedWriter(new FileWriter(htmlFile));
			
			bw.write("<html>");
			bw.write("\n");
			bw.write("<head>");
			bw.write("\t<title>Module Inducer: Results</title>");
			bw.write("\t<meta http-equiv=\"Refresh\" content=\"5\" />");
			bw.write("</head>");


			bw.write("<body>");

			bw.write("<div style=\"border-style:solid; border-width:1px; border-color: #348017;  font-family: Verdana, Geneva, sans-serif; text-align: center; font-size: 9pt;\">");
			bw.write("\t<h2 style=\"text-align: center;  font-family: Verdana, Geneva, sans-serif; font-size: 13pt;\">Module Inducer</h2>");
			bw.write("\tExtract knowledge from biological data.<br/>");
			bw.write("\tOksana Korol and Marcel Turcotte, University of Ottawa");
			bw.write("\t<br/>&nbsp;");
			bw.write("</div>");
				
			bw.write("\t<div style=\"margin: 2em 2em 2em 2em; font-family: Verdana, Geneva, sans-serif; \">");
			bw.write("\t<h3> Your results will appear shortly. </h3>");
				
			bw.write("\t</div>");

			bw.write("</body>");
			bw.write("</html>");
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public String runILP(){
		String ilpTheory = "";
		int exitVal = -1;
		try {
			Runtime rt = Runtime.getRuntime();
			
			Process pr;
			File ilpDir = new File(ilpDirName);
			
			if (System.getProperty("os.name").startsWith("Mac")){
				final String cmd = "./runILP.sh 2>/dev/null";	
				
				 pr = rt.exec(new String[] { "/bin/sh", "-c", cmd }, null, ilpDir);
				
			} else { // i.e. Windows
								
				pr = rt.exec(new String[] {"cmd", "/c", "C:\\\"Program Files (x86)\"\\pl\\bin\\plcon.exe -f none -g \"['runILP']\" -t go"}, 
						 null, ilpDir);
				
			}
	
			
			// Normal Stream
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;			
			while ((line = input.readLine()) != null) {
				ilpTheory = ilpTheory + line + "\n";
			}
	
			
			//Error stream
			System.out.println("\n ***** Error stream ******");
			BufferedReader inputErr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			line = null;
			while ((line = inputErr.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("***** End error stram *****");		
			
			

			

			
			exitVal = pr.waitFor();
//			System.out.println("Exited with error code " + exitVal);
			
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		
		return ilpTheory;
	}


}













/*** Older Rules ***/

//% Background model 
//
//has_tfbs(Gene,TFBS) :- tfbs(TFBS,Gene,_,_,_,_). 
//
//before(TF1, TF2, Gene) :- 
//	 tfbs(TF1, Gene, Pos1,_,_,_), tfbs(TF2, Gene, Pos2,_,_,_), Pos1<Pos2, not(TF1 == TF2).
//
//distance(BS1, BS2, Seq, D) :-
//	 tfbs(BS1, Seq, StartPos1,EndPos1,_,_), tfbs(BS2, Seq, StartPos2, _,_,_), BS1 \== BS2, 
//	 StartPos1 < StartPos2, !,
//	 D is StartPos2 - EndPos1.
//
//distance(BS1, BS2, Seq, D) :-
//	 tfbs(BS1, Seq, StartPos1,_,_,_), tfbs(BS2, Seq, _, EndPos2,_,_), BS1 \== BS2,
//	 D is StartPos1 - EndPos2.
//
//distance_interval(BS1, BS2, Seq, D, Offset) :-
//	 var(D), !,
//	 distance(BS1, BS2, Seq, D),  range(Offset).
//
//distance_interval(BS1, BS2, Seq, D, Offset) :-
//	 distance(BS1, BS2, Seq, D1), D1 < D+Offset, D1 > D-Offset.
//
//range(2).
//range(3).
//range(4).


/*** Older Rules ***/


//% same_strand(TF1, TF2, Gene) :-
//%	tfbs(TF1, Gene, _, _, Strand1, _), tfbs(TF2, Gene, _, _, Strand2, _), Strand1 == Strand2. 
//	   
//% before_same_strand(TF1, TF2, Gene) :- 
//%	tfbs(TF1, Gene, Pos1, _, Strand1,_), tfbs(TF2, Gene, Pos2, _, Strand2,_), Pos1<Pos2, Strand1 == Strand2.
//
//% before_diff_strands(TF1, TF2, Gene) :-
//%       tfbs(TF1, Gene, Pos1, _, Strand1,_), tfbs(TF2, Gene, Pos2, _, Strand2,_), Pos1<Pos2, not(Strand1 == Strand2).	   
//
//% distance(BS1, BS2, Seq, D) :-
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D is Pos2 - Pos1.
//
//% distance_interval(BS1, BS2, Seq, D, Offset) :-
//	% var(D), !,
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D is Pos2 - Pos1, range(Offset).
//
//% distance_interval(BS1, BS2, Seq, D, Offset) :-
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D1 is Pos2 - Pos1, D1 < D+Offset, D1 > D-Offset.
//
//% same_strand(TF1, TF2, Gene) :-
//%	tfbs(TF1, Gene, _, _, Strand1, _), tfbs(TF2, Gene, _, _, Strand2, _), Strand1 == Strand2. 
//	   
//% before_same_strand(TF1, TF2, Gene) :- 
//%	tfbs(TF1, Gene, Pos1, _, Strand1,_), tfbs(TF2, Gene, Pos2, _, Strand2,_), Pos1<Pos2, Strand1 == Strand2.
//
//% before_diff_strands(TF1, TF2, Gene) :-
//%       tfbs(TF1, Gene, Pos1, _, Strand1,_), tfbs(TF2, Gene, Pos2, _, Strand2,_), Pos1<Pos2, not(Strand1 == Strand2).	   
//
//% distance(BS1, BS2, Seq, D) :-
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D is Pos2 - Pos1.
//
//% distance_interval(BS1, BS2, Seq, D, Offset) :-
//	% var(D), !,
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D is Pos2 - Pos1, range(Offset).
//
//% distance_interval(BS1, BS2, Seq, D, Offset) :-
//	% tfbs(BS1, Seq, Pos1,_,_,_), tfbs(BS2, Seq, Pos2,_,_,_), BS1 \== BS2, D1 is Pos2 - Pos1, D1 < D+Offset, D1 > D-Offset.
