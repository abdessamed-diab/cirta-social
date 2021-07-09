package dz.cirta.data.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dz.cirta.configures.web.serializers.CommentSerializer;
import dz.cirta.configures.web.serializers.ResourceToBase64;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;

@Entity
public class Notification implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;

   @OneToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "notification_comment", nullable = false, unique = true, updatable = false, referencedColumnName = "id")
   @JsonSerialize(using = CommentSerializer.class)
   private Comment reply;

   @ManyToOne(fetch = FetchType.LAZY)
   @JsonSerialize(using = ResourceToBase64.class)
   private Book book;

   @Column
   private boolean checked;

   @Column
   private String type;

   public static Notification CREATE_NOTIFICATION(Book book, Comment reply) throws IOException {
      Notification notification = new Notification();
      notification.setBook(book);
      notification.setChecked(false);
      notification.setReply(reply);
      notification.setType(Comment.class.getSimpleName().toLowerCase());

      return notification;
   }

   public Comment getReply() {
      return reply;
   }

   public void setReply(Comment reply) {
      this.reply = reply;
   }

   public Book getBook() {
      return book;
   }

   public void setBook(Book book) {
      this.book = book;
   }

   public boolean isChecked() {
      return checked;
   }

   public void setChecked(boolean checked) {
      this.checked = checked;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }
}
