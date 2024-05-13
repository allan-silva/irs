package br.dev.contrib.lucenelabs.library;

import br.dev.contrib.lucenelabs.library.lucene.BookSearcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LibrarySearchApp {
    public static void main(String[] args) throws IOException {
        var indexPath = args[0];
        var bookSearcher = new BookSearcher(Path.of(indexPath));

        try (var in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.print("Enter search mode: ");
            var searchMode = SearchModes.valueOf(in.readLine().toUpperCase());

            while (true) {
                System.out.print("Type a search term or query (l-q to exit): ");

                var query = in.readLine();

                System.out.println("\n\n->Starting new search: " + query);

                if (query.equals("l-q")) {
                    break;
                }

                if (searchMode == SearchModes.TERM) {
                    bookSearcher.termSearch(query);
                }
            }
        }
    }

    enum SearchModes {
        TERM
    }
}
