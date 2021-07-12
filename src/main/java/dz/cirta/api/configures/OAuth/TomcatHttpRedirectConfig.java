package dz.cirta.api.configures.OAuth;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Abdessamed Diab
 */
@Configuration
public class TomcatHttpRedirectConfig {

   @Bean
   public ServletWebServerFactory servletContainer() {
      TomcatServletWebServerFactory tomcat = new TomcatServletWebServer();
      tomcat.addAdditionalTomcatConnectors(getHttpConnector());
      return tomcat;
   }

   private Connector getHttpConnector() {
      Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
      connector.setScheme("http");
      connector.setPort(80);
      connector.setSecure(false);
      connector.setRedirectPort(443);
      return connector;
   }

   private class TomcatServletWebServer extends TomcatServletWebServerFactory {
      @Override
      protected void postProcessContext(Context context) {
         SecurityConstraint securityConstraint = new SecurityConstraint();
         securityConstraint.setUserConstraint("CONFIDENTIAL");
         SecurityCollection collection = new SecurityCollection();
         collection.addPattern("/*");
         securityConstraint.addCollection(collection);
         context.addConstraint(securityConstraint);
      }
   }
}
