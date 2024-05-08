package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import org.apache.lucene.document.*;

import java.util.ArrayList;
import java.util.List;

public class DocumentParser {
    public List<Document> parse(Book book) {
        var documents = new ArrayList<Document>();

        for (var page: book.getPages()) {
            var document = createEmpty(book.getId());

            // Book single tokens
            document.add(new StringField(Book.BookFields.AUTHOR, book.getAuthor(), Field.Store.YES));
            document.add(new StringField(Book.BookFields.TITLE, book.getTitle(), Field.Store.YES));
            document.add(new StringField(Book.BookFields.SUBJECT, book.getSubject(), Field.Store.NO));
            document.add(new StringField(Book.BookFields.MetadataFields.FILE_NAME, book.getFileMetadata().getFileName(), Field.Store.YES));

            document.add(new StringField(Book.BookFields.MetadataFields.TOTAL_PAGES, book.getFileMetadata()., Field.Store.YES));

            // Book full text search fields
            document.add(new TextField(Book.BookFields.KEYWORDS, book.getKeywords(), Field.Store.NO));

            // Page range tokens
            document.add(new IntPoint(Book.BookFields.PageFields.NUMBER, page.getNumber())); // range is not stored
            document.add(new StoredField(Book.BookFields.PageFields.NUMBER_STORED, page.getNumber())); // Store for retrieval

            // Page full text search
            document.add(new TextField(Book.BookFields.PageFields.CONTENT, page.getContent(), Field.Store.YES));
        }

        return documents;
    }

    private Document createEmpty(String id) {
        var document = new Document();
        document.add(new StringField(Book.BookFields.ID, id, Field.Store.YES));
        return document;
    }
}
