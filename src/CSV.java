import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * CSV.java
 * @author Robby Grodin :: grodin.robby@gmail.com
 * 
 * This class will handle writing to a comma separated value document
 * to be imported to Excel, or any other program which hopefully is 
 * much better than Excel.
 **/
public class CSV {
	BufferedWriter writer;
	String file;

	// Data to be inserted to CSV
	private int[] indexWithIgnore;
	private int[] klScores;
	private int[] groupCodes;

	// Public constructor
	public CSV(String filename){
		file = filename;

		try{
			writer = new BufferedWriter(new FileWriter(file,true));
		}catch(IOException e){
			e.printStackTrace();	
		}
	}


	// Setters
	public void setIndexWithIgnore(int[] array){
		indexWithIgnore = array;
	}

	public void setKLScores(int[] array){
		klScores = array;
	}

	public void setGroupCodes(int[] array){
		groupCodes = array;
	}

	// Writers
	public void write(){
		for(int i = 0; i < indexWithIgnore.length; i++){
			char c = ',';
			String out = "time, action #, action # with ignore, grouped action code, KL score" + System.getProperty("line.separator") +
			i + c + Integer.toString(indexWithIgnore[i]) + 
			c + Integer.toString(klScores[i]) + c + Integer.toString(groupCodes[i]) + System.getProperty("line.separator");

			try {
				writer.write(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void write(String str){
		try {
			writer.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(long time, int index, int indexWithIgnore, int code, int action, double kLOut){
		char c = ',';
		String out = Long.toString(time) + c + Integer.toString(index) + c + Integer.toString(indexWithIgnore) + 
		c + Integer.toString(code) + c + Integer.toString(action) + c + Double.toString(kLOut) + System.getProperty("line.separator");

		try {
			writer.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Close the buffer
	public void close(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("CSV written successfully!");
	}
}