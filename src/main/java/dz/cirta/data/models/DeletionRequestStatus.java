package dz.cirta.data.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class DeletionRequestStatus implements Serializable {

   @Id
   private String userId;

   @Column(updatable = false)
   private String url;

   @Column(updatable = false)
   private String confirmationCode;

   protected DeletionRequestStatus() {
   }

   public DeletionRequestStatus(String url, String confirmationCode) {
      this.url = url;
      this.confirmationCode = confirmationCode;
   }

   public DeletionRequestStatus(String userId, String url, String confirmationCode) {
      this(url, confirmationCode);
      this.userId = userId;
   }

   public String getUrl() {
      return url;
   }

   public String getConfirmationCode() {
      return confirmationCode;
   }
}
