package dz.cirta.service;

import dz.cirta.store.models.*;
import dz.cirta.store.repo.CirtaCommonsRepository;
import dz.cirta.store.tools.PdfStreamApi;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abdessamed Diab
 */
@Service
public class BusinessLogic implements IBusinessLogic, InitializingBean {
   private static final Logger logger = LoggerFactory.getLogger(BusinessLogic.class);

   @Value("${dz.cirta.app.volume}")
   private String volumeBaseDir;

   @Autowired
   private CirtaCommonsRepository cirtaCommonsRepository;

   @Autowired
   private Session hibernateSession;

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
   public Set<CirtaAuthority> findAuthoritiesIn(String ... values) {
      if (values.length > 0) {
         return cirtaCommonsRepository.findAuthoritiesIn(values);
      } else {
         return cirtaCommonsRepository.findAuthoritiesIn(
               CirtaAuthority.AuthorityEnum.DEVELOPER.label,
               CirtaAuthority.AuthorityEnum.TESTER.label
         );
      }
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

   @Override
   @Transactional(readOnly = true)
   public <T extends Serializable> List<T> findAllByClass(Class<T> type) {
      return (List<T>) cirtaCommonsRepository.findAll(type);
   }

   @Override
   @Transactional(readOnly = true)
   public <T> Optional<T> findOptionalById(Class<T> type, String idPropertyName, long primaryKey) {
      return cirtaCommonsRepository.findOptionalById(type, idPropertyName, primaryKey);
   }

   @Override
   @Transactional(readOnly = true)
   public <T> T findById(Class<T> type, String idPropertyName, long primaryKey) throws IllegalArgumentException {
      return cirtaCommonsRepository.findById(type, idPropertyName, primaryKey);
   }

   @Override
   @Transactional(readOnly = true)
   public <T> Optional<T> findFetchOptionalById(Class<T> type, String idPropertyName, long primaryKey, String ... fetchPropertyName) throws IllegalArgumentException {
      return cirtaCommonsRepository.findOptionalById(type, idPropertyName, primaryKey, fetchPropertyName);
   }

   @Override
   @Transactional(readOnly = true)
   public <T> T loadFromLocalCache(Class<T> type, long primaryKey) {
      return cirtaCommonsRepository.findByReference(type, primaryKey);
   }

   @Override
   @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "hibernateTransactionManager")
   public boolean save(Serializable reference) {
      Serializable result = cirtaCommonsRepository.save(reference);
      return result != null;
   }

   @Override
   @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "hibernateTransactionManager")
   public void saveOrUpdate(Serializable reference) {
      cirtaCommonsRepository.saveOrUpdate(reference);
   }

   @Override
   @Transactional(readOnly = true)
   public Collection<Comment> findParentCommentsByGivenBookCoordinates(long bookId, int pageNumber) {
      String hqlQuery = "select cs from Comment cs LEFT JOIN FETCH cs.replies R " +
                        "where cs.book.id = :book and cs.pageNumber = :pageNumber " +
                              "AND cs.parent = TRUE " +
                        "ORDER BY cs.publishedAt DESC";
      return cirtaCommonsRepository.createQuery(hqlQuery, Comment.class)
            .setParameter("book", bookId)
            .setParameter("pageNumber", pageNumber)
            .getResultList();
   }

   @Override
   @Transactional(readOnly = true)
   @Deprecated(forRemoval = true, since = "2.0")
   public CirtaUser findUserByNameAndPassword(String name, String password) {
      CriteriaQuery<CirtaUser> criteriaQuery = hibernateSession.getCriteriaBuilder().createQuery(CirtaUser.class);
      CriteriaBuilder criteriaBuilder = hibernateSession.getCriteriaBuilder();
      Root<CirtaUser> root = criteriaQuery.from(CirtaUser.class);
      criteriaQuery = criteriaQuery.select(root).where(
         criteriaBuilder.and(
            criteriaBuilder.equal(root.get(CirtaUser_.name), name),
            criteriaBuilder.equal(root.get(CirtaUser_.password), password)
         )
      );

      return hibernateSession.createQuery(criteriaQuery).uniqueResult();
   }

   @Override
   @Transactional(readOnly = true)
   public CirtaUser findUserByFacebookId(String facebookId) {
      return cirtaCommonsRepository.findUniqByPropertyNameAndValue(CirtaUser.class, CirtaUser_.FACEBOOK_ID, facebookId);
   }

   @Override
   @Transactional(readOnly = true)
   public CirtaUser findUserByTempAuthenticationKey(String tempAuthenticationKey) {
      String hqlQuery = "SELECT CU FROM CirtaUser CU where CU.tempAuthentication.key = :key";
      return cirtaCommonsRepository.createQuery(hqlQuery, CirtaUser.class)
            .setParameter("key", tempAuthenticationKey)
            .uniqueResultOptional()
            .orElse(null);
   }

   @Override
   @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "hibernateTransactionManager")
   public void update(Serializable serializable) {
      cirtaCommonsRepository.update(serializable);
   }

   @Override
   @Transactional(readOnly = true)
   public Set<Notification> fetchNotificationsByUsernameAndMinDate(String userName, LocalDateTime minDate) {
      String hqlQuery =    "SELECT n FROM Notification n LEFT JOIN FETCH n.book B " +
                           "WHERE n.reply.parentComment.author.name = :username " +
                              "AND n.reply.author.name != :username " +
                              "AND n.reply.publishedAt > :minDate   " +
                           "ORDER BY n.reply.publishedAt DESC    ";

      Stream<Notification> stream = cirtaCommonsRepository.createQuery(hqlQuery, Notification.class)
            .setParameter("username", userName)
            .setParameter("minDate", minDate).stream();

      Set<Notification> result = stream.collect(Collectors.toSet());

      stream.close();
      return result;
   }

   @Override
   @Transactional(readOnly = true)
   public int countNotifications(String userName, LocalDateTime minDate, String notificationType) {
      String hqlQuery =    "SELECT n.id FROM Notification n " +
                           "WHERE n.reply.parentComment.author.name = :username " +
                              "AND n.reply.author.name != :username " +
                              "AND n.reply.publishedAt > :minDate " +
                              "AND n.type = :notificationType " +
                           "ORDER BY n.reply.publishedAt DESC";

      return cirtaCommonsRepository.createQuery(hqlQuery, Long.class)
            .setParameter("username", userName)
            .setParameter("minDate", minDate)
            .setParameter("notificationType", notificationType)
            .getResultList()
            .size();
   }

   @Override
   @Transactional(readOnly = true)
   public <T extends Serializable> Collection<T> findByQueryAndParams(Class<T> clazz, Map<String, Object> params, String hqlQuery) {
      Query<T> query = cirtaCommonsRepository.createQuery(hqlQuery, clazz);

      params.keySet().forEach(key -> {
         query.setParameter(key, params.get(key));
      });

      return query.getResultList();
   }

   /**
    * update summary data, hibernate search framework take care of updating elasticSearch cluster.
    * since we have a free bonsai.io cluster account, we need to push one single row at a time.
    * this operation will be launched once deployment is scheduled.
    * @return true if all books with there respective summaries were saved successfully.
    */
   private boolean loadBooksWithSummariesToElasticSearchCluster() throws IOException {
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
