package classes;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <strong>S</strong>tring <strong>E</strong>longation <strong>M</strong>atrix
 * 
 * @author Mattia Zago <info@zagomattia.it>
 */
public class SEM_Matrix {

    /**
     * Separatore dei campi della matrice.
     */
    public static final String sep_campi = ";";
    /**
     * Separatore delle righe della matrice.
     */
    public static final String sep_righe = "|";

    /**
     * Nome della pagina relativa alla matrice
     */
    private String name;
    /**
     * Ultimo esamero non accoppiato
     * <em>Nota: Non so se effettivamente ci serva oppure no</em>
     */
    private Hexamer last_unmatched = null;
    /**
     * Basi residue alla fine della pagina
     * <em>Nota: Non so se effettivamente ci serva oppure no</em>
     */
    private char[] residuo = new char[5];
    /**
     * Matrice vera e propria
     */
    private List<SEM_row> matrix;

    /**
     * Costruttore. Non inizializza la matrice (aggiunta dati dinamica)
     * 
     * @param name
     *        Nome della pagina di riferimento
     * @throws InvalidHexamerException
     */
    public SEM_Matrix(String name) {
        this.name = name;
        try {
            initFull();
            System.out.println("Matrice Inizializzata");
        } catch (Exception e) {
            System.err.println("Matrice non inizializzata " + e.getCause()
                + "\n\t" + e.getMessage());
        }
    }

    /**
     * Inizializza la matrice aggiungendo per ogni esamero il valore null
     * 
     * @throws InvalidHexamerException
     *         Problema nell'inizializzazionedi Esamero
     */
    private void initFull() throws InvalidHexamerException {
        this.matrix = new LinkedList<SEM_row>();
        System.out.println("Inizializzazione Matrice");

        for (int i = 0; i < 4096; ++i) {
            String row = this.name + "-" + i + "-" + Hexamer.getHexamer(i);
            // System.out.print("|<"+i+","+row+">");
            this.matrix.add(new SEM_row(row));
        }
        System.out.println("Dimensione Matrice: " + this.matrix.size());
    }

    /**
     * Aggiunge un elemento alla riga indicata
     * 
     * @param row
     *        Riga identificata dall'esamero in questione
     * @param e
     *        Esamero da aggiungere
     * @throws InvalidHexamerException
     *         Problema nell'inizializzazionedi Esamero
     */
    public boolean push(Hexamer row, Hexamer e) throws InvalidHexamerException {
        // System.out.print(" -M- Put ("+row.toString()+","+e.toString()+")");
        SEM_row extract = this.getRow(row); // Estrai dalla mappa la riga
                                            // indicata
        if (extract == null) {
            System.out.print(" ... ERROR\n");
            return false;
        }
        extract.push(e);
        // System.out.print("|PUSHROW|");
        this.matrix.set(Hexamer.getPosition(row), extract);
        // System.out.print("UPDATEMATRIX|");
        // System.out.print(" - DONE\n");
        return true;
    }

    public boolean push(Hexamer row, SEM_row e) throws InvalidHexamerException {
        // System.out.print(" -M- PutROW ("+row.toString()+","+e.name+")");
        SEM_row extract = this.getRow(row); // Estrai dalla mappa la riga
                                            // indicata
        if (extract == null) {
            // System.out.print(" ... ERROR\n");
            return false;
        }
        extract.push(e);
        // System.out.print("|PUSHROW|");
        this.matrix.set(Hexamer.getPosition(row), extract);
        // System.out.print("UPDATEMATRIX|");
        // System.out.print(" - DONE\n");
        return true;
    }

    /**
     * Restituisce la riga corrispondente all'esamero scelto
     * 
     * @param row
     *        Tradotto con Esamero.getEsamero(row)
     * @return SEM_row
     * @throws InvalidHexamerException
     *         Problema nell'inizializzazionedi Esamero
     */
    public SEM_row getRow(int row) {
        // System.out.print("\n\tTry to get row from row: "+row);
        SEM_row extract = null;
        try {
            extract = this.matrix.get(row);
            // System.out.print(" - Found!! Return SEM_row("+extract.name+")\n");
        } catch (Exception e) {
            // System.out.print(" - NOT Found!! ("+e.getMessage()+") Return null\n");
        }
        return extract;
    }

    /**
     * Restituisce la riga corrispondente all'esamero scelto
     * 
     * @param esamero
     *        es. AATCAG
     * @return SEM_row
     * @throws InvalidHexamerException
     *         Problema nell'inizializzazionedi Esamero
     */
    public SEM_row getRow(Hexamer esamero) throws InvalidHexamerException {
        // System.out.println("\n\tTry to get row from esamero: "+esamero.toString());
        return this.getRow(Hexamer.getPosition(esamero));
    }

    /**
     * Numero di elementi della matrice
     * 
     * @return Numero di elementi inizializzati della matrice
     */
    public int getNum_rows() {
        return this.matrix.size();
    }

    /**
     * Nome della pagina di riferimento
     * 
     * @return Nome della pagina di riferimento
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setta il nome della pagina di riferimento
     * 
     * @param name
     *        Nome della pagina di riferimento
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Restituisce l'ultimo esamero non accoppiato
     * 
     * @return Esamero
     */
    public Hexamer getLast_unmatched() {
        return this.last_unmatched;
    }

    /**
     * Setta l'ultimo esamero non accoppiato
     * 
     * @param last_unmatched
     *        Esamero
     */
    public void setLast_unmatched(Hexamer last_unmatched) {
        this.last_unmatched = last_unmatched;
    }

    /**
     * Restituisce gli ultimi caratteri della pagina che non formano un esamero
     * 
     * @return da 0 a 5 caratteri
     */
    public char[] getResiduo() {
        return this.residuo;
    }

    /**
     * Restituisce gli ultimi caratteri della pagina che non formano un esamero
     * 
     * @return da 0 a 5 caratteri
     */
    public String getResiduoAsString() {
        return Arrays.toString(this.getResiduo());
    }

    /**
     * Setta il residuo della pagina
     * 
     * @param residuo
     *        da 1 a 5 caratteri
     */
    public void setResiduo(char[] residuo) {
        this.residuo = residuo;
    }

    /**
     * Setta il residuo della pagina
     * 
     * @param residuo
     *        da 1 a 5 caratteri
     */
    public void setResiduo(String residuo) {
        this.residuo = residuo.toCharArray();
    }

    /**
     * Restituisce la matrice nel formato Mappa
     * 
     * @return Mappa&lt;Esamero,SEM_row&gt;
     */
    public List<SEM_row> getMatrix() {
        return this.matrix;
    }

    @Override
    /**
     * Formato espanso.
     */
    public String toString() {
        try {
            return SEM_Matrix.toString(this);
        } catch (InvalidHexamerException e) {
            e.printStackTrace();
            return "InvalidHexamerException found. See Stack Trace please";
        }
    }

    /**
     * Codifica la matrice in una stringa secondo questo formato:<br>
     * NOME;ESAMERO<sub>1</sub>-[CODIFICA_SEMrow<sub>1</sub>]|...|ESAMERO<sub>
     * 4095</sub>-[CODIFICA_SEMrow<sub>4095</sub>];LASTUNMATCH;RESIDUO
     * 
     * @param m
     *        Matrice da codificare
     * @return Stringa con la matrice codificata
     * @throws InvalidHexamerException
     */
    public static String toString(SEM_Matrix m) throws InvalidHexamerException {
        String result = "";

        // Codifica della matrice:
        // NOME;ESAMERO1-[RIGA1]|...|ESAMERO4095-[RIGA4095];LASTUNMATCH;RESIDUO

        result += m.getName();
        result += sep_campi;
        for (int it = 0; it < 4096; ++it) {
            // for(Iterator<Esamero> it = m.matrix.keySet().iterator();
            // it.hasNext(); ) {
            Hexamer tmp = Hexamer.getHexamer(it);
            result += tmp.toString();
            result += "-";
            result += m.getRow(it);

            result += sep_righe;
        }

        /*
         * Codifica della matrice SEM_Matrix per la scrittura su file
Ale dovresti controllare dove ho fatto result.replaceAll. L'idea è quella di 
fare un ltrim in SEM_row e rtrim in SEM_Matrix, ma non sono sicuro che sia 
corretto.
            Ossia: sono sicuro che i separatori siano piazzati correttamente?
         */
        result += "#";
        result = result.replaceAll(sep_righe + "#", ""); // Rimuovo l'ultimo
                                                         // separatore di riga

        result += sep_campi;
        result += (m.getLast_unmatched() != null) ? m.getLast_unmatched()
                .toString() : "-";
        result += sep_campi;
        result += m.getResiduoAsString();

        return result;
    }

    /**
     * Decodifica la matrice a partire da una stringa con questo formato:<br>
     * NOME;ESAMERO<sub>1</sub>-[CODIFICA_SEMrow<sub>1</sub>]|...|ESAMERO<sub>
     * 4095</sub>-[CODIFICA_SEMrow<sub>4095</sub>];LASTUNMATCH;RESIDUO
     * 
     * @param encoded
     *        Stringa codificata della matrice
     * @return SEM_Matrix
     * @throws InvalidHexamerException
     *         Errore nella conversione di un esamero
     */
    public static SEM_Matrix toMatrix(String encoded) throws InvalidHexamerException {
        // Codifica della matrice:
        // NOME;ESAMERO1-[RIGA1]|...|ESAMERO4095-[RIGA4095];LASTUNMATCH;RESIDUO

        String[] pieces = encoded.split(sep_campi);

        SEM_Matrix result = new SEM_Matrix(pieces[0]);

        String[] matrix = pieces[1].split(sep_righe);
        for (int i = 0; i < matrix.length; ++i) {
            // Ogni elemento di matrix rappresenta una riga della matrice
            String[] riga = matrix[i].split("-");
            result.push(new Hexamer(riga[0]), SEM_row.toSEM_row(riga[1]));
        }

        result.setLast_unmatched(new Hexamer(pieces[2]));
        result.setResiduo(pieces[3]);
        return result;
    }

    /**
     * Stampa la matrice su un oggetto <tt>PrintWriter</tt>.
     * @param writer Oggetto <tt>PrintWriter</tt> su cui scrivere.
     * @throws InvalidHexamerException Lanciata se la matrice contiene dati non
     * validi.
     */
    public void print(PrintWriter writer) throws InvalidHexamerException {
        for (int i = 0; i < 4096; ++i) {
            this.getRow(i).print(writer);
            writer.print("\n");
        }
    }
}