package dz.cirta.store.models;

import dz.cirta.service.BusinessLogic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Abdessamed Diab
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=dev")
class CirtaUserTest {

    @Autowired
    private BusinessLogic businessLogic;

    @Test
    public void testSave() {
        // test #1
        CirtaUser cirtaUser = new CirtaUser("what", "what", "what", "this is my name");
        businessLogic.save(cirtaUser);
        assertNotNull(cirtaUser.getId());

        // test #2
        CirtaUser cirtaUser2 = new CirtaUser("what2", "what", "what", "this is my name2");
        Set<CirtaAuthority> authoritiesUsingSqlNativeQuery = businessLogic.cirtaEndUserAuthorities();
        Set<CirtaAuthority> authoritiesUsingCriteriaQuery = businessLogic.findAuthoritiesIn();

        assumeTrue(authoritiesUsingSqlNativeQuery.size() ==  authoritiesUsingCriteriaQuery.size());

        cirtaUser2.setAuthorities(
              authoritiesUsingCriteriaQuery
        );

        businessLogic.save(cirtaUser2);
        assertTrue(!cirtaUser2.getAuthorities().isEmpty());
    }

    @Test
    public void testSaveUpdateTempAuth() {
        // test #1
        TempAuthentication tempAuthentication = new TempAuthentication("keyXXXXXXX");
        CirtaUser cirtaUser = new CirtaUser("what the hell", "what", "what", "what");
        cirtaUser.setTempAuthentication(tempAuthentication);
        businessLogic.save(cirtaUser);
        assertEquals(tempAuthentication.getKey(), cirtaUser.getTempAuthentication().getKey());

        // test #2
        cirtaUser = Optional.of(
                businessLogic.findUserByFacebookId(cirtaUser.getFacebookId())
        ).orElse(
                new CirtaUser("what the hell", "what", "what", "what")
        );
        TempAuthentication tempAuthentication_2 = new TempAuthentication("KeySecond");
        cirtaUser.setTempAuthentication(tempAuthentication_2);
        businessLogic.saveOrUpdate(cirtaUser);
        assertEquals(tempAuthentication_2.getKey(), cirtaUser.getTempAuthentication().getKey());
    }

    @Test
    public void testFindFirstByTempAuthentication_Key() {
        // test #1
        TempAuthentication tempAuthentication = new TempAuthentication("find first by temp key");
        CirtaUser cirtaUser = new CirtaUser("find first by temp key", "what", "what", "what ths is ");
        cirtaUser.setTempAuthentication(tempAuthentication);
        businessLogic.save(cirtaUser);
        assertEquals("find first by temp key",
              businessLogic.findUserByTempAuthenticationKey(
                    tempAuthentication.getKey()).getFacebookId()
        );

    }

    @Test
    public void testFindAllByAuthorityIn() {
        // test #1
        CirtaUser cirtaUser = new CirtaUser("what find All By Authority In", "what", "what", "what what goes around");
        Set<CirtaAuthority> authoritiesUsingNativeSqlQuery = businessLogic.cirtaEndUserAuthorities();
        Set<CirtaAuthority> authoritiesUsingCriteriaQuery = businessLogic.findAuthoritiesIn();

        assumeTrue(authoritiesUsingNativeSqlQuery.size() == authoritiesUsingCriteriaQuery.size());

        cirtaUser.setAuthorities(
              authoritiesUsingCriteriaQuery
        );

        businessLogic.save(cirtaUser);
        Set<CirtaAuthority> authoritiesByUserIdNativeSql = businessLogic.findAllByUser(cirtaUser.getId());
        List<CirtaAuthority> authoritiesByUserIdHqlQuery = businessLogic.findAllAuthoritiesByUserId(cirtaUser.getId());

        assumeTrue(authoritiesByUserIdNativeSql.size() == authoritiesByUserIdHqlQuery.size());

        Optional<CirtaAuthority> result = authoritiesByUserIdHqlQuery.stream()
                .filter(authority -> authority.getAuthority().equals("TESTER"))
                .findFirst();

        assertTrue(result.isPresent());
    }

    @Test
    public void dropTempAuthentication() {
        // test #1
        TempAuthentication tempAuthentication = new TempAuthentication("key to drop");
        CirtaUser cirtaUser = new CirtaUser("drop temp auth", "what", "what", "what last");
        cirtaUser.setTempAuthentication(tempAuthentication);
        businessLogic.saveOrUpdate(cirtaUser);
        cirtaUser.setTempAuthentication(null); // remove association with temp auth
        businessLogic.saveOrUpdate(cirtaUser);
        assertNull(businessLogic.findOptionalById(CirtaUser.class, CirtaUser_.ID, cirtaUser.getId()).get().getTempAuthentication());
        assertNull(businessLogic.findUserByTempAuthenticationKey(tempAuthentication.getKey()));
    }
}
