package dz.cirta.tools;

import dz.cirta.data.models.Book;
import dz.cirta.data.models.Bookmark;
import dz.cirta.data.models.SummaryItem;
import dz.cirta.exceptions.PdfSummaryNotFoundException;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public final class PdfStreamApi {
   private static final Logger logger = LoggerFactory.getLogger(PdfStreamApi.class);

   public static boolean splitPdfFile(final int start, final int end, final String source) throws IOException {
      Resource resource = FileUtils.LOAD_FILE_FROM_CLASSPATH(source);
      PDDocument pdf = PDDocument.load(resource.getFile());
      List<PDDocument> documents = getDefaultSplitter(start, end).split(pdf); // only one document, check start, end and at page parameters.

      if (documents.isEmpty()) {
         throw new IllegalArgumentException("end of file.");
      }

      String fineName = resource.getFilename();
      String nextName = fineName
            .substring(0, fineName.lastIndexOf(".")) + "_" + start + ".pdf";
      try {
         nextName = resource.getFile().getAbsolutePath().replaceFirst(fineName, nextName);
         File file = new File(nextName);
         file.createNewFile();
         documents.get(0).save(file);
         documents.get(0).close();
         pdf.close();
         file.delete();
         return true;
      } catch (IOException e) {
         e.printStackTrace();
      }

      return false;
   }

   public static void splitPdfToOutputStream(final int start, final int end, final String source, OutputStream outputStream) throws IOException {
      Resource resource = FileUtils. LOAD_FILE_FROM_CLASSPATH(source);
      PDDocument pdf = PDDocument.load(resource.getFile());
      List<PDDocument> documents = getDefaultSplitter(start, end).split(pdf); // only one document, check start, end and at page parameters.

      if (documents.isEmpty()) {
         pdf.close();
         throw new IllegalArgumentException("end of file.");
      }

      documents.get(0).save(outputStream);
      documents.get(0).close();
      pdf.close();
   }

   public static List<Book> initializeBookProperties(final String pdfDocumentDirectory,
                                                     final String coverPhotoFileNameMappingSourceFileUrl) throws IOException {
      final List<Book> result = new ArrayList<>();
      final List<String> lines = lines(coverPhotoFileNameMappingSourceFileUrl);

      paths(pdfDocumentDirectory)
            .map(path -> path.getFileName())
            .forEach(fileName -> {

               try {
                  Optional<String> coverPhotoPath = lines
                        .stream()
                        .filter(
                              line -> fileName.startsWith(line.split("=")[0])
                        )
                        .findFirst();

                  if (coverPhotoPath.isPresent()) {
                     Book book = new Book();
                     book.setCoverPhotoUrl(coverPhotoPath.get().split("=")[1]);
                     PdfStreamApi.fillBookMetaData(book, pdfDocumentDirectory + fileName);
                     // TODO ad check weather removing map from book model.
                     book.setBookAttributes(extractPdfBookmark(pdfDocumentDirectory + fileName));
                     book.setSummaryItems(extractSummary(pdfDocumentDirectory + fileName, book));
                     result.add(book);
                  }

               } catch (IndexOutOfBoundsException | IOException ex) {
                  logger.error(ex.getMessage(), ex);
               }
            });

      return result;
   }

   public static void fillBookMetaData(Book book, String resourceUrl) throws IOException {
      if (StringUtils.isEmpty(book.getCoverPhotoUrl())) {
         throw new IllegalArgumentException("books needs cover photo url.");
      }

      Resource resource = FileUtils.LOAD_FILE_FROM_CLASSPATH(resourceUrl);
      PDDocument doc = PDDocument.load(resource.getFile());
      PDDocumentInformation pdfInformation = doc.getDocumentInformation();

      book.setPublisher(
            Optional.ofNullable(pdfInformation.getCreator())
                  .orElse(pdfInformation.getProducer())
      );

      book.setSubject(
            Optional.ofNullable(pdfInformation.getSubject())
                  .orElse(Paths.get(book.getCoverPhotoUrl()).getFileName().toString())
      );

      book.setTitle(pdfInformation.getTitle());

      book.setAuthor(pdfInformation.getAuthor());

      book.setReleaseDate(
            pdfInformation.getCreationDate() != null ?
                  LocalDateTime.ofInstant(
                        pdfInformation.getCreationDate().getTime().toInstant(),
                        ZoneId.of("Europe/Paris")) :
                  LocalDateTime.now().minusYears(2)
      );

      book.setKeywords(pdfInformation.getKeywords());

      book.setSourceUrl(resourceUrl);

      doc.close();
   }

   private static Map<String, List<Bookmark>> extractPdfBookmark(String resourceUrl) throws IOException {
      Resource resource = FileUtils.LOAD_FILE_FROM_CLASSPATH(resourceUrl);
      PDDocument doc = PDDocument.load(resource.getFile());
      PDDocumentOutline pdfDocOutline = doc.getDocumentCatalog().getDocumentOutline();

      if (pdfDocOutline == null) {
         throw new PdfSummaryNotFoundException("can't find any bookmark for book: " + resource.getFilename());
      }

      Map<String, List<Bookmark>> result = mapTitlesToPages(pdfDocOutline.getFirstChild(), doc); // bookmark node

      doc.close();
      return result;
   }

   private static Map<String, List<Bookmark>> mapTitlesToPages(PDOutlineItem firstBookmarkItem, PDDocument doc) throws IOException {
      Map<String, List<Bookmark>> result = new LinkedHashMap<>();

      while (firstBookmarkItem != null) {
         List<Bookmark> bookmarks = new ArrayList<>();
         Iterator<PDOutlineItem> children = firstBookmarkItem.children().iterator();
         while (children.hasNext()) {
            Bookmark bookmark = new Bookmark();
            PDOutlineItem child = children.next();
            bookmark.title = child.getTitle();

            PDDestination dest = child.getDestination();
            PDAction pageDestination;
            if (dest == null) {
               pageDestination = child.getAction();
               if (pageDestination instanceof PDActionGoTo) {
                  dest = ((PDActionGoTo) pageDestination).getDestination();
               }
            }

            if (dest instanceof PDNamedDestination) {
               PDPageDestination pdPageDestination = doc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) dest);
               bookmark.page = pdPageDestination.getPageNumber();

            } else {
               if (dest instanceof PDPageDestination) {
                  bookmark.page = ((PDPageDestination) dest).retrievePageNumber();
               }
            }
            bookmarks.add(bookmark);
         }

         if (!bookmarks.isEmpty()) {
            result.put(firstBookmarkItem.getTitle(), bookmarks);
         }

         firstBookmarkItem = firstBookmarkItem.getNextSibling();
      }

      return result;
   }

   private static synchronized List<SummaryItem> extractSummary(String resourceUrl, Book book) throws IOException {
      Resource resource = FileUtils.LOAD_FILE_FROM_CLASSPATH(resourceUrl);
      PDDocument doc = PDDocument.load(resource.getFile());
      PDDocumentOutline pdfDocOutline = doc.getDocumentCatalog().getDocumentOutline();

      if (pdfDocOutline == null) {
         throw new PdfSummaryNotFoundException("can't find any bookmark for book: " + resource.getFilename());
      }

      List<SummaryItem> all = new ArrayList<>();
      PDOutlineItem firstBookmarkItem = pdfDocOutline.getFirstChild();

      while (firstBookmarkItem != null) {
         SummaryItem parent = new SummaryItem(firstBookmarkItem.getTitle());
         parent.book = book;
         Iterator<PDOutlineItem> children = firstBookmarkItem.children().iterator();

         while (children.hasNext()) {
            PDOutlineItem child = children.next();
            SummaryItem item = new SummaryItem(child.getTitle());
            item.parent = parent;
            item.book = book;

            PDDestination dest = child.getDestination();
            PDAction pageDestination;
            if (dest == null) {
               pageDestination = child.getAction();
               if (pageDestination instanceof PDActionGoTo) {
                  dest = ((PDActionGoTo) pageDestination).getDestination();
               }
            }

            if (dest instanceof PDNamedDestination) {
               PDPageDestination pdPageDestination = doc.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) dest);
               item.page = pdPageDestination.getPageNumber();

            } else {
               if (dest instanceof PDPageDestination) {
                  item.page = ((PDPageDestination) dest).retrievePageNumber();
               }
            }

            if (parent.page < 1) {
               parent.page = item.page;
               all.add(parent);
            }

            all.add(item);
         }

         firstBookmarkItem = firstBookmarkItem.getNextSibling();
      }

      doc.close();
      return all;
   }

   private static Stream<Path> paths(String pdfDocumentDirectory) throws IOException {
      Resource basePdfFileSourceDirectory = FileUtils.LOAD_FILE_FROM_CLASSPATH(pdfDocumentDirectory);

      return Files.find(
            Paths.get(basePdfFileSourceDirectory.getURI()),
            1,
            (Path path, BasicFileAttributes attributes) -> attributes.isRegularFile() && path.getFileName().toString().endsWith("pdf")
         );
   }

   private static List<String> lines(String coverPhotoFileNameMappingSourceFileUrl) throws IOException {
      File file = FileUtils.LOAD_FILE_FROM_CLASSPATH(coverPhotoFileNameMappingSourceFileUrl).getFile();
      return file.exists() && file.isFile() ?  Files.readAllLines(file.toPath()) : Arrays.asList();
   }

   private static Splitter getDefaultSplitter(final int start, final int end) {
      Splitter splitter = new Splitter();
      splitter.setStartPage(start);
      splitter.setEndPage(end);
      splitter.setSplitAtPage(end);
      return splitter;
   }

}
