package dz.cirta.data.models;

import dz.cirta.data.service.BusinessLogic;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev") // integration test because we have filters.
public class NotificationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTest.class);

    @Autowired
    private Session hibernateSession;

    @Test
    public void testSave() {
        CirtaUser author = new CirtaUser("123456987", "baid", "last NAME", "ALTO PALO");
        Book book = new Book();
        book.setCoverPhotoUrl("77.jpg");
        book.setSourceUrl("77.jpg");
        book.setSubject("whatwhat");
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setBook(book);
        comment.setBadge(Comment.BADGES.BADGE_DARK.value);
        comment.setContent("what the fuck!");
        comment.setPageNumber(13);
        comment.setPublishedAt(LocalDateTime.now().minusMinutes(10));

        Notification notification = new Notification();
        notification.setBook(book);
        notification.setChecked(false);
        notification.setReply(comment);
        notification.setType(Comment.class.getSimpleName().toLowerCase());

        comment.setReplyNotification(notification);

        hibernateSession.beginTransaction();
        hibernateSession.saveOrUpdate(author);
        hibernateSession.saveOrUpdate(book);
        hibernateSession.save(comment);
        hibernateSession.getTransaction().commit();

        assertNotNull(comment.getReplyNotification());

        List list = hibernateSession.createQuery("FROM Notification ").getResultList();
        assertTrue(!list.isEmpty());
    }

}