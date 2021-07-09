package dz.cirta.data.models;

import dz.cirta.tools.PdfStreamApi;
import dz.cirta.data.service.BusinessLogic;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "spring.profiles.active=dev"
) // integration test because we have filters.
class BookTest {

    @Value("${dz.cirta.app.volume}")
    private String volumeBaseDir;

    @Autowired
    private BusinessLogic businessLogic;

    @Autowired
    private Session hibernateSession;

    @Test
    public void testExtractBookMetaData() throws IOException {
        Book book = new Book();
        book.setCoverPhotoUrl(volumeBaseDir+"subjects/1.jpg");

        PdfStreamApi.fillBookMetaData(book, volumeBaseDir+"books/spring-social-reference.pdf");

        assertNotNull(book.getTitle());
        assertNotNull(book.getSubject());
        assertNotNull(book.getAuthor());
        assertNotNull(book.getCoverPhotoUrl());
        assertNotNull(book.getPublisher());
    }

    @Test
    public void testSaveBooksAndBookSummaries() throws IOException {
        String pdfDocumentDirectory= volumeBaseDir+"books/test/";
        String coverPhotoFileNameMappingSourceFileUrl= volumeBaseDir+"books/cover_photo_file_name_mapping.txt";
        List<Book> books = PdfStreamApi.initializeBookProperties(pdfDocumentDirectory, coverPhotoFileNameMappingSourceFileUrl);

        assertTrue(businessLogic.saveBooksAndSummaries(books));
        List<SummaryItem> items = hibernateSession.createNativeQuery("SELECT * from summary_item", SummaryItem.class).getResultList();
        assertTrue(!items.isEmpty());
    }


}
