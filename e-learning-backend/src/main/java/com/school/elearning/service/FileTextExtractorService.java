package com.school.elearning.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileTextExtractorService {

    public String extraireTexte(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("Fichier invalide");
        }

        String extension = filename.toLowerCase();

        if (extension.endsWith(".pdf")) {
            return extraireDePdf(file.getInputStream());
        } else if (extension.endsWith(".pptx")) {
            return extraireDePowerPoint(file.getInputStream());
        } else {
            throw new RuntimeException("Format non supporté. Utilisez PDF ou PPTX.");
        }
    }

    private String extraireDePdf(InputStream is) throws IOException {
        try (PDDocument doc = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extraireDePowerPoint(InputStream is) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        sb.append(textShape.getText()).append("\n");
                    }
                }
                sb.append("\n---\n"); // séparateur entre slides
            }
            return sb.toString();
        }
    }
    
    
    
}