package dz.cirta.data.models;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

@Entity
public class CirtaAuthority implements GrantedAuthority, Comparable<CirtaAuthority> {
   public enum AuthorityEnum {
      DEVELOPER("DEVELOPER"),
      TESTER("TESTER");

      public final String label;

      AuthorityEnum(String label) {
         this.label = label;
      }
   }

   @Id
   private int id;

   @Column(updatable = false, unique = true, nullable = false)
   private String authority;

   @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authorities")
   private Set<CirtaUser> users;

   protected CirtaAuthority() {
   }

   public CirtaAuthority(int id, String authority) {
      this.id = id;
      this.authority = authority;
   }

   @Override
   public String getAuthority() {
      return authority;
   }


   @Override
   public int compareTo(CirtaAuthority comparedTo) {
      return authority.compareTo(comparedTo.authority);
   }

   @Override
   public boolean equals(Object toObj) {
      if (toObj == null || !toObj.getClass().isAssignableFrom(CirtaAuthority.class)) {
         return false;
      }

      CirtaAuthority equalsTo = (CirtaAuthority) toObj;

      return Integer.valueOf(id).equals(equalsTo.id);
   }
}
