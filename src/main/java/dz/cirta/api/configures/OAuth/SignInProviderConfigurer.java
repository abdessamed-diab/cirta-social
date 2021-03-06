package dz.cirta.api.configures.OAuth;

import dz.cirta.service.IBusinessLogic;
import dz.cirta.store.models.CirtaAuthority;
import dz.cirta.store.models.CirtaUser;
import dz.cirta.store.models.TempAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.support.OAuth2Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

/**
 * @author Abdessamed Diab
 */
@Configuration
public class SignInProviderConfigurer {

   @Autowired
   private IBusinessLogic businessLogic;

   @Autowired
   private UsersConnectionRepository usersConnectionRepository;

   @Value("${mobile.cirta.app.dns}")
   private String frontEndDns;

   @Bean(name = "signInAdapter")
   public SignInAdapter signInAdapter() {
      return new SignInAdapter() {

         @Override
         public String signIn(String s, Connection<?> connection, NativeWebRequest nativeWebRequest) {
            OAuth2Connection<FacebookTemplate> oAuth2Connection = (OAuth2Connection<FacebookTemplate>) connection;

            // we should use only interfaces when casting because spring use proxy based interfaces. check AOP!
            Facebook facebookTemplate = oAuth2Connection.getApi();
            UserOperations userOperations = facebookTemplate.userOperations();

            CirtaUser cirtaUser = Optional.ofNullable(
                  businessLogic.findUserByFacebookId(userOperations.getUserProfile().getId())
            ).orElse(
                  fbUserToCirtaUser(userOperations.getUserProfile())
            );

            TempAuthentication tempAuthentication = new TempAuthentication(String.valueOf(Math.random() * 1000000));
            cirtaUser.setTempAuthentication(tempAuthentication);
            businessLogic.saveOrUpdate(cirtaUser);

            return frontEndDns + "/" + cirtaUser.getTempAuthentication().getKey();
         }

      };
   }

   private CirtaUser fbUserToCirtaUser(User facebookTemplateUser) {
      CirtaUser cirtaUser = new CirtaUser(facebookTemplateUser.getId(),
            facebookTemplateUser.getFirstName(),
            facebookTemplateUser.getLastName(),
            facebookTemplateUser.getName()
      );

      cirtaUser.setAuthorities(businessLogic.findAuthoritiesIn(
            CirtaAuthority.AuthorityEnum.DEVELOPER.label, CirtaAuthority.AuthorityEnum.TESTER.label
      ));
      return cirtaUser;
   }
}
