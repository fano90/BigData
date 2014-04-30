import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import classes.*;

/**
 * Miscellaneous tools to enable the MapReduce project to be run.
 * @author Mattia Zago <info@zagomattia.it>
 * @author Alessandro Menti
 */
public class Tools {

	/**
	 * Alfabeto genomico. Percentuali: A,T: 20% C-G: 30%
	 */
    public static final String[] alfabeto = {"A", "C", "G", "T"};

    /**
     * Argomenti richiesti: &lt;NOME_PROCESSO&gt; &lt;DIMENSIONE_RIGA&gt; &lt;CARTELLA_ROOT&gt; &lt;TENTATIVO&gt; &lt;LANCIA_HADOOP?&gt;
     * @param args &lt;NOME_PROCESSO&gt; &lt;DIMENSIONE_RIGA&gt; &lt;CARTELLA_ROOT&gt; &lt;TENTATIVO&gt; &lt;LANCIA_HADOOP?&gt;
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	    long time_all_start = System.currentTimeMillis();
	    try {
	    	// Per velocità nel test di questo sistema imposto io i parametri a mano
	        args = new String[7];							// FIXME: Da rimuovere nella versione finale
	        args[0] = "ChrX-5";                                                     // FIXME: Da rimuovere nella versione finale
	        args[1] = "50000";							// FIXME: Da rimuovere nella versione finale
	        args[2] = "/home/student/Downloads/ChrX-5";		// FIXME: Da rimuovere nella versione finale
	        args[3] = "001";							// FIXME: Da rimuovere nella versione finale
	        args[4] = "-hadoop";							// FIXME: Da rimuovere nella versione finale
            args[5] = "-preparse";
            args[6] = "-postparse";
	        /* ---------------------------------------------------------------------
	         * Configurazione dei parametri del progetto
	         * ---------------------------------------------------------------------
	         */
	        
	        // Nome del processo
	        String name = args[0];
	        
	        // Numero di esameri per ogni esecuzione del mapper (si suggerisce <= 100.000
	        int num_exam_per_blocco = Integer.valueOf(args[1]); 
	        
	        // File di input - Deve necessariamente essere pulito della riga 
	        // di intestazione che inizia con '>'
	        File sequence = new File(args[2] + "/sequence.fasta"); 
	        
	        // File pulito da passare ad Hadoop
	        File input = new File(args[2] + "/sequence.input"); 
	        
	        // Jar di questo pacchetto
	        String jarFile = args[2] + "/genome.jar";
	        
	        // Tentativo - Deve essere forzato un numero progressivo
	        String progress = args[3]; 
	        
	        // Cartella di output dei risultati di Hadoop
	        String outputFolder = args[2] + "/" + name + "-" + progress; 
	        
	        // Flag per stabilire se eseguire o meno il processo di Hadoop
	        boolean exec_hadoop = false;
                boolean exec_preparser = false;
                boolean exec_postparser = false;
                for(int i=0; i<args.length; i++) {
                    if(args[i].equalsIgnoreCase("-hadoop")) exec_hadoop = true;
                    if(args[i].equalsIgnoreCase("-preparse")) exec_preparser = true;
                    if(args[i].equalsIgnoreCase("-postparse")) exec_postparser = true;
                }
	
	        // File HDFS per l'input 
	        String hadoop_input = "INPUT/" + name + "-" + progress; 
	        
	        // Cartella HDFS per l'output
	        String hadoop_output = "OUTPUT/" + name + "-" + progress; 
	
	        // Comando di shell per l'upload del file su HDFS
	        String hadoop_put_cmd = "hadoop fs " +
	        						"-put " + 
	        						input.getAbsolutePath() + 
	        						" " + 
	        						hadoop_input; 
	        
	        // Comando di shell per il download da HDFS della cartella di output
	        String hadoop_get_cmd = "hadoop fs " +
	        						"-get " + 
	    							hadoop_output + 
	    							" " + 
	    							args[2]; 
	        
	        // Numero dei reducer per il cluster
	        String num_reducer = "4";
	        
	        // Comando di shell per lanciare il processo di Hadoop
	        String hadoop_main_cmd = 	"hadoop jar " + 
	        							jarFile + 
	    								" SEM " +
	        							num_reducer +
	        							" " + 
	        							hadoop_input + 
	        							" " + 
	        							hadoop_output; 
	
	        
	        
	        /* ---------------------------------------------------------------------
	         * Fase 1 - Preparse dei dati
	         * ---------------------------------------------------------------------
	         */
	
	        if(exec_preparser) Tools.preparser(num_exam_per_blocco, sequence, input);
	
	        /* ---------------------------------------------------------------------
	         * Fase 2 - Lavoro sul cluster
	         * NB. Può essere evitata impostando in 5° argomento a 'false'
	         * ---------------------------------------------------------------------
	         */
	        if(exec_hadoop) {
	        	/*
	        	 * Configurazione dei parametri per la redirezione dell'output del cluster 
	        	 */
		        String std_out;
		        BufferedReader br;
		        OutputStream outputStream;
		        PrintStream printStream;
		        
		
		        // FASE 1: Upload del file input.fasta su HDFS
		        long hadoop_time = System.currentTimeMillis();
		        System.out.println("\n----------------------------------------------------");
		        System.out.println(""+time(hadoop_time));
		        System.out.println("Start Hadoop put cmd: " + hadoop_put_cmd);
		        Process hadoop_put = Runtime.getRuntime().exec(hadoop_put_cmd);
		        System.out.println("Wait for process end (Process Name: " + hadoop_put.toString() + ")");
		        hadoop_put.waitFor();
		        
		        // Redireziono l'output
		        br = new BufferedReader(new InputStreamReader(hadoop_put.getErrorStream()));
		        while ((std_out = br.readLine()) != null) System.out.println(std_out);
		        br.close();
		        outputStream = hadoop_put.getOutputStream();
		        printStream = new PrintStream(outputStream);
		        printStream.println();
		        printStream.flush();
		        printStream.close();
		        
		        // Termino la FASE 1 segnando il tempo di esecuzione parziale
		        System.out.println("Hadoop put end in: " + time(hadoop_time));
		        System.out.print("Exit Value: " + hadoop_put.exitValue());
		        if (hadoop_put.exitValue() != 0) {
		        	System.out.print(" - ERROR. Task Killed\n");
		        	return;
		        } else {
		        	System.out.print(" - Go on\n");
		        }
		        System.out.println("----------------------------------------------------");
		        // TERMINE FASE 1
		        
		        // FASE 2: Lancio del cluster parametrizzato
		        hadoop_time = System.currentTimeMillis();
		        System.out.println("\n----------------------------------------------------");
		        System.out.println(""+time(hadoop_time));
		        System.out.println("Start Hadoop main23 cmd: " + hadoop_main_cmd);
		        Process hadoop_main = Runtime.getRuntime().exec(hadoop_main_cmd);
		        System.out.println("Wait for process end (Process Name: " + hadoop_main.toString() + ")");
		        hadoop_main.waitFor();
		        
		        // Redireziono l'output
		        br = new BufferedReader(new InputStreamReader(hadoop_main.getErrorStream()));
		        while ((std_out = br.readLine()) != null) System.out.println(std_out);
		        br.close();
		        outputStream = hadoop_main.getOutputStream();
		        printStream = new PrintStream(outputStream);
		        printStream.println();
		        printStream.flush();
		        printStream.close();
		        
		        // Termino la FASE 2 segnando il tempo di esecuzione parziale
		        System.out.println("Hadoop main end in: " + time(hadoop_time));
		        System.out.print("Exit Value: " + hadoop_main.exitValue());
		        if (hadoop_main.exitValue() != 0) {
		        	System.out.print(" - ERROR. Task Killed\n");
		        	return;
		        } else {
		        	System.out.print(" - Go on\n");
		        }
		        System.out.println("----------------------------------------------------");
		        // TERMINE FASE 2
		
		        // FASE 3: Download dei risultati da HDFS
		        hadoop_time = System.currentTimeMillis();
		        System.out.println("\n----------------------------------------------------");
		        System.out.println(""+time(hadoop_time));
		        System.out.println("Start Hadoop get cmd: " + hadoop_get_cmd);
		        Process hadoop_get = Runtime.getRuntime().exec(hadoop_get_cmd);
		        System.out.println("Wait for process end (Process Name: " + hadoop_get.toString() + ")");
		        hadoop_get.waitFor();
		        
		        // Redireziono l'output
		        br = new BufferedReader(new InputStreamReader(hadoop_get.getErrorStream()));
		        while ((std_out = br.readLine()) != null) System.out.println(std_out);
		        br.close();
		        outputStream = hadoop_get.getOutputStream();
		        printStream = new PrintStream(outputStream);
		        printStream.println();
		        printStream.flush();
		        printStream.close();
		        
		        // Termino la FASE 3 segnando il tempo di esecuzione parziale
		        System.out.println("Hadoop get end in: " + time(hadoop_time));
		        System.out.print("Exit Value: " + hadoop_get.exitValue());
		        if (hadoop_get.exitValue() != 0) {
		        	System.out.print(" - ERROR. Task Killed\n");
		        	return;
		        } else {
		        	System.out.print(" - Go on\n");
		        }
		        System.out.println("----------------------------------------------------");
		        // TERMINE FASE 3
	        
	        } // Termine della porzione di codice eseguita solamente se il 5° argomento è true
	        
                if(exec_postparser) {
                    // Creo il file di output basandomi sulla cartella di output di Hadoop
                    File output = Tools.mergeReducer(outputFolder, Tools.listFile(new File(outputFolder))); 
                    //File output = new File("C:\\Users\\Mattia\\Downloads\\HmnChrY\\New Folder\\merged_result");
                    if(!output.exists()) {
                            System.out.println("Output folder does not exists. Task killed");
                            return;
                    }

                    /*
                     * Finalizzo il processo. Scrivo la matrice "name" a partire dal file
                     * output nel file SEM. Esporto in MEM la matrice SEM
                     */
                    Tools.decodeResult(output, args[2]);
                }
	    } finally {
	    	System.out.println("\n#====================================================");
	        System.out.println("| Processo completato in: "+time(time_all_start));
	    	System.out.println("#====================================================");
	    }
    }

    public static String force4char(int num) {
    	if(num<10) return "000"+num;
    	else if(num<100) return "00"+num;
    	else if(num<1000) return "0"+num;
    	else if(num<10000) return ""+num;
    	else return null;
    }
    
    /**
     * Il metodo prende in input la cartella di output di hadoop e crea il file di risultato finale
     * @param folder_path Cartella di output
     * @param hadoop_output_folder_list Lista dei file di Output di Hadoop
     * @return File con il merge dei risultati di Hadoop
     * @throws IOException
     */
    private static File mergeReducer(String folder_path, List<File> hadoop_output_folder_list) throws IOException {
        System.out.println("Start merge reducer on " + folder_path);
        
        File result = new File(folder_path + "/../merged_result");
        result.createNewFile();
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(result, true)));

        // Scansiono ogni file nella cartella (ed in tutte le sottodirectory ricorsivamente)
        for (int i = 0; i < hadoop_output_folder_list.size(); i++) {
            File tmp = hadoop_output_folder_list.get(i);
            System.out.print("Look: " + tmp.getAbsolutePath());
            if (tmp.isFile() && tmp.getAbsolutePath().contains("part-r-")) {
                // Il file è un output del reducer (contiene la sigla 'part-r-'
                FileInputStream fis = new FileInputStream(tmp);
                byte[] data = new byte[(int) tmp.length()];
                fis.read(data);
                fis.close();
                out.append(new String(data, "UTF-8"));
                System.out.print(" \tCOPIED\n");
            } else {
                System.out.print(" \tSKIPPED\n");
            }

        }
        out.close();
        return result;
    }

    /**
     * Decodifica tutte le righe del file result passato come argomento.<br>
     * Crea la seguente struttura:<br>
     * root_folder/<br>
     *  |-SEM/<br>
     *  |- |-1<br>
     *  |- |-...<br>
     *  |- |-4096<br>
     *  |-MEM/<br>
     *  |- |-1<br>
     *  |- |-...<br>
     *  |- |-4096<br>
     *  |-BEM/<br>
     *  |- |-1<br>
     *  |- |-...<br>
     *  |- |-4096<br>
     *  |-DEM/<br>
     *  |- |-1<br>
     *  |- |-...<br>
     *  |- |-4096<br>
     *  |-SEM.matrix<br>
     *  |-MEM.matrix<br>
     *  |-BEM.matrix<br>
     *  |-DEM.matrix<br>
     *  |-num_hex.csv<br>
     *  Le cartelle *EM contengono sempre 4096 file, ovvero un file per ogni esamero. 
     *  Ciascun file &egrave; il risultato dell'elaborazione della riga corrispondente 
     *  nel file di risultato.
     * @param result File risultato
     * @param root_folder Path della cartella del progetto
     * @throws Exception 
     */
    public static void decodeResult(File result, String root_folder) throws Exception {
    	// Inizializzo la variabile per il monitoraggio dei tempi di esecuzione
    	long time_start = System.currentTimeMillis();
    	
    	System.out.println("\n----------------------------------------------------");
        System.out.println(""+time(time_start)+"\tStart '"+result.getPath()+"' decoding");
        System.out.println("----------------------------------------------------");
        
        // Creo le cartelle con i file degli esameri
        File folder_SEM = new File(root_folder+"/SEM");
        File folder_MEM = new File(root_folder+"/MEM");
        File folder_BEM = new File(root_folder+"/BEM");
        File folder_DEM = new File(root_folder+"/DEM");
        // Creo il file num_hex per tenere traccia della dimensione di ogni singola riga
        PrintWriter writer_NumHex = new PrintWriter(new File(root_folder+"/num_hex.csv"));
        //File image_MEM = new File(root_folder+"/MEM.png");
       
        System.out.println(""+time(time_start)+"\t\tCreate SEM folder");
        folder_SEM.mkdirs();
        System.out.println(""+time(time_start)+"\t\tCreate MEM folder");
        folder_MEM.mkdirs();
        System.out.println(""+time(time_start)+"\t\tCreate BEM folder");
        folder_BEM.mkdirs();
        System.out.println(""+time(time_start)+"\t\tCreate DEM folder");
        folder_DEM.mkdirs();
        
        // Variabili per tenere traccia della massima e della minima lunghezza
        int num_hex_minValue = Integer.MAX_VALUE;
        int num_hex_maxValue = Integer.MIN_VALUE;
        
        // Decodifica del file risultato
        String line; 
        int line_counter = 0;
        BufferedReader br = new BufferedReader(new FileReader(result));
        while ((line = br.readLine()) != null) {
            System.out.print(""+time(time_start)+"\t\t\tProcess Line "+(++line_counter));
            /* 
             * Ogni riga del file è codificata così: 
             * AAAACC\tFINAL-5-AAAACC#SATTAGC,AGTCAG,TTCATT,GTTGAAE
             */
            
            // Ottengo il nome dell'esamero in questione
            String tmp_name = line.split("\t")[0];
            
            // Ottengo la sequenza degli esameri, ignorando il tag 'FINAL-5-AAAACC'
            line = (line.split("\t")[1]).split(SEM_row.fs)[1];
            
            // Sovrascrivo la sequenza eliminando i tag di inizio 'S' e di fine 'E'
            String sost = line.replaceAll("S", "");
            line = sost.replaceAll("E", "");
            
            // Splitto la sequenza degli esameri in un array di stringhe
            String[] esameri = line.split(SEM_row.sep); 
            System.out.print(" \t- Name: "+tmp_name+" #HEX: "+esameri.length+"\n");
            
            // Conto il numero di esameri in questa sequenza e lo scrivo nel file num_hex
            writer_NumHex.println(tmp_name+";"+esameri.length);
            
            // Inizializzo i file locali alle cartelle *EM ed i relativi writer
            File tmp_SEM = new File(folder_SEM.getPath()+"/"+Hexamer.getPosition(new Hexamer(tmp_name)));
            File tmp_MEM = new File(folder_MEM.getPath()+"/"+Hexamer.getPosition(new Hexamer(tmp_name)));
            File tmp_BEM = new File(folder_BEM.getPath()+"/"+Hexamer.getPosition(new Hexamer(tmp_name)));
            File tmp_DEM = new File(folder_DEM.getPath()+"/"+Hexamer.getPosition(new Hexamer(tmp_name)));
            PrintWriter writer_SEM = new PrintWriter(tmp_SEM);
            PrintWriter writer_MEM = new PrintWriter(tmp_MEM);
            PrintWriter writer_BEM = new PrintWriter(tmp_BEM);
            PrintWriter writer_DEM = new PrintWriter(tmp_DEM);
            
            /* 
             * Inizializzo il vettore di supporto a 0
             * Nota: Effettivamente se non trovassi tutti i 4096 esameri in una riga
             * spreco del tempo prezioso, tuttavia così mi evito un controllo inutile 
             * ad ogni esamero letto della sequenza
             */ 
            int[] tmp_MEM_support = new int[4096]; 
            for(int i=0; i<4096; i++) 
                tmp_MEM_support[i]=0;
            
            // Ciclo lungo tutta la sequenza per estrarre gli esameri 
            for(int i=0; i<esameri.length; i++) {
            	// Creo l'oggetto esamero -> necessario per controllare che non ci siano caratteri esterni
                Hexamer ex = new Hexamer(esameri[i]);
                int pos = Hexamer.getPosition(ex);
                
                // Scrivo il risultato nel file aperto in SEM/
                writer_SEM.print(pos+";");
                
                /*
                 * Controllo nel vettore di supporto se ho già incontrato prima questo esamero.
                 * Se il conteggio è zero allora è nuovo, devo quindi scriverlo nel file aperto in DEM/
                 */
                if(tmp_MEM_support[pos] == 0) writer_DEM.print(ex+";");
                
                // Aggiorno il vettore di supporto
                tmp_MEM_support[pos]++;
                
                // Aggiorno il valore di massimo e minimo
                if(tmp_MEM_support[pos]<num_hex_minValue) num_hex_minValue=tmp_MEM_support[pos]; // tengo traccia del minimo per poter normalizzare
                if(tmp_MEM_support[pos]>num_hex_maxValue) num_hex_maxValue=tmp_MEM_support[pos]; // tengo traccia del massimo per poter normalizzare
            }
            
            // Ciclo lungo tutto il vettore di supporto
            for(int i=0; i<4096; i++) {
                int tmp = tmp_MEM_support[i];
                
                // Scrivo il risultato nel file temporaneo in MEM/
                writer_MEM.print(tmp+";");
                
                /*
                 * Se il valore è diverso da zero allora devo forzarlo a uno.
                 * Scrivo quindi il risultato nel file temporaneo in BEM/ 
                 */
                if(tmp == 0) writer_BEM.print("0;");
                else writer_BEM.print("1;");
            }
            
            // Chiudo i file aperti nelle cartelle *EM
            writer_SEM.close();
            writer_MEM.close();
            writer_BEM.close();
            writer_DEM.close();
        
        } // Fine while -> Ho processato tutto il file risultato
        
        // Chiudo i file aperti non più necessari
        br.close();
        writer_NumHex.close();
        
        /*
         * Procedura di merge per le matrici *EM.
         * Per ogni matrice leggo in ordine tutti i file presenti nelle rispettive cartelle 
         * e li concateno in *EM.matrix
         */
        // Apertura e creazione dei file necessari
        File result_SEM = new File(root_folder + "/SEM.matrix");
        File result_MEM = new File(root_folder + "/MEM.matrix");
        File result_BEM = new File(root_folder + "/BEM.matrix");
        File result_DEM = new File(root_folder + "/DEM.matrix");
        
        // ==== START SEM MERGE ============================================================
        System.out.println(""+time(time_start)+"\t\tStart merge '"+folder_SEM.getPath()+"' in '"+result_SEM.getPath()+"'");
        result_SEM.createNewFile();
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(result_SEM, true)));
        for (int i = 0; i < 4096; i++) {
            File tmp = new File(folder_SEM.getPath()+"/"+i);
            System.out.print(""+time(time_start)+"\t\t\tLook: " + tmp.getAbsolutePath());
            if (tmp.isFile()) {
                // Il file è un output del reducer
                FileInputStream fis = new FileInputStream(tmp);
                byte[] data = new byte[(int) tmp.length()];
                fis.read(data);
                fis.close();
                out.append('\n');
                out.append(new String(data, "UTF-8"));
                System.out.print(" \tCOPIED\n");
            } else {
                System.out.print(" \tSKIPPED\n");
            }
        }
        out.close();
        System.out.println(""+time(time_start)+"\t\tSEM merge completed");
        // ==== END SEM MERGE ============================================================
        
        // ==== START MEM MERGE ============================================================
        System.out.println(""+time(time_start)+"\t\tStart merge '"+folder_MEM.getPath()+"' in '"+result_MEM.getPath()+"'");
        result_MEM.createNewFile();
        out = new PrintWriter(new BufferedWriter(new FileWriter(result_MEM, true)));
        for (int i = 0; i < 4096; i++) {
            File tmp = new File(folder_MEM.getPath()+"/"+i);
            System.out.print(""+time(time_start)+"\t\t\tLook: " + tmp.getAbsolutePath());
            if (tmp.isFile()) {
                // Il file è un output del reducer
                FileInputStream fis = new FileInputStream(tmp);
                byte[] data = new byte[(int) tmp.length()];
                fis.read(data);
                fis.close();
                out.append('\n');
                out.append(new String(data, "UTF-8"));
                System.out.print(" \tCOPIED\n");
            } else {
                System.out.print(" \tSKIPPED\n");
            }
        }
        out.close();
        System.out.println(""+time(time_start)+"\t\tMEM merge completed");
        // ==== END MEM MERGE ============================================================
        
        // ==== START BEM MERGE ============================================================
        System.out.println(""+time(time_start)+"\t\tStart merge '"+folder_BEM.getPath()+"' in '"+result_BEM.getPath()+"'");
        result_BEM.createNewFile();
        out = new PrintWriter(new BufferedWriter(new FileWriter(result_BEM, true)));
        for (int i = 0; i < 4096; i++) {
            File tmp = new File(folder_BEM.getPath()+"/"+i);
            System.out.print(""+time(time_start)+"\t\t\tLook: " + tmp.getAbsolutePath());
            if (tmp.isFile()) {
                // Il file è un output del reducer
                FileInputStream fis = new FileInputStream(tmp);
                byte[] data = new byte[(int) tmp.length()];
                fis.read(data);
                fis.close();
                out.append('\n');
                out.append(new String(data, "UTF-8"));
                System.out.print(" \tCOPIED\n");
            } else {
                System.out.print(" \tSKIPPED\n");
            }
        }
        out.close();
        System.out.println(""+time(time_start)+"\t\tBEM merge completed");
        // ==== END BEM MERGE ============================================================
        
        // ==== START DEM MERGE ============================================================
        System.out.println(""+time(time_start)+"\t\tStart merge '"+folder_DEM.getPath()+"' in '"+result_DEM.getPath()+"'");
        result_DEM.createNewFile();
        out = new PrintWriter(new BufferedWriter(new FileWriter(result_DEM, true)));
        for (int i = 0; i < 4096; i++) {
            File tmp = new File(folder_DEM.getPath()+"/"+i);
            System.out.print(""+time(time_start)+"\t\t\tLook: " + tmp.getAbsolutePath());
            if (tmp.isFile()) {
                // Il file è un output del reducer
                FileInputStream fis = new FileInputStream(tmp);
                byte[] data = new byte[(int) tmp.length()];
                fis.read(data);
                fis.close();
                out.append('\n');
                out.append(new String(data, "UTF-8"));
                System.out.print(" \tCOPIED\n");
            } else {
                System.out.print(" \tSKIPPED\n");
            }
        }
        out.close();
        System.out.println(""+time(time_start)+"\t\tDEM merge completed");
        // ==== END DEM MERGE ============================================================
        
        // Creo un'immagine a partire dalla matrice MEM.
        // Devo normalizzare tutti i dati di MEM sulla scala 0-255
        /*
        System.out.println("Start creating MEM image");
        System.out.println("Max: "+max_value+" - Min: "+min_value+" - Delta: "+(max_value-min_value));
        float lower = 0;
        float upper = 255;
        int molt = 2000;
        BufferedImage img = new BufferedImage(4100, 4100, BufferedImage.TYPE_INT_RGB);
        br = new BufferedReader(new FileReader(result_MEM));
        int r = 0;
        while ((line = br.readLine()) != null) {
            if(!line.contains(";")) continue;
            String[] row = line.split(";");
            for(int c=0; c<1000; c++) {
                System.out.print("["+r+","+c+"]");
                float tmp = Integer.parseInt(row[c]);
                //System.out.println("\tReal: "+tmp);
                //if(tmp==max_value) tmp = upper;
                //else if(tmp==min_value) tmp = lower;
                //else tmp = (molt * upper * (tmp)/(max_value-min_value)); 
                
                if(tmp > upper) tmp = upper;
                
                System.out.print("\tNorm: "+Math.round(tmp)+"   \t"+tmp+"\n");
                img.setRGB(c, r, Math.round(2*tmp));
            }
            r++;
            if(r%100==0) ImageIO.write(img, "png", image_MEM);
            if(r==500) break;
        }
        
        System.out.print("Write img to: "+image_MEM.getAbsolutePath());
        if(ImageIO.write(img, "png", image_MEM)) System.out.print(" - DONE\n\n");
        else System.out.print(" - ERROR\n\n");
        */
        
        // Termino stampando il tempo impiegato nell'elaborazione
        System.out.println("----------------------------------------------------");
        System.out.println("END '"+result.getPath()+"' decoding");
        System.out.println("Time: "+time(time_start));
        System.out.println("----------------------------------------------------");
    }

    /**
     * Restituisce il differenziale del tempo
     * @param start Millisecondi iniziali
     * @return now()-start nel formato H:mm:ss
     */
    public static String time(long start) {
    	long millis = System.currentTimeMillis() - start;
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hour, minute, second);
    }
    
    public static void removeLineBreak(final File file, final File clean) throws Exception {

        PrintWriter writer = null;
        FileInputStream fis = null;

        try {
            writer = new PrintWriter(clean);
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            //
            String s = new String(data, "UTF-8");
    
            System.out.println("Dimensione: " + s.length() + " (Esameri: "
                + (s.length() / 6) + ")");
    
            writer.print(s.replaceAll("\n", ""));
    
            System.out.println("FINISH");
        } finally {
            if (writer != null) {
                writer.close();
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                // Nothing to do
            }
        }
    }

    /**
     * Sostituisce lo splitter custom di hadoop. Rimuove tutti i line-break
     * 
     * @param numExam
     *        Numero di esameri per ogni blocco <=> ogni k*numExam inserisce uno
     *        '//n'
     * @param fin
     *        File di input
     * @param fout
     *        File di output
     * @throws Exception
     */
    public static void preparser(int numExam, File fin, File fout) throws Exception {
    	long time_start = System.currentTimeMillis();
    	System.out.println("\n----------------------------------------------------");
        System.out.println(""+time(time_start));
        System.out.println("Start preparser");
        System.out.println("Hexamer/line: "+numExam);
        System.out.println("Input: "+fin.getAbsolutePath());
        System.out.println("Output: "+fout.getAbsolutePath());
        System.out.println("----------------------------------------------------");
  
        int tmpchar;
        int esameri = numExam;
        int riga=esameri*6;
        int[] last = new int[6];

        BufferedReader input = new BufferedReader(new FileReader(fin));
        BufferedWriter output = new BufferedWriter(new FileWriter(fout));
        
        tmpchar = input.read();
        
        // Tengo traccia del numero e dell'elenco dei caratteri non validi.
        // Uso una mappa per non dovermi curare del problema dei doppioni
        Map<String,Integer> invalidMap = new HashMap<String,Integer>();
        Map<String,Integer> validMap = new HashMap<String,Integer>();
        validMap.put("A", 0);
        validMap.put("C", 0);
        validMap.put("G", 0);
        validMap.put("T", 0);
        int unvalid = 0, valid = 0;
        
        while (tmpchar >= 0) {
        	char val = (char) tmpchar;
            
        	switch (val) {
        		case 'A': case 'a': {val = 'A';} break;
        		case 'T': case 't': {val = 'T';} break;
        		case 'G': case 'g': {val = 'G';} break;
        		case 'C': case 'c': {val = 'C';} break;
        		case '\n': case '\r': {val = '\n';} break;
        	}
        	
        	if (val=='A'||val=='G'||val=='C'||val=='T'){
        		valid++;
        		validMap.put(""+val, validMap.get(""+val)+1);
        		
				if (riga<=6 && riga>0){
					last[6-riga]=tmpchar;
				}
				if (riga==0){
					output.write('\n');
					riga=esameri*6;
					for(int i=0;i<6;i++){
						output.write((char)last[i]);
		   			}
		   		}
				output.write(val);
				riga--;	
			} else {
				
				if(val!='\n') {
					unvalid++;
					if(invalidMap.containsKey(""+val)) {
						int tmp = invalidMap.get(""+val);
						invalidMap.put(""+val, tmp+1);
					} else {
						invalidMap.put(""+val, 1);
					}
				}
			}
			tmpchar= input.read();
            
        }
        System.out.println("Found "+valid+" valid chars. Details:");
        for(Iterator<String> it =validMap.keySet().iterator(); it.hasNext();) {
        	String tmp = it.next();
        	System.out.println(" - Char '"+tmp+"': "+validMap.get(tmp));
        }
        System.out.println("Found "+unvalid+" unvalid chars. Details:");
        for(Iterator<String> it =invalidMap.keySet().iterator(); it.hasNext();) {
        	String tmp = it.next();
        	System.out.println(" - Char '"+tmp+"': "+invalidMap.get(tmp));
        }
        System.out.println("-------------------------------------------");
        input.close();
        output.close();
        // routine di controllo del numero di linee
        input = new BufferedReader(new FileReader(fout));
        int n = 0;
        int tot = 0;
        int line = 0;
        String tmp;
        while ((tmp = input.readLine()) != null) {
            n++;
            line = tmp.length() / 6;
            tot += line;
        }
        System.out.println("Total: " + n + " row");
        System.out.println("Hexamer (estimated): " + tot);
        System.out.println("Preparser complete in: "+time(time_start));
        System.out.println("-------------------------------------------");
    }

    /**
     * Lista tutti i file presenti in una directory (ricorsivamente)
     * 
     * @param folder
     *        Cartella da analizzare
     * @return List&lt;File1,File2,File3,File4&gt;
     */
    public static List<File> listFile(final File folder) {
        List<File> list = new LinkedList<File>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFile(fileEntry);
            } else {
                list.add(fileEntry);
            }
        }
        return list;
    }

    /**
     * Stampa tutti gli esameri da AAAAAA fino a TTTTTT
     */
    public static void printEsameri() {
        BufferedWriter out = null;
        try {
            // Create file
            FileWriter fstream = new FileWriter("esameri.txt");
            out = new BufferedWriter(fstream);
            int count = 0;
            for (int i1 = 0; i1 < 4; i1++) {
                for (int i2 = 0; i2 < 4; i2++) {
                    out.newLine();
                    for (int i3 = 0; i3 < 4; i3++) {
                        for (int i4 = 0; i4 < 4; i4++) {
                            for (int i5 = 0; i5 < 4; i5++) {
                                for (int i6 = 0; i6 < 4; i6++) {
                                    out.write(Tools.alfabeto[i1]);
                                    out.write(Tools.alfabeto[i2]);
                                    out.write(Tools.alfabeto[i3]);
                                    out.write(Tools.alfabeto[i4]);
                                    out.write(Tools.alfabeto[i5]);
                                    out.write(Tools.alfabeto[i6]);
                                    out.write(",");
                                    // out.newLine();
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Count: " + count);
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                // Nothing to do
            }
        }

    }
}