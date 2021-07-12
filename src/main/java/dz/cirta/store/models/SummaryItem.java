package dz.cirta.store.models;

import dz.cirta.api.configures.web.converters.SummaryItemBinder;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.TypeBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.TypeBinding;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Abdessamed Diab
 */
@Entity
@Indexed(index = "summary_item_index")
@TypeBinding(binder = @TypeBinderRef(type = SummaryItemBinder.class))
public class SummaryItem implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   public long summaryItemId;

   @Column
   public String title;

   @Column
   public int page;

   @ManyToOne(targetEntity = SummaryItem.class, fetch = FetchType.EAGER, optional = true)
   @JoinColumn(name = "parent", referencedColumnName = "summaryItemId")
   public SummaryItem parent;

   @ManyToOne(fetch = FetchType.EAGER, optional = false)
   public Book book;

   protected SummaryItem() {
   }

   public SummaryItem(String title) {
      this.title = title;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + summaryItemId;
   }
}
