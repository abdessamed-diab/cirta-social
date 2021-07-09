package dz.cirta.configures.OAuth;

import dz.cirta.data.models.CirtaUser;
import dz.cirta.data.repo.CirtaUsersRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class StatelessAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

   @Value("${app.jwt.secret}")
   private String secret;

   @Autowired
   private CirtaUsersRepository cirtaUsersRepository;

   protected StatelessAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
      super(requiresAuthenticationRequestMatcher);
      super.setContinueChainBeforeSuccessfulAuthentication(true);
   }

   @Override
   public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
      String authorizationHeader = request.getHeader("Authorization");
      if (StringUtils.isEmpty(authorizationHeader))
         return redirectToFailingAuthentication(request, response,
               new BadCredentialsException("no Authorization header found for requested resource: " + request.getRequestURL())
         );


      String subject = null;
      try {
         subject = extractSubjectFromJwt(authorizationHeader.substring(7));
      } catch (JwtException | IllegalArgumentException ex) {
         return redirectToFailingAuthentication(request, response,
               new BadCredentialsException(ex.getMessage()));
      }

      final Optional<CirtaUser> user = cirtaUsersRepository.findById(Long.valueOf(subject));
      Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.get(),
            null,
            user.get().getAuthorities() // because we have Lazy load.
      );

      return this.getAuthenticationManager().authenticate(authentication);
   }

   @Override
   protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
   }

   private Authentication redirectToFailingAuthentication(HttpServletRequest request, HttpServletResponse response, RuntimeException exception) throws IOException, ServletException {
      logger.error(exception.getMessage());
      super.unsuccessfulAuthentication(request, response, new BadCredentialsException(exception.getMessage()));
      return null;
   }

   private String extractSubjectFromJwt(String encodedJwt) throws JwtException, IllegalArgumentException {
      return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(encodedJwt)
            .getBody()
            .getSubject();
   }
}
