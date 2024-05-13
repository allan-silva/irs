package br.dev.contrib.lucenelabs.library;


import br.dev.contrib.lucenelabs.library.lucene.BookIndexer;
import br.dev.contrib.lucenelabs.library.lucene.BookSearcher;
import br.dev.contrib.lucenelabs.library.lucene.DocumentParser;
import br.dev.contrib.lucenelabs.library.pdf.DirectoryIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LibraryApp
{
    private static final Logger logger = LogManager.getLogger(LibraryApp.class.getName());

    private final DirectoryIterator directoryIterator;

    private final BookIndexer bookIndexer;

    private final BookSearcher bookSearcher;

    public LibraryApp(DirectoryIterator directoryIterator, BookIndexer bookIndexer, BookSearcher bookSearcher) {
        this.directoryIterator = directoryIterator;
        this.bookIndexer = bookIndexer;
        this.bookSearcher = bookSearcher;
    }


    public static void main(String[] args) throws IOException, ParseException {
        assert args.length > 0 : "Invalid program arguments";

        var pdfDirectory = Path.of(args[0]);
        var indexPath = Path.of(args[1]);
        var op = args[2];

        var app = new LibraryApp(
                new DirectoryIterator(pdfDirectory),
                new BookIndexer(indexPath, new DocumentParser()),
                new BookSearcher(indexPath));

        switch (op) {
            case "index": app.index(); break;
            case "search": {
                try(var in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

                    while (true) {
                        System.out.print("Type a search term or query (l-q to exit): ");

                        var query = in.readLine();

                        System.out.println("\n\n->Starting new search: " + query);

                        if (query.equals("l-q")) {
                            break;
                        }

                        app.search(query);
                    }
                }
                break;
            }
            default: throw new IllegalArgumentException("Invalid program arguments");
        }
    }

    private void index() throws IOException {
        directoryIterator.load(pdfFile -> {
            logger.info("Found PFD: {} - pages: {}", pdfFile.getFile(), pdfFile.getPdf().getNumberOfPages());
            try {
                bookIndexer.index(Book.from(pdfFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bookIndexer.closeIndex();
    }

    private void search(String query) throws ParseException, IOException {
        bookSearcher.queryStringSearch(query);
    }
}
