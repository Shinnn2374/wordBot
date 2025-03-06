package com.example.WordBot.helper;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.util.Units;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WordDocumentHelper {

    public static File createDocument(String fullName, String birthDate, String gender, String photoPath) throws IOException {
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("ФИО: " + fullName);
        run.addBreak();
        run.setText("Дата рождения: " + birthDate);
        run.addBreak();
        run.setText("Пол: " + gender);

        if (photoPath != null) {
            try (FileInputStream photoStream = new FileInputStream(photoPath)) {
                run.addBreak();
                run.addPicture(photoStream, XWPFDocument.PICTURE_TYPE_JPEG, photoPath, Units.toEMU(200), Units.toEMU(200));
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        }

        File file = File.createTempFile("document", ".docx");
        FileOutputStream out = new FileOutputStream(file);
        document.write(out);
        out.close();
        document.close();

        return file;
    }
}