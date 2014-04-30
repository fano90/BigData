package classes;

import org.apache.hadoop.io.Text;

/**
 * Classe che rappresenta un singolo esamero.
 */
public class Hexamer {

    /**
     * Valore grezzo dell'esamero.
     */
    private char[] value = new char[6];

    /**
     * Crea un nuovo esamero a partire da un oggetto <tt>Text</tt>.
     * @param t Oggetto <tt>Text</tt> che specifica l'esamero.
     * @throws InvalidHexamerException Eccezione lanciata se l'oggetto <tt>t</tt>
     * non è una stringa di sei basi.
     */
    public Hexamer(Text t) throws InvalidHexamerException {
        this.value = convert(t);
    }

    /**
     * Crea un nuovo esamero a partire da una stringa.
     * @param t Stringa che specifica l'esamero.
     * @throws InvalidHexamerException Eccezione lanciata se l'oggetto <tt>t</tt>
     * non è una stringa di sei basi.
     */
    public Hexamer(String s) throws InvalidHexamerException {
        this.value = convert(s);
    }

    /**
     * Restituisce l'esamero espresso dall'oggetto come array di basi.
     * @return Array delle sei basi dell'esamero.
     */
    public char[] getEsamero() {
        return this.value;
    }

    /**
     * Restituisce l'esamero espresso dall'oggetto come <tt>Text</tt>.
     * @return Oggetto <tt>Text</tt> contenente le sei basi dell'esamero.
     */
    public Text getText() {
        return new Text(this.toString());
    }

    @Override
    /**
     * Restituisce l'esamero espresso dall'oggetto come stringa.
     * @return Oggetto <tt>String</tt> contenente le sei basi dell'esamero.
     */
    public String toString() {
        return new String(this.value);
    }

    /**
     * Restituisce un esamero a partire dalla sua codifica numerica posizionale.
     * @param position Codifica numerica posizionale dell'esamero.
     * @return Oggetto <tt>Hexamer</tt> contenente l'esamero.
     * @throws InvalidHexamerException 
     */
    public static Hexamer getHexamer(int position) throws InvalidHexamerException {
        int[] offset = {1024, 256, 64, 16, 4, 1};
        String result = "";
        int value = position;
        for (int i = 0; i < 6; ++i) {
            // Divisione intera: mi restituisce la lettera corrispondente
            switch (value / offset[i]) {
                case 0:
                    result += 'A';
                    break;
                case 1:
                    result += 'C';
                    break;
                case 2:
                    result += 'G';
                    break;
                case 3:
                    result += 'T';
                    break;
                default:
                    throw new InvalidHexamerException("Invalid positional"
                        + " encoding (offset is " + (value / offset[i])
                        + ", should be 0, 1, 2 or 3)");
            }
            // Modulo: mi restituisce il valore successivo da analizzare
            value = value % offset[i];
        }

        return new Hexamer(result);
    }

    /**
     * Supponendo un ordine alfabetico {A,C,G,T}, restituisce la codifica
     * posizionale da 0 a 4095 dell'esamero passato come argomento.
     * @param esamero Esamero da passare in forma testuale (esempio:
     * <pre>A,A,A,A,A,A</pre>).
     * @return Posizione {0,1,...,4095}
     */
    public static int getPosition(char[] esamero) throws InvalidHexamerException {
        int[] val = new int[6];
        for (int i = 0; i < 6; i++) {
            switch (esamero[i]) {
                case 'A':
                case 'a':
                    val[i] = 0;
                    break;
                case 'C':
                case 'c':
                    val[i] = 1;
                    break;
                case 'G':
                case 'g':
                    val[i] = 2;
                    break;
                case 'T':
                case 't':
                    val[i] = 3;
                    break;
                default:
                    throw new InvalidHexamerException("Invalid character"
                        + " encountered in the encoding (" + esamero[i] + ")");
            }
        }
        return 0 + +val[0]
            * 1024 // Partizionamento di 4096 su un alfabeto di 4 caratteri
            + val[1] * 256 + val[2] * 64 + val[3] * 16 + val[4] * 4 + val[5]
            * 1;
    }

    /**
     * Supponendo un ordine alfabetico {A,C,G,T}, restituisce la codifica
     * posizionale da 0 a 4095 dell'esamero passato come argomento.
     * @param esamero Oggetto <tt>Hexamer</tt> contenente l'esamero.
     * @return Posizione {0,1,...,4095}
     */
    public static int getPosition(Hexamer esamero) throws InvalidHexamerException {
        return Hexamer.getPosition(esamero.getEsamero());
    }

    /**
     * Converte un esamero in una sequenza di basi.
     * @param esamero Oggetto <tt>Text</tt> contenente l'esamero da convertire.
     * @return Array contenente la sequenza di basi.
     * @throws InvalidHexamerException Lanciata se l'oggetto contiene basi non
     * valide.
     */
    public static char[] convert(Text esamero) throws InvalidHexamerException {
        return convert(esamero.toString());
    }

    /**
     * Converte un esamero in una sequenza di basi.
     * @param esamero Oggetto <tt>CharSequence</tt> contenente l'esamero da
     * convertire.
     * @return Array contenente la sequenza di basi.
     * @throws InvalidHexamerException Lanciata se l'oggetto contiene basi non
     * valide.
     */
    public static char[] convert(CharSequence esamero) throws InvalidHexamerException {
        return convert(esamero.toString());
    }

    /**
     * Converte un esamero in una sequenza di basi.
     * @param esamero Oggetto <tt>String</tt> contenente l'esamero da
     * convertire.
     * @return Array contenente la sequenza di basi.
     * @throws InvalidHexamerException Lanciata se l'oggetto contiene basi non
     * valide.
     */
    public static char[] convert(String esamero) throws InvalidHexamerException {
        // A: 65
        // C: 67
        // T: 84
        // G: 71
        char[] result = new char[6];
        for (int i = 0; i < 6; ++i) {
            switch (esamero.charAt(i)) {
                case 'A':
                case 'a':
                    result[i] = 'A';
                    break;
                case 'C':
                case 'c':
                    result[i] = 'C';
                    break;
                case 'G':
                case 'g':
                    result[i] = 'G';
                    break;
                case 'T':
                case 't':
                    result[i] = 'T';
                    break;
                default:
                    System.err.println("Carattere non previsto ("
                        + esamero.charAt(i) + ")");
                    throw new InvalidHexamerException("Invalid character"
                        + " encountered (" + esamero.charAt(i) + ")");
            }
        }
        return result;
    }

    /**
     * Restituisce la codifica posizionale corrispondente a quest'esamero.  
     * @return Codifica posizionale di questo esamero.
     * @throws InvalidHexamerException Lanciata se l'esamero contiene basi non
     * valide.
     */
    int getCode() throws InvalidHexamerException {
        return getPosition(this);
    }
}
