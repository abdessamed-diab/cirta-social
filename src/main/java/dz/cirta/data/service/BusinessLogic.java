package dz.cirta.data.service;

import dz.cirta.data.models.Book;
import dz.cirta.data.models.CirtaAuthority;
import dz.cirta.data.repo.CirtaCommonsRepository;
import dz.cirta.tools.PdfStreamApi;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class BusinessLogic implements IBusinessLogic, InitializingBean {
   private static final Logger logger = LoggerFactory.getLogger(BusinessLogic.class);

   @Value("${dz.cirta.app.volume}")
   private String volumeBaseDir;

   @Autowired
   private Session hibernateSession;

   @Autowired
   private CirtaCommonsRepository cirtaCommonsRepository;

   @Override
   public void afterPropertiesSet() {
      try {
         logger.info("start loading summaries to elastic search cluster...");
         loadBooksWithSummariesToElasticSearchCluster();
      } catch (IOException ex) {
         logger.warn("can't load summaries to elastic search cluster.");
         ex.printStackTrace();
      }
   }

   @Deprecated(since = "2.0", forRemoval = true)
   @Transactional(readOnly = true)
   public Set<CirtaAuthority> cirtaEndUserAuthorities() {
      List<CirtaAuthority> authorities = hibernateSession
            .createNativeQuery("select * from cirta_authority where authority in ('DEVELOPER', 'TESTER')", CirtaAuthority.class)
            .getResultList();

      return new TreeSet<>(authorities);
   }

   @Override
   @Transactional(readOnly = true)
   public Set<CirtaAuthority> findAuthoritiesIn() {
      return cirtaCommonsRepository.findAuthoritiesIn(
            CirtaAuthority.AuthorityEnum.DEVELOPER.label,
            CirtaAuthority.AuthorityEnum.TESTER.label
      );
   }

   @Deprecated(since = "2.0", forRemoval = true)
   @Transactional(readOnly = true)
   public Set<CirtaAuthority> findAllByUser(Long userId) {
      List<CirtaAuthority> authorities = hibernateSession
            .createNativeQuery("select cirta_authority.id, cirta_authority.authority from cirta_authority " +
                  "inner join user_authority on cirta_authority.id = user_authority.cirta_authority_id " +
                  "and  user_authority.cirta_user_id = ?", CirtaAuthority.class)
            .setParameter(1, userId)
            .getResultList();

      return new TreeSet<>(authorities);
   }

   @Override
   @Transactional(readOnly = true)
   public List<CirtaAuthority> findAllAuthoritiesByUserId(Long userId) {
      return cirtaCommonsRepository.findAllAuthoritiesByUserId(userId);
   }

   /**
    * update summary data, hibernate search framework take care of updating elasticSearch cluster.
    * since we have a free bonsai.io cluster account, we need to push one single row at a time.
    * this operation will be launched once deployment is scheduled.
    * @return true if all books with there respective summaries were saved successfully.
    */
   private boolean loadBooksWithSummariesToElasticSearchCluster() throws IOException {
      // check elastic search limit of queries per second.
      String pdfDocumentDirectory = volumeBaseDir + "books/";
      String coverPhotoFileNameMappingSourceFileUrl = volumeBaseDir + "books/cover_photo_file_name_mapping.txt";

      List<Book> books = PdfStreamApi.initializeBookProperties(pdfDocumentDirectory,
            coverPhotoFileNameMappingSourceFileUrl);

      books.stream().forEach(
            book -> {
               hibernateSession.getTransaction().begin();
               hibernateSession.persist(book);
               hibernateSession.getTransaction().commit();
            }
      );

      books.stream().forEach(
            book -> {
               book.getSummaryItems().stream().filter(
                     summaryItem -> summaryItem.parent == null
               ).forEach(
                     summaryItem -> {
                        hibernateSession.getTransaction().begin();
                        hibernateSession.persist(summaryItem);
                        hibernateSession.getTransaction().commit();
                     }
               );
            }
      );

      books.stream().forEach(
            book -> {
               book.getSummaryItems().stream().filter(
                     summaryItem -> summaryItem.parent != null
               ).forEach(
                     summaryItem -> {
                        hibernateSession.getTransaction().begin();
                        hibernateSession.persist(summaryItem);
                        hibernateSession.getTransaction().commit();
                     }
               );
            }
      );

      hibernateSession.clear();
      return true;
   }
}
