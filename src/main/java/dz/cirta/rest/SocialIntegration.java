package dz.cirta.rest;

import dz.cirta.data.models.CirtaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Collections;

public interface SocialIntegration {
   Logger logger = LoggerFactory.getLogger(SocialIntegration.class);
   String host = "https://graph.facebook.com/v9.0/";

   default Facebook initFacebookTemplate(ConnectionRepository repository) throws IllegalArgumentException, NullPointerException {
      UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (!(user instanceof CirtaUser)
            ||
            StringUtils.isEmpty(((CirtaUser) user).getFacebookId())
      ) {
         throw new IllegalArgumentException("cannot fetch user profile picture for : " + user.getUsername());
      }

      Connection<?> connection = repository.getConnection(
            new ConnectionKey(
                  Facebook.class.getSimpleName().toLowerCase(),
                  ((CirtaUser) user).getFacebookId()
            )
      );

      return (Facebook) connection.getApi();
   }

   default String getUserProfilePicture(ConnectionRepository repository) {
      try {
         Facebook facebook = initFacebookTemplate(repository);
         return Base64.getEncoder().encodeToString(facebook.userOperations().getUserProfileImage());
      } catch (IllegalArgumentException | NullPointerException ex) {
         logger.warn(ex.getMessage(), ex);
      }

      return null;
   }

   // TODO dont forget to add href and ref url parameters. and avoid sending to much notification to someone.
   @Deprecated
   default boolean sendNotification(ConnectionRepository repository, CirtaUser recipient, CirtaUser commentIssuer) {
      if (recipient.getFacebookId().equals(commentIssuer.getFacebookId())) {
         logger.debug("commentIssuer is the recipient. no send notification.");
         return false;
      }

      Facebook facebook = initFacebookTemplate(repository);
      Connection<?> connection = repository.getConnection(
            new ConnectionKey(
                  Facebook.class.getSimpleName().toLowerCase(),
                  facebook.userOperations().getUserProfile().getId()
            )
      );
      String uri = host + recipient.getFacebookId() + "/notifications" +
            "?access_token=" + connection.createData().getAccessToken() +
            "&template=hello_hello";

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

      ResponseEntity<String> response = facebook.restOperations().exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
      return !response.getStatusCode().isError();
   }

}
