package dz.cirta;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.orm.hibernate5.HibernateTransactionManager;

import javax.persistence.EntityManagerFactory;

/**
 * entry point of launching all necessary beans and bootstrap <a href="https://cirta.app">cirta.app (CaaS)</a>
 * <br> class used to bootstrap and launch an instance of {@link ConfigurableApplicationContext}, <br>
 * three packages need to be loaded on startup, api package covering front controllers, service package exposing all needed operations <br>
 * and a repository package for interaction with the database. <br>
 * on startup, in production environment, initializing elasticsearch cluster is mandatory {@link dz.cirta.service.BusinessLogic} <br>
 * add admin user to database on startup is required to administer, operate and manage database. {@link org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration}
 * @author Abdessamed Diab
 * @version 2.0
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = {"dz.cirta.api.**", "dz.cirta.service","dz.cirta.store.repo"})
public class Main {
   private static final Logger logger = LoggerFactory.getLogger(Main.class);

   @Autowired
   private EntityManagerFactory entityManagerFactory;

   /**
    * launch spring boot application using {@link Main} class as annotation based configuration class.
    * @param args env variables contains at least active profile dev, release or production
    */
   public static void main(String[] args) {
      SpringApplication springApplication = new SpringApplication(Main.class);
      springApplication.setLazyInitialization(false);
      springApplication.setBannerMode(Banner.Mode.OFF);
      springApplication.setAdditionalProfiles(args[0]);
      ConfigurableApplicationContext context = springApplication.run(args);
      logger.info(context.getApplicationName()+" launched successfully");
   }

   @Bean("hibernateTransactionManager")
   public HibernateTransactionManager hibernateTransactionManager() {
      return new HibernateTransactionManager(entityManagerFactory.unwrap(SessionFactoryImplementor.class));
   }

   @Bean(name = "hibernateSession")
   public Session hibernateEntityManager() {
      return entityManagerFactory.unwrap(SessionFactoryImplementor.class).openSession();
   }

   @Bean
   @Order(Ordered.LOWEST_PRECEDENCE)
   public SearchSession fullTextEntityManager() throws InterruptedException {
      return Search.session(hibernateEntityManager());
   }


}
