package dz.cirta.store.models;

import dz.cirta.service.IBusinessLogic;
import dz.cirta.store.tools.PdfStreamApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "spring.profiles.active=dev"
) // integration test because we have filters.
class BookTest {

    @Value("${dz.cirta.app.volume}")
    private String volumeBaseDir;

    @Autowired
    private IBusinessLogic businessLogic;

    @Test
    public void testFillBookMetaData() throws IOException {
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
    public void testLoadBooksWithSummariesToElasticSearchCluster() {
        Collection<Book> books = businessLogic.findAllByClass(Book.class);
        assertTrue(!books.isEmpty());
    }


}
