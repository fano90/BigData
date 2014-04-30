import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class FASTAPreparser{
   public static void main(String[] args){
	int tmpchar;
	int esameri=Integer.valueOf(args[0]);
	System.out.println(esameri);
	int riga=esameri*6;
	int[] last= new int[6];
	try{
		BufferedReader input = new BufferedReader(new FileReader("input.fasta"));
        	BufferedWriter output= new BufferedWriter(new FileWriter("output.fasta"));

		tmpchar= input.read();

		while(tmpchar>=0){
			if (((char)tmpchar)=='>'){
				input.readLine();
			}
			else {
				if (((char)tmpchar)=='A'||((char)tmpchar)=='G'||((char)tmpchar)=='C'||((char)tmpchar)=='T'){
					if (riga<=6&&riga>0){
						last[6-riga]=tmpchar;
					}
					if (riga==0){
						output.write('\n');
						riga=esameri*6;
						for(int i=0;i<6;i++){
							output.write((char)last[i]);
		   				}
		   			}
					output.write(tmpchar);
					riga--;	
				}
			}
			tmpchar= input.read();
		}
		input.close();
		output.close();


	} catch(IOException e){
	//IOException is the only exception which could be raised if the input is correct, others are caused by an out of bounds input.
	System.out.println(e.getMessage());
	} catch(ArrayIndexOutOfBoundsException e){
	System.out.println(e.getMessage());
	} catch(NumberFormatException e){
	System.out.println(e.getMessage());
	}
   }
}