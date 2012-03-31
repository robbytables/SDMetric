import java.util.ArrayList;

/**
 * PatternEquations.java
 * @author Robby Grodin
 * 
 * Resource file to store all the equations needed to compute our metric.
 */

public class PatternEquations {

	/**
	 *  Constructs a single cell of the likelihood matrix. 
	 *  
	 *  @param sequence An ordered sequence of steps in the given procedure.
	 *  @param transition A Markov chain represented by its transition table.
	 *  
	 *  @return output The likelihood of the sequence given the transition table.
	 */
	public double markovProbability(int[] sequence, double[][] transition ) {
		// Initialize output probability
		double output = 1;

		// Compute the sum of the sequence given a markov chain
		for( int i = 0; i < sequence.length - 1; i++) {
			output = transition[sequence[i]][sequence[i+1]] * output;
		}	
		return output;
	}

	/**
	 * Constructs the likelihood matrix cell by cell. For our purposes, the likelihood matrix will always be 2x2.
	 * 
	 * @param sequences Set including the ideal sequence for the given procedure and the actual sequence.
	 * @param transitions The corresponding transition tables for each sequence.
	 * 
	 * @return likelihoodMatrix The 2x2 likelihood matrix containing the likelihoods of each sequence over each model.
	 */
	public double[][] likelihoodMatrix(int[][] sequences, double[][][] transitions) {
		// Initialize a 2x2 output matrix
		double[][] likelihoodMatrix = new double[2][2];

		for( int i = 0; i < sequences.length; i++) {
			for( int j = 0; j < transitions.length; j++) {
				likelihoodMatrix[i][j] = markovProbability(sequences[i], transitions[j]);
			}
		}
		return likelihoodMatrix;
	}

	/**
	 * Composes a Markov chain trained on the input sequence.
	 * 
	 * @param sequence The sequence from which we will derive a Markov chain.
	 * @param states The number of states present in the Markov chain.
	 * 
	 * @return markovOut The composed Markov chain, represented as a transition table.
	 */
	public double[][] toMarkov(int s, int[] sequence) {
		// Initialize the markov chain, size states x states
		double[][] markovOut = markovInit( s );
		int states = s;

		while( states >= 0 ) { // Loop once for each starting state
			for( int i = 0; i < sequence.length - 1; i++ ){
				if( sequence[i] == states ) { // If the current step of the sequence is the state we're looking for...
					markovOut[states][sequence[i+1]] += 1; // Increment the proper cell in the transition table
				}
			}
			states -= 1;
		}
		return markovOut;
	}

	/**
	 * Initialize a Markov chain such that each cell contains 0.
	 * 
	 * @param states The number of states observed in the chain.
	 */
	public double[][] markovInit(int states) {
		double[][] markov = new double[states][states];

		for( int i = 0; i < markov.length-1; i++ ) {
			for( int j = 0; j < markov.length-1; j++ ) {
				markov[i][j] = 0;
			}
		}
		return markov;
	}

	/**
	 * Observes the number of novel, singleton, ... events in the given markov chain
	 */
	public double[] occurenceCount(double[][] markovActual, int[] sequence) {
		double[] count = new double[sequence.length];
		for(int i = 0; i < markovActual.length; i++) {
			for(int j = 0; j < markovActual.length; j++ ) {
				count[(int) markovActual[i][j]] += 1;
			}
		}
		return count;
	}

	/**
	 * Implementation of the Good Turing Smoothing technique.
	 */
	public double[][] GTSmooth(double[][] markovIn, double[] count, double states, int[] sequence) {
		double[][] markovOut = new double[markovIn.length][markovIn.length];
		double novelCount = count[0];
		double singletonCount = count[1]==0 ? 1 : count[1];
		double newNovelValue = singletonCount / ((sequence.length - 1) * novelCount);
		
		//System.out.println("GOOD-TURING ESTIMATOR: " + singletonCount + " / (" + states + " * "+ novelCount + ") = " + newNovelValue);
		
		for( int i = 0; i < markovIn.length; i++) {
			for( int j = 0; j < markovIn.length; j++) {
				if(markovIn[i][j] < 1){
					markovOut[i][j] = newNovelValue;
				} else {
					markovOut[i][j] = (double) markovIn[i][j];
				}
			}
		}
		return markovOut;
	}
	
	/**
	 * Implementation of the KL Divergence algorithm.
	 */
	public double KLDivergance( double[][] likelihood ) { 
		double kl1 = 0;
		double kl2 = 0;
		
		for(int i = 0; i < likelihood.length; i++) {
				kl1 += likelihood[i][0] * Math.log(likelihood[i][0] / likelihood[i][1]);
			}
		for(int i = 0; i < likelihood.length; i++) {
			kl2 += likelihood[i][1] * Math.log(likelihood[i][1] / likelihood[i][0]);
		}
		
		return (kl1 + kl2) / 2;
	}
	
	/**
	 * Compares the two procedures and calculates steps added and steps skipped by creating an occurence count
	 * for each, then returning the differences.
	 */
	public int[] getSkippedAdded(int[] ideal, int[] actual, int states){
		int[] output = new int[2];
		int[] occurenceIdeal = new int[states];
		int[] occurenceActual = new int[states];
		
		// Build occurrence counts
		for(int i = 0; i < ideal.length; i++){
			occurenceIdeal[ideal[i]] += 1;
		}
		for(int i = 0; i < actual.length; i++){
			occurenceActual[actual[i]] += 1;
		}
		
		// Count the states skipped/added
		int skipped = 0;
		int added = 0;
		for(int i = 0; i < states; i++){
			if(occurenceActual[i] < occurenceIdeal[i]){
				added += occurenceIdeal[i] - occurenceActual[i];
			} else {
				skipped += occurenceActual[i] - occurenceIdeal[i];
			}
		}
		output = new int[]{added,skipped};
		return output;
	}
	
	/**
	 * Normalizes a 1 dimensional array by dividing each item by their sum
	 */
	public double[] normalize(double[] array){
		double sum = 0;
		
		for(int i = 0; i < array.length; i++){
			sum += array[i];
		}
		
		for(int i = 0; i < array.length; i++){
			array[i] = array[i] / sum;
		}
		
		return array;
	}
	
	/**
	 * Loops through normalize for a 2d array
	 */
	public double[][] normalizeLoop(double[][] array){
		
		for(int i = 0; i < array.length; i++){
			normalize(array[i]);
		}
		return array;
	}
}

