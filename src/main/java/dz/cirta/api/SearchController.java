package dz.cirta.api;

import dz.cirta.api.configures.web.serializers.SearchableSummaryItem;
import dz.cirta.service.IBusinessLogic;
import dz.cirta.store.models.CirtaUser;
import dz.cirta.store.models.CirtaUser_;
import dz.cirta.store.models.Notification;
import dz.cirta.store.models.SummaryItem;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * base controller for handling search operations. Cirta-social uses elastic-search cluster to boost performance of text based search.
 * some of the basic requirements that Cirta-social should deal with is finding the right subject. indexing phase is a must since the queries will be executed against search engine clusters and not on configured database.
 * <br>when launching Cirta-social on production environment, hibernate search framework will drop all the shards from the cluster and starting indexing and sending back new records to elastic search cluster using Https protocol.
 * <br>only one free cluster is used with two shards
 * @author Abdessamed Diab
 * @since 1.0
 * @see <a href="https://app.bonsai.io/login">bonsai.io</a> for more information on how allocating shards on a free cluster.
 */
@RestController
@RequestMapping(path = "/search")
public class SearchController implements SocialIntegration {
   private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

   @Autowired
   private SearchSession searchSession;

   @Autowired
   private UsersConnectionRepository usersConnectionRepository;

   @Autowired(required = false)
   private ConnectionRepository connectionRepository;

   @Autowired
   private IBusinessLogic businessLogic;

   @GetMapping(path = "/{toLowerCaseKeyword}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<List<SearchableSummaryItem>> searchByKeyword(@PathVariable(required = true, name = "toLowerCaseKeyword") final String toLowerCaseKeyword) {

      final List<SummaryItem> summaryItems = searchSession.search(SummaryItem.class)
            .where(f -> f.simpleQueryString().field("title").matching(toLowerCaseKeyword))
            .fetch(10)
            .hits();

      final List<SearchableSummaryItem> dto = SearchableSummaryItem.bookToBookItem(summaryItems);

      return new ResponseEntity<>(dto, HttpStatus.OK);
   }

   @GetMapping(path = "/user/profile", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<UserProfile> fetchUserProfile() {
      try {
         Facebook facebook = initFacebookTemplate(connectionRepository);
         UserOperations userOperations = facebook.userOperations();
         String image = Base64.getEncoder().encodeToString(userOperations.getUserProfileImage());
         CirtaUser cirtaUser = (CirtaUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

         Optional<CirtaUser> user = businessLogic.findOptionalById(CirtaUser.class, CirtaUser_.ID, cirtaUser.getId());
         if (user.isPresent()) {
            cirtaUser.setProfileImage(image);
            businessLogic.update(cirtaUser);
         } else {
            cirtaUser.setProfileImage(image);
            businessLogic.save(cirtaUser);
         }

         return new ResponseEntity<>(
               new UserProfile(
                     userOperations.getUserProfile().getName(),
                     image
               ),
               HttpStatus.OK
         );
      } catch (IllegalArgumentException ex) {
         logger.warn(ex.getMessage());
         return new ResponseEntity<>(null, HttpStatus.METHOD_NOT_ALLOWED);
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

   @GetMapping(path = "/notifications/{username}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Set<Notification>> fetchNotifications(@PathVariable(name = "username", required = true) final String username) {
      Set<Notification> notifications = businessLogic.fetchNotificationsByUsernameAndMinDate(username, LocalDateTime.now().minusMonths(3));

      return new ResponseEntity<>(notifications, HttpStatus.OK);
   }

   public static class UserProfile {
      public String username;
      public String userProfileImage;

      public UserProfile(String username, String userProfileImage) {
         this.username = username;
         this.userProfileImage = userProfileImage;
      }
   }

}
