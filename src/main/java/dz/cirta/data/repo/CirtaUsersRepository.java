package dz.cirta.data.repo;

import dz.cirta.data.models.CirtaUser;
import org.springframework.data.repository.CrudRepository;

public interface CirtaUsersRepository extends CrudRepository<CirtaUser, Long> {
   CirtaUser findFirstByNameAndPassword(String name, String password);

   CirtaUser findFirstByTempAuthentication_KeyAndTempAuthenticationNotNull(String tempAuthenticationKey);

   CirtaUser findFirstByFacebookId(String facebookId);
}
