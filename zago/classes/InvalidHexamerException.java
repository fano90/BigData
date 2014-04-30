package classes;

/**
 * Eccezione lanciata quando si tenta di creare un esamero a partire da dati
 * non validi.
 * @author Alessandro Menti
 * @author Mattia Zago <info@zagomattia.it>
 */
public class InvalidHexamerException extends Exception {

    /**
     * UID statico per la serializzazione.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Crea una nuova eccezione <tt>InvalidHexamerException</tt>.
     */
    public InvalidHexamerException() {
        super();
    }

    /**
     * Crea una nuova eccezione <tt>InvalidHexamerException</tt> con il
     * messaggio specificato.
     * @param message Messaggio da inserire.
     */
    public InvalidHexamerException(String message) {
        super(message);
    }

    /**
     * Crea una nuova eccezione <tt>InvalidHexamerException</tt> con il
     * messaggio e la causa specificati.
     * @param message Messaggio da inserire.
     * @param causa Causa da inserire.
     */
    public InvalidHexamerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Crea una nuova eccezione <tt>InvalidHexamerException</tt> con la
     * causa specificata.
     * @param causa Causa da inserire.
     */
    public InvalidHexamerException(Throwable cause) {
        super(cause);
    }
}