package dz.cirta.data.models;

import dz.cirta.data.service.BusinessLogic;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev") // integration test because we have filters.
public class CommentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentTest.class);

    @Autowired
    private BusinessLogic dao;

    @Autowired
    private Session hibernateSession;

    @Test
    public void testSave() {
        CirtaUser author = new CirtaUser("123456", "diab", "abdessamed", "diab abdessamed");
        Book book = new Book();
        book.setCoverPhotoUrl("1.jpg");
        book.setSourceUrl("1.jpg");
        book.setSubject("math");
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setBook(book);
        comment.setBadge(Comment.BADGES.BADGE_DARK.value);
        comment.setContent("what the fuck!");
        comment.setPageNumber(13);
        comment.setPublishedAt(LocalDateTime.now().minusMinutes(10));

        hibernateSession.beginTransaction();
        hibernateSession.saveOrUpdate(author);
        hibernateSession.saveOrUpdate(book);
        hibernateSession.save(comment);
        hibernateSession.getTransaction().commit();

        List<Comment> comments = hibernateSession.createQuery("FROM Comment", Comment.class).getResultList();

        assertTrue(true);
    }

    @Test
    public void testReplies() {
        CirtaUser author = new CirtaUser("12345678", "diab", "abdessamed", "diab abdess");
        Book book = new Book();
        book.setCoverPhotoUrl("2.jpg");
        book.setSourceUrl("2.jpg");
        book.setSubject("physics");

        Comment parent = new Comment();
        parent.setAuthor(author);
        parent.setBook(book);
        parent.setBadge(Comment.BADGES.BADGE_DARK.value);
        parent.setContent("what the fuck!");
        parent.setPageNumber(13);
        parent.setPublishedAt(LocalDateTime.now().minusMinutes(10));

        List<Comment> comments = new ArrayList(5);
        for (int i =0; i < 5; i++) {
            Comment comment = new Comment();
            comment.setAuthor(author);
            comment.setBook(book);
            comment.setBadge(i % 2 == 0 ? Comment.BADGES.BADGE_DARK.value : Comment.BADGES.BADGE_LIGHT.value);
            comment.setContent("what the fuck! "+i);
            comment.setPageNumber(i);
            comment.setPublishedAt(LocalDateTime.now().minusMinutes(i));
            comments.add(i, comment);
        }

        parent.setReplies(comments);

        hibernateSession.beginTransaction();
        hibernateSession.saveOrUpdate(author);
        hibernateSession.saveOrUpdate(book);
        hibernateSession.save(parent);
        hibernateSession.getTransaction().commit();
        assertTrue(parent.getReplies().get(0).getId() != null);

        Comment fetchParent = hibernateSession.find(Comment.class, parent.getId());
        assertTrue(!fetchParent.getReplies().isEmpty());
    }

}
