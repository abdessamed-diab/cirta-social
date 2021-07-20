package dz.cirta.store.models;

import dz.cirta.service.IBusinessLogic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class CirtaEnumAuthorityTest {

    @Autowired
    private IBusinessLogic businessLogic;

    @Test
    public void testSaveUserWithAuthorities() {
        CirtaUser cirtaUser = new CirtaUser();
        cirtaUser.setUserName("mechel");
        cirtaUser.setPassword("mechel");
        cirtaUser.setFirstName("Abdessamed");
        cirtaUser.setLastName("DIAB");



        Set<CirtaAuthority> authorities = businessLogic.findAuthoritiesIn(CirtaAuthority.AuthorityEnum.DEVELOPER.label, CirtaAuthority.AuthorityEnum.TESTER.label);
        cirtaUser.setAuthorities(authorities);
        businessLogic.save(cirtaUser);

        cirtaUser.setAuthorities(authorities);

        businessLogic.saveOrUpdate(cirtaUser);

        assertEquals("DEVELOPER",
                businessLogic.findUserByNameAndPassword(cirtaUser.getUsername(), cirtaUser.getPassword())
                .getAuthorities().stream().filter(
                                (GrantedAuthority cirtaAuthority) -> cirtaAuthority.getAuthority().equals("DEVELOPER")
                        ).findFirst().get().getAuthority()
        );
    }

    @Test
    public void testFindFirstByNameAndPassword() {
        CirtaUser cirtaUser = new CirtaUser();
        cirtaUser.setUserName("OCD");
        cirtaUser.setPassword("mechel178");
        cirtaUser.setFirstName("Abdessamed");
        cirtaUser.setLastName("DIAB");

        businessLogic.save(cirtaUser);

        CirtaUser result = businessLogic.findUserByNameAndPassword(cirtaUser.getUsername(), "what the hell");

        assertNull(result);

        result = businessLogic.findUserByNameAndPassword(cirtaUser.getUsername(), cirtaUser.getPassword());

        assertEquals(cirtaUser.getPassword(), result.getPassword());
    }

}
