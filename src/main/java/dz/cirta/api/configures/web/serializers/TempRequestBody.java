package dz.cirta.api.configures.web.serializers;

import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @author Abdessamed Diab
 */
public class TempRequestBody implements Serializable {
   private String key;
   private String username;
   private String password;

   protected TempRequestBody() {
   }

   public TempRequestBody(final String key) {
      this.key = key;
   }

   public TempRequestBody(final String username, final String password) {
      this.username = username;
      this.password = password;
   }

   @Override
   public String toString() {
      return !StringUtils.isEmpty(key) ? "key: " + key : "username: " + username;
   }

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }
}
