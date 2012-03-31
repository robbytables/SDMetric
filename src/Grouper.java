import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Grouper.java
 *
 * This class will alter procedures based on a configuration file that outlines how to group
 * events.
 * 
 * NOTE- Miscellaneous events are to be categorized as '0'(zero).
 * 
 * @author Robby Grodin
 */
public class Grouper {
	String file = "doc/Config";
	int[] procedureActual;
	int[] procedureIdeal;
	int module;
	ArrayList<Integer> key = new ArrayList<Integer>();

	int states = 270;

	public Grouper(int[] actual, int[] ideal, int mod) {
		procedureActual = actual;
		procedureIdeal = ideal;
		module = mod;
		file += module + ".txt";

		for(int i = 0; i < states; i++){
			key.add(0);
		}
	}

	public void group(){
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileReader(file));
			scanner.useDelimiter("=");
			while( scanner.hasNextLine() ) {
				processLine( scanner.nextLine());
			}

			replace(procedureActual);
			replace(procedureIdeal);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			scanner.close();
		} 
	}

	protected void processLine(String line) {
		Scanner scanner = new Scanner(line);
		String index;
		String code;

		if ( scanner.hasNext() ){
			index = scanner.next().trim();
			scanner.next();
			code = scanner.next().trim();
			key.set(Integer.parseInt(index), Integer.parseInt(code));
		}
	}
	
	public int getStates(){
		int max = 0;
		for(int i = 0; i < key.size(); i++){
			int k = key.get(i);
			if(max < k)
				max = k;
		}
		return max;
	}

	public void replace(int[] al) {
		for(int i = 0; i < al.length; i++){
			al[i] = key.get(al[i]);
		}
	}

	public int[][] remove(int r, int[] al){
		ArrayList<Integer> temp = new ArrayList<Integer>();  // New Procedure
		ArrayList<Integer> temp2 = new ArrayList<Integer>(); // Old Indexes
		int[][] out;
		for(int i = 0; i < al.length; i++){
			if(al[i]!=r){
				temp.add(al[i]);
				temp2.add(i);
			}
		}

		out = new int[temp.size()][temp.size()];

		for(int i = 0; i < temp.size(); i++){
			out[0][i] = temp.get(i);
			out[1][i] = temp2.get(i);
		}
		return out;
	}

	/**
	 * Returns the given 'small' array with 0 padding at the end to match the length of the given 'big' array.
	 * @param small array to be padded
	 * @param big size reference
	 * @return padded array
	 */
	public int[] pad(int[] small, int[] big){

		if(small.length < big.length) {           // If small >= large, we don't do anything to it.
			int[] temp = new int[big.length];

			for(int j = 0; j < small.length; j++){
				temp[j] = (small[j]);
			}

			return temp;
		} else return small;
	}
}