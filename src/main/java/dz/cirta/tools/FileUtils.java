package dz.cirta.tools;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class FileUtils {


   public static String ENCODE_FILE_CONTENT(String resourceUrl) throws IOException {
      URI uri = LOAD_FILE_FROM_CLASSPATH(resourceUrl).getURI();
      byte[] fileContent = Files.readAllBytes(Paths.get(uri));
      return Base64.getEncoder().encodeToString(fileContent);
   }

   @Deprecated(since = "2.0", forRemoval = true)
   public static Resource LOAD_FILE(String resourceUrl) {
      return new FileSystemResource(resourceUrl);
   }

   @Deprecated(since = "2.0", forRemoval = true)
   public static Resource LOAD_FILE_FROM_CLASSPATH(String resourceUrl) {
      return new ClassPathResource(resourceUrl);
   }

   public static InputStream INPUT_STREAM_FROM_CLASSPATH(String classPathResourceUrl) throws IOException {
      return LOAD_FILE_FROM_CLASSPATH(classPathResourceUrl).getInputStream();
   }

}
