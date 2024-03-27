package br.dev.contrib;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static br.dev.contrib.ResourcesUtils.getResourceFileStream;

public class PDFBoxExploratoryTest {

    @Test
    void loadPDF() throws IOException {
        try (var stream = getResourceFileStream("pdf/grimm_s_fairy_tales__complete_a_-_jacob_grimm.pdf");
             var pdf = Loader.loadPDF(new RandomAccessReadBuffer(stream))) {
            var stripper = new PDFTextStripper();
            stripper.setStartPage(3);
            stripper.setEndPage(3);
            stripper.setSortByPosition(true);

            var text = stripper.getText(pdf);
        }
    }

}
