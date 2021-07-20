package dz.cirta.api;

import dz.cirta.service.IBusinessLogic;
import dz.cirta.store.models.Comment;
import dz.cirta.store.models.Comment_;
import dz.cirta.store.models.GroupInstallWebHookObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Abdessamed Diab
 */
@RestController
@RequestMapping("/api")
public class CirtaWebHookHandler {
   private static final Logger logger = LoggerFactory.getLogger(CirtaWebHookHandler.class);
   @Value("${logging.file.name}")
   private String logsFilePath;

   @Value("${dz.cirta.app.facebook.webhooks.verify_token}")
   private String verifyToken;

   @Autowired
   private IBusinessLogic businessLogic;

   // TODO tdd how to manage multiple log files like cirta-social-3.log ? check spring boot logback configuration.
   @GetMapping(path = "/logs", produces = "text/plain")
   public String logFileToText() {
      File file = new File(logsFilePath);
      try {
         BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
         StringBuffer buffer = new StringBuffer();
         bufferedReader.lines().forEach(line -> buffer.append(line + "\n"));
         return buffer.toString();
      } catch (FileNotFoundException e) {
         throw new IllegalStateException("file note found: " + logsFilePath);
      }
   }

   @GetMapping(path = "/webhooks", produces = MediaType.TEXT_PLAIN_VALUE)
   public ResponseEntity<String> facebookWebHookQuery(@RequestParam(name = "hub.mode", required = true) final String mode,
                                                      @RequestParam(name = "hub.challenge", required = true) final long challenge,
                                                      @RequestParam(name = "hub.verify_token", required = false) final String verify_token) {
      logger.info("facebookWebHookQuery - challenge: " + challenge);
      if (!verifyToken.equalsIgnoreCase(verify_token)) {
         logger.warn("facebook supplied verify token do not much app's dashboard configured verify token.");
         return new ResponseEntity<>("", HttpStatus.PRECONDITION_FAILED);
      }

      return new ResponseEntity<>(String.valueOf(challenge), HttpStatus.OK);
   }

   @PostMapping(path = "/webhooks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<String> facebookEventNotification(@RequestBody(required = true) GroupInstallWebHookObject body) {
      logger.info("facebookEventNotification  -  body: " + body);
      return new ResponseEntity<>("", HttpStatus.OK);
   }

   // TODO ad move this from here
   @GetMapping(path = "/{commentId}/fetchNewChildren", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public SseEmitter fetchNewestChildComments(
         @PathVariable(name = "commentId", required = true) final long childCommentId,
         @RequestParam(name = "language", required = false, defaultValue = "1") final int language) {

      SseEmitter emitter = new SseEmitter(15000L); // 10 sec

      ExecutorService executorService = Executors.newFixedThreadPool(1);
      new DelegatingSecurityContextExecutorService(executorService).execute(
         () -> {
            try {
               Thread.sleep(10000L);
            } catch (InterruptedException e) {
               logger.error(e.getMessage());
            }

            Collection<Comment> newest = null;

            Comment comment = businessLogic.findFetchOptionalById(Comment.class, Comment_.ID, childCommentId, Comment_.PARENT_COMMENT).orElse(null);

            Map<String, Object> params = new HashMap<>();
            if (!comment.isParent()) {
               params.put("parentId", comment.getParentComment().getId());
               params.put("time", comment.getPublishedAt());

               newest = businessLogic.findByQueryAndParams(
                           Comment.class,
                           params,
                   "SELECT c FROM Comment c " +
                           "WHERE c.parentComment.id = :parentId " +
                              "AND c.publishedAt > :time " +
                           "ORDER BY c.publishedAt DESC"
                        );
            } else {
               params.put("parentId", comment.getId());
               newest = businessLogic.findByQueryAndParams(
                              Comment.class,
                              params,
                              "SELECT c FROM Comment c WHERE c.parentComment.id = :parentId ORDER BY c.publishedAt DESC"
                        );
            }

            try {
               emitter.send(newest, MediaType.APPLICATION_JSON);
               emitter.complete();
            } catch (IOException e) {
               logger.warn(e.getMessage());
               emitter.completeWithError(e);
            }

         }
      );

      executorService.shutdown();
      return emitter;
   }

   @GetMapping(path = "/notifications/{username}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public SseEmitter fetchNotifications(@PathVariable(name = "username", required = true) final String username) {
      SseEmitter emitter = new SseEmitter(15000L); // 15 sec
      ExecutorService executorService = Executors.newFixedThreadPool(1);
      new DelegatingSecurityContextExecutorService(executorService).execute(
         () -> {
            try {
               Thread.sleep(10000L);
            } catch (InterruptedException e) {
               logger.error(e.getMessage());
            }

            int value = businessLogic.countNotifications(username,
                  LocalDateTime.now().minusMonths(3),
                  "comment");

            try {
               emitter.send(value, MediaType.APPLICATION_JSON);
               emitter.complete();
            } catch (IOException e) {
               logger.warn(e.getMessage());
               emitter.completeWithError(e);
            }
         }
      );

      executorService.shutdown();
      return emitter;
   }

}
