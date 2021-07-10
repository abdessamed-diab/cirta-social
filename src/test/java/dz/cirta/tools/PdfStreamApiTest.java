package dz.cirta.tools;

import dz.cirta.data.models.Book;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfStreamApiTest {
   private static final String resourceSubjectName = "1.jpg";

   @Test
   public void testEncodeFileContent() throws IOException {
      String resourceUrl = "volume" + File.separator + "subjects" + File.separator + resourceSubjectName;

      String result = FileUtils.ENCODE_FILE_CONTENT(resourceUrl);
      assertNotNull(result);
   }

   @Test
   public void testEncodeFileContentFileNotFoundException() {
      String resourceUrl = "volume" + File.separator + "subjects" + File.separator + "what.jpg";

      assertThrows(IOException.class, () -> FileUtils.ENCODE_FILE_CONTENT(resourceUrl));
   }

   @Test
   public void testSplitPdfFile() throws IOException {
      String path = "volume" + File.separator + "books" + File.separator + "spring-social-reference.pdf";
      boolean isExtracted = PdfStreamApi.splitPdfFile(1, 5, path);
      assertTrue(isExtracted);

      isExtracted = PdfStreamApi.splitPdfFile(5, 23, path);
      assertTrue(isExtracted);
   }

   @Test
   public void testSplitPdfFileDoesNotExist() {
      String path = "volume" + File.separator + "books" + File.separator + "13.pdf";
      assertThrows(FileNotFoundException.class,
            () -> PdfStreamApi.splitPdfFile(1, 5, path));
   }

   @Test
   public void testSplitPdfFileStartPageDoesNotExists() {
      String path = "volume" + File.separator + "books" + File.separator + "spring-social-reference.pdf";
      assertThrows(IllegalArgumentException.class, () -> {
         PdfStreamApi.splitPdfFile(215, 220, path);
      });
   }

   @Test
   public void testSplitPdfFileToOutputStream() throws IOException {
      String path = "volume" + File.separator + "books" + File.separator + "spring-social-reference.pdf";
      OutputStream outputStream = new BufferedOutputStream(new ByteArrayOutputStream());
      PdfStreamApi.splitPdfToOutputStream(35, 38, path, outputStream);
      assertNotNull(outputStream);
      outputStream.close();
   }

   @Test
   public void testSplitPdfFileToOutputStreamPdfFileDoesNotExist() {
      String path = "volume" + File.separator + "books" + File.separator + "The.of.System.and.Network.Administration.2nd.Edition.pdf";
      String targetFileUrl = "volume" + File.separator + "books" + File.separator + "splitPdfResult.pdf";

      try (
            OutputStream os = new FileOutputStream(
                  FileUtils.LOAD_FILE_FROM_CLASSPATH(targetFileUrl).getFile()
            )
      ) {
         assertThrows(FileNotFoundException.class, () -> PdfStreamApi.splitPdfToOutputStream(27, 29, path, os));
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   @Test
   public void testInitializeBookProperties() throws IOException {
      String pdfDocumentDirectory = "volume" + File.separator + "books" + File.separator;
      String coverPhotoFileNameMappingSourceFileUrl = "volume" + File.separator + "books" + File.separator + "cover_photo_file_name_mapping.txt";
      List<Book> books = PdfStreamApi.initializeBookProperties(pdfDocumentDirectory, coverPhotoFileNameMappingSourceFileUrl);
      assertTrue(!books.isEmpty());
   }

   @Test
   public void testInitializeBookPropertiesNoSuchDirectory() throws IOException {
      String pdfDocumentDirectory = "volume"+File.separator+"bookss"+File.separator;
      String coverPhotoFileNameMappingSourceFileUrl = "volume"+File.separator+"books"+File.separator+"cover_photo_file_name_mapping.txt";
      List<Book> books = PdfStreamApi.initializeBookProperties(pdfDocumentDirectory, coverPhotoFileNameMappingSourceFileUrl);

      assertTrue(books.isEmpty());
   }

   @Test
   public void testInitializeBookPropertiesMappingSourceDoesNotExist() throws IOException {
      String pdfDocumentDirectory = "volume"+File.separator+"books"+File.separator;
      String coverPhotoFileNameMappingSourceFileUrl = "volume"+File.separator+"books"+File.separator+"cover_photo_file_name_mappingg.txt";

      assertThrows(
            FileNotFoundException.class,
            () -> PdfStreamApi.initializeBookProperties(pdfDocumentDirectory, coverPhotoFileNameMappingSourceFileUrl)
      );
   }

}
