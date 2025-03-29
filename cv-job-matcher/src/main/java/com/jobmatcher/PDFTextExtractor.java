package com.jobmatcher;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class PDFTextExtractor {
    public static String extractTextFromPDF(String pdfPath) {
        try {
            File file = new File(pdfPath);
            PDDocument document = PDDocument.load(file);
            
            if (!document.isEncrypted()) {
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setSortByPosition(true);
                textStripper.setSuppressDuplicateOverlappingText(true);
                String text = textStripper.getText(document);
                document.close();
                return text;
            } else {
                document.close();
                return "Error: PDF is encrypted";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Unable to process PDF - " + e.getMessage();
        }
    }
}