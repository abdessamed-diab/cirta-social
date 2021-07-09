package dz.cirta.configures.web.serializers;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dz.cirta.data.models.Book;
import dz.cirta.tools.FileUtils;

import java.io.IOException;

public class ResourceToBase64 extends JsonSerializer<Book> {

   public ResourceToBase64() {
      super();
   }

   @Override
   public void serialize(Book book, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("id", book.getId());
      jsonGenerator.writeStringField("subject", book.getSubject());
      jsonGenerator.writeStringField("title", book.getTitle());
      jsonGenerator.writeStringField("author", book.getAuthor());
      jsonGenerator.writeObjectField("releaseDate", book.getReleaseDate());
      jsonGenerator.writeObjectField("sourceUrl", book.getSourceUrl());
      jsonGenerator.writeObjectField("coverPhotoUrl", book.getCoverPhotoUrl());

      String encodedFileContentToBase64 = FileUtils.ENCODE_FILE_CONTENT(book.getCoverPhotoUrl());
      jsonGenerator.writeStringField("coverPhotoBase64", encodedFileContentToBase64);
      jsonGenerator.writeEndObject();
   }

}
