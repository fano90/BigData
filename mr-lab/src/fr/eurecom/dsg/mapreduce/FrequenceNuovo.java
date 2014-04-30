package fr.eurecom.dsg.mapreduce;



import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;



public class FrequenceNuovo extends Configured implements Tool {
	
	static class WCMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		private HashMap<String, Integer> partialResults;
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z]+", " ");
			StringTokenizer st = new StringTokenizer(line); //itero su ogni riga
			
			while(st.hasMoreTokens()){
			
				String word = st.nextToken().trim(); 	//prendo ciascuna parola pulita dagli spazi			
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
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			this.partialResults = new HashMap<String, Integer>();
		}

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String line = value.toString();
			line = line.replaceAll("[^a-zA-Z]+", " ");
			StringTokenizer st = new StringTokenizer(line); //itero su ogni riga
			
			while(st.hasMoreTokens()){
			
				String word = st.nextToken().trim(); 	//prendo ciascuna parola pulita dagli spazi			
			
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
	
	static class FReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int paroleTotali = context.getConfiguration().getInt("paroleTotali", 0);
			System.out.println("paroleTotali =  "+ paroleTotali);
			int sum = 0;
			for (IntWritable value : values)
				sum += value.get();
			System.out.println("frequenza: "+ sum/(double)paroleTotali);
			//this.finalResults.put(key.toString(), sum);
			context.write(key,new DoubleWritable(sum/(double)paroleTotali));
		}
	}

	
	
	
	static class OMap extends  Mapper<LongWritable, Text, DoubleWritable, Text> {
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
	    {
	        String line = value.toString();
	        StringTokenizer stringTokenizer = new StringTokenizer(line);
	        {
	            double number = 0.0; 
	            String word = "empty";

	            if(stringTokenizer.hasMoreTokens())
	            {
	                String str0= stringTokenizer.nextToken();
	                word = str0.trim();
	            }

	            if(stringTokenizer.hasMoreElements())
	            {
	                String str1 = stringTokenizer.nextToken();
	                number = Double.parseDouble(str1.trim());
	            }

	            context.write(new DoubleWritable(number), new Text(word));
	        }

	    }

	}

	static class OReduce extends Reducer<DoubleWritable, Text, DoubleWritable, Text> {
		
	    public void reduce(DoubleWritable key, Iterator<Text> values, Context context) throws IOException, InterruptedException {
	        while((values.hasNext()))
	        {
	            context.write(key, values.next());
	        }

	    }

	}
	
	
	public static class DescendingKeyComparator extends WritableComparator {
	    protected DescendingKeyComparator() {
	        super(DoubleWritable.class, true);
	    }

	    @SuppressWarnings("rawtypes")
	    @Override
	    public int compare(WritableComparable w1, WritableComparable w2) {
	        DoubleWritable key1 = (DoubleWritable) w1;
	        DoubleWritable key2 = (DoubleWritable) w2;          
	        return -1 * key1.compareTo(key2);
	    }
	}
	
	
	
	
	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] args) throws Exception {
		
		Configuration conf = this.getConf();
		
////////////primo job del wordcount
		Job job1 = new Job(conf,"Word Count");
		
		job1.setInputFormatClass(TextInputFormat.class);
		
		job1.setMapperClass(WCMapper.class);		
		job1.setMapOutputKeyClass(Text.class);
		job1.setMapOutputValueClass(IntWritable.class);
		
		job1.setReducerClass(WCReducer.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);
		
		job1.setOutputFormatClass(TextOutputFormat.class);
		
		//VALORI PRESI  DAL FILE DI LANCIO
		
		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));
		
		//STATICO
		job1.setNumReduceTasks(Integer.parseInt("1"));
		
		job1.setJarByClass(FrequenceNuovo.class);
		job1.waitForCompletion(true);
		
		//apro file appena scritto per ricavare numero parole totali
		Path path = new Path(args[1]+"/part-r-00000");
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream is = fs.open(path);
		String line = null;
		
		line= is.readLine();
		String[] words = line.split("\t");
		int numero = Integer.parseInt(words[1]);
        fs.close();
		
		conf.setInt("paroleTotali", numero);

/////////////secondo job per le frequenze
		Job job2 = new Job(conf,"Frequenze in memory combiner");
		
		job2.setInputFormatClass(TextInputFormat.class);
		
		job2.setMapperClass(FMapper.class);		
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(IntWritable.class);
		
		job2.setReducerClass(FReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(DoubleWritable.class);
		job2.setOutputFormatClass(TextOutputFormat.class);		

		FileInputFormat.addInputPath(job2, new Path(args[0]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		
		job2.setNumReduceTasks(2);
		
		job2.setJarByClass(FrequenceNuovo.class);

		job2.waitForCompletion(true);
		
		

/////////////terzo job per le frequenze
		Job job3 = new Job(conf,"Ordinmento");
		
		job3.setInputFormatClass(TextInputFormat.class);
		
		job3.setMapperClass(OMap.class);		
		job3.setMapOutputKeyClass(DoubleWritable.class);
		job3.setMapOutputValueClass(Text.class);
		
		job3.setReducerClass(OReduce.class);
		job3.setOutputKeyClass(DoubleWritable.class);
		job3.setOutputValueClass(Text.class);
		job3.setOutputFormatClass(TextOutputFormat.class);		
	
		//TODO gli input sono uguali al numero di reducer
		FileInputFormat.addInputPath(job3, new Path(args[2]+"/part-r-00000"));
		FileInputFormat.addInputPath(job3, new Path(args[2]+"/part-r-00001"));
		FileOutputFormat.setOutputPath(job3, new Path(args[3]));
		
		job3.setSortComparatorClass(DescendingKeyComparator.class);
		job3.setNumReduceTasks(1);
		
		job3.setJarByClass(FrequenceNuovo.class);
	
		job3.waitForCompletion(true);
		
		
		return 0;
	}
	
	
	public static void main(String args[]) throws Exception {
		System.out.println("prima del job-------");
		Configuration conf = new Configuration();
		ToolRunner.run(conf, new FrequenceNuovo(), args);
	}
		
		
	
}

