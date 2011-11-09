package ca.uottawa.okorol.bioinf.ModuleInducer.tools;

public class ArrayTools {

	
	/* JUnit4 supposed to handle it, but Mac only has 3
	 */
	public static boolean arrayEquals(double[] expected, double[] actual){
		if (expected == null && actual == null){
			return true;
		} else if (expected == null || actual == null){
			return false;
		}
		
		if (expected.length != actual.length){
			return false;
		}
		
		for (int i = 0; i < expected.length; i++){
			if (Double.compare(expected[i], actual[i]) != 0){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean arrayEquals(double[][] expected, double[][] actual){
		if (expected == null && actual == null){
			return true;
		} else if (expected == null || actual == null){
			return false;
		}
		
		if ((expected.length != actual.length) || 
				(expected[0].length != actual[0].length) ){
			return false;
		}
		
		for (int i = 0; i < expected.length; i++){
			for (int j = 0; j < expected[0].length; j++){
				if (Double.compare(expected[i][j], actual[i][j]) != 0){
					return false;
				}
			}
		}
		
		return true;
	}
	
	/* JUnit4 supposed to handle it, but Mac only has 3
	 */
	public static boolean arrayEquals(String[] expected, String[] actual){
		if (expected == null && actual == null){
			return true;
		} else if (expected == null || actual == null){
			return false;
		}
		
		if (expected.length != actual.length){
			return false;
		}
		
		for (int i = 0; i < expected.length; i++){
			if (!expected[i].equals(actual[i])){
				return false;
			}
		}
		
		return true;
	}
	
	
	public static void printMatrix(double[] matrix){
		System.out.println("Transition array for MM0");
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("	"+matrix[i]);
		}
		System.out.println();
	}
	
	public static void printMatrix(double[][] matrix){
		System.out.println("Transition matrix MM1");
		for (int j = 0; j < matrix[0].length; j++) {
			for (int i = 0; i < matrix.length; i++) {
				System.out.print("	"+matrix[i][j]);
			}
			System.out.println();
		}
	}
	
	public static void printArray(String[] matrix){
		System.out.println("Array: ");
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("	"+matrix[i]);
		}
		System.out.println();
	}
	
	public static boolean approxEqual(double[] matrix1, double[] matrix2, double delta){
		if (matrix1.length != matrix2.length) return false;
		
		for (int i = 0; i < matrix1.length; i++) {
			if (!((matrix1[i] + delta >= matrix2[i]) && (matrix1[i] - delta <= matrix2[i])))
				return false;
		}
		return true;
	}
	
	public static boolean approxEqual(double[][] matrix1, double[][] matrix2, double delta){
		if (matrix1.length != matrix2.length) return false;
		
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				
				if (!((matrix1[i][j] + delta >= matrix2[i][j]) && (matrix1[i][j] - delta <= matrix2[i][j])))
					return false;
				
			}
		}
		return true;
	}

}
