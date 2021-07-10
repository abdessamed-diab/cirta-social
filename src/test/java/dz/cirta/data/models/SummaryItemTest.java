package dz.cirta.data.models;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class SummaryItemTest {

    @Autowired
    private Session hibernateSession;


    @Test
    public void testSave() {
        SummaryItem parent = new SummaryItem("parent title");
        parent.page = 0;

        SummaryItem item_1 = new SummaryItem();
        item_1.title = "child 1";
        item_1.page = 15;
        SummaryItem item_2 = new SummaryItem();
        item_2.title = "child 2";
        item_2.page = 17;

        Book book = new Book();
        book.setCoverPhotoUrl("test");
        book.setSourceUrl("test deux");
        book.setSubject("subject");
        book.setTitle("title");
        hibernateSession.persist(book);

        parent.book = book;
        item_1.parent = parent;
        item_1.book = book;
        item_2.parent = parent;
        item_2.book = book;

        hibernateSession.persist(parent);
        hibernateSession.persist(item_1);
        hibernateSession.persist(item_2);

        assertNull(parent.parent);
        assertNotNull(item_1.parent);

        hibernateSession.remove(item_2);
        hibernateSession.remove(item_1);
        hibernateSession.remove(parent);
    }

}
