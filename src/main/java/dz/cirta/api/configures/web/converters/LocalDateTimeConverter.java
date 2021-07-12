package dz.cirta.api.configures.web.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Abdessamed Diab
 */
@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {
   public static final DateTimeFormatter dataBaseLocalDateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
   private static final Logger logger = LoggerFactory.getLogger(LocalDateTimeConverter.class);

   @Override
   public String convertToDatabaseColumn(LocalDateTime localDateTime) {
      String str = dataBaseLocalDateTimePattern.format(localDateTime);
      return str;
   }

   @Override
   public LocalDateTime convertToEntityAttribute(String strDate) {
      LocalDateTime result = LocalDateTime.parse(strDate, dataBaseLocalDateTimePattern);
      return result;
   }
}
