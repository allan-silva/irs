package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public void queryStringSearch(String queryString) throws ParseException, IOException {
        queryStringSearch(queryString, new StandardAnalyzer());
    }

    public void queryStringSearch(String queryString, Analyzer analyzer) throws ParseException, IOException {
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

    public void termSearch(String searchTerm) throws IOException {
        var term = new Term(Book.BookFields.PageFields.CONTENT, searchTerm);
        var spanQuery = new TermQuery(term);
        var topDocs = indexSearcher.search(spanQuery, 1000);

        for (int i = 0; i < topDocs.scoreDocs.length; ++i) {
            var scoreDoc = topDocs.scoreDocs[i];
            var doc = indexSearcher.doc(scoreDoc.doc);
//            var termsV = indexSearcher.getIndexReader().getTermVector(scoreDoc.doc, Book.BookFields.PageFields.CONTENT);
//
//            TermsEnum te = termsV.iterator();
//
//            BytesRef br = new BytesRef(searchTerm);
//            te.seekExact(br);

            System.out.println();
            System.out.println("-> Matched book: " + doc.get(Book.BookFields.TITLE));
            System.out.println("-> Score: " + scoreDoc.score);
            System.out.println("-- File: " + doc.get(Book.BookFields.MetadataFields.FILE_NAME));
            System.out.println("-- Page: " + doc.get(Book.BookFields.PageFields.NUMBER_STORED));
            highlightContent(doc, scoreDoc.doc, searchTerm);

            System.out.println();
        }
    }

    private void highlightContent(Document document, int docId, String searchTerm) throws IOException {
        var content = document.get(Book.BookFields.PageFields.CONTENT);
        var termsV = indexSearcher.getIndexReader().getTermVector(docId, Book.BookFields.PageFields.CONTENT);

        if (termsV != null) {
            TermsEnum te = termsV.iterator();

            if (te.seekExact(new BytesRef(searchTerm))) {

                PostingsEnum pe = te.postings(null, PostingsEnum.OFFSETS);
                pe.nextDoc();

                int frequency = pe.freq();
                List<Pair<Integer, Integer>> termsOffset = new ArrayList<>();

                int minOffset = -1;
                int maxOffset = 0;

                for (int i = 0; i < frequency; ++i) {
                    pe.nextPosition();

                    if (minOffset == -1)
                        minOffset = pe.startOffset();

                    minOffset = Math.min(minOffset, pe.startOffset());
                    maxOffset = Math.max(maxOffset, pe.endOffset());
                    termsOffset.add(new ImmutablePair<>(pe.startOffset(), pe.endOffset()));
                }

                maxOffset = maxOffset + termsOffset.size() * 2;

                var contentStart = Math.max(0, minOffset - 10);
                var contentEnd = Math.min(content.length(), maxOffset + 15 + termsOffset.size() * 2);
                System.out.println("------------------> Content Start:");
                System.out.println("... " + highlight(termsOffset, content).substring(contentStart, contentEnd) + " ...");
                System.out.println("<------------------: Content end");
            }
        }
    }

    private String highlight(List<Pair<Integer, Integer>> termsOffset, String content) {
        var contentBuilder = new StringBuilder(content);
        for(int i = 0; i< termsOffset.size(); ++i) {
            contentBuilder.insert(termsOffset.get(i).getLeft() + (i * 2), "*");
            contentBuilder.insert(termsOffset.get(i).getRight() + (i * 2) + 1, "*");
        }
        return contentBuilder.toString();
    }
}
