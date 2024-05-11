package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import br.dev.contrib.lucenelabs.library.LibraryApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;
import java.nio.file.Path;

public class BookSearcher {

    private static final Logger logger = LogManager.getLogger(BookSearcher.class.getName());

    public BookSearcher(Path indexPath) throws IOException {
        DirectoryReader directoryReader = null;

        try {
            directoryReader = DirectoryReader.open(FSDirectory.open(indexPath));
        } catch (Exception ex) {
            logger.warn("Error on open index for search - search is unavailable");
        }

        this.directoryReader = directoryReader;
        this.indexSearcher = this.directoryReader != null ? new IndexSearcher(this.directoryReader) : null;
    }

    private final DirectoryReader directoryReader;

    private final IndexSearcher indexSearcher;

    public void search(String queryString) throws ParseException, IOException {
        search(queryString, new StandardAnalyzer());
    }

    public void search(String queryString, Analyzer analyzer) throws ParseException, IOException {
        var queryParser = new QueryParser(Book.BookFields.DEFAULT, analyzer);
        var query = queryParser.parse(queryString);

        var sort = new Sort(new SortField(Book.BookFields.TITLE, SortField.Type.STRING),
                new SortedNumericSortField(Book.BookFields.PageFields.NUMBER, SortField.Type.INT));

        var topDocs = indexSearcher.search(query, 1000, sort, true);

        System.out.printf("Documents matched: %s, relation: %s\n", topDocs.totalHits.value, topDocs.totalHits.relation.name());

        for (int i = 0; i < topDocs.scoreDocs.length; ++i) {
            var scoreDoc = topDocs.scoreDocs[i];
            var doc = indexSearcher.doc(scoreDoc.doc);

            System.out.println();
            System.out.println("-> Matched book: " + doc.get(Book.BookFields.TITLE));
            System.out.println("-> Score: " + scoreDoc.score);
            System.out.println("-- File: " + doc.get(Book.BookFields.MetadataFields.FILE_NAME));
            System.out.println("-- Page: " + doc.get(Book.BookFields.PageFields.NUMBER_STORED));
            System.out.println();
        }
    }
}
