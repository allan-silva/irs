package br.dev.contrib.lucenelabs.library;

import br.dev.contrib.lucenelabs.library.pdf.PDFFile;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Book {

    private String title;

    private String author;

    private String keywords;

    private String subject;

    private FileMetadata fileMetadata;

    private List<BookPage> pages;

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
        public FileMetadata(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        private String fileName;
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

        var book = new Book(
                pdfDocumentInformation.getTitle(),
                pdfDocumentInformation.getAuthor(),
                pdfDocumentInformation.getKeywords(),
                pdfDocumentInformation.getSubject()
        );
        book.setPages(pages);
        book.setFileMetadata(new FileMetadata(pdfFile.getFile()));

        return book;
    }
}
