package dz.cirta.api.configures.web.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dz.cirta.store.models.Bookmark;

import javax.persistence.AttributeConverter;
import java.util.List;
import java.util.Map;

/**
 * @author Abdessamed Diab
 */
public class BookmarksConverter implements AttributeConverter<Map<String, List<Bookmark>>, String> {
   @Override
   public String convertToDatabaseColumn(Map<String, List<Bookmark>> bookmarks) {
      try {
         return new ObjectMapper().writeValueAsString(bookmarks);
      } catch (JsonProcessingException e) {
         e.printStackTrace();
      }

      return null;
   }

   @Override
   public Map<String, List<Bookmark>> convertToEntityAttribute(String jsonBookmarks) {
      try {
         TypeReference<Map<String, List<Bookmark>>> typeReference = new TypeReference<Map<String, List<Bookmark>>>() {
         };
         return new ObjectMapper().readValue(jsonBookmarks, typeReference);
      } catch (JsonProcessingException e) {
         e.printStackTrace();
      }

      return null;
   }
}
