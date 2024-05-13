package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import br.dev.contrib.lucenelabs.library.LibraryApp;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookIndexer {

    private static final Logger logger = LogManager.getLogger(BookIndexer.class.getName());

    private final IndexWriter indexWriter;

    private final DocumentParser documentParser;

    private final StopWatch stopWatch;

    private final List<Document> documentsBuffer = new ArrayList<>();

    private static final int INDEX_COMMIT_TIME_SECS = 5;

    public BookIndexer(Path indexPath, DocumentParser documentParser) throws IOException {
        var indexConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexConfig.setCodec(new SimpleTextCodec());
        Directory directory = FSDirectory.open(indexPath);
        indexWriter = new IndexWriter(directory, indexConfig);
        stopWatch = StopWatch.createStarted();

        this.documentParser = documentParser;
    }

    public void index(Book book) throws IOException {
        var documents = documentParser.parse(book);
        documentsBuffer.addAll(documents);
        onDocumentAdded();
    }

    private void onDocumentAdded() throws IOException {
        if(stopWatch.getTime(TimeUnit.SECONDS) > INDEX_COMMIT_TIME_SECS) {
            addDocuments();
        }
    }

    private void addDocuments() throws IOException {
        for(var document : documentsBuffer){
            var documentId = document.get(Book.BookFields.PageFields.ID);
            var segment = indexWriter.updateDocument(new Term(Book.BookFields.PageFields.ID, documentId), document);
            logger.info("Add or update document: {}, sequence operation number: {}", documentId, segment);
        }

        var lastCommitedSegment = indexWriter.commit();

        if (lastCommitedSegment != -1) {
            logger.info("Changes commited to index, last sequence number: {}", lastCommitedSegment);
        } else {
            logger.info("No changes was write to index");
        }

        documentsBuffer.clear();
        stopWatch.reset();
        stopWatch.start();
    }

    public void closeIndex() throws IOException {
        addDocuments();
        indexWriter.close();
    }
}
