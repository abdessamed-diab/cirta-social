package dz.cirta.api.configures.OAuth;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.support.OAuth2Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Abdessamed Diab
 */
public class SignUpConnection implements ConnectionSignUp {

   public SignUpConnection() {
      fixProfileFields(); // remove this fix just after next release of spring-social-facebook.
   }

   // TODO ad you should refactor this method.
   @Override
   public String execute(Connection<?> connection) {
      OAuth2Connection<FacebookTemplate> oAuth2Connection = (OAuth2Connection<FacebookTemplate>) connection;

      // we should use only interfaces when casting because spring use proxy based interfaces. check AOP!
      Facebook facebookTemplate = oAuth2Connection.getApi();
      UserOperations userOperations = facebookTemplate.userOperations();

      return userOperations.getUserProfile().getName();
   }

   private void fixProfileFields() {
      String[] PROFILE_FIELDS = new String[]{"id", "about", "age_range", "birthday", "cover", "currency", "devices", "education", "email", "favorite_athletes", "favorite_teams", "first_name", "gender", "hometown", "inspirational_people", "installed", "install_type", "is_verified", "languages", "last_name", "link", "locale", "location", "meeting_for", "middle_name", "name", "name_format", "political", "quotes", "payment_pricepoints", "relationship_status", "religion", "significant_other", "sports", "timezone", "third_party_id", "updated_time", "verified", "video_upload_limits", "website", "work"};
      try {
         Field profileFields = UserOperations.class.getDeclaredField("PROFILE_FIELDS");
         profileFields.setAccessible(true);

         Field modifiers = profileFields.getClass().getDeclaredField("modifiers");
         modifiers.setAccessible(true);
         modifiers.setInt(profileFields, profileFields.getModifiers() & ~Modifier.FINAL);

         profileFields.set(null, PROFILE_FIELDS);
      } catch (NoSuchFieldException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      }
   }
}
