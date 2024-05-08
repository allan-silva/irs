package br.dev.contrib.lucenelabs.library.lucene;

import br.dev.contrib.lucenelabs.library.Book;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookIndexer {

    private final Directory directory;

    private final IndexWriter indexWriter;

    private final DocumentParser documentParser;

    private final StopWatch stopWatch;

    private final List<Document> documentsBuffer = new ArrayList<>();

    private static final int INDEX_COMMIT_TIME_SECS = 5;

    public BookIndexer(Path indexPath, DocumentParser documentParser) throws IOException {
        var indexConfig = new IndexWriterConfig(new StandardAnalyzer());
        directory = FSDirectory.open(indexPath);
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
            for(var document : documentsBuffer){
                indexWriter.addDocument(document);
            }

            indexWriter.commit();
            documentsBuffer.clear();
            stopWatch.reset();
            stopWatch.start();
        }
    }
}
