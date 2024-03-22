package br.dev.contrib.lucenelabs.library.pdf;

import org.apache.pdfbox.Loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DirectoryIterator {

    private final Path directoryPath;

    public DirectoryIterator(Path directoryPath) {
        assert directoryPath.toFile().isDirectory();
        this.directoryPath = directoryPath;
    }

    public void load(Consumer<PDFFile> documentConsumer) {

        try(var directoryStream = Files.newDirectoryStream(directoryPath, "*.pdf")) {
            directoryStream.forEach(path -> {
                try(var pdf = Loader.loadPDF(path.toFile())) {
                    documentConsumer.accept(new PDFFile(pdf, path.toString()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
