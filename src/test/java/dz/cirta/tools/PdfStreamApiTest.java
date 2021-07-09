package dz.cirta.tools;

import dz.cirta.data.models.Book;
import dz.cirta.tools.FileUtils;
import dz.cirta.tools.PdfStreamApi;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfStreamApiTest {
    private static final String resourceSubjectName = "1.jpg";

    @Test
    public void testEncodeFileContent() throws IOException {
        String resourceUrl = "/appli/volume/subjects/"+ resourceSubjectName;

        String result = FileUtils.ENCODE_FILE_CONTENT(resourceUrl);
        assertNotNull(result);
    }

    @Test
    public void testEncodeFileContentFileNotFoundException() {
        String resourceUrl = "/appli/volume/subjects/what.jpg";

        assertThrows(IOException.class, () -> FileUtils.ENCODE_FILE_CONTENT(resourceUrl));
    }

    @Test
    public void testSplitPdfFile() throws IOException {
        String path = "/appli/volume/" + "books" + File.separator + "spring-social-reference.pdf";
        boolean isExtracted = PdfStreamApi.splitPdfFile(1, 5, path);
        assertTrue(isExtracted);

        isExtracted = PdfStreamApi.splitPdfFile(5, 23, path);
        assertTrue(isExtracted);
    }

    @Test
    public void testSplitPdfFileDoesNotExist() {
        String path = "volume/" + "books" + File.separator + "13.pdf";
        assertThrows(FileNotFoundException.class,
                () -> PdfStreamApi.splitPdfFile(1, 5, path));
    }

    @Test
    public void testSplitPdfFileStartPageDoesNotExists() {
        String path = "/appli/volume/" + "books" + File.separator + "spring-social-reference.pdf";
        assertThrows(IllegalArgumentException.class, () ->{
            PdfStreamApi.splitPdfFile(215, 220, path);
        });
    }

    @Test
    public void testSplitPdfFileToOutputStream() throws IOException {
        String path = "/appli/volume/" + "books" + File.separator + "The.Practice.of.System.and.Network.Administration.2nd.Edition.pdf";
        OutputStream outputStream = new BufferedOutputStream(new ByteArrayOutputStream());
        PdfStreamApi.splitPdfToOutputStream(101, 106, path, outputStream);
        assertNotNull(outputStream);
        outputStream.close();
    }

    @Test
    public void testSplitPdfFileToOutputStreamPdfFileDoesNotExist() throws FileNotFoundException{
        String path = "/appli/volume/" + "books" + File.separator + "The.of.System.and.Network.Administration.2nd.Edition.pdf";
        OutputStream os = new FileOutputStream("/appli/volume/subjects/"+ resourceSubjectName);
        assertThrows(FileNotFoundException.class, () -> PdfStreamApi.splitPdfToOutputStream(101, 106, path, os));
    }

    @Test
    public void testInitializeBookProperties() throws IOException {
        String resource = "/appli/volume/books/hibernate_reference.pdf";
        List<Book> books = PdfStreamApi.initializeBookProperties("/appli/volume/books/",
            "/appli/volume/books/cover_photo_file_name_mapping.txt");
        assertTrue(!books.isEmpty());
    }

    @Test
    public void testInitializeBookPropertiesNoSuchDirectoryException() {
        assertThrows(NoSuchFileException.class, () -> PdfStreamApi.initializeBookProperties("/appli/volume/bookss/",
            "/appli/volume/books/cover_photo_file_name_mapping.txt"));
    }

    @Test
    public void testInitializeBookPropertiesMappingSourceDoesNotExist() throws IOException {
        List<Book> books = PdfStreamApi.initializeBookProperties("/appli/volume/books/",
              "/appli/volume/books/cover_photo_file_name_mappingg.txt");

        assertTrue(books.isEmpty());
    }

}
