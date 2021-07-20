package dz.cirta.store.tools.exceptions;

/**
 * exception to be thrown when there is no subject summary could be extracted using {@link org.apache.pdfbox} api.
 * by design, this exception is un unchecked exception, since it is used at most cases to propagate outside the method.
 * @author Abdessamed Diab
 * @version 2.0
 * @since 1.0
 */
public class PdfSummaryNotFoundException extends RuntimeException {

   public PdfSummaryNotFoundException(String message) {
      super(message);
   }
}
