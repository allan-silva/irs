package br.dev.contrib.lucenelabs.library;

import br.dev.contrib.lucenelabs.library.pdf.PDFFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Book {

    private String id;

    private String title;

    private String author;

    private String keywords;

    private String subject;

    private FileMetadata fileMetadata;

    private List<BookPage> pages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getSubject() {
        return subject;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    public List<BookPage> getPages() {
        return pages;
    }

    public void setPages(List<BookPage> pages) {
        this.pages = pages;
    }

    private Book(String title, String author, String keywords, String subject) {
        this.title = title;
        this.author = author;
        this.keywords = keywords;
        this.subject = subject;
    }

    public static class BookPage {

        private int number;

        private String content;

        public int getNumber() {
            return number;
        }

        public String getContent() {
            return content;
        }

        public BookPage(Integer number, String content) {
            this.number = number;
            this.content = content;
        }
    }

    public static class FileMetadata {
        public FileMetadata(String fileName, int totalPages) {
            this.fileName = fileName;
            this.totalPages = totalPages;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public String getFileName() {
            return fileName;
        }

        private final String fileName;

        private final int totalPages;
    }

    public static Book from(PDFFile pdfFile) throws IOException {
        var pdf = pdfFile.getPdf();
        var pdfDocumentInformation = pdfFile.getPdf().getDocumentInformation();
        var numberOfPages = pdfFile.getPdf().getNumberOfPages();
        var pages = new ArrayList<BookPage>();

        for (int page = 0; page < numberOfPages; ++page) {
            var stripper = new PDFTextStripper();
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            stripper.setSortByPosition(true);

            var bookPage = new BookPage(page, stripper.getText(pdf));
            pages.add(bookPage);
        }

        var fileName = Path.of(pdfFile.getFile()).getFileName().toString();
        var bookTitle = pdfDocumentInformation.getTitle() != null ?
                pdfDocumentInformation.getTitle() :  fileName;

        var book = new Book(
                bookTitle,
                pdfDocumentInformation.getAuthor(),
                pdfDocumentInformation.getKeywords(),
                pdfDocumentInformation.getSubject()
        );
        book.setId(DigestUtils.sha1Hex(fileName));
        book.setPages(pages);
        book.setFileMetadata(new FileMetadata(pdfFile.getFile(), numberOfPages));

        return book;
    }

    public static class BookFields {

        public static final  String DEFAULT = PageFields.CONTENT;

        public static final String ID = "id";

        public static final String TITLE = "title";

        public static final String AUTHOR = "author";

        public static final String KEYWORDS = "keywords";

        public static final String SUBJECT = "subject";

        public static class PageFields {

            public static final String PATH = "page";

            public static final String NUMBER = PATH + ".number";

            public static final String NUMBER_STORED = NUMBER + ".stored";

            public static final String CONTENT = PATH + ".content";
        }

        public static class MetadataFields {
            public static final String PATH = "page";

            public static final String FILE_NAME = PATH + ".filename";

            public static final String TOTAL_PAGES = PATH + ".totalpages";
        }
    }
}
