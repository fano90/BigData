/*Prende in input il primo esamero, un array 1x4096 (indice) inizializzato a 0 ed un array 4096x4096 contenente SEM
i=0
begin
Legge della riga dell'i-esimo esamero  la colonna numero segnato dall'indice nella cella i-esima, 
setta i al numero contenuto nella colonna, 
incrementa la cella dell'esamero nell'indice,
scrive in output il numero contenuto nella colonna,
do finchè non leggi un numero pari a 0.
*/
import java.io.*;
import java.util.*;
import java.lang.*;
//args: 
//1: first hexamer of the original FASTA file
public class FastaRebuilder{
   public static String translate(int code){
	String out="";
	int code0=code/1024;
	code=code-1024*code0;
	int code1=code/256;
	code=code-256*code0;
	int code2=code/1024;
	code=code-64*code0;
	int code3=code/1024;
	code=code-16*code0;
	int code4=code/1024;
	code=code-4*code0;
	int code5=code/1024;
	
	if (code0==0) out=out+"A";
	if (code0==1) out=out+"C";
	if (code0==1) out=out+"G";
	if (code0==3) out=out+"T";
	
	if (code1==0) out=out+"A";
	if (code1==1) out=out+"C";
	if (code1==2) out=out+"G";
	if (code1==3) out=out+"T";
	
	if (code2==0) out=out+"A";
	if (code2==1) out=out+"C";
	if (code2==2) out=out+"G";
	if (code2==3) out=out+"T";
	
	if (code3==0) out=out+"A";
	if (code3==1) out=out+"C";
	if (code3==2) out=out+"G";
	if (code3==3) out=out+"T";
	
	if (code4==0) out=out+"A";
	if (code4==1) out=out+"C";
	if (code4==2) out=out+"G";
	if (code4==3) out=out+"T";
	
	if (code5==0) out=out+"A";
	if (code5==1) out=out+"C";
	if (code5==2) out=out+"G";
	if (code5==3) out=out+"T";
	return out;
	}
   public static void main(String[] args){
try{
	
	int n=0;
	BufferedReader input = new BufferedReader(new FileReader("sem.data"));
        BufferedWriter outputFile= new BufferedWriter(new FileWriter("sem.fasta"));
	int inputchar=0;
	int counter=0;
	int number=0;
	int length=0;
	
	ArrayList<ArrayList<Integer>> sem= new ArrayList<ArrayList<Integer>>();
	while (number<4095){
		number++;
		sem.add(new ArrayList<Integer>());
	}
	number=0;

	int tmpchar=0;
	while (tmpchar>=0){
		while((char)tmpchar!='\n'&&tmpchar>=0){
			while(((char)tmpchar)!=';'&&tmpchar>=0&&(char)tmpchar!='\n'){
				counter++;
				n=n+tmpchar*((int)Math.pow(10.0,(double)counter));
				tmpchar=input.read();
			}
			sem.get(number).add(n);
			n=0;
			counter=0;
			tmpchar=input.read();
			length++;
		}
		tmpchar=input.read();
		number++;
	}
	n=0;
	counter=0;
	number=0;
	int[] index= new int[4096];
	int row= Integer.parseInt(args[0]);
System.out.println("outside");
	while(sem.get(row).get(index[row])!=null){
System.out.println("inside");
		outputFile.write(translate(sem.get(row).get(index[row])));
		index[row]++;
		row=sem.get(row).get(index[row]);
System.out.println(n);
		n++;
	}
	
System.out.println("end");

	outputFile.close();
	input.close();
}
catch(Exception e){e.getMessage();}
   }
}