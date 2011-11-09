package ca.uottawa.okorol.bioinf.ModuleInducer.data;

public class Feature implements Comparable<Feature>{
	public static final String TYPE_REGULATORY_REGION = "regulatory_region";

	//TODO CUrrently, all regulatory elements are typed as TYPE_TFBS. Check with M.
	public static final String TYPE_TFBS = "TF_binding_site"; // child of regulatory_region
	public static final String TYPE_TATA_BOX = "TATA_box"; // child of TF_binding_site
	public static final String TYPE_GC_BOX = "GC_box"; //TODO not confirmed by SO
	public static final String TYPE_CAP_SIGNAL = "CAP_signal"; //TODO not confirmed by SO
	public static final String TYPE_CAAT_SIGNAL = "CAAT_signal"; // child of TF_binding_site
	
	//private String seqid;
	
	private String id; //accession number for a gene; 
	private String name; //matrix name for a TFBS
	private String parent;
	private String note;  //fore genes, chromosome number, on which the gene is located

	private String source; // PATSER, db...
	private String type; //gene; TF_binding_site
	private int startPosition;
	private int endPosition;
	private double score;
	private String strand;
	
	
	//private String sequence;
	private String sequence;
	//private String species;
	//private String tissue;
	
	
	public Feature(){
	}

	public Feature(String type){
		this.type = type;
	}
	
	//for regulatory regions
	public Feature(String accessionNum, String type, String chromosome, int startPosition, int endPosition, String upstreamSeq, double score){
		this.id = accessionNum;
		this.type = type;
		this.note = chromosome;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.sequence = upstreamSeq;
		this.score = score;
	}
	
	//for regulator elements
	public Feature(String type, String tfbsName, String geneName, String strand, int startPosition, int endPosition, double matchingScore){
		this.type = type;
		this.name = tfbsName;
		this.parent = geneName;
		this.strand = strand;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.score = matchingScore;
	}
	

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public int getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	
	public int getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public String getStrand() {
		return strand;
	}
	public void setStrand(String strand) {
		this.strand = strand;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String accessionNum) {
		this.id = accessionNum;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String attrName) {
		this.name = attrName;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String attrParent) {
		this.parent = attrParent;
	}
	
	public String getNote() {
		return note;
	}
	public void setNote(String chromosome) {
		this.note = chromosome;
	}
	
/*	
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
*/
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String upstreamSequence) {
		this.sequence = upstreamSequence;
	}
	
	private boolean areEqual(String val1, String val2){
		if (val1 == null && val2 == null) return true;
		
		if (val1 == null || val2 == null) return false;
		
		return val1.equals(val2);
	}
	
	
public int compareTo(Feature otherHit){
		
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    
	    // first sort by type
	    if (this.type != null){
	    	if (this.type.compareTo(otherHit.type) < 0) return BEFORE;
	    	if (this.type.compareTo(otherHit.type) > 0) return AFTER;
	    }
	    
	    // then sort by gene name
	    if (this.parent != null){ 
	    	if (this.parent.compareTo(otherHit.parent) < 0) return BEFORE;
	    	if (this.parent.compareTo(otherHit.parent) > 0) return AFTER;
	    }
	    
	    // then by TFBS name
	    if (this.name != null){
	    	if (this.name.compareTo(otherHit.name) < 0) return BEFORE;
	    	if (this.name.compareTo(otherHit.name) > 0) return AFTER;
	    }
	    
	    // then by TFBS start position
	    if (this.startPosition < otherHit.startPosition) return BEFORE;
	    if (this.startPosition > otherHit.startPosition) return AFTER;
	    
	    if (this.endPosition < otherHit.endPosition) return BEFORE;
	    if (this.endPosition > otherHit.endPosition) return AFTER;
	    
	    if (this.score < otherHit.score) return BEFORE;
	    if (this.score > otherHit.score) return AFTER;
	    
	    if (this.sequence != null){
	    	if (this.sequence.compareTo(otherHit.sequence) < 0) return BEFORE;
	    	if (this.sequence.compareTo(otherHit.sequence) > 0) return AFTER;
	    }
	    
	    // if all of these have passed, then the objects are equal
	    return EQUAL;
	    

	}

@Override 
public boolean equals(Object aThat) {
	
	if (this == null && aThat == null) return true;
	if ( !(aThat instanceof Feature) ) return false;
	
	Feature that = (Feature) aThat;
	
	if (!areEqual(this.type, that.type)) return false;
	if (!areEqual(this.id, that.id)) return false;
	if (!areEqual(this.name, that.name)) return false;
	if (!areEqual(this.parent, that.parent)) return false;
	if (!areEqual(this.note, that.note)) return false;
	if (!areEqual(this.strand, that.strand)) return false;
    if (this.startPosition != that.startPosition) return false;
    if (this.endPosition != that.endPosition) return false;
	if (Double.compare(this.score, that.score) != 0) return false;
	
	if (!areEqual(this.source, that.source)) return false;
	if (!areEqual(this.sequence, that.sequence)) return false;
	
	return true;
}

	

}
