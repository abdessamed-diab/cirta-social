package dz.cirta.store.models;

import dz.cirta.store.repo.CirtaUsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class CirtaEnumAuthorityTest {

    @Autowired
    private CirtaUsersRepository cirtaUsersRepository;

    @Autowired
    private EntityManager hibernateSession;

    @Test
    public void testSaveUserWithAuthorities() {
        CirtaUser cirtaUser = new CirtaUser();
        cirtaUser.setUserName("mechel");
        cirtaUser.setPassword("mechel");
        cirtaUser.setFirstName("Abdessamed");
        cirtaUser.setLastName("DIAB");

        hibernateSession.getTransaction().begin();
        List<CirtaAuthority> authorities = hibernateSession
                .createNativeQuery("select * from cirta_authority where authority in ('DEVELOPER', 'TESTER')", CirtaAuthority.class)
                .getResultList();
        cirtaUser.setAuthorities(new TreeSet<>(authorities));
        hibernateSession.persist(cirtaUser);
        hibernateSession.getTransaction().commit();

        cirtaUser.setAuthorities(new TreeSet<>(authorities));

        cirtaUser = cirtaUsersRepository.save(cirtaUser);

        assertEquals("DEVELOPER",
                cirtaUsersRepository.findFirstByNameAndPassword(cirtaUser.getUsername(), cirtaUser.getPassword())
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

        cirtaUser = cirtaUsersRepository.save(cirtaUser);

        CirtaUser result = cirtaUsersRepository.findFirstByNameAndPassword(cirtaUser.getUsername(), "what the hell");

        assertNull(result);

        result = cirtaUsersRepository.findFirstByNameAndPassword(cirtaUser.getUsername(), cirtaUser.getPassword());

        assertEquals(cirtaUser.getPassword(), result.getPassword());
    }

}
