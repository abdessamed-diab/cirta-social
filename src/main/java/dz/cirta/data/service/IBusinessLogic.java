package dz.cirta.data.service;

import dz.cirta.data.models.CirtaAuthority;

import java.util.List;
import java.util.Set;

public interface IBusinessLogic {

   Set<CirtaAuthority> findAuthoritiesIn();

   List<CirtaAuthority> findAllAuthoritiesByUserId(Long userId);

}
