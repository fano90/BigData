package fr.eurecom.dsg.mapreduce;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

//import fr.eurecom.dsg.mapreduce.WordCount.WCMapper;
//import fr.eurecom.dsg.mapreduce.WordCount.WCReducer;
import fr.eurecom.dsg.mapreduce.WordCount;



public class FrequenceVecchio extends Configured implements Tool {
	
	static class WCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private HashMap<String, Integer> partialResults;
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}
		//CONTA IL NUMERO DI PAROLE TOTALI ATTRAVERSO LE @
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			//TODO ricordati di mettere accenti ��������!!!!!!
			line = line.replaceAll("[^a-zA-Z]+", " ");
			System.out.println(line);
			StringTokenizer st = new StringTokenizer(line);
			 
			System.out.println("---- Split by space ------");
			while(st.hasMoreTokens()){
			//	System.out.println(st.nextElement());
				String word = st.nextToken().trim();
				if (this.partialResults.containsKey("@"))
					this.partialResults.put("@", this.partialResults.get("@")+1);
				else
					this.partialResults.put("@", 1);
			}
		}
		
		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
		
			for (Entry<String, Integer> entry : this.partialResults.entrySet())
				context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
			this.partialResults.clear();
			
			super.cleanup(context);
		}
	}

	static class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable value : values)
				sum += value.get();
			context.write(key,new IntWritable(sum));
		}
	}
	
	
	
	
	static class FMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private HashMap<String, Integer> partialResults;
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}

		//CONTA IL NUMERO DI PAROLE LA LORO OCCORRENZA
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			
			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z]+", " ");
			System.out.println(line);
			StringTokenizer st = new StringTokenizer(line);
			 
			System.out.println("---- Split by space ------");
			while(st.hasMoreTokens()){
			//	System.out.println(st.nextElement());
				String word = st.nextToken().trim();			
				if (this.partialResults.containsKey(word))
					this.partialResults.put(word, this.partialResults.get(word)+1);
				else
					this.partialResults.put(word, 1);
			}
		}
		
		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
		
			for (Entry<String, Integer> entry : this.partialResults.entrySet())
				context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
			this.partialResults.clear();
			
			super.cleanup(context);
		}
	}
	
	

	static class FReducer extends Reducer<Text, IntWritable, Text, FloatWritable> {
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int paroleTotali = context.getConfiguration().getInt("paroleTotali", 0);
			System.out.println("paroleTotali = "+ paroleTotali);
			int sum = 0;
			for (IntWritable value : values)
				sum += value.get();
			//this.finalResults.put(key.toString(), sum);
			context.write(key,new FloatWritable(sum/(float)paroleTotali));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] args) throws Exception {
		
		Configuration conf = this.getConf();
		
		//primo job del wordcount
		Job job1 = new Job(conf,"Word Count");
		
		job1.setInputFormatClass(TextInputFormat.class);
		
		job1.setMapperClass(WCMapper.class);		
		job1.setMapOutputKeyClass(Text.class);
		job1.setMapOutputValueClass(IntWritable.class);
		
		job1.setReducerClass(WCReducer.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);
		
		job1.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.addInputPath(job1, new Path(args[1]));
		FileOutputFormat.setOutputPath(job1, new Path(args[2]));
		
		job1.setNumReduceTasks(Integer.parseInt(args[0]));
		
		job1.setJarByClass(WordCount.class);

		job1.waitForCompletion(true);
		
		//apro file appena scritto per ricavare numero parole totali
		Path path = new Path("OUTPUT/p1/"+"part-r-00000");
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream is = fs.open(path);
		String line = null;
		
		line= is.readLine();
		String[] words = line.split("\t");
		int numero = Integer.parseInt(words[1]);
		//System.out.println(numero);
        fs.close();
		
		conf.setInt("paroleTotali", numero);

		//secondo job per le frequenze
		Job job2 = new Job(conf,"Frequenze in memory combiner");
		
		job2.setInputFormatClass(TextInputFormat.class);
		
		job2.setMapperClass(FMapper.class);		
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(IntWritable.class);
		
		job2.setReducerClass(FReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(FloatWritable.class);
		job2.setOutputFormatClass(TextOutputFormat.class);		

		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[3]));
		
		job2.setNumReduceTasks(Integer.parseInt(args[0]));
		
		job2.setJarByClass(FrequenceVecchio.class);

		job2.waitForCompletion(true);

		
		return 0;
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println("prima del job-------");
		Configuration conf = new Configuration();
		ToolRunner.run(conf, new FrequenceVecchio(), args);
	/*	
		Path path = new Path(args[2]+"part-r-00000");
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream is = fs.open(path);
		String linea=null;
		int byteRead;
		ByteBuffer buffer=null;
		
		while((byteRead = is.read(buffer)) > 0)
			linea+=buffer.toString();
		//String linea = is.readUTF(); 
		String[] parole = linea.split("\\s+");
		System.out.println("------" + linea);
		
		int frazione=0;
		for(int i=0; i < parole.length ;i++) {
			if(parole[i].equals("@"))
				frazione = Integer.parseInt(parole[i+1]);
		}
		
		for(int i=1; i < parole.length - 1 ;i=i+2) {
			double temp = Integer.parseInt(parole[i+1])/frazione;
			System.out.println(parole[i]+"  " + temp);	
		}
		
	*/
	}
		
		
	
}

