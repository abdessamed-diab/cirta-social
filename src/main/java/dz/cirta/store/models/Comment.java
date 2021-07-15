package dz.cirta.store.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dz.cirta.api.configures.web.converters.LocalDateTimeConverter;
import dz.cirta.api.configures.web.serializers.CommentSerializer;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Abdessamed Diab
 */
@Entity
@JsonSerialize(using = CommentSerializer.class)
public class Comment implements Serializable, Comparable<Comment> {

   public enum BADGES {
      BADGE_LIGHT("badge-light"),
      BADGE_BLUE("badge-primary"),
      BADGE_DARK("badge-dark");

      public String value;

      BADGES(String value) {
         this.value = value;
      }
   }

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;

   @ManyToOne(optional = false, fetch = FetchType.EAGER)
   @JoinColumn(name = "comment_user", referencedColumnName = "id")
   private CirtaUser author;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "book_id")
   private Book book;

   @Column(nullable = false)
   private int pageNumber;

   @Column(nullable = false)
   private String content;

   @Column(nullable = false)
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE dd MMMM yyyy HH:mm")
   @Convert(converter = LocalDateTimeConverter.class)
   private LocalDateTime publishedAt;

   @Column
   private String badge;

   @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentComment", orphanRemoval = true, cascade = CascadeType.ALL)
   @OrderBy(value = "publishedAt DESC")
   private List<Comment> replies;

   @ManyToOne(fetch = FetchType.LAZY)
   private Comment parentComment;

   @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "reply")
   private Notification replyNotification;

   @Override
   public int compareTo(Comment o) {
      return o.publishedAt.compareTo(this.publishedAt);
   }

   public void addChild(Comment comment) {
      replies.add(comment);
      comment.setParentComment(this);
   }

   public void removeChild(Comment comment) {
      replies.remove(comment);
      comment.parentComment = null;
   }

   @Column
   private boolean parent = false;

   @Transient
   public long tempId;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public CirtaUser getAuthor() {
      return author;
   }

   public void setAuthor(CirtaUser author) {
      this.author = author;
   }

   public Book getBook() {
      return book;
   }

   public void setBook(Book book) {
      this.book = book;
   }

   public int getPageNumber() {
      return pageNumber;
   }

   public void setPageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public LocalDateTime getPublishedAt() {
      return publishedAt;
   }

   public void setPublishedAt(LocalDateTime publishedAt) {
      this.publishedAt = publishedAt;
   }

   public String getBadge() {
      return badge;
   }

   public void setBadge(String badge) {
      this.badge = badge;
   }

   public List<Comment> getReplies() {
      return replies;
   }

   public void setReplies(List<Comment> replies) {
      this.replies = replies;
   }

   public boolean isParent() {
      return parent;
   }

   public void setParent(boolean parent) {
      this.parent = parent;
   }

   public Comment getParentComment() {
      return parentComment;
   }

   public void setParentComment(Comment parentComment) {
      this.parentComment = parentComment;
   }

   public Notification getReplyNotification() {
      return replyNotification;
   }

   public void setReplyNotification(Notification replyNotification) {
      this.replyNotification = replyNotification;
   }
}
