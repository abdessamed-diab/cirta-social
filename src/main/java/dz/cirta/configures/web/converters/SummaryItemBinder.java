package dz.cirta.configures.web.converters;

import dz.cirta.data.models.SummaryItem;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.engine.backend.types.Norms;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;

public class SummaryItemBinder implements TypeBinder {

   @Override
   public void bind(TypeBindingContext typeBindingContext) {
      typeBindingContext.dependencies().use("title").use("page").use("parent").use("book");

      IndexFieldReference<String> titleFieldReference = typeBindingContext.indexSchemaElement()
            .field("title", f -> f.asString().searchable(Searchable.YES).norms(Norms.YES)
                  .analyzer("english")
            ).toReference();

      IndexFieldReference<Integer> pageFieldReference = typeBindingContext.indexSchemaElement()
            .field("page", f -> f.asInteger()).toReference();

      IndexFieldReference<Long> parentFieldReference = typeBindingContext.indexSchemaElement()
            .field("parent", f -> f.asLong()).toReference();

      IndexFieldReference<Long> bookFieldReference = typeBindingContext.indexSchemaElement()
            .field("book", f -> f.asLong()).toReference();

      typeBindingContext.bridge(SummaryItem.class, new Bridge(
            titleFieldReference,
            pageFieldReference,
            parentFieldReference,
            bookFieldReference
      ));
   }

   private static class Bridge implements TypeBridge<SummaryItem> {
      private final IndexFieldReference<String> titleField;
      private final IndexFieldReference<Integer> pageField;
      private final IndexFieldReference<Long> itemParentField;
      private final IndexFieldReference<Long> bookIdField;

      private Bridge(IndexFieldReference<String> titleField,
                     IndexFieldReference<Integer> pageField, IndexFieldReference<Long> itemParentField, IndexFieldReference<Long> bookIdField) {
         this.titleField = titleField;
         this.pageField = pageField;
         this.itemParentField = itemParentField;
         this.bookIdField = bookIdField;
      }

      @Override
      public void write(DocumentElement target, SummaryItem summaryItem, TypeBridgeWriteContext typeBridgeWriteContext) {
         target.addValue(titleField, summaryItem.title);
         target.addValue(pageField, summaryItem.page);
         target.addValue(itemParentField, summaryItem.parent != null ? summaryItem.parent.summaryItemId : 0);
         target.addValue(bookIdField, summaryItem.book.getId());
      }
   }
}
