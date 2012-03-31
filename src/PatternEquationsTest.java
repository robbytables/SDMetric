public class PatternEquationsTest{
	static PatternEquations pe = new PatternEquations();
	
	public static void main(String[] args) {
		run();
	}
	
	public static void run(){
		double[][] testMat1 = new double[2][2];
		double[][] testMat2 = new double[3][3];
		int[] testSeq = new int[12];
		
		testMat1[0][0] = 0.0;
		testMat1[0][1] = 0.0;
		testMat1[1][0] = 0.0;
		testMat1[1][1] = 0.0;
		
		testSeq[0] = 0;
		testSeq[1] = 0;
		testSeq[2] = 1;
		testSeq[3] = 1;
		testSeq[4] = 2;
		testSeq[5] = 2;
		testSeq[6] = 0;
		testSeq[7] = 0;
		testSeq[8] = 1;
		testSeq[9] = 1;
		testSeq[10] = 2;
		testSeq[11] = 1;
		
		testMat2[0][0] = 2;
		testMat2[0][1] = 2;
		testMat2[0][2] = 0;
		testMat2[1][0] = 0;
		testMat2[1][1] = 2;
		testMat2[1][2] = 2;
		testMat2[2][0] = 1;
		testMat2[2][1] = 1;
		testMat2[2][2] = 1;
		
		System.out.println("Test markovInit results: " + (pe.markovInit(2) == testMat1));
		System.out.println(pe.markovInit(2)[1][1]);
		System.out.println("Test toMarkov results: " + (pe.toMarkov(3, testSeq) == testMat2));
		System.out.println(pe.toMarkov(3, testSeq)[0][1]);
		System.out.println(pe.toMarkov(3, testSeq).length);
	}
}