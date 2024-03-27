package br.dev.contrib.lucenelabs.library.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFFile {

    public PDDocument getPdf() {
        return pdf;
    }

    public String getFile() {
        return file;
    }

    private final PDDocument pdf;

    private final String file;

    public PDFFile(PDDocument pdf, String file) {
        this.pdf = pdf;
        this.file = file;
    }
}
