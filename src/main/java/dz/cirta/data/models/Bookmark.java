package dz.cirta.data.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class Bookmark implements Comparable<Bookmark>, Serializable {
   public String title;
   public int page;

   public Bookmark() {
   }

   public Bookmark(String title) {
      this.title = title;
   }

   @Override
   public int compareTo(Bookmark o) {
      return this.title.toLowerCase().compareTo(o.title.toLowerCase());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null || !obj.getClass().isAssignableFrom(Bookmark.class)) {
         return false;
      }

      Bookmark comparedTo = (Bookmark) obj;
      return comparedTo.title.toLowerCase().contains(this.title);
   }
}
