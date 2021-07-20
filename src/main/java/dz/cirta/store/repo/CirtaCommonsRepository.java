package dz.cirta.store.repo;

import dz.cirta.store.models.*;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Abdessamed Diab
 */
@Repository
public class CirtaCommonsRepository {
   private static final Logger logger = LoggerFactory.getLogger(CirtaCommonsRepository.class);

   @Autowired
   private EntityManagerFactory entityManagerFactory;

   private Session getCurrentSession() {
      return entityManagerFactory.unwrap(SessionFactoryImplementor.class).getCurrentSession();
   }

   public Set<CirtaAuthority> findAuthoritiesIn(String ... args) {
      final Session hibernateSession = getCurrentSession();
      CriteriaQuery<CirtaAuthority> criteriaQuery = hibernateSession.getCriteriaBuilder().createQuery(CirtaAuthority.class);
      Root<CirtaAuthority> root = criteriaQuery.from(CirtaAuthority.class);
      criteriaQuery.select(root);
      criteriaQuery.where(root.get(CirtaAuthority_.authority).in(args));

      Stream<CirtaAuthority> streamOfCirtaAuthorities = hibernateSession.createQuery(criteriaQuery).stream();
      Set<CirtaAuthority> result = streamOfCirtaAuthorities.collect(Collectors.toSet());
      streamOfCirtaAuthorities.close();
      return result;
   }

   public List<CirtaAuthority> findAllAuthoritiesByUserId(Long userId) {
      return getCurrentSession()
            .createQuery("SELECT DISTINCT CA FROM CirtaAuthority CA JOIN FETCH CA.users US WHERE US.id =: userId")
            .setParameter("userId", userId)
            .getResultList();
   }

   public <T> Collection<T> findAll(Class<T> clazz) {
      final Session hibernateSession = getCurrentSession();
      CriteriaQuery<T> criteriaQuery = hibernateSession.getCriteriaBuilder().createQuery(clazz);
      Root<T> root = criteriaQuery.from(clazz);
      criteriaQuery.select(root);

      return hibernateSession.createQuery(criteriaQuery).getResultList();
   }

   public <T> Optional<T> findOptionalById(Class<T> type, String idPropertyName,long primaryKey, String ... fetchPropertiesNames) {
      final Session hibernateSession = getCurrentSession();
      CriteriaBuilder criteriaBuilder = hibernateSession.getCriteriaBuilder();
      CriteriaQuery<T> criteriaQuery= criteriaBuilder.createQuery(type);

      Root<T> root = criteriaQuery.from(type);
      List.of(fetchPropertiesNames).forEach(fetchPropertyName -> {
         root.fetch(fetchPropertyName, JoinType.LEFT);
      });

      criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(idPropertyName), primaryKey));
      return hibernateSession.createQuery(criteriaQuery).uniqueResultOptional();
   }

   public <T> Optional<T> findOptionalById(Class<T> type, String idPropertyName,long primaryKey) {
      final Session hibernateSession = getCurrentSession();
      CriteriaBuilder criteriaBuilder = hibernateSession.getCriteriaBuilder();
      CriteriaQuery<T> criteriaQuery= criteriaBuilder.createQuery(type);

      Root<T> root = criteriaQuery.from(type);
      criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(idPropertyName), primaryKey));

      return hibernateSession.createQuery(criteriaQuery).uniqueResultOptional();
   }

   public <T> T findById(Class<T> type, String idPropertyName,long primaryKey) throws IllegalArgumentException {
      Optional<T> optionalResult = findOptionalById(type, idPropertyName, primaryKey);

      if (!optionalResult.isPresent()) {
         throw new IllegalArgumentException("no "+type.getSimpleName() +" was found whose primary key equal: "+primaryKey);
      }

      return optionalResult.orElse(null);
   }

   @Deprecated(forRemoval = true, since = "2.0")
   public <T> T findByReference(Class<T> type, long primaryKey) {
      return getCurrentSession().getReference(type, primaryKey);
   }

   public <T> T findUniqByPropertyNameAndValue(Class<T> clazz, String propertyName, String propertyValue) {
      final Session hibernateSession = getCurrentSession();
      CriteriaQuery<T> criteriaQuery = hibernateSession.getCriteriaBuilder().createQuery(clazz);
      CriteriaBuilder criteriaBuilder = hibernateSession.getCriteriaBuilder();
      Root<T> root = criteriaQuery.from(clazz);
      criteriaQuery = criteriaQuery.select(root).where(
            criteriaBuilder.equal(root.get(propertyName), propertyValue)
      );

      return hibernateSession.createQuery(criteriaQuery).uniqueResult();
   }


   public <T extends Serializable> Serializable save(T reference) {
      Session session =entityManagerFactory.unwrap(SessionFactoryImplementor.class).getCurrentSession();
      return session.save(reference);
   }

   public <T extends Serializable> void saveOrUpdate(T reference) {
      getCurrentSession().saveOrUpdate(reference);
   }

   public <T> Query<T> createQuery(String hqlQuery, Class<T> clazz) {
      return getCurrentSession().createQuery(hqlQuery, clazz);
   }

   public void update(Serializable serializable) {
      Session hibernateSession = getCurrentSession();
      hibernateSession.update(serializable);
   }

}
