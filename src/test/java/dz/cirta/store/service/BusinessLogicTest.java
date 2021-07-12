package dz.cirta.store.service;

import dz.cirta.store.models.CirtaAuthority;
import dz.cirta.service.IBusinessLogic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(
      webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
      properties = "spring.profiles.active=dev"
)
class BusinessLogicTest {

   @Autowired
   private IBusinessLogic businessLogic;

   @Test
   void testFindAuthoritiesIn() {

      Set<CirtaAuthority> authorities = businessLogic.findAuthoritiesIn();

      assertTrue(!authorities.isEmpty());

   }

   @Test
   void testFindAllAuthoritiesByUserId() {
      List<CirtaAuthority> authorities = businessLogic.findAllAuthoritiesByUserId(5L);

      assertTrue(authorities.isEmpty());
   }
}
