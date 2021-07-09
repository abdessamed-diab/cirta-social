package dz.cirta.data.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class TempAuthentication implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;

   @Column(nullable = false, unique = true, updatable = true)
   private String key;

   protected TempAuthentication() {
   }

   public TempAuthentication(String key) {
      this.key = key;
   }

   public long getId() {
      return id;
   }

   public String getKey() {
      return key;
   }

}
