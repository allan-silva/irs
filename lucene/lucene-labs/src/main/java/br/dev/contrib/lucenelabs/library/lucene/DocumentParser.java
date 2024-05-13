package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.List;

public class DocumentParser {
    public List<Document> parse(Book book) {
        var documents = new ArrayList<Document>();

        for (var page: book.getPages()) {
            var documentPageId = DigestUtils.sha1Hex(book.getId() + page.getNumber());
            var document = createEmpty(documentPageId);

            // Book single tokens
            addStringField(Book.BookFields.AUTHOR, book.getAuthor(), document, Field.Store.YES);
            addStringField(Book.BookFields.SUBJECT, book.getSubject(), document, Field.Store.NO);
            addStringField(Book.BookFields.MetadataFields.FILE_NAME, book.getFileMetadata().getFileName(), document, Field.Store.YES);
            addStringField(Book.BookFields.MetadataFields.TOTAL_PAGES, String.valueOf(book.getFileMetadata().getTotalPages()), document, Field.Store.YES);

            // Book full text search fields
            addTextField(Book.BookFields.KEYWORDS, book.getKeywords(), document, Field.Store.NO);
            addTextField(Book.BookFields.TITLE, book.getTitle(), document, Field.Store.YES);

            // Page range fields
            document.add(new IntPoint(Book.BookFields.PageFields.NUMBER, page.getNumber())); // range is not stored
            document.add(new StoredField(Book.BookFields.PageFields.NUMBER_STORED, page.getNumber())); // Store for retrieval

            // Page full text search, with term vector offsets enabled.
            addTextWithOffsetsEnabled(Book.BookFields.PageFields.CONTENT, page.getContent(), document, Field.Store.YES);

            // Store fields for sorting, aggregations and others.
            // This is necessary because Lucene stores it in a columnar fashion, in another file other than inverted index.
            document.add(new SortedDocValuesField(Book.BookFields.TITLE, new BytesRef(book.getTitle())));
            document.add(new SortedNumericDocValuesField(Book.BookFields.PageFields.NUMBER, page.getNumber()));

            documents.add(document);
        }

        return documents;
    }

    private void addStringField(String name, String value, Document document, Field.Store store) {
        if (value != null) {
            document.add(new StringField(name, value, store));
        }
    }

    private void addTextField(String name, String value, Document document, Field.Store store) {
        if (value != null) {
            document.add(new TextField(name, value, store));
        }
    }

    private void addTextWithOffsetsEnabled(String name, String value, Document document, Field.Store store) {
        if (value != null) {
            var fieldType = new FieldType(store == Field.Store.YES ? TextField.TYPE_STORED : TextField.TYPE_NOT_STORED);
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            fieldType.setStoreTermVectors(true);
            fieldType.setStoreTermVectorOffsets(true);
            var field = new Field(name, value, fieldType);
            document.add(field);
        }
    }

    private Document createEmpty(String id) {
        var document = new Document();
        document.add(new StringField(Book.BookFields.PageFields.ID, id, Field.Store.YES));
        return document;
    }
}
