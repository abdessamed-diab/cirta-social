package dz.cirta.api.configures.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dz.cirta.api.configures.web.WebConfigurer;
import dz.cirta.store.models.CirtaUser;
import dz.cirta.store.models.Comment;
import dz.cirta.api.SearchController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * @author Abdessamed Diab
 */
public class CommentSerializer extends JsonSerializer<Comment> {

   @Override
   public void serialize(Comment comment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

      final int userLanguage;
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof CirtaUser) {
         userLanguage = ((CirtaUser) authentication.getPrincipal()).getLanguage();
      } else {
         userLanguage = 1;
      }

      jsonGenerator.writeStartObject();

      jsonGenerator.writeObjectField("id", comment.getId());
      // TODO you can leave only cirta User id field.
      jsonGenerator.writeObjectField("author", comment.getAuthor());

      jsonGenerator.writeObjectField("userProfile",
            new SearchController.UserProfile(
                  comment.getAuthor().getUsername(),
                  comment.getAuthor().getProfileImage()
            )
      );

      jsonGenerator.writeObjectField("bookId", comment.getBook().getId());
      jsonGenerator.writeNumberField("pageNumber", comment.getPageNumber());
      jsonGenerator.writeStringField("content", comment.getContent());
      jsonGenerator.writeStringField("publishedAt",
            comment.getPublishedAt().format(
                  userLanguage == 1 ? WebConfigurer.ukDateTimeFormatter : WebConfigurer.dzDateTimeFormatter
            )
      );
      jsonGenerator.writeStringField("badge", comment.getBadge());
      jsonGenerator.writeNumberField("tempId", comment.tempId);

      if (comment.isParent() && comment.getReplies() != null && !comment.getReplies().isEmpty()) {

         jsonGenerator.writeFieldName("replies");
         jsonGenerator.writeStartArray();
         comment.getReplies().forEach(
               reply -> {
                  try {
                     jsonGenerator.writeStartObject();
                     jsonGenerator.writeObjectField("id", reply.getId());
                     jsonGenerator.writeObjectField("author", reply.getAuthor());
                     jsonGenerator.writeObjectField("userProfile",
                           new SearchController.UserProfile(
                                 reply.getAuthor().getUsername(),
                                 reply.getAuthor().getProfileImage()
                           )
                     );
                     jsonGenerator.writeStringField("content", reply.getContent());
                     jsonGenerator.writeStringField("publishedAt",
                           reply.getPublishedAt().format(
                                 userLanguage == 1 ? WebConfigurer.ukDateTimeFormatter : WebConfigurer.dzDateTimeFormatter
                           )
                     );

                     jsonGenerator.writeStringField("badge", reply.getBadge());
                     jsonGenerator.writeNumberField("tempId", comment.tempId);

                     jsonGenerator.writeEndObject();
                  } catch (IOException e) {
                     e.printStackTrace();
                  }
               }
         );

         jsonGenerator.writeEndArray();
      }

      jsonGenerator.writeEndObject();
   }
}
