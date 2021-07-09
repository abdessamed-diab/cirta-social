package dz.cirta.configures.web.serializers;

import dz.cirta.data.models.SummaryItem;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class SearchableSummaryItem implements Serializable {
   public Long id;
   public String title;
   public Integer page;
   public Long parentId;
   public String parentTitle;
   public Long bookId;
   public String bookName;
   public String bookSourceUrl;
   public String keywords;
   public String coverPhotoUrl;


   protected SearchableSummaryItem() {
   }

   public static List<SearchableSummaryItem> bookToBookItem(final List<SummaryItem> items) {
      return items.stream().map(item -> {
         SearchableSummaryItem searchableSummaryItem = new SearchableSummaryItem();
         searchableSummaryItem.id = item.summaryItemId;
         searchableSummaryItem.title = item.title;
         searchableSummaryItem.page = item.page;
         searchableSummaryItem.parentId = item.parent != null ? item.parent.summaryItemId : null;
         searchableSummaryItem.parentTitle = item.parent != null ? item.parent.title : null;
         searchableSummaryItem.bookId = item.book.getId();
         searchableSummaryItem.bookName = item.book.getTitle();
         searchableSummaryItem.bookSourceUrl = item.book.getSourceUrl();
         searchableSummaryItem.keywords = item.book.getKeywords();
         searchableSummaryItem.coverPhotoUrl = item.book.getCoverPhotoUrl();
         return searchableSummaryItem;
      }).collect(Collectors.toList());
   }


}
