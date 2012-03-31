import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Parser.java
 * @author Robby Grodin
 *
 * Class that will read in a file and produce procedural sequences for analysis.
 */

public class Parser {
	String file;
	int module;
	int subject;
	
	static InputStreamReader ir = new InputStreamReader(System.in);
	static BufferedReader br = new BufferedReader(ir);
	static ArrayList<Integer> procedureActual;
	static Parser paActual;
	static ArrayList<Long> timeArray = new ArrayList<Long>();
	static ArrayList<Integer> procedureIdeal;	
	static Parser paIdeal;
	

	/**
	 * The main method first takes information as to which file to parse, then parses and returns a procedure to be evaluated.
	 * 
	 * @param args
	 */
	public void run() {
			paActual = new Parser();
			procedureActual = new ArrayList<Integer>();
			paActual.parse(procedureActual, "doc/Module " + module + " Subject " + subject + ".txt",true);

			paIdeal = new Parser();
			procedureIdeal = new ArrayList<Integer>();
			paIdeal.parse(procedureIdeal, "doc/Ideal " + module + ".txt",false);
	}

	public void parse(ArrayList<Integer> al, String fileName, boolean getTime) {
		Scanner scanner = null;
		file = fileName;

		try {
			scanner = new Scanner(new FileReader(file));
			scanner.useDelimiter("=");
			while( scanner.hasNextLine() ) {
				processLine( scanner.nextLine(), al, getTime);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			scanner.close();
		} 
	}

	protected void processLine(String line, ArrayList<Integer> al, boolean getTime) {
		Scanner scanner = new Scanner(line);
		if ( scanner.hasNext() ){
			if(getTime){
				if(line.startsWith("Time = ")){
					scanner.next();
					scanner.next();
					String time = scanner.next();
					timeArray.add(Long.parseLong(time));
					System.out.println("Time = " + time);
				}
			}
			if(line.startsWith("Code =")){
				scanner.next();
				scanner.next();
				String code = scanner.next();
				al.add(Integer.parseInt(code));
				System.out.println("Code: " + code);
			}
		}
	}
	
	public int[] getIdealProcedure(){
		int[] out = new int[procedureIdeal.size()];
		
		for(int i = 0; i < out.length; i++) {
			out[i] = procedureIdeal.get(i).intValue();
		}
		return out;
	}
	
	public int[] getActualProcedure(){
		int[] out = new int[procedureActual.size()];
		
		for(int i = 0; i < out.length; i++) {
			out[i] = procedureActual.get(i).intValue();
		}
		return out;
	}
	
	public long[] getTime(){
		long [] out = new long[timeArray.size()];
		
		for(int i = 0; i < out.length; i++){
			out[i] = timeArray.get(i);
		}
		return out;
	}
	
	public int getModule(){
		return module;
	}
	
	public void setModule(int module){
		this.module = module;
	}
	
	public void setSubject(int subject){
		this.subject = subject;
	}
}