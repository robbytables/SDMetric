import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * SDMetric.java
 * @author Robby Grodin :: grodin.robby@gmail.com
 * 
 * Main class that calculates our procedural adherence metric. 
 */
public class SDMetric {
	// Sequence and subject information
	static int subject;
	static int module;
	static int modules;
	static int firstModule;
	static int states;
	static int subjects;
	static int firstSubject;
	static int[] sequenceIdeal;
	static int[] sequenceActual;
	static int[] codesForCSV;
	static int[] originalIndexes;
	static long[] timeArray;
	
	// Information to be displayed in the interface
	static double[] finalScores;
	static int[][] subjectStates;
	static double[][] subjectScores;
	static int[][] subjectStats;

	int statesActual = sequenceActual.length;
	int statesIdeal = sequenceIdeal.length;

	/**
	 * Computes the KL divergence between two given sequences, ideal and performed. Calls the <code>calculate</code> method.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in)); // Open a buffer from which we will communicate with the user
		try{
			System.out.println("Please enter the first subject number");
			String s = input.readLine();

			firstSubject = Integer.parseInt(s);


			System.out.println("Please enter the last subject number");
			s = input.readLine();

			subjects = Integer.parseInt(s) - firstSubject + 1;

			System.out.println("Please enter the first module number");
			s = input.readLine();

			firstModule = Integer.parseInt(s);

			System.out.println("Please enter the last module number");
			s = input.readLine();

			modules = Integer.parseInt(s) - firstModule + 1;

		} catch(IOException e) {
			System.out.println(e);
		}

		finalScores = new double[subjects];
		subjectStates = new int[subjects][];
		subjectScores = new double[subjects][];
		subjectStats = new int[subjects][];

		calculate();
	}

	/**
	 * Using algorithms from the <class>PatternEquations</class> class, calculates the divergence score.
	 * @throws IOException 
	 */
	public static void calculate() throws IOException{

		for(int subs = 0; subs < subjects; subs++){
			for(int mods = 0; mods < modules; mods++){
				PatternEquations pe = new PatternEquations();
				Parser parser = new Parser();
				Grouper grouper;
				BufferedWriter writer;
				ArrayList<Integer> seqIdealCum;
				ArrayList<Integer> seqActualCum;
				int currentSub = (subs+firstSubject);
				int currentMod = (mods+firstModule);
				double[][] markovIdeal;
				double[][] markovActual;
				double[][] likelihoodMatrix;
				double[] occurenceCountActual;
				double[] occurenceCountIdeal;
				double KLOut;
				double[] KLCumulative;

				// Create a file to output scores

				String file = "subject " + currentSub + " module " + currentMod + ".csv";
				CSV csv = new CSV(file);

				csv.write("time, action, action with ignore, code, grouped code, KL score" + System.getProperty("line.separator"));
				writer = new BufferedWriter(
						new FileWriter("Scores for Module " + currentMod + 
								" Subject " + currentSub + ".txt", true)); // Create the file
				try {
					writer.write("Subject " + currentSub + " Module " 
							+ currentMod + System.getProperty("line.separator")); // Write the header.
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Parse input files
				parser.setModule(currentMod);
				parser.setSubject(currentSub); // add the firstSubject to the count to increment through the user specified range
				parser.run();

				timeArray = parser.getTime();
				sequenceIdeal = parser.getIdealProcedure();
				sequenceActual = parser.getActualProcedure();
				codesForCSV	= parser.getActualProcedure();

				seqIdealCum = new ArrayList<Integer>();
				seqActualCum = new ArrayList<Integer>();

				// Group procedures based on config file
				grouper = new Grouper(sequenceActual,sequenceIdeal,currentMod);
				grouper.group();
				states = grouper.getStates();
				int[][] temp = grouper.remove(states, sequenceActual);
				sequenceActual = temp[0];
				originalIndexes = temp[1];
				sequenceIdeal = grouper.remove(states, sequenceIdeal)[0];
				originalIndexes = grouper.pad(originalIndexes,sequenceIdeal);

				subjectStats[subs] = pe.getSkippedAdded(sequenceIdeal, sequenceActual, states);
				subjectStates[subs] = sequenceActual;

				int loopFor = Math.max(sequenceActual.length, sequenceIdeal.length);
				KLCumulative = new double[loopFor];

				seqIdealCum.add(sequenceIdeal[0]);
				seqActualCum.add(sequenceActual[0]);

				for(int i = 1; i < loopFor; i++){
					if(seqIdealCum.size()<sequenceIdeal.length){ seqIdealCum.add(sequenceIdeal[i]);}
					if(seqActualCum.size()<sequenceActual.length){ seqActualCum.add(sequenceActual[i]);}

					int[] tempIdeal = new int[seqIdealCum.size()];
					int[] tempActual = new int[seqActualCum.size()];
					for(int j = 0; j < tempIdeal.length; j++){ tempIdeal[j] = seqIdealCum.get(j); }
					for(int j = 0; j < tempActual.length; j++){ tempActual[j] = seqActualCum.get(j); }

					// Train markov chains for each procedure
					markovIdeal = pe.toMarkov(states, tempIdeal);
					markovActual = pe.toMarkov(states, tempActual);

					// Create a table to record how many novel events, singletons, doubletons, etc. occur in each markov
					occurenceCountIdeal = pe.occurenceCount(markovIdeal, tempIdeal);
					occurenceCountActual = pe.occurenceCount(markovActual, tempActual);

					// Replace each 0 value in the markov chain with a Good-Turing estimator to prevent 0% probabilities.
					markovIdeal = pe.GTSmooth(markovIdeal, occurenceCountIdeal, states, tempIdeal);
					markovActual = pe.GTSmooth(markovActual, occurenceCountActual, states, tempActual);

					// Normalize each row so the probabilities sum to 1
					markovIdeal = pe.normalizeLoop(markovIdeal);
					markovActual = pe.normalizeLoop(markovActual);

					// Assemble a likelihood matrix from the sequences and their respective markov chains
					likelihoodMatrix = pe.likelihoodMatrix(new int[][]{tempIdeal,tempActual}, new double[][][]{markovIdeal,markovActual});

					// Normalize the likelihood matrix's rows
					likelihoodMatrix = pe.normalizeLoop(likelihoodMatrix);

					// Calculate the score and push it out
					KLOut = pe.KLDivergance(likelihoodMatrix);
					KLCumulative[i] = KLOut;
					int original = originalIndexes[i];
					int actual;
					int code;
					long time;

					if(i >= sequenceActual.length || (i > 0 && original == 0)){
						actual = -1;
						time = -1;
						code = -1;
					} else {
						actual = sequenceActual[i];
						time = timeArray[i];
						code = codesForCSV[i];
					}

					csv.write(time,i,original,code,actual,KLOut);


					// Write score to file
					try {
						int step;
						if(originalIndexes.length > i-1){
							step = i-1;
						} else {
							step = originalIndexes.length-1;
						}

						writer.write(originalIndexes[step] + "   " + KLOut + System.getProperty("line.separator"));

					} catch (IOException e) {
						e.printStackTrace();
					}


				}	

				// Close score file
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				csv.close();

				subjectScores[subs] = KLCumulative;
				finalScores[subs] = KLCumulative[KLCumulative.length-1];
			}
			reportStats();
		}
	}

	/**
	 * Prints the subjectStats[added,skipped] data to its own file.
	 */
	public static void reportStats(){

		try {
			BufferedWriter w = new BufferedWriter(
					new FileWriter("Subject Stats.txt", true)); // Create the file
			w.write("Sub.     add    skip" + System.getProperty("line.separator"));
			for(int i = 0; i < subjectStats.length - 1; i++){
				w.write((firstSubject+i) + "        " + subjectStats[i][0] + "        " + subjectStats[i][1] + System.getProperty("line.separator"));
			}
			w.close();
			System.out.println("File written successfully!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}