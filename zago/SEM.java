import classes.Hexamer;
import classes.SEMIDComparator;
import classes.SEM_Matrix;
import classes.SEM_row;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 *  1. Dividi in sottostringhe numerate (per sapere l'ordine degli
 *     spezzoni) replicando l'ultimo simbolo della sottostringa precedente
 *     come primo simbolo della successiva (processo pure questo
 *     parallelizzabile).
 *  2. Per ogni sottostringa crea una matrice con N righe e M colonne dove
 *     M è la lunghezza della sottostringa ed N il numero di simboli ammessi
 *     nel linguaggio (gli esameri).
 *  3. Per ogni sottostringa crea un array monodimensionale di dimensione N.
 *  4. Scorri la sottostringa, il primo simbolo letto non scriverlo da
 *     nessuna parte.
 *  5. Poi spostati nella riga del simbolo stesso e leggi il prossimo.
 *  6. Scrivilo nella prima cella libera della riga, incrementa di uno la
 *     cella dell'array corrispondente al simbolo e se non era l'ultimo
 *     simbolo ritorna a 5.
 *  7. Fai la somma degli array degli spezzoni precedenti e copia ogni
 *     riga della tua matrice nelle righe corrispondenti a partire dalla
 *     posizione i-esima dove i è la somma delle celle corrispondenti al
 *     simbolo della riga negli array degli spezzoni precedenti in una
 *     matrice che risulterà essere SEM.
 */

/**
 * Matrice SEM
 */
public class SEM extends Configured implements Tool {

    private int numReducers;
    private Path inputPath;
    private Path outputDir;

