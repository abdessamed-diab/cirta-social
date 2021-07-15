package dz.cirta.api.configures.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import dz.cirta.store.models.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Abdessamed Diab
 */
@Configuration
public class WebConfigurer {
   public static final Locale dzLocale = new Locale("ar", "DZ"); // read from right to left.
   public static final Locale ukLocale = Locale.UK;
   public static final DateTimeFormatter dzDateTimeFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy HH:mm")
         .withLocale(dzLocale)
         .withDecimalStyle(DecimalStyle.of(dzLocale))
         .withZone(TimeZone.getTimeZone("Central European Time").toZoneId());

   public static final DateTimeFormatter ukDateTimeFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy HH:mm")
         .withLocale(ukLocale)
         .withDecimalStyle(DecimalStyle.of(ukLocale))
         .withZone(TimeZone.getTimeZone("Central European Time").toZoneId());


   @Autowired
   private ConcurrentTaskExecutor concurrentTaskExecutor;

   @Bean
   public WebMvcConfigurer webMvcConfigurer() {
      return new WebMvcConfigurer() {

         @Override
         public void configureViewResolvers(ViewResolverRegistry registry) {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix("/public/pages/");
            resolver.setSuffix(".html");
            resolver.setCache(false);
            registry.viewResolver(resolver);
         }

         @Override
         public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/").setViewName("index");
            registry.addViewController("/privacy_policy").setViewName("privacy_policy");
            registry.addViewController("/terms_conditions").setViewName("terms_conditions");
         }

         @Override
         public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/doc/**")
                  .addResourceLocations("classpath:/public/pages/doc/")
                  .resourceChain(true)
                  .addResolver(new PathResourceResolver());
         }

         @Override
         public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(0, myMappingJackson2HttpMessageConverter());
         }

         @Override
         public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
            configurer.setTaskExecutor(concurrentTaskExecutor);
         }

         // enable matrix variables.
         @Override
         public void configurePathMatch(PathMatchConfigurer configurer) {
            UrlPathHelper pathHelper = new UrlPathHelper();
            pathHelper.setRemoveSemicolonContent(false);
            configurer.setUrlPathHelper(pathHelper);
         }
      };
   }

   @Bean(name = "myMappingJackson2HttpMessageConverter")
   public MappingJackson2HttpMessageConverter myMappingJackson2HttpMessageConverter() {
      DateTimeFormatter dzDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm yyyy EEEE dd MMMM")
            .withLocale(dzLocale)
            .withDecimalStyle(DecimalStyle.of(dzLocale))
            .withZone(TimeZone.getTimeZone("Central European Time").toZoneId());

      Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
      builder.timeZone(TimeZone.getTimeZone("UTC+1"))
            .indentOutput(true)
            .failOnEmptyBeans(true)
            .serializers(
                  new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd", dzLocale)),
                  new LocalDateTimeSerializer(dzDateTimeFormatter)
            ).deserializers(
            new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd", dzLocale)),
            new LocalDateTimeDeserializer(dzDateTimeFormatter)
      )//.serializerByType(Book.class, new BookSerializer())
            //.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) disabled by default.
            .modulesToInstall(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());

      return new MappingJackson2HttpMessageConverter(builder.build());
   }

   private class BookSerializer extends JsonSerializer<Book> {
      public BookSerializer() {
         super();
      }

      @Override
      public void serialize(Book book, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      }
   }

}
