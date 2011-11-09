/*
package ca.uottawa.okorol.bioinf.ModuleInducer.tools;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;

public class BioJavaFastaExtractor {

	private String fastaFileName;

	public BioJavaFastaExtractor(String fastaFileName) {
		this.fastaFileName = fastaFileName;
	}

	public String extractSubSequence(String chromosome, int beginIndex, int endIndex) {
		String resultString = "";
		
		BufferedReader br = null;
		try {

			//create a buffered reader to read the sequence file 
			br = new BufferedReader(new FileReader(fastaFileName));

		}
		catch (FileNotFoundException ex) {
			//can't find the file 
			ex.printStackTrace();
			System.exit(-1);
		}


		//read the Fasta File
		Namespace defaultNamespace = RichObjectFactory.getDefaultNamespace();
		RichSequenceIterator fileSequeceIterator = IOTools.readFastaDNA(br, defaultNamespace);
		
		//iterate through the sequences
		while(fileSequeceIterator.hasNext()){
			try {
				RichSequence sequence = fileSequeceIterator.nextRichSequence();
				//System.out.println("* "+ sequence.getAccession());
				
				if (chromosome.equals(sequence.getAccession())){
					resultString =  sequence.subStr(beginIndex, endIndex);
					break;
				}
				
				//force garbage collection
				Runtime r = Runtime.getRuntime();
				r.gc();

				
				
			}catch (BioException ex) {
				//not in Fasta format
				ex.printStackTrace();
			}catch (NoSuchElementException ex) {
				//request for more sequence when there isn't any
				ex.printStackTrace();
			}	
		}
		
		
		return resultString;
	}


}
*/