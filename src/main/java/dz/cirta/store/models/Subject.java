package dz.cirta.store.models;

import java.io.Serializable;

public interface Subject extends Serializable {
   enum TYPES {
      BOOK("book"),
      MOVIE("movie");

      String type;
      TYPES(String value) {
         type = value;
      }
   }

   long getId();

   String type();

}
