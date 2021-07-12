package dz.cirta.store.repo;

import dz.cirta.store.models.CirtaUser;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Abdessamed Diab
 */
public interface CirtaUsersRepository extends CrudRepository<CirtaUser, Long> {
   CirtaUser findFirstByNameAndPassword(String name, String password);

   CirtaUser findFirstByTempAuthentication_KeyAndTempAuthenticationNotNull(String tempAuthenticationKey);

   CirtaUser findFirstByFacebookId(String facebookId);
}
