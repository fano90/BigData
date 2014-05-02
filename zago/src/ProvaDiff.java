import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
//CIAO
public class ProvaDiff {
	public static void main(String args[]) throws IOException, InterruptedException{
    FileReader f,f2;
    f=new FileReader("/home/student/Risultati/frequenzeTUTTI.txt");

    BufferedReader br = new BufferedReader(f);
    
    
    
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
    	System.out.println("lontananza finale"+" "+"= " + lontananza);
    	br.close();
 }
}
