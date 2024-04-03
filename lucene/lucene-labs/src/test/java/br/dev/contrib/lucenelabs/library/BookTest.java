package br.dev.contrib.lucenelabs.library;

import br.dev.contrib.lucenelabs.library.pdf.PDFFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static br.dev.contrib.ResourcesUtils.getResourceFileStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {

    @Test
    void from() throws IOException {
        var fileName = "pdf/grimm_s_fairy_tales__complete_a_-_jacob_grimm.pdf";
        try (var stream = getResourceFileStream(fileName);
             var pdf = Loader.loadPDF(new RandomAccessReadBuffer(stream))) {
            var pdfFile = new PDFFile(pdf, fileName);
            var book = Book.from(pdfFile);
            assertEquals("Grimm's Fairy Tales: Complete and Illustrated", book.getTitle());
            assertEquals("Jacob Grimm & Wilhelm Grimm & Maplewood Books", book.getAuthor());
            assertEquals(768, book.getPages().size());
            assertEquals(fileName, book.getFileMetadata().getFileName());

            var stripper = new PDFTextStripper();
            stripper.setStartPage(3);
            stripper.setEndPage(3);
            stripper.setSortByPosition(true);

            var text = stripper.getText(pdf);
            assertTrue(text.contains("GRIMM'S FAIRY TALES"));
        }
    }
}