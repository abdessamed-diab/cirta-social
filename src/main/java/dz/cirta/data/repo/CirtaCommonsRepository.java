package dz.cirta.data.repo;
// https://www.baeldung.com/hibernate-criteria-queries
// https://docs.jboss.org/hibernate/orm/6.0/javadocs/

import dz.cirta.data.models.CirtaAuthority;
import dz.cirta.data.models.CirtaAuthority_;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CirtaCommonsRepository {

   @Autowired
   public Session hibernateSession;

   public Set<CirtaAuthority> findAuthoritiesIn(String ... args) {
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
      return hibernateSession
            .createQuery("SELECT DISTINCT CA FROM CirtaAuthority CA JOIN FETCH CA.users US WHERE US.id =: userId")
            .setParameter("userId", userId)
            .getResultList();
   }
}
