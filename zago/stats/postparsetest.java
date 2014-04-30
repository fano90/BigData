package stats;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class postparsetest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String path = args[0];

		  try 
		  {
			  BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/" + "STATSEM.txt")));
			  for(int i = 0; i < 5; ++i) { 
				  MediaResults results = parseForMedia( path + "/" + (i+1) + "/SEM.matrix");
				  writer.write("SEM "  + (i+1)  + ": max " + results.getMaximum() + " min " + results.getMinimum() + " media " + results.getMedia() + "\n");
			  }
		  }
		  catch (IOException e) {
			  e.printStackTrace();

		  }

		  try 
		  {
			  BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/" + "STATDEM.txt")));
			  for(int i = 0; i < 5; ++i) { 
				  MediaResults results = parseForMedia( path + "/" + (i+1) + "/DEM.matrix");
				  writer.write("DEM " + (i+1)  + ": max " + results.getMaximum() + " min " + results.getMinimum() + " media " + results.getMedia() + "\n");
			  }
		  }
		  catch (IOException e) {
			  e.printStackTrace();

		  }
		  
		  // Generate file names 
		  String[] paths = new String[5];
		  
		  for(int i = 0; i < 5; ++i) { 
			  paths[i] = path + "/" + (i+1) + "/MEM.matrix";
		  }
		  
		  try { 
			  MemUNION(paths, path + "MEMTOTAL.matrix");
		  }
		  catch (IOException e) {
			  e.printStackTrace();
		  }
		 

		
		  paths = new String[5];
		  for(int i = 0; i < 5; ++i) { 
			  paths[i] = path + "/" + (i+1) + "/DEM.matrix";
		  }
		  
		  try { 
			  DemUNION(paths, path + "/DEMTOTAL.matrix");
		  }
		  catch (IOException e) {
			  e.printStackTrace();
		  }
		  
		  paths = new String[5];
		  for(int i = 0; i < 5; ++i) { 
			  paths[i] = path + "/" + (i+1) + "/BEM.matrix";
		  }
		  
		  try { 
			  BemUNION(paths, path + "/BEMTOTAL.matrix");
		  }
		  catch (IOException e) {
			  e.printStackTrace();
		  }
		 

		
		 try {
			 BemFlatNormalUNION(path +"/BEMTOTAL.matrix", path + "/DEMTOTALFLATNORMAL.matrix");
		 }
		 catch (IOException e) { // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 
		try {
			for(int i = 0; i < 5; ++i) {
				BemFlat75UNION(path + "/" + (i+1) + "/BEM.matrix", path + "/BEM75FLAT" + (i+1) + ".matrix");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * Parses a file and gather minimum, maximum and media of elements for the lines.
	 * @param path The path to open for parsing
	 * @return A @see MediaResults with the results.
	 * @throws IOException
	 */
	public static MediaResults parseForMedia(String path) throws IOException {
		MediaResults results = new MediaResults();
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;

		// Skip the first line as it seems to be always an empty line.
		br.readLine();

		// Used to calculate the media.
		double accumulator = 0;

		while ((line = br.readLine()) != null) {
			String[] entries = line.split(";");
			results.setMaximum(Math.max(entries.length, results.getMaximum()));
			results.setMinimum(Math.min(entries.length, results.getMinimum()));
			accumulator += entries.length;
		}

		// Calculate the media. There are always 4096 entries.
		results.setMedia(accumulator / 4096);

		br.close();

		return results;
	}

	/**
	 * Does a union of BEM (usually 5) by doing the or between cells.
	 * @param paths The list of paths to read the BEM.
	 * @param outPath The output BEM.
	 * @throws IOException
	 */
	public static void BemUNION(String[] paths, String outPath)
			throws IOException {
		// Prepare reader and writers for all provided paths.
		BufferedReader[] readers = new BufferedReader[paths.length];
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				outPath)));

		for (int i = 0; i < paths.length; ++i) {
			readers[i] = new BufferedReader(new FileReader(new File(paths[i])));
		}

		// Skip the first line as it is always an empty/non needed line.
		for (BufferedReader reader : readers) {
			reader.readLine();
		}

		while (true) {
			// Prepare space for the lines to be read at this loop.
			String[] lines = new String[readers.length];

			// Indicates whatever one file reached EOF.
			boolean success = true;

			// Keeps a map of all the values for this line in order to update
			// them on need.
			boolean[] outputMap = new boolean[4096];

			// Read the lines from all files. If one file fails this is
			// considered an exit condition.
			for (int i = 0; i < readers.length; ++i) {
				lines[i] = readers[i].readLine();
				if (lines[i] == null) {
					success = false;
				}
			}

			// Exit as at least one file ended.
			if (!success) {
				break;
			}

			// For each string read check where 1s are.
			for (String line : lines) {
				// Split it in the sub-cells
				String[] entries = line.split(";");

				// There are always 4096 entries.
				for (int i = 0; i < 4096; ++i) {
					// Update the output map depending if it's already true or
					// 1s are found.
					outputMap[i] = (outputMap[i] || entries[i].equals("1"));
				}
			}

			// Write the output OR line of all the provided files.
			for (boolean entry : outputMap) {
				writer.write(entry ? "1" : "0");
				writer.write(";");
			}

			writer.write("\n");
		}

		// Close all.
		for (BufferedReader reader : readers) {
			reader.close();
		}

		writer.close();
	}

	/**
	 * Does a union of MEM (usually 5) by doing the sum between cells.
	 * @param paths The list of paths to read the BEM.
	 * @param outPath The output BEM.
	 * @throws IOException
	 */
	public static void MemUNION(String[] paths, String outPath)
			throws IOException {
		// Prepare reader and writers for all provided paths.
		BufferedReader[] readers = new BufferedReader[paths.length];
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				outPath)));

		for (int i = 0; i < paths.length; ++i) {
			readers[i] = new BufferedReader(new FileReader(new File(paths[i])));
		}

		// Skip the first line as it is always an empty/non needed line.
		for (BufferedReader reader : readers) {
			reader.readLine();
		}

		while (true) {
			// Prepare space for the lines to be read at this loop.
			String[] lines = new String[readers.length];

			// Indicates whatever one file reached EOF.
			boolean success = true;

			// Keeps a map of all the values for this line in order to update
			// them on need.
			int[] outputMap = new int[4096];

			// Read the lines from all files. If one file fails this is
			// considered an exit condition.
			for (int i = 0; i < readers.length; ++i) {
				lines[i] = readers[i].readLine();
				if (lines[i] == null) {
					success = false;
				}
			}

			// Exit as at least one file ended.
			if (!success) {
				break;
			}

			// For each string read check where 1s are.
			for (String line : lines) {
				// Split it in the sub-cells
				String[] entries = line.split(";");

				// There are always 4096 entries.
				for (int i = 0; i < 4096; ++i) {
					// Update the output map depending if it's already true or
					// 1s are found.
					outputMap[i] += Integer.parseInt(entries[i]);
				}
			}

			// Write the output OR line of all the provided files.
			for (int entry : outputMap) {
				writer.write(String.valueOf(entry));
				writer.write(";");
			}

			writer.write("\n");
		}

		// Close all.
		for (BufferedReader reader : readers) {
			reader.close();
		}

		writer.close();
	}

	/**
	 * Does a union of DEM (usually 5) by doing the adding
	 * items in the list from all the DEM while keeping them unique.
	 * @param paths The list of paths to read the DEM.
	 * @param outPath The output DEM.
	 * @throws IOException
	 */
	public static void DemUNION(String[] paths, String outPath)
			throws IOException {
		// Prepare reader and writers for all provided paths.
		BufferedReader[] readers = new BufferedReader[paths.length];
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				outPath)));

		for (int i = 0; i < paths.length; ++i) {
			readers[i] = new BufferedReader(new FileReader(new File(paths[i])));
		}

		// Skip the first line as it is always an empty/non needed line.
		for (BufferedReader reader : readers) {
			reader.readLine();
		}

		while (true) {
			// Prepare space for the lines to be read at this loop.
			String[] lines = new String[readers.length];

			// Indicates whatever one file reached EOF.
			boolean success = true;

			// Keeps a map of all the values for this line in order to update
			// them on need.
			Set<String> outputMap = new HashSet<String>();

			// Read the lines from all files. If one file fails this is
			// considered an exit condition.
			for (int i = 0; i < readers.length; ++i) {
				lines[i] = readers[i].readLine();
				if (lines[i] == null) {
					success = false;
				}
			}

			// Exit as at least one file ended.
			if (!success) {
				break;
			}

			// For each string read check where 1s are.
			for (String line : lines) {
				// Split it in the sub-cells
				String[] entries = line.split(";");
                                outputMap.addAll(Arrays.asList(entries));
			}

			// Write the output OR line of all the provided files.
			for (String entry : outputMap) {
				writer.write(entry);
				writer.write(";");
			}

			writer.write("\n");
		}

		// Close all.
		for (BufferedReader reader : readers) {
			reader.close();
		}

		writer.close();
	}

	/**
	 * Flattens a BEM by taking all lines and doing the or between them.
	 * A single row will be the result.
	 * @param path The path to read the BEM from.
	 * @param outPath The path where to write the result.
	 * @throws IOException
	 */
	public static void BemFlatNormalUNION(String path, String outPath)
			throws IOException {
		// Prepare reader and writers for all provided paths.
		BufferedReader reader = new BufferedReader(new FileReader(
				new File(path)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				outPath)));

		// Prepare space for the lines to be read at this loop.
		String line;

		int[] outputMap = new int[4096];

		while ((line = reader.readLine()) != null) {
			// Split it in the sub-cells
			String[] entries = line.split(";");
			for (int i = 0; i < 4096; ++i) {
				// Update the output map depending if it's already true or 1s
				// are found.
				outputMap[i] += Integer.parseInt(entries[i]);
			}
		}

		// Write the output OR line of all the provided files.
		for (int entry : outputMap) {
			writer.write(String.valueOf(((double) entry) / 4096));
			writer.write(";");
		}

		writer.write("\n");

		// close all.
		reader.close();

		writer.close();
	}

	/**
	 * Flattens a BEM by taking all lines and doing the or between them.
	 * A single row will be the result. The only lines considered are the ones
	 * with at least 75% of 1s.
	 * @param path The path to read the BEM from.
	 * @param outPath The path where to write the result.
	 * @throws IOException
	 */
	public static void BemFlat75UNION(String path, String outPath)
			throws IOException {
		// Prepare reader and writers for all provided paths.
		BufferedReader reader = new BufferedReader(new FileReader(
				new File(path)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				outPath)));

		// Prepare space for the lines to be read at this loop.
		String line;

		boolean[] outputMap = new boolean[4096];

		while ((line = reader.readLine()) != null) {
			// Split it in the sub-cells
			String[] entries = line.split(";");

			// count how many 1s we have
			int counter = 0;
			for (String entry : entries) {
				if (entry.equals("1")) {
					counter++;
				}
			}

			// Consider the line only if at least 75% of the entries are 1s.
			if (counter >= 3072) {
				for (int i = 0; i < 4096; ++i) {
					// Update the output map depending if it's already true or
					// 1s are found.
					outputMap[i] = (outputMap[i] || entries[i].equals("1"));
				}
			}
		}

		// Write the output OR line of all the provided files.
		for (boolean entry : outputMap) {
			writer.write(entry ? "1" : "0");
			writer.write(";");
		}

		writer.write("\n");

		// close all.
		reader.close();

		writer.close();
	}


	//input: MEM file of 4096 rows and 4096 columns
	//output: File with number of occurrences of the single hexamers.
	public static void MemFlatNormalUNION(String path, String outPath) throws IOException {

		// Prepare reader and writers for all provided paths.

		BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outPath)));


		// Prepare space for the lines to be read at this loop.

		String line;


		int[] outputMap = new int[4096];
		for (int row = 0; row < 4096; row++) {
			line = reader.readLine();
			// Split it in the sub-cells

			String[] entries = line.split(";");

			for (int i = 0; i < 4096; i++) {

				// Update the output map
				outputMap[row] += Integer.parseInt(entries[i]);

			}

		}
		// Write the output OR line of all the provided files.

		for (int entry = 0 ; entry < 4096; entry++) {

			writer.write(outputMap[entry]);

			writer.write(";");

		}

		writer.write("\n");
		// close all.

		reader.close();


		writer.close();

	}


        //da lanciare per ogni cromosoma.
	//input: FlatMEM (self-explanatory), media (media non normalizzata che compare nella prima tabella creata da Mattia)
	//output: variance (it is needed to understand if a given hexamer or ordered set of hexamers are statistically significant)
		public static double calculateVariance(int[] FlatMEM, double media) throws IOException {

		// Prepare space for the hexamer occurrence, the expected value and the variance to be read at this loop.

		double variance=0.0;
		double expectedValue;

                    for (int i = 0; i < 4096; i++) {
				variance+=(FlatMEM[i]-media)*(FlatMEM[i]-media);
		    }
                variance=variance/4096;
                return variance;
	}



    public static double calcExpectedValue(int hexamer, double A, double C, double G, double T, double hexamerCount) {
        int[] offset = {1024, 256, 64, 16, 4, 1};
        double result=1.0;
        for (int i = 0; i < 6; ++i) {
            // Divisione intera: mi restituisce la lettera corrispondente
            switch (hexamer / offset[i]) {
                case 0:
                    result = result*A;
                    break;
                case 1:
                    result = result*C;
                    break;
                case 2:
                    result = result*G;
                    break;
                case 3:
                    result = result*T;
                    break;
            }
            // Modulo: mi restituisce il valore successivo da analizzare
            result= result*hexamerCount;
        }
        return result;
    }

	//input: intero limite della lunghezza delle stringhe, file containing number of occurrences of the hexamers, path of output file,
	//       probability of occurrence of A, C, G, T, hexamerCount=number of hexamers in the original fasta file, 	
	//       interest threshold (a [0,1] double representing a probability).
	//output: set of hexamers which are statistically significant, the others aren't interesting enough to elongate them 
	//(that means: the probability that they're not just random strings is lower of the selected threshold).

	public void getSignificantStrings(String MEMpath, String inpath, String outpath, double A, double C, double G, double T, double hexamerCount, double threshold) throws IOException {
		double[] entries = new double[4096];
		BufferedReader reader = new BufferedReader(new FileReader(new File(inpath)));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outpath)));
		String line;
		line = reader.readLine();
		String[] rawLine = line.split(";");
		for (int i = 0; i < 4096; i++) {
			entries[i] = (double) Integer.parseInt(rawLine[i]);
		}
                int[][] MEM = getMEM(MEMpath);
                double avgValue;
                String elo="";
                for (int i = 0; i < 4096; i++) {
                    // get average value and see if the hexamer is significant
                    avgValue=calcExpectedValue(i, A, C, G, T, hexamerCount);
                    if (getProb(hexamerCount, entries[i], avgValue) >= threshold) {
                        elo=elo+i;
                        writer.write(elo+"\n");
                        MEM[i][i]--;
                        stringElongation(elo, entries, A, C, G, T, hexamerCount, threshold, i , writer, MEM);
                        elo="";
                    }
                }    
                reader.close();	
                writer.close();
	}

        public double factorial(double input) {
            double factorial = 1;
            for(int i = 2; i <= input; i++) 
                factorial *= i;               
            return factorial;
        }

        public double getProb(double n, double k, double p) {
            double q = 1.0-p;
            double Bernoulli=(factorial(n)) / (factorial(k) * factorial(n-k));
            return Math.pow(p,k)*Math.pow(q, (n-k))*Bernoulli;
        }
        
        public void stringElongation(String elo, double[] entries, double A, double C, double G, double T, double hexamerCount, double threshold, int previous, BufferedWriter writer, int[][] MEM ) throws IOException {
            double avgValue; 
            String line ="";
            String[] rawLine;
            for (int i = 0; i < 4096; i++) {
		// get average value and see if the hexamer is significant
                avgValue=calcExpectedValue(i, A, C, G, T, hexamerCount);
                if (getProb(entries[i], MEM[previous][i], avgValue)*previous >= threshold) {
                    elo=elo+";"+i;
                    writer.write(elo+"\n");
                    MEM[previous][i]--;
                    stringElongation(elo, entries, A, C, G, T, hexamerCount, threshold, i , writer, MEM);
                }
            }
        }
                public int[][] getMEM(String MEMpath)throws IOException{
                    BufferedReader reader = new BufferedReader(new FileReader(new File(MEMpath)));
                    int[][] MEM=new int[4096][4096];
                    String line = "";
                    String[] rawLine;
                    
                    for (int i = 0; i < 4096; i++) {
                        line = reader.readLine();
                        rawLine = line.split(";");
                        for (int l = 0; l < 4096; l++) {
                            MEM[i][l] = Integer.parseInt(rawLine[l]);
                        }
                    }
                    return MEM;
        }
}

