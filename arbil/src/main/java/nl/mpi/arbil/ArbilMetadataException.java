package nl.mpi.arbil;

/**
 * Document   : ArbilMetadataException
 * Created on : Oct 8, 2010, 12:20:35 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMetadataException extends Exception {

    public ArbilMetadataException(String messageString) {
	super(messageString);
    }

    public ArbilMetadataException(String messageString, Throwable cause) {
	super(messageString, cause);
    }
}