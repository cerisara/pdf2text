/*
 *
 * Licensed to Christophe Cerisara
 * Adapted from an Apache pdfbox example:
 *
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xtof54;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.regex.Pattern;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * This is an example adapted from the following example (see below) to extract in a text file
 * the text from a PDF and write each text at its correct location.
 *
 * This is an example on how to get some x/y coordinates of text and to show them in a rendered
 * image.
 *
 * @author Christophe Cerisara
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class App extends PDFTextStripper
{
    private final String filename;
    static final int SCALE = 4;
    private final PDDocument document;

    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @param document
     * @param filename
     * @throws IOException If there is an error loading the properties.
     */
    public App(PDDocument document, String filename) throws IOException
    {
        this.document = document;
        this.filename = filename;
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
        }
        else
        {
            PDDocument document = null;
	            try
	            {
	                document = PDDocument.load(new File(args[0]));
	
	                App stripper = new App(document, args[0]);
	                stripper.setSortByPosition(true);
	
	                for (int page = 0; page < document.getNumberOfPages(); ++page)
	                {
	                    stripper.stripPage(page);
	                }
	            }
	            finally
	            {
	                if (document != null)
	                {
	                    document.close();
	                }
	            }
	        }
	    }
	
        /*    
	    @Override
	    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, Vector displacement) throws IOException
	    {
	        super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);
	
	        // in cyan:
	        // show actual glyph bounds. This must be done here and not in writeString(),
	        // because writeString processes only the glyphs with unicode, 
	        // see e.g. the file in PDFBOX-3274
	        Shape cyanShape = calculateGlyphBounds(textRenderingMatrix, font, code);
	
	        if (cyanShape != null)
	        {
	            cyanShape = flipAT.createTransformedShape(cyanShape);
	            cyanShape = rotateAT.createTransformedShape(cyanShape);
	            cyanShape = transAT.createTransformedShape(cyanShape);
	
	            g2d.setColor(Color.CYAN);
	            g2d.draw(cyanShape);
	        }
	    }

	    // this calculates the real individual glyph bounds
	    private Shape calculateGlyphBounds(Matrix textRenderingMatrix, PDFont font, int code) throws IOException
	    {
	        GeneralPath path = null;
	        AffineTransform at = textRenderingMatrix.createAffineTransform();
	        at.concatenate(font.getFontMatrix().createAffineTransform());
	        if (font instanceof PDType3Font)
	        {
	            PDType3Font t3Font = (PDType3Font) font;
	            PDType3CharProc charProc = t3Font.getCharProc(code);
	            if (charProc != null)
	            {
	                PDRectangle glyphBBox = charProc.getGlyphBBox();
	                if (glyphBBox != null)
	                {
	                    path = glyphBBox.toGeneralPath();
	                }
	            }
	        }
	        else if (font instanceof PDVectorFont)
	        {
	            PDVectorFont vectorFont = (PDVectorFont) font;
	            path = vectorFont.getPath(code);
	
	            if (font instanceof PDTrueTypeFont)
	            {
	                PDTrueTypeFont ttFont = (PDTrueTypeFont) font;
	                int unitsPerEm = ttFont.getTrueTypeFont().getHeader().getUnitsPerEm();
	                at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
	            }
	            if (font instanceof PDType0Font)
	            {
	                PDType0Font t0font = (PDType0Font) font;
	                if (t0font.getDescendantFont() instanceof PDCIDFontType2)
	                {
	                    int unitsPerEm = ((PDCIDFontType2) t0font.getDescendantFont()).getTrueTypeFont().getHeader().getUnitsPerEm();
	                    at.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
	                }
	            }
	        }
	        else if (font instanceof PDSimpleFont)
	        {
	            PDSimpleFont simpleFont = (PDSimpleFont) font;
	
	            // these two lines do not always work, e.g. for the TT fonts in file 032431.pdf
	            // which is why PDVectorFont is tried first.
	            String name = simpleFont.getEncoding().getName(code);
	            path = simpleFont.getPath(name);
	        }
	        else
	        {
	            // shouldn't happen, please open issue in JIRA
	            System.out.println("Unknown font class: " + font.getClass());
	        }
	        if (path == null)
	        {
	            return null;
	        }
	        return at.createTransformedShape(path.getBounds2D());
	    }
        */
	    private void stripPage(int page) throws IOException
	    {
	        PDFRenderer pdfRenderer = new PDFRenderer(document);
	        
	        PDPage pdPage = document.getPage(page);
	        PDRectangle cropBox = pdPage.getCropBox();
	
	        setStartPage(page + 1);
	        setEndPage(page + 1);
	
	        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            minx=miny=maxx=maxy=0;
            allx.clear();
            pos.clear();
            txt.clear();
	        writeText(document, dummy);
            System.out.println("page "+page+" "+minx+" "+maxx+" "+miny+" "+maxy);
            ArrayList<Float> xx = new ArrayList<Float>();
            xx.addAll(allx);
            Collections.sort(xx);
            /*
            for (int i=1;i<xx.size();i++)
                System.out.println("PAGE "+page+" "+(xx.get(i)-xx.get(i-1)));
                */

            float dy=(maxy-miny)/500f;
            String prevs="";
            for (float l=miny;l<maxy;l+=dy) {
                ArrayList<Lettre> charsligne = new ArrayList<Lettre>();
                for (int i=0;i<pos.size();i++) {
                    if (pos.get(i)[1]>l-dy && pos.get(i)[1]<l+dy) {
                        charsligne.add(new Lettre(pos.get(i)[0],pos.get(i)[1],txt.get(i)));
                    }
                }
                Collections.sort(charsligne);
                String s="", s2="";
                int[] cols = {70,360,400,517};
                String[] scols = {"","","","",""};
                int col=0;
                for (int i=0;i<charsligne.size();i++) {
                    if (col<cols.length && charsligne.get(i).x>cols[col]) {
                        scols[col]=""+s2.trim();
                        col++;
                        s2="";
                    }
                    s+=charsligne.get(i).s;
                    s2+=charsligne.get(i).s;
                }
                scols[col]=""+s2.trim();
                if (!prevs.equals(s) && s.trim().length()>0) {
                    if (zone==0&&s.toLowerCase().contains("ancien solde")) zone=1;
                    if (zone==1) {
                        if (scols[0].trim().length()>0 && p.matcher(scols[0]).matches())
                            System.out.println(scols[0]+" | "+scols[1]+" | "+scols[2]+" | "+scols[3]+" | "+scols[4]);
                        if (s.toLowerCase().contains("solde en euros")) zone=2;
                    }
                }
                //s="";
                //for (int i=0;i<charsligne.size();i++) s+=charsligne.get(i).x+" ";
                //System.out.println(l+" : "+s);
                
               prevs=s; 
            }

	    }

        private class Lettre implements Comparable {
            float x,y;
            String s;
            public Lettre(float x, float y, String s) {
                this.x=x;
                this.y=y;
                this.s=s;
            }
            public int compareTo(Object l0) {
                Lettre l=(Lettre) l0;
                if (y<l.y) return -1;
                else {
                    if (y>l.y) return 1;
                    else return 0;
                }
            }
        }

        final Pattern p = Pattern.compile("^\\d\\d.\\d\\d$");
        int zone=0;
        HashSet<Float> allx = new HashSet<Float>();
        ArrayList<float[]> pos = new ArrayList<float[]>();
        ArrayList<String> txt = new ArrayList<String>();
        float minx=0;
        float maxx=0;
        float miny=0;
        float maxy=0;

	    /**
	     * Override the default functionality of PDFTextStripper.
	     */
	    @Override
	    protected void writeString(String string, List<TextPosition> textPositions) throws IOException
	    {
	        for (TextPosition text : textPositions) {
                float x=text.getXDirAdj();
                float y=text.getYDirAdj();
                String t=text.getUnicode();
                float[] p = {x,y};
                pos.add(p);
                txt.add(t);
            }
	        for (TextPosition text : textPositions) {
                float x=text.getXDirAdj();
                float y=text.getYDirAdj();
                /*
                String t=text.getUnicode();
                float h=text.getHeightDir();
                float ligne0 = y-h*0.1;
                float ligne1 = y+h*0.1;
                */

                allx.add(x);
                if (x<minx) {minx=x;}
                else if (x>maxx) {maxx=x;}
                if (y<miny) {miny=y;}
                else if (y>maxy) {maxy=y;}

                /*
	            System.out.println("String[" + text.getXDirAdj() + ","
	                    + text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale="
	                    + text.getXScale() + " height=" + text.getHeightDir() + " space="
	                    + text.getWidthOfSpace() + " width="
	                    + text.getWidthDirAdj() + "]" + text.getUnicode());
	            */
	        }
	    }
	
	    /**
	     * This will print the usage for this document.
	     */
	    private static void usage()
	    {
	        System.err.println("Usage: java " + App.class.getName() + " <input-pdf>");
	    }
	}
