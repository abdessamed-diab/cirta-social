package dz.cirta.api.configures.OAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * @author Abdessamed Diab
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

   @Value("${spring.social.facebook.appId}")
   private String appId;

   @Value("${spring.social.facebook.appSecret}")
   private String appSecret;

   @Value("${rahba.net.dns}")
   private String frontEndDns;

   @Autowired
   private DataSource dataSource;

   @Autowired
   private Environment env;

   @Autowired
   private SignInAdapter signInAdapter;

   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and().requiresChannel().anyRequest().requiresSecure()
            .and().authorizeRequests()
            .antMatchers("/signin/**", "/signup/**"        // service provider OAuth2.
                  , "/api/**"                                       // API we want to expose.
                  , "/public/**"                                    // static resource repository is permitted.
                  , "/doc/**"                                    // static resource repository is permitted.
                  , "/*"                                            // welcome page url, privacy policy and terms and conditions.
            ).permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .headers().frameOptions().sameOrigin().and()
            .addFilterBefore(statelessAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
   }


   @Override
   protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(new AuthenticationProvider() {
         @Override
         public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
         }

         @Override
         public boolean supports(Class<?> aClass) {
            return aClass.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
         }
      });
   }

   @Bean
   public StatelessAuthenticationFilter statelessAuthenticationFilter() throws Exception {
      OrRequestMatcher or = new OrRequestMatcher(new AntPathRequestMatcher("/subject/**"), new AntPathRequestMatcher("/search/**"));
      StatelessAuthenticationFilter statelessAuthenticationFilter = new StatelessAuthenticationFilter(or);
      statelessAuthenticationFilter.setAuthenticationManager(authenticationManagerBean());
      return statelessAuthenticationFilter;
   }

   @Bean(name = "usersConnectionRepository")
   public UsersConnectionRepository usersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
      return "dev".equalsIgnoreCase(env.getActiveProfiles()[0]) ?
            new InMemoryUsersConnectionRepository(connectionFactoryLocator) :
            new JdbcUsersConnectionRepository(
                  dataSource,
                  connectionFactoryLocator,
                  Encryptors.noOpText()
            );
   }

   @Bean(name = "connectionFactoryLocator")
   public ConnectionFactoryLocator connectionFactoryLocator() {
      ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
      registry.addConnectionFactory(new FacebookConnectionFactory(appId, appSecret));

      return registry;
   }

   /**
    * this is the service provider we want to use, like facebook OAuth1 or OAuth2 service providers.
    * check spring social
    * @return {@link ProviderSignInController} front controller responsable of signing in users using OAuth2 open standard.
    */
   @Bean
   public ProviderSignInController providerSignInController() {
      ConnectionFactoryLocator connectionFactoryLocator = connectionFactoryLocator();

      UsersConnectionRepository usersConnectionRepository = usersConnectionRepository(connectionFactoryLocator);
      usersConnectionRepository.setConnectionSignUp(new SignUpConnection());

      return new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository, signInAdapter);
   }


   private CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.addAllowedOrigin(frontEndDns);
      configuration.setAllowedMethods(Arrays.asList("*"));
      configuration.setAllowedHeaders(Arrays.asList("*"));
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
   }

}
