package Data;

public class AVTable {
	public int n;           // number of samples
	public int m;           // number of labels
	public int d;           // number of features
	public int[][] y;
	public AVPair[][] x;
	
	
	static public double[] getPrior( AVTable data){
		double[] prior = new double[data.d];

		int[] indices = new int[data.n];
		for( int i = 0; i < data.n; i++ ) indices[i] = 0;

		int numOfLabels = 0;
		
		// assume that the labels are ordered
		for( int i = 0; i < data.m; i++ ) {
			int numOfPositives = 0;
			for( int j = 0; j < data.n; j++ ) {
				if ( (indices[j] < data.y[j].length) &&  (data.y[j][indices[j]] == i) ){ 				
					numOfPositives++;
					indices[j]++;
				} 
			}
			prior[i] = (double)numOfPositives;
			numOfLabels += numOfPositives;
		}
		
		for( int i = 0; i < data.m; i++ ) {
			prior[i] /= ((double) numOfLabels); 
		}
		
		return prior;
	}
	
}