    @Override
    public int run(String[] args) throws Exception {

        // this.numReducers = Integer.parseInt(args[0]);
        // this.inputPath = new Path(args[1]);
        // this.outputDir = new Path(args[2]);

        Configuration conf = this.getConf();
        @SuppressWarnings("deprecation")
		Job job = new Job(conf, "SEM Mapper");

        // set job input format
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // set map class and the map output key and value classes
        job.setMapperClass(SEM_Mapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // set reduce class and the reduce output key and value classes
        job.setReducerClass(SEM_Reducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // set job output format
        job.setOutputFormatClass(TextOutputFormat.class);

        // add the input file as job input (from HDFS) to the variable inputFile
        FileInputFormat.addInputPath(job, this.inputPath);

        // set the output path for the job results (to HDFS) to the variable
        // outputPath
        FileOutputFormat.setOutputPath(job, this.outputDir);

        // set the number of reducers using variable numberReducers
        job.setNumReduceTasks(this.numReducers);

        // set the jar class
        job.setJarByClass(getClass());

        // this will execute the job
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public SEM(String[] args) {
        if (args.length != 3) {
            System.out
                    .println("Usage: SEM <num_reducers> <input_path> <output_path>");
            System.exit(0);
        }
        this.numReducers = Integer.parseInt(args[0]);
        this.inputPath = new Path(args[1]);
        this.outputDir = new Path(args[2]);
    }
    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(new Configuration(), new SEM(args), args);
        System.exit(res);
    }
}

class SEM_Mapper extends Mapper<LongWritable, Text, Text, Text> { 

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

    	String mapper_name = ""+key.toString();
    	
        // Inizializzo la stringa dei caratteri e la matrice del risultato
    	System.out.println("Mapper execute with key: "+mapper_name);
        String list = value.toString();
        SEM_Matrix matrix = new SEM_Matrix(mapper_name);
        int count_exam = 0;
        // Ciclo finchè ho ancora caratteri per estrarre almeno un esamero
        while (list.length() > 5) {
            // System.out.println("List Size: "+list.length());
            try {
                // System.out.print("First: ");
                // System.out.print(list.substring(0, 6));
                // System.out.println();
                String tmp = list.substring(0, 6);
                Hexamer first = new Hexamer(tmp); // Estrazione 012345
                list = list.replaceFirst(tmp, ""); // Rimuovo dalla stringa i
                                                   // primi 6 caratteri
                //System.out.print("Esamero: " + first.toString());
                // System.out.println("New list Size: "+list.length());
                /*
                 * Se la lista, una volta estratto il primo elemento non
                 * contiene abbastanza caratteri per un secondo esamero mi segno
                 * fin dove sono arrivato ed il residuo della stringa.
                 */
                if (list.length() < 6) {
                    //System.out.print("\nSet UNMATCH ("+first+")");
                    matrix.setLast_unmatched(first);
                    //System.out.print(" - DONE");
                    //System.out.print("\nSet RESIDUO ("+list+")");
                    matrix.setResiduo(list);
                    //System.out.print(" - DONE\n");
                    break;
                }

                // Estraggo il secondo esamero
                Hexamer succ = new Hexamer(list.substring(0, 6)); // Estrazione
                                                                  // 012345
                //System.out.print(" - Second: " + succ.toString());
                // Inserisco nella matrice l'esamero
                if (matrix.push(first, succ)) {
                    count_exam++;
                    //System.out.print(" - DONE\t" + count_exam + "\n");
                } else {
                    //System.out.print(" - ERROR\n");
                }

            } catch (Exception ex) { // Se arrivo qui significa che ho una
                                     // lettera che non è ATCG
                // System.out.println("Cavallo - "+ex);
                Logger.getLogger(SEM_Mapper.class.getName()).log(Level.SEVERE,
                                                                 null, ex);
            }
        }
        System.out
                .println("\n======================================================================\n"
                    + "Matrice completata\n"
                    + "======================================================================\n");
        int count_emit = 0;
        for (int i = 0; i < matrix.getNum_rows(); i++) {
            try {
                //System.out.print("Look <" + i);
                Hexamer tmp = Hexamer.getHexamer(i);
                //System.out.print("-" + tmp.toString() + ">: \t");
                SEM_row row = matrix.getRow(i);
                if (row.isEmpty()) {
                    System.out.print("Row is empty -> SKIP context.write\n");
                } else {
                    count_emit++;
                    //System.out.print(row.getName() + " \t");
                    //System.out.print("context.write(" + tmp.getText() + ", "
                    //    + row.toString() + ")");
                    context.write(tmp.getText(), row.getText());
                    //System.out.print(" - DONE\n");
                }

            } catch (Exception ex) {
                System.out.println(" - CATCH EXCEPTION: '" + ex.getCause()
                    + "'\n");
                System.err.println(ex.getCause() + " when do context.write()\n"
                    + ex.getMessage());
                Logger.getLogger(SEM_Mapper.class.getName()).log(Level.SEVERE,
                                                                 null, ex);
            }
        } // end for
        if (count_exam > 0)
            System.out
                    .println("\n======================================================================\n"
                        + "Esaminati "
                        + count_exam
                        + " esameri. Emessi "
                        + count_emit
                        + " risultati\n"
                        + "======================================================================\n");
        else
            System.out
                    .println("\n======================================================================\n"
                        + "Esaminati "
                        + count_exam
                        + " esameri. Emessi "
                        + count_emit
                        + " risultati\n"
                        + "======================================================================\n");

    }
}

class SEM_Reducer extends Reducer<Text, // Chiave: Esamero
Text, // Matrice relativa alla porzione analizzata dal mapper
Text, // Chiave univoca del cromosoma (?)
Text> { // Matrice finale (?)

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        System.out.println("Reducer KEY<" + key + ">");
        try {
            Hexamer e = new Hexamer(key);

            String row_name = "FINAL-" + Hexamer.getPosition(e) + "-" + key;

            SEM_row result = new SEM_row(row_name);
            
            // Ordino i valori
            List<Text> list = new LinkedList<Text>();
            
            for (Iterator<Text> it = values.iterator(); it.hasNext();) {
            	String tmp = it.next().toString();
            	list.add(new Text(tmp));
            	//System.out.println("\t"+tmp);
            }
            
            //for(int i=0; i<list.size(); i++) System.out.println("\tList["+i+"]: "+list.get(i));
            Collections.sort(list, new SEMIDComparator());
            //System.out.println("\tRiordino");
            //for(int i=0; i<list.size(); i++) System.out.println("\tList["+i+"]: "+list.get(i));
            
            
            // La lista è ordinata, quindi posso fare i push senza preoccuparmi della sequenzialità
            //System.out.println("-----------------------------------");
            for (int i = 0; i<list.size(); i++) {
                try {
                    Text tmp = list.get(i);
                    //System.out.print("IT<"+tmp+">: ");
                    SEM_row row = SEM_row.toSEM_row(tmp.toString());
                    //System.out.print(row.getName()+" - PUSH: ");
                    result.push(row);
                    //System.out.print("DONE\n");
                } catch (Exception ex) {
                    //System.out.print("ERROR\n");
                    System.out.println(ex);
                    Logger.getLogger(SEM_Reducer.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("-----------------------------------");

            //System.out.println("\tSEM_row: " + result);
            context.write(key, result.getText());
            //System.out.println();
        } catch (Exception ex) {
            // System.out.print(" - ERROR\n");
            System.out.println(ex);
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
