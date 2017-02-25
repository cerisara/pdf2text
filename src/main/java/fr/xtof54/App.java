package fr.xtof54;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        String string = null;
        try {
            String pdf=args[0];
            System.out.println(pdf);
            PDFParser pdfParser = new PDFParser(new FileInputStream(pdf));
            pdfParser.parse();
            PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
            string = pdfTextStripper.getText(pdDocument);

            PrintWriter f = new PrintWriter(new FileWriter("todo.txt"));
            f.println(string);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
}
