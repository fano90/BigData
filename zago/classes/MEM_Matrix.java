package classes;

import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Matrice MEM.
 */
public class MEM_Matrix {
    /**
     * Nome della pagina relativa alla matrice
     */
    private String name;

    /**
     * Matrice vera e propria
     */
    private Integer[][] matrix = new Integer[4096][4096];

    /**
     * Crea una nuova matrice MEM.
     * @param sem Matrice SEM associate.
     */
    public MEM_Matrix(SEM_Matrix sem) {
        this.name = sem.getName();
        this.fillZero();

        for (int i = 0; i < sem.getNum_rows(); ++i) {
            parseRow(i, sem.getRow(i));
        }
    }

    /**
     * Inizializza a 0 tutte le celle della matrice.
     */
    private void fillZero() {
        System.out.print("Matrice MEM: Inizializzazione in corso");
        for (int i = 0; i < 4096; ++i) {
            for (int j = 0; j < 4096; ++j) {
                this.matrix[i][j] = 0;
            }
        }
        System.out.print(" - DONE\n");
    }

    /**
     * d
     * @param row
     * @param sem
     */
    private void parseRow(int row, SEM_row sem) {
        for (int i = 0; i < sem.size(); ++i) {
            try {
                Integer index = Hexamer.getPosition(sem.get(i));
                ++(this.matrix[row][index]);
            } catch (InvalidHexamerException ex) {
                Logger.getLogger(MEM_Matrix.class.getName()).log(Level.SEVERE,
                    null, ex);
            }
        }
    }

    /**
     * Restituisce il nome della pagina relativa alla matrice.
     * @return Nome della pagina relativa alla matrice.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Restituisce la matrice.
     * @return Matrice.
     */
    public Integer[][] getMatrix() {
        return this.matrix;
    }

    /**
     * Restituisce una riga della matrice.
     * @param row Indice della riga da restituire.
     * @return Riga della matrice specificata.
     */
    public Integer[] getRow(int row) {
        return this.getMatrix()[row];
    }

    /**
     * Scrive una riga della matrice su un oggetto <tt>PrintWriter</tt>.
     * @param writer Oggetto <tt>PrintWriter</tt> su cui scrivere.
     * @param row Indice della riga da restituire.
     */
    public void rowToString(PrintWriter writer, int row) {
        for (int i = 0; i < 4096; i++) {
            if (this.getMatrix()[row][i] != 0) {
                writer.print(this.getMatrix()[row][i] + ";");
            } else {
                writer.print(";");
            }
        }
    }

    /**
     * Scrive la matrice su un oggetto <tt>PrintWriter</tt>.
     * @param writer Oggetto <tt>PrintWriter</tt> su cui scrivere.
     */
    public void matrixToString(PrintWriter writer) {
        for (int i = 0; i < 4096; i++) {
            rowToString(writer, i);
            writer.print("\n");
        }
    }
}