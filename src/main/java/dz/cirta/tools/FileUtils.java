package dz.cirta.tools;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class FileUtils {


   public static String ENCODE_FILE_CONTENT(String resourceUrl) throws IOException {
      URI uri = LOAD_FILE(resourceUrl).getURI();
      byte[] fileContent = Files.readAllBytes(Paths.get(uri));
      return Base64.getEncoder().encodeToString(fileContent);
   }

   public static Resource LOAD_FILE(String resourceUrl) {
      return new FileSystemResource(resourceUrl);
   }

}