package com.example.WordBot.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class WordDocumentService {
    public void createDocument(String fullName, String birthDate, String gender) throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("ФИО: " + fullName);
        run.addBreak();
        run.setText("Дата рождения: " + birthDate);
        run.addBreak();
        run.setText("Пол: " + gender);

        try (FileOutputStream out = new FileOutputStream("user_info.docx")) {
            document.write(out);
        }

        document.close();
    }
}