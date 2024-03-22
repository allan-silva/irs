package br.dev.contrib.lucenelabs.library;


import br.dev.contrib.lucenelabs.library.pdf.DirectoryIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class LibraryApp
{
    private static final Logger logger = LogManager.getLogger(LibraryApp.class.getName());

    public static void main(String[] args)
    {
        assert args.length > 0 : "Invalid program arguments";

        var directory = Path.of(args[0]);
        var directoryIterator = new DirectoryIterator(directory);
        directoryIterator.load(pdfFile -> {
            logger.info("Found PFD: {} - pages: {}", pdfFile.getFile(), pdfFile.getPdf().getNumberOfPages());
        });
    }
}
