package classes;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.io.Text;

/**
 * Riga di una matrice di elongazione.
 */
public class SEM_row {
    public static final String sep = ","; // FIXME: Ale controlla
    public static final String fs = "#"; // FIXME: Ale controlla
    public static final String so = "S"; // FIXME: Ale controlla
    public static final String sc = "E"; // FIXME: Ale controlla

    /**
     * Riga della matrice di elongazione (lista di esameri).
     */
    List<Hexamer> row;
    /**
     * Nome della riga.
     */
    String name;

    /**
     * Restituisce il nome della riga.
     * @return Nome della riga.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Crea una riga della matrice di elongazione con nome e riga specificati.
     * @param name Nome della riga.
     * @param row Array di <tt>Hexamer</tt> che costituisce la riga.
     */
    public SEM_row(String name, Hexamer[] row) {
        this(name);
        this.row = Arrays.asList(row);
    }

    /**
     * Crea una riga della matrice di elongazione con il nome specificato.
     * @param name Nome della riga.
     */
    public SEM_row(String name) {
        this.name = name;
        this.row = new LinkedList<Hexamer>();
    }

    /**
     * Restituisce la lunghezza della riga.
     * @return Lunghezza della riga.
     */
    public int size() {
        return this.row.size();
    }

    /**
     * Determina se la riga è vuota.
     * @return <strong>true</strong> se la riga è vuota, <strong>false</strong>
     * altrimenti.
     */
    public boolean isEmpty() {
        return this.row.isEmpty();
    }

    /**
     * Restituisce l'esamero in <em>i</em>-esima posizione.
     * @param i Posizione dell'esamero da restituire.
     * @return Esamero in <em>i</em>-esima posizione.
     */
    public Hexamer get(int i) {
        return this.row.get(i);
    }

    /**
     * Restituisce questa riga come lista.
     * @return Riga come lista di <tt>Hexamer</tt>.
     */
    public List<Hexamer> getRow() {
        return this.row;
    }

    /**
     * Aggiunge tutti gli esameri di una riga alla riga corrente.
     * @param r Riga contenente gli esameri da aggiungere.
     */
    public void push(SEM_row r) {
        this.row.addAll(r.getRow());
    }

    /**
     * Aggiunge un esamero alla riga corrente.
     * @param e Esamero da aggiungere.
     */
    public void push(Hexamer e) {
        this.row.add(e);
    }

    /**
     * Restituisce un oggetto <tt>Text</tt> contenente la riga.
     * @return Oggetto <tt>Text</tt> contenente la riga.
     */
    public Text getText() {
        return new Text(this.toString());
    }

    /**
     * Costruisce una matrice SEM a partire da un oggetto <tt>Text</tt>.
     * @param t Oggetto <tt>Text</tt> che contiene gli esameri della matrice
     * separati da tabulazioni.
     * @throws InvalidHexamerException Lanciata se l'oggetto contiene basi non
     * valide.
     */
    public SEM_row(Text t) throws InvalidHexamerException {
        String[] full = t.toString().split("\t");
        this.name = full[0];
        this.row = new LinkedList<Hexamer>();
        for (int i = 1; i < full.length; ++i) {
            this.row.add(new Hexamer(full[i]));
        }
    }

    @Override
    /**
     * Codifica la riga secondo questo formato:
     * NOME#-S-ESAMERO<sub>1</sub>;ESAMERO<sub>2</sub>;...;ESAMERO<sub>n</sub>-E-
     */
    public String toString() {
        // Formato NOME#-S-Esam1,Esam2,Esam3,...,EsamN-E-
        String result = "";

        for (int i = 0; i < this.row.size(); ++i) {
            result += sep + this.row.get(i).toString() + "";
        }

        result = so + result + sc; // Aggiungo le parentesi esterne
        result = result.replaceFirst(so + sep, so); // Rimuovo la prima virgola
        result = this.getName() + fs + result;
        return result;
    }

    /**
     * Decodifica la stringa secondo questo formato:
     * [ESAMERO<sub>1</sub>;ESAMERO<sub>2</sub>;...;ESAMERO<sub>n</sub>]
     * 
     * @param encode Stringa da decodificare
     * @exception InvalidHexamerException Lanciata quando un esamero contiene
     * basi non valide.
     */
    public static SEM_row toSEM_row(String encode) {
        // System.out.println("\tTry to decode: "+encode);
        String name = encode.split(SEM_row.fs)[0];
        // System.out.println("\tName: "+name);
        String newencode = encode.split(SEM_row.fs)[1];

        // Rimuovo le parentesi che circondano la stringa
        newencode = newencode.replaceAll(SEM_row.so, "");
        newencode = newencode.replaceAll(SEM_row.sc, "");

        // System.out.println("\tList: "+encode);
        String[] esameri = newencode.split(SEM_row.sep);
        // System.out.print("\tInizializzo SEM_row <"+name+"> ");
        SEM_row result = new SEM_row(name);
        // System.out.print("DONE\n");

        for (int i = 0; i < esameri.length; ++i) {
            try {
                // System.out.print("\t\t["+i+"]="+esameri[i]);
                Hexamer tmp = new Hexamer(esameri[i]);
                // System.out.print(" - DONE\n");
                result.push(tmp);
                // System.out.println("\t\t\tACTUAL: "+result.toString());
            } catch (Exception ex) {
                // System.out.print(" - CATCH EXCEPTION: '"+ex.getClass()+"'\n");
                // System.err.println(ex.getCause()+" when convert esamero\n"+ex.getMessage());
                Logger.getLogger(SEM_row.class.getName()).log(Level.SEVERE,
                                                              null, ex);
            }
        }
        // System.out.println();
        // System.out.println("\tSEM_row\n\tName:\t"+result.getName()+"\n\tSize:\t"+result.size()+"\n\tString:\t"+result.toString());
        return result;
    }

    /**
     * Stampa la matrice su un oggetto <tt>PrintWriter</tt>.
     * @param writer Oggetto <tt>PrintWriter</tt> su cui scrivere.
     * @throws InvalidHexamerException Lanciata quando un esamero della matrice
     * contiene basi non valide.
     */
    void print(PrintWriter writer) throws InvalidHexamerException {
        for (int i = 0; i < this.size(); ++i) {
            writer.print(this.get(i).getCode() + ";");
        }
    }

}
