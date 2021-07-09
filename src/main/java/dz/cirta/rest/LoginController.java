package dz.cirta.rest;

import dz.cirta.configures.web.serializers.TempRequestBody;
import dz.cirta.data.models.CirtaUser;
import dz.cirta.data.models.DeletionRequestStatus;
import dz.cirta.data.repo.CirtaUsersRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@RestController
public class LoginController {
   private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

   @Autowired
   private CirtaUsersRepository cirtaUsersRepository;

   @Value("${app.jwt.secret}")
   private String secret;

   @Value("${rahba.net.dns}")
   private String frontDns;

   @Autowired
   private Environment env;

   @Autowired
   private UsersConnectionRepository usersConnectionRepository;

   @Autowired
   private ConnectionRepository connectionRepository;

   @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<AuthResponse> login(
         @RequestBody(required = true) final TempRequestBody body,
         @RequestHeader(name = "language", required = true) final int language) {

      CirtaUser cirtaUser = Optional.ofNullable(
            cirtaUsersRepository.findFirstByTempAuthentication_KeyAndTempAuthenticationNotNull(body.getKey())
      ).orElse(
            cirtaUsersRepository.findFirstByNameAndPassword(body.getUsername(), body.getPassword())
      );
      if (cirtaUser == null) {
         logger.warn("anonymous user tried to get valid jwt using: " + body.toString());
         return new ResponseEntity<AuthResponse>(new AuthResponse().setJwtToken("empty", language), HttpStatus.NOT_FOUND);
      }

      cirtaUser.setTempAuthentication(null);
      cirtaUser.setLanguage((byte) language);
      cirtaUsersRepository.save(cirtaUser);

        /*final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                cirtaUser,
                null,
                cirtaUser.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        Connection<?> connection = connectionRepository.getConnection(new ConnectionKey(Facebook.class.getSimpleName().toLowerCase(), cirtaUser.getFacebookId()));
        Facebook facebook = (Facebook) connection.getApi();
        PagedList<GroupMembership> groups = facebook.groupOperations().getMemberships();
        String url = "https://graph.facebook.com/v9.0/"+groups.get(0).getId()+"/opted_in_members?access_token="+connection.createData().getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        ResponseEntity<GroupMemberReference> members = facebook.restOperations().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), GroupMemberReference.class);*/

      return new ResponseEntity<AuthResponse>(new AuthResponse().setJwtToken(createTokenForUser(cirtaUser), language), HttpStatus.OK);
   }

   // TODO ad we should clear connections from repository when user disconnect.
   @Bean(name = "connectionRepository")
   @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
   public ConnectionRepository connectionRepository() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null) {
         throw new IllegalArgumentException("unable to find connectionRepository: no user signed in.");
      }

      return usersConnectionRepository.createConnectionRepository(authentication.getName());
   }

   private String createTokenForUser(final CirtaUser user) {
      Instant duration = null;
      switch (env.getActiveProfiles()[0]) {
         case "prod":
            duration = ZonedDateTime.now().plusMinutes(5).toInstant();
            break;
         default:
            duration = ZonedDateTime.now().plusHours(2).toInstant();
      }

      return Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setExpiration(Date.from(duration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
   }

   @PostMapping(path = "/dataDeletionRequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
   public DeletionRequestStatus dataDeletionRequest(HttpEntity<String> callbackBody) {
      return new DeletionRequestStatus("https://rahba.net", "200");
   }

   @GetMapping(path = "/signin*")
   public RedirectView redirectToIndex() {
      LoggerFactory.getLogger(getClass())
            .warn("the user has been redirected to signin end point, because facebook based OAth2 authentication failed.");
      return new RedirectView(frontDns);
   }

   private class AuthResponse {
      public String jwtToken;
      public String csrfToken;
      public int language;

      // we are not in the same domain, #1 front dont have access to hor-domain cookies, and #2 we have a stat-less sessions.
      // in other words we should secure app without activating csrf filter, instead by providing another approach.
      public AuthResponse() {
      }

      private AuthResponse setJwtToken(String jwtToken, int language) {
         this.jwtToken = jwtToken;
         this.language = language;
         return this;
      }

      private AuthResponse setCsrfToken(String csrfToken) {
         this.csrfToken = csrfToken;
         return this;
      }
   }
}
