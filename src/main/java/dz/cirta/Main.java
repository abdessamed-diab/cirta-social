package dz.cirta;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.persistence.EntityManagerFactory;

@SpringBootApplication(scanBasePackages = {"dz.cirta.configures.**", "dz.cirta.rest", "dz.cirta.data.**"})
public class Main {

   @Autowired
   private EntityManagerFactory entityManagerFactory;

   public static void main(String[] args) throws Exception {
      SpringApplication springApplication = new SpringApplication(Main.class);
      springApplication.setLazyInitialization(false);
      springApplication.setBannerMode(Banner.Mode.OFF);
      springApplication.setAdditionalProfiles(args[0]);
      ConfigurableApplicationContext context = springApplication.run(args);
      context.getBeanFactory().destroyBean("replaceWithDDL");
      System.gc();
   }

   @Bean(name = "hibernateSession")
   public Session hibernateEntityManager() {
      return entityManagerFactory.unwrap(SessionFactory.class).openSession();
   }

   @Bean
   @Order(Ordered.LOWEST_PRECEDENCE)
   public SearchSession fullTextEntityManager() throws InterruptedException {
      return Search.session(hibernateEntityManager());
   }

}
