package dz.cirta.store.tools;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * utility class for handling I/O operations.
 * <br> since version 2.0, we are no longer loading resources from filesystem, instead we are using only classpath.<br>
 * this design was suggested as a best practice in our case not only for software development but also for distribution and accessibility.
 * @author Abdessamed Diab
 * @since 1.0
 * @version 2.0
 */
public class FileUtils {

   /**
    * convert a resource content to base64 format
    * @param resourceUrl fully qualified resource url
    * @return a text representing a resource fully encoded in base64.
    * @throws IOException if no classpath file available with given supplied argument.
    */
   public static String ENCODE_FILE_CONTENT(String resourceUrl) throws IOException {
      byte[] fileContent = INPUT_STREAM_FROM_CLASSPATH(resourceUrl).readAllBytes();
      return Base64.getEncoder().encodeToString(fileContent);
   }

   /**
    * return a resource I/O located in hard drive.
    * <br> we should avoid using this method, since all IO resources were stored in classpath, filesystem should be avoided from mow on.
    * @param resourceUrl fully qualified resource url
    * @return {@link Resource} wrap up resource that wa found based on url
    */
   @Deprecated(since = "2.0", forRemoval = true)
   public static Resource LOAD_FILE(String resourceUrl) {
      return new FileSystemResource(resourceUrl);
   }

   @Deprecated(since = "2.0", forRemoval = true)
   public static Resource LOAD_FILE_FROM_CLASSPATH(String resourceUrl) {
      return new ClassPathResource(resourceUrl);
   }

   /**
    * load a resource from a classpath and return it's content as a stream of bytes.
    * <br> consider getting resources from the classpath instead of filesystem for many reasons such as: all dependencies needs to be bundled within generated artifact.
    * @param classPathResourceUrl relative url to classpath, resources directory represent default classpath located under src subFolder
    * @return {@link InputStream} stream of bytes
    */
   public static InputStream INPUT_STREAM_FROM_CLASSPATH(String classPathResourceUrl) throws IOException {
      return LOAD_FILE_FROM_CLASSPATH(classPathResourceUrl).getInputStream();
   }

}
