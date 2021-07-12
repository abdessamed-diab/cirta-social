package dz.cirta.store.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dz.cirta.api.configures.web.converters.BookmarksConverter;
import dz.cirta.api.configures.web.serializers.ResourceToBase64;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Abdessamed Diab
 */
@Entity
@JsonSerialize(using = ResourceToBase64.class)
public class Book implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;

   // @Column(nullable = false)
   @Column
   private String title;

   @Column(nullable = false, updatable = false)
   private String coverPhotoUrl;

   transient private String coverPhotoBase64;

   @Column(nullable = false, updatable = false, unique = true)
   private String sourceUrl;

   // @Column(nullable = false) check books metadata please!
   @Column
   private String Author;

   // @Column(nullable = false)
   @Column
   private String publisher; // creator or producer

   @Column(nullable = false)
   private String subject;  // for whom this book is meant for. or domain

   @Column
   private LocalDateTime releaseDate;

   @Column
   private String keywords; // for search engines.

   @Column(length = 50000)
   @Convert(converter = BookmarksConverter.class)
   private Map<String, List<Bookmark>> bookAttributes;

   @OneToMany(cascade = {CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "book", fetch = FetchType.LAZY)
   private List<SummaryItem> summaryItems;

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getCoverPhotoUrl() {
      return coverPhotoUrl;
   }

   public void setCoverPhotoUrl(String coverPhotoUrl) {
      this.coverPhotoUrl = coverPhotoUrl;
   }

   public String getAuthor() {
      return Author;
   }

   public void setAuthor(String author) {
      Author = author;
   }

   public LocalDateTime getReleaseDate() {
      return releaseDate;
   }

   public void setReleaseDate(LocalDateTime releaseDate) {
      this.releaseDate = releaseDate;
   }

   public String getPublisher() {
      return publisher;
   }

   public void setPublisher(String publisher) {
      this.publisher = publisher;
   }

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getKeywords() {
      return keywords;
   }

   public void setKeywords(String keywords) {
      this.keywords = keywords;
   }

   public String getSourceUrl() {
      return sourceUrl;
   }

   public void setSourceUrl(String sourceUrl) {
      this.sourceUrl = sourceUrl;
   }

   public Map<String, List<Bookmark>> getBookAttributes() {
      return bookAttributes;
   }

   public void setBookAttributes(Map<String, List<Bookmark>> bookAttributes) {
      this.bookAttributes = bookAttributes;
   }

   public List<SummaryItem> getSummaryItems() {
      return summaryItems;
   }

   public void setSummaryItems(List<SummaryItem> summaryItems) {
      this.summaryItems = summaryItems;
   }

   public String getCoverPhotoBase64() {
      return coverPhotoBase64;
   }

   public void setCoverPhotoBase64(String coverPhotoBase64) {
      this.coverPhotoBase64 = coverPhotoBase64;
   }
}
