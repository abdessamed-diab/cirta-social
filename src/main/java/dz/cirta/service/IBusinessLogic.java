package dz.cirta.service;

import dz.cirta.store.models.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Abdessamed Diab
 */
public interface IBusinessLogic {

   Set<CirtaAuthority> findAuthoritiesIn(String ... values);

   List<CirtaAuthority> findAllAuthoritiesByUserId(Long userId);

   <T extends Serializable> List<T> findAllByClass(Class<T> type);

   <T> Optional<T> findOptionalById(Class<T> type, String idPropertyName, long primaryKey);

   <T> Optional<T> findFetchOptionalById(Class<T> type, String idPropertyName, long primaryKey, String ... fetchPropertyName) throws IllegalArgumentException;

   <T> T findById(Class<T> type, String idPropertyName,long primaryKey) throws IllegalArgumentException;

   @Deprecated(forRemoval = true, since = "2.0")
   <T> T loadFromLocalCache(Class<T> type, long primaryKey);

   boolean save(Serializable reference);

   void saveOrUpdate(Serializable reference);

   Collection<Comment> findParentCommentsByGivenBookCoordinates(long bookId, int pageNumber) ;

   CirtaUser findUserByNameAndPassword(String name, String password);

   CirtaUser findUserByFacebookId(String facebookId);

   CirtaUser findUserByTempAuthenticationKey(String tempAuthenticationKey);

   Set<Notification> fetchNotificationsByUsernameAndMinDate(String userName, LocalDateTime localDateTime);

   int countNotifications(String userName, LocalDateTime minDate, String notificationType);

   void update(Serializable serializable);

   <T extends Serializable> Collection<T> findByQueryAndParams(Class<T> clazz, Map<String, Object> params, String hqlQuery);

}
