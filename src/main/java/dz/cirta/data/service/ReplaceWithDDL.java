package dz.cirta.data.service;

import dz.cirta.data.models.Book;
import dz.cirta.data.models.CirtaAuthority;
import dz.cirta.data.models.CirtaUser;
import dz.cirta.tools.PdfStreamApi;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ReplaceWithDDL implements InitializingBean {
   private static final Logger logger = LoggerFactory.getLogger(ReplaceWithDDL.class);

   @Value("${dz.cirta.app.volume}")
   private String volumeBaseDir;

   @Autowired
   private Session hibernateSession;

   @Autowired
   private BusinessLogic businessLogic;

   @Override
   public void afterPropertiesSet() throws Exception {
      fillAuthorities();
      addAdminUser();
      fillBookTable();
      System.gc();
   }

   private void fillAuthorities() {
      hibernateSession.persist(new CirtaAuthority(1, "DEVELOPER"));
      hibernateSession.persist(new CirtaAuthority(2, "TESTER"));

      logger.info("fillAuthorities DONE.");
   }

   private void addAdminUser() {
      CirtaUser cirtaUser = new CirtaUser();
      cirtaUser.setUserName("tester");
      cirtaUser.setPassword("rahba");
      cirtaUser.setFirstName("Abdessamed");
      cirtaUser.setLastName("DIAB");

      cirtaUser.setAuthorities(businessLogic.findAuthoritiesIn());
      hibernateSession.getTransaction().begin();
      hibernateSession.persist(cirtaUser);
      hibernateSession.getTransaction().commit();

      logger.info("addAdminUser DONE.");
   }

   private void fillBookTable() throws IOException {
      String pdfDocumentDirectory = volumeBaseDir + "books/";
      String coverPhotoFileNameMappingSourceFileUrl = volumeBaseDir + "books/cover_photo_file_name_mapping.txt";

      List<Book> books = PdfStreamApi.initializeBookProperties(pdfDocumentDirectory,
            coverPhotoFileNameMappingSourceFileUrl);

      businessLogic.saveBooksAndSummaries(books);
      logger.info("fillBookTable DONE.");
   }

}
