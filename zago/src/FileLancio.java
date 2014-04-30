import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.StringTokenizer;

public class FileLancio {
	public static void main(String args[]) throws IOException, InterruptedException{
		 try {
			//funzione che mi richiama il jar
			String std_out;
			BufferedReader br;
			OutputStream outputStream;
			PrintStream printStream;
	 		String autore= "Simond";
	 		 //pER ORA IL VALORE 2 NON VIENE GUARDATO!
	 		String hadoop_main_cmd = 	"hadoop jar " + 
	 									"/home/student/prof.jar fr.eurecom.dsg.mapreduce.FrequenceNuovo "+
	 									"/user/student/INPUT/autori/" + autore +
	 									" OUTPUT/frequence1 OUTPUT/frequence2 OUTPUT/risultati";
	 		
	 		
	 		//COMANDO PEr eseguire le frequenze del libro di test 2.txt
	 		String numeroLibro = "3.txt";
	 		String autoreTest ="Simond";
	 		String hadoop_test_cmd = 	"hadoop jar " + 
	 									"/home/student/prof.jar fr.eurecom.dsg.mapreduce.FrequenceNuovo "+
								 		"/user/student/INPUT/autori/" +autoreTest+ "/"+ numeroLibro +
								 		" OUTPUTEST/frequence1 OUTPUTEST/frequence2 OUTPUTEST/risultati";
	 		
	 		//Downlaod delle frequenze di tutti i libri dell'Autore X
	 		String hadoop_risultato = "hadoop fs -get OUTPUT/risultati/part-r-00000 "+
	 								  "/home/student/Risultati/frequenzeTUTTI.txt";
	 		
	 		//Download delle frequenze del libro di test
	 		String hadoop_risultato_test = "hadoop fs -get OUTPUTEST/risultati/part-r-00000 "+
	 										"/home/student/Risultati/frequenzeTEST.txt";
		 		
	 		
	 		//ELIMINA CARTELLE DI OUTPUT
	 	
	 		Process hadoop_delete= Runtime.getRuntime().exec("hadoop fs -rm -r OUTPUT/ OUTPUTEST/");
	 		hadoop_delete.waitFor();
	 		
	 		
	 		//ELIMINARE FILE DI RISULTATI
	 		String fileName1 = "/home/student/Risultati/frequenzeTUTTI.txt";
	 		String fileName2 = "/home/student/Risultati/frequenzeTEST.txt";
	 		
	 		File ff1 = new File(fileName1);
	 		File ff2 = new File(fileName2);
	 		
	 		ff1.delete();
	 		ff2.delete();
	 		
		 	//lancio il JAR	 --> per tutti i libri
			System.out.println("\n----------------------------------------------------");
	        System.out.println("Start Hadoop put cmd: " + hadoop_main_cmd);
	        System.out.println("TUTTI I LIBRI");
	        Process hadoop_main= Runtime.getRuntime().exec(hadoop_main_cmd);
	        System.out.println("Wait for process end (Process Name: " + hadoop_main.toString() + ")");
	        hadoop_main.waitFor();
	        br = new BufferedReader(new InputStreamReader(hadoop_main.getErrorStream()));
	        while ((std_out = br.readLine()) != null) 
	        	System.out.println(std_out);
	        br.close();
	        outputStream = hadoop_main.getOutputStream();
	        printStream = new PrintStream(outputStream);
	        printStream.println();
	        printStream.flush();
	        printStream.close();
//---------------------FINE PROCESSO 1 ----------------------------------------------------
	        
	      //inizio processo 2 lancio il JAR	 --> per tutti i libri
			System.out.println("\n----------------------------------------------------");
	        System.out.println("Start Hadoop put cmd: " + hadoop_test_cmd);
	        System.out.println("LANCIO IL TEST");
	        Process hadoop_test= Runtime.getRuntime().exec(hadoop_test_cmd);
	        System.out.println("Wait for process end (Process Name: " + hadoop_test.toString() + ")");
	        hadoop_test.waitFor();
	        br = new BufferedReader(new InputStreamReader(hadoop_test.getErrorStream()));
	        while ((std_out = br.readLine()) != null) 
	        	System.out.println(std_out);
	        br.close();
	        outputStream = hadoop_test.getOutputStream();
	        printStream = new PrintStream(outputStream);
	        printStream.println();
	        printStream.flush();
	        printStream.close();
	        
//---------------------FINE PROCESSO 2 ----------------------------------------------------	        
	        
	        
//INIZIA IL DOWNLAOD DEI DATI	        

//funzione che accede all HDFS per avere i risultati di TUTTI i libri
	        System.out.println("Start Hadoop put cmd: " + hadoop_risultato);
	        Process hadoop_download= Runtime.getRuntime().exec(hadoop_risultato);
	        System.out.println("Wait for process end (Process Name: " + hadoop_download.toString() + ")");
	        System.out.println("LIbro test");
	        hadoop_download.waitFor();
	        
	        br = new BufferedReader(new InputStreamReader(hadoop_download.getErrorStream()));
	        while ((std_out = br.readLine()) != null) 
	        	System.out.println(std_out);
	        br.close();
	        
	        outputStream = hadoop_download.getOutputStream();
	        printStream = new PrintStream(outputStream);
	        printStream.println();
	        printStream.flush();
	        printStream.close();
	        System.out.println("FINE DOWNLAOD FILE 1");
	        
//funizone che accede all HDFS per avere i risultati del libro di TEST
	        System.out.println("Start Hadoop put cmd: " + hadoop_risultato_test);
	        Process hadoop_download_test= Runtime.getRuntime().exec(hadoop_risultato_test);
	        System.out.println("Wait for process end (Process Name: " + hadoop_download_test.toString() + ")");
	        hadoop_download_test.waitFor();
	        
	        br = new BufferedReader(new InputStreamReader(hadoop_download_test.getErrorStream()));
	        while ((std_out = br.readLine()) != null) 
	        	System.out.println(std_out);
	        br.close();
	        
	        outputStream = hadoop_download_test.getOutputStream();
	        printStream = new PrintStream(outputStream);
	        printStream.println();
	        printStream.flush();
	        printStream.close();
	        System.out.println("FINE DOWNLAOD FILE 2");
	        
	        //TODO fare un metodo invece di scriverlo qua
	        FileReader f,f2;
	        f=new FileReader("/home/student/Risultati/frequenzeTUTTI.txt");

	        br=new BufferedReader(f);
	        
	        
	        
	        //Legge riga per riga dal file
	        String line,line2;

	        
	        
	        //TODO 	poi scrivere su un altro FILe IL METODO
	        HashMap< String, Double> firma1 = new HashMap<String, Double>();
	         
	        for (int i=0;i<30;i++){
	        	line=br.readLine();
	        	StringTokenizer tokens = new StringTokenizer(line);
	        	/**value --> frequenza Key --> parola*/
	        	Double value = Double.parseDouble(tokens.nextToken());
	        	String key = tokens.nextToken();
	        	
	        	firma1.put(key , value);
	        	System.out.println("frequenza"+" " + key +": " +firma1.get(key));
	        	
	        }
	        System.out.println("FINE primo File-------------------------------------");
	        br.close();
	        
	        //apro nuovo file
	        f2=new FileReader("/home/student/Risultati/frequenzeTEST.txt");
	        br=new BufferedReader(f2);
	        Double lontananza=0.0;
	        for (int i=0;i<30;i++){
	        	line2=br.readLine();
	        	StringTokenizer tokens = new StringTokenizer(line2);
	        	/**value --> frequenza Key --> parola*/
	        	Double value2 = Double.parseDouble(tokens.nextToken());
	        	String key2 = tokens.nextToken();
	        	
	        	if (firma1.containsKey(key2)){
	        		lontananza += Math.abs(firma1.get(key2) - value2);
	        		System.out.println("lontananza  "+ key2 +" "+"= " + lontananza);
	        	}
	        	else{
	        		lontananza += 1;
	        		System.out.println("lontananza vuota"+" "+"= " + lontananza);
	        	}
	        }
	        	System.out.println("lontananza"+" " + lontananza);
	        	br.close();
		 }
		 catch (Exception e) {
			 System.out.println("#===="+e);
		}
}
	}