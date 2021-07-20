package dz.cirta.api;

import dz.cirta.service.IBusinessLogic;
import dz.cirta.store.models.*;
import dz.cirta.store.tools.PdfStreamApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Abdessamed Diab
 */
@RestController
@RequestMapping(path = "/subject")
public class SubjectController implements SocialIntegration {
   private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

   @Autowired
   private UsersConnectionRepository usersConnectionRepository;

   @Autowired
   private IBusinessLogic businessLogic;

   // TODO ad you should implement real favorite subjects.
   @GetMapping(path = "/favorites/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, headers = {"Authorization"})
   public <T extends Subject> List<T> favoriteSubjects(@PathVariable(name = "userId", required = false) String id, Locale locale,
                                                      @RequestHeader(name = "Authorization") String authorization) {

      return (List<T>) businessLogic.findAllByClass(Book.class);
   }

   @GetMapping(path = "/book/{bookId}/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
   public ResponseEntity<StreamingResponseBody> streamBookByBookId(
         @PathVariable(name = "bookId", required = true) Long bookId,
         @RequestHeader(required = true, name = "source-url") final String sourceUrl) {
      StreamingResponseBody streamingResponseBody = (OutputStream out) -> {

         Resource resource = new FileSystemResource(sourceUrl);
         InputStream inputStream = resource.getInputStream();
         BufferedOutputStream bos = new BufferedOutputStream(out);
         bos.write(
               Files.readAllBytes(
                     Paths.get(resource.getURI())
               )
         );

         inputStream.close();
         bos.close();
      };
      return new ResponseEntity<>(streamingResponseBody, HttpStatus.OK);
   }

   // TODO ad bug detected after calling this method over and over, hikari connection pool leak, the problem is related with findById DAO service, perhaps transaction!
   @GetMapping(path = "/book/{bookId}/stream/{startPage}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
   public StreamingResponseBody streamPagesOfBook(
         @PathVariable(name = "bookId", required = true) final Long bookId,
         @PathVariable(name = "startPage", required = true) final int startPage,
         @RequestHeader(required = true, name = "source-url") final String sourceUrl) {

      return (OutputStream out) -> {
         try {
            PdfStreamApi.splitPdfToOutputStream(startPage,
                  startPage + 4,
                  sourceUrl,
                  out);
         } catch (IllegalArgumentException | IOException ex) {
            logger.error(ex.getMessage(), ex);
         }
      };
   }

   @GetMapping(path = "/book/{bookId}/bookmark", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Map<String, List<Bookmark>>> extractBookmark(@PathVariable(required = true, name = "bookId") Long bookId,
                                                                      @RequestHeader(required = true, name = "source-url") String sourceUrl) {

      Optional<Book> book = businessLogic.findOptionalById(Book.class, Book_.ID, bookId);

      if (book.isPresent()) {
         return new ResponseEntity(new BookChartModalData(book.get().getBookAttributes()), HttpStatus.OK);
      } else {
         return new ResponseEntity(null, HttpStatus.NOT_ACCEPTABLE);
      }
   }

   @Bean(name = "connectionRepository")
   @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
   public ConnectionRepository connectionRepository() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null) {
         throw new IllegalArgumentException("unable to find connectionRepository: no user signed in.");
      }

      return usersConnectionRepository.createConnectionRepository(authentication.getName());
   }

   @PostMapping(path = "/book/{bookId}/comment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Comment> postComment(@PathVariable(name = "bookId", required = true) final long bookId,
                                              @RequestBody final Comment comment) {

      try {
         comment.setPublishedAt(LocalDateTime.now());
         comment.setBook(businessLogic.loadFromLocalCache(Book.class, bookId));
         comment.setParent(true);
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         // TODO tou should remove next statement, we know that we have authentication, fix test.
         if (authentication != null && authentication.isAuthenticated()) {
            comment.setAuthor((CirtaUser) authentication.getPrincipal());
         }

         boolean isSaved = businessLogic.save(comment);

         return new ResponseEntity<>(comment, HttpStatus.CREATED);
      } catch (Exception ex) {
         logger.error(ex.getMessage(), ex);
         return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
      }
   }

   @GetMapping(path = "/book/{bookId}/comments/{pageNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Set<Comment>> fetchPageComments(@PathVariable(name = "bookId", required = true) final long bookId,
                                                          @PathVariable(name = "pageNumber", required = true) final int pageNumber) {

      Collection<Comment> comments = businessLogic.findParentCommentsByGivenBookCoordinates(bookId, pageNumber);

      if (!comments.isEmpty()) {
         return new ResponseEntity(new LinkedHashSet<>(comments), HttpStatus.OK);
      } else {
         return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
      }
   }

   @PostMapping(path = "/comment/addTo/{parentId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<TreeSet<Comment>> addCommentToParent(@PathVariable(name = "parentId", required = true) final Long parentId,
                                                              @RequestBody(required = true) final Comment comment) throws IOException {

      Comment parent = businessLogic.findFetchOptionalById(Comment.class, Comment_.ID, parentId, Comment_.REPLIES, Comment_.BOOK).orElse(null);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      comment.setAuthor((CirtaUser) authentication.getPrincipal());
      comment.setPublishedAt(LocalDateTime.now());
      comment.setBook(parent.getBook());
      comment.setReplyNotification(Notification.CREATE_NOTIFICATION(parent.getBook(), comment));
      parent.addChild(comment);

      businessLogic.saveOrUpdate(parent);

      Collections.sort(parent.getReplies());
      return new ResponseEntity(parent.getReplies(), HttpStatus.OK);
   }

   public class BookChartModalData {
      public Map<String, List<Bookmark>> bookmarks;

      public BookChartModalData(Map<String, List<Bookmark>> bookmarks) {
         this.bookmarks = bookmarks;
      }
   }

   @GetMapping(path = "/mostVisited")
   public void mostVisitedBooks() {

   }

   @GetMapping(path = "/lastVisited")
   public void lastVisitedBooks() {

   }

}
