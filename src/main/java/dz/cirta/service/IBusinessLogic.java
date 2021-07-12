package dz.cirta.service;

import dz.cirta.store.models.CirtaAuthority;

import java.util.List;
import java.util.Set;

/**
 * @author Abdessamed Diab
 */
public interface IBusinessLogic {

   Set<CirtaAuthority> findAuthoritiesIn();

   List<CirtaAuthority> findAllAuthoritiesByUserId(Long userId);

}
