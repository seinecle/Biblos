/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biblioportrait;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 *
 * @author C. Levallois
 */
public class Utils {

    static void ExtractAuthors(String fileName) throws FileNotFoundException, IOException {
        HashMultiset multisetAuthors = HashMultiset.create();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder stringBuilderAuthors = new StringBuilder();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("co-authors_" + fileName))) {
            String currLine;
            StringBuilder bufferToWrite = new StringBuilder();
            while ((currLine = br.readLine()) != null) {
                if (currLine.startsWith("author")) {
                    currLine = currLine.substring(currLine.lastIndexOf("{") + 1, currLine.indexOf("}"));
                    if (currLine.contains("and")) {
                        String[] arrayAuthors = currLine.split("and");
                        for (String author : arrayAuthors) {
                            multisetAuthors.add(author.trim());
                        }
                        for (int i = 0; i < arrayAuthors.length - 1; i++) {
                            for (int j = i + 1; j < arrayAuthors.length; j++) {
                                bufferToWrite.append(arrayAuthors[i].trim()).append(";").append(arrayAuthors[j].trim()).append("\n");

                            }
                        }
                    }
                }

            }
            Iterator<String> multisetAuthorsIterator = multisetAuthors.elementSet().iterator();
            while (multisetAuthorsIterator.hasNext()) {
                stringBuilderAuthors.append(multisetAuthorsIterator.next()).append("\n");
            }
            bw.write(stringBuilderAuthors.toString());
            bw.write(bufferToWrite.toString());
            bw.close();
        }
    }

    static void ExtractAuthorsForCircos(String fileName, String dateRange, String fieldDelimiter) throws FileNotFoundException, IOException {

        String filePath = "D:\\Docs Pro Clement\\circos\\circos-0.60\\";
        StringBuilder circosConfig = new StringBuilder();

        //Extracting the dates of the DATE RANGE
        String[] stringDates = dateRange.split("-");
        int startDate = Integer.parseInt(stringDates[0]);
        int endDate = Integer.parseInt(stringDates[1]);


        HashMultiset<String> multisetAuthorsGlobal = HashMultiset.create();
        HashMultiset<String> multisetAuthorsLocal = HashMultiset.create();
        HashMap<String, String> mapAuthorsToColors = new HashMap();
        HashMultimap<String, String[]> multimapColorsToLinks = HashMultimap.create();
        BufferedReader br;
        StringBuilder stringBuilderAuthors = new StringBuilder();

        BufferedWriter bw;


        String currLine;
        HashSet<String> setCircosColors = CircosColors.getSetBasicColors();
        Iterator<String> setCircosColorsIterator = setCircosColors.iterator();

        Integer currId = null;
        String currAuthor = null;
        String currAuthors = null;
        String currTitle = null;
        String currJournal = null;
        Integer currYearInt = null;
        String currYearString = null;
        Integer minYear = 2100;
        Integer maxYear = 0;

        br = new BufferedReader(new FileReader(fileName));

        LinkedHashMap<Integer, HashMap<String, String>> listLinesOfOriginalFile = new LinkedHashMap();
        HashMultiset<String> setCurrentAuthors = HashMultiset.create();;
        while ((currLine = br.readLine()) != null) {

            //System.out.println("currLine is: "+currLine);
            if (currLine.startsWith("@")) {
                setCurrentAuthors = HashMultiset.create();
                currId = Integer.parseInt(currLine.replaceAll("(.*:)(.*),", ("$2")));
                System.out.println("currId: " + currId);
                continue;
            }
            if (currLine.startsWith("author")) {
                currAuthors = currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2");
//                System.out.println("currAuthor: " + currAuthor);
                if (currAuthors.contains("and")) {
                    String[] arrayAuthors = currAuthors.split("and");
                    for (String author : arrayAuthors) {
                        currAuthor = author.trim().replaceAll("[, ]", ".");
                        if (!setCircosColorsIterator.hasNext()) {
                            setCircosColorsIterator = setCircosColors.iterator();
                        }
                        mapAuthorsToColors.put(currAuthor, setCircosColorsIterator.next());
                        setCurrentAuthors.add(currAuthor);
                        multisetAuthorsGlobal.add(currAuthor);


                    }
                    continue;
                }
            }
            if (currLine.startsWith("title")) {
                currTitle = currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2");
//                System.out.println("currTitle: " + currTitle);
                continue;
            }
            if (currLine.startsWith("journal")) {
                currJournal = currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2");
//                System.out.println("currJournal: " + currJournal);
                continue;
            }
            if (currLine.startsWith("year")) {
                currYearInt = Integer.parseInt(currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2"));
                currYearString = currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2");
                System.out.println("currYear: " + currYearInt);

                if (currYearInt < minYear) {
                    minYear = currYearInt;
                }
                if (currYearInt > maxYear) {
                    maxYear = currYearInt;
                }

                continue;
            }

            if (currLine.startsWith("}")) {
                HashMap<String, String> currEntry = new HashMap();
                currEntry.put("authors", currAuthors);
                System.out.println("author(s): " + currAuthors);
                currEntry.put("title", currTitle);
                currEntry.put("journal", currJournal);
                currEntry.put("year", currYearString);
                System.out.println("currYear: " + currYearInt);
                listLinesOfOriginalFile.put(currId, currEntry);
                if (currYearInt > startDate & currYearInt < endDate) {
                    multisetAuthorsLocal.addAll(setCurrentAuthors);
                }
            }
        }
        System.out.println("minYear: " + minYear);
        System.out.println("maxYear: " + maxYear);
        System.out.println("multisetAuthorsLocal size: " + multisetAuthorsLocal.size());
        System.out.println("author nbs size: " + multisetAuthorsLocal.count(BiblioPortrait.nameOfAuthor.replaceAll("[, ]", ".")));


        //calculating the offset to put the main author at the right angle on the right
        bw = new BufferedWriter(new FileWriter(filePath + "image.conf"));
        float offsetAngle = ((float) multisetAuthorsLocal.count(BiblioPortrait.nameOfAuthor.replaceAll("[, ]", ".")) + multisetAuthorsLocal.elementSet().size()) / (float) multisetAuthorsLocal.size() * 180;
        System.out.println("offsetAngle = " + offsetAngle);
        bw.write("angle_offset = 152\n");
        bw.write("background = white" + "\n");
        bw.write("dir   = ." + "\n");
        bw.write("file  = circos_"+BiblioPortrait.rangeDates+".png" + "\n");
        bw.write("png   = yes" + "\n");
        bw.write("svg   = yes" + "\n");
        bw.write("# radius of inscribed circle in image" + "\n");
        bw.write("radius         = 1500p" + "\n");

        bw.write("# by default angle=0 is at 3 o'clock position" + "\n");

        bw.write("#angle_orientation = clockwise" + "\n");

        bw.write("auto_alpha_colors = yes" + "\n");
        bw.write("auto_alpha_steps  = 5" + "\n");
        bw.close();


        //writing the co-authors in an ordered list beginning with the main author
        circosConfig.append(CircosConfigFile.getConfig());
        circosConfig.append("chromosomes  = ");
        Iterator<String> multisetAuthorsLocalIterator = multisetAuthorsLocal.elementSet().iterator();
        Iterator<String> multisetAuthorsGlobalIterator = multisetAuthorsLocal.elementSet().iterator();
        while (multisetAuthorsGlobalIterator.hasNext()) {

            currAuthor = multisetAuthorsGlobalIterator.next();
            circosConfig.append(currAuthor);
            circosConfig.append(",");
        }

        circosConfig.append("\n");

        circosConfig.append("chromosomes_order  = ");
        circosConfig.append(BiblioPortrait.nameOfAuthor.replaceAll("[, ]", "."));
        circosConfig.append(",");

        multisetAuthorsGlobalIterator = multisetAuthorsGlobal.elementSet().iterator();

        while (multisetAuthorsGlobalIterator.hasNext()) {

            currAuthor = multisetAuthorsGlobalIterator.next();
            if (!currAuthor.equals(BiblioPortrait.nameOfAuthor.replaceAll("[, ]", "."))) {
                circosConfig.append(currAuthor);
                circosConfig.append(",");
            }
        }

        circosConfig.append("\n");




        // CREATES EDGES FOR EACH PAIR OF CO-AUTHOR
        Iterator<Entry<Integer, HashMap<String, String>>> listLinesOfOriginalFileIterator = listLinesOfOriginalFile.entrySet().iterator();
        while (listLinesOfOriginalFileIterator.hasNext()) {

            Entry<Integer, HashMap<String, String>> currEntry = listLinesOfOriginalFileIterator.next();
            currAuthors = currEntry.getValue().get("authors");
            Integer currYear = Integer.parseInt(currEntry.getValue().get("year"));
            if (currYear > startDate & currYear < endDate & currAuthors.contains("and")) {
                String[] arrayAuthors = currAuthors.split("and");

                for (int i = 0; i < arrayAuthors.length - 1; i++) {
                    for (int j = i + 1; j < arrayAuthors.length; j++) {
                        String[] onePairOfCoAuthors = new String[6];
                        String sourceAuthor = arrayAuthors[i].trim().replaceAll("[, ]", ".");
                        String targetAuthor = arrayAuthors[j].trim().replaceAll("[, ]", ".");

                        //this insures that the edges will have the most frequent node as the source, insuring that the edges will be of the color of the largest nodes.
                        if (multisetAuthorsLocal.count(targetAuthor) > multisetAuthorsLocal.count(sourceAuthor)) {
                            sourceAuthor = targetAuthor;
                            targetAuthor = arrayAuthors[i].trim().replaceAll("[, ]", ".");
                        }
                        if (sourceAuthor.equals(BiblioPortrait.nameOfAuthor.replaceAll("[, ]", "."))) {
//                        onePairOfCoAuthors.append(sourceAuthor).append(fieldDelimiter).append("0").append(fieldDelimiter).append(multisetAuthorsLocal.count(sourceAuthor)).append(fieldDelimiter).append(targetAuthor).append(fieldDelimiter).append(0).append(fieldDelimiter).append(multisetAuthorsLocal.count(targetAuthor)).append("\n");
                            onePairOfCoAuthors[0] = sourceAuthor;
                            onePairOfCoAuthors[1] = "0";
                            onePairOfCoAuthors[2] = String.valueOf(multisetAuthorsGlobal.count(sourceAuthor));
                            onePairOfCoAuthors[3] = targetAuthor;
                            onePairOfCoAuthors[4] = "0";
                            onePairOfCoAuthors[5] = String.valueOf(multisetAuthorsGlobal.count(targetAuthor));
                            multimapColorsToLinks.put(mapAuthorsToColors.get(sourceAuthor), onePairOfCoAuthors);
                        }

                    }
                }
            }
        }



        // CREATES THE CIRCOS CONFIG FILE AND ONE FILE PER COLOR OF LINKS

        Iterator<String> multimapColorsToLinksIterator = multimapColorsToLinks.keySet().iterator();

        circosConfig.append("<links>");
        circosConfig.append("\n");
        circosConfig.append("crest  = 0.5");
        circosConfig.append("\n");
        circosConfig.append("bezier_radius        = 0.5r");
        circosConfig.append("\n");
        circosConfig.append("bezier_radius_purity = 0.75");
        circosConfig.append("\n");
        circosConfig.append("thickness    = 0.1");
        circosConfig.append("\n");
        circosConfig.append("radius       = 0.975r");
        circosConfig.append("\n");
        while (multimapColorsToLinksIterator.hasNext()) {
            String currColor = multimapColorsToLinksIterator.next();
            Iterator<String[]> multimapColorsToLinksSetOfLinksIterator = multimapColorsToLinks.get(currColor).iterator();
            while (multimapColorsToLinksSetOfLinksIterator.hasNext()) {

                String[] currEdge = multimapColorsToLinksSetOfLinksIterator.next();

                String currFileName = currColor + "_" + currEdge[3] + ".txt";
                int thickness = Integer.parseInt(currEdge[5]) / 2;
                if (thickness < 1) {
                    thickness = 1;
                }
                circosConfig.append("<link>").append("\n");
                circosConfig.append("show         = yes").append("\n");
                circosConfig.append("thickness    =").append(thickness).append("\n");
                circosConfig.append("file         = ").append(currFileName).append("\n");
                circosConfig.append("color        = ").append(currColor).append("\n");
                circosConfig.append("record_limit = 5000").append("\n");
                circosConfig.append("ribbon = no").append("\n");
                circosConfig.append("</link>").append("\n");

                bw = new BufferedWriter(new FileWriter(filePath + currFileName));
                String edgeToWrite = currEdge[0] + fieldDelimiter + currEdge[1] + fieldDelimiter + currEdge[2] + fieldDelimiter + currEdge[3] + fieldDelimiter + currEdge[4] + fieldDelimiter + currEdge[5];
                bw.write(edgeToWrite);
                bw.close();
            }

        }
        circosConfig.append("</links>").append("\n");
        circosConfig.append("<<include etc/housekeeping.conf>>");

        bw = new BufferedWriter(new FileWriter(filePath + "circosConfig.txt"));

        bw.write(circosConfig.toString());
        bw.close();


        // CREATES A LIST OF NODES AND THEIR COLORS
        bw = new BufferedWriter(new FileWriter(filePath + "nodes.txt"));
        multisetAuthorsGlobalIterator = multisetAuthorsGlobal.elementSet().iterator();
        int authorCounter = 0;

        while (multisetAuthorsGlobalIterator.hasNext()) {
            authorCounter++;
            currAuthor = multisetAuthorsGlobalIterator.next();
//            System.out.println("currAuthor: " + currAuthor);
//            if (multisetAuthorsLocal.count(currAuthor) > 0) {
            stringBuilderAuthors.append("chr -").append(fieldDelimiter).append(currAuthor).append(fieldDelimiter).append(currAuthor).append(fieldDelimiter).append("0").append(fieldDelimiter).append(multisetAuthorsGlobal.count(currAuthor)).append(fieldDelimiter).append(mapAuthorsToColors.get(currAuthor)).append("\n");
        }
//        }

        bw.write(stringBuilderAuthors.toString());
        bw.close();
    }

    static void ExtractTitles(String fileName) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        BufferedWriter bw = new BufferedWriter(new FileWriter("titles_" + fileName));

        String currLine;
        StringBuilder bufferToWrite = new StringBuilder();
        int count = 0;
        while ((currLine = br.readLine()) != null) {
            if (currLine.startsWith("title")) {

                currLine = currLine.substring(currLine.lastIndexOf("{") + 1, currLine.indexOf("}"));
                bufferToWrite.append(currLine).append("\n");

            }
        }

        bw.write(bufferToWrite.toString());
        bw.close();
    }

    static void ExtractText(String folderPath) throws FileNotFoundException, IOException {


        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\C. Levallois\\Documents\\NetBeansProjects\\BiblioPortrait\\" + "Bosch_text.txt"));
        System.out.println("file writer is: " + "C:\\Users\\C. Levallois\\Documents\\NetBeansProjects\\BiblioPortrait\\" + "Bosch_text.txt");
        StringBuilder bufferToWrite = new StringBuilder();
        File path = new File(folderPath);

        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            String extractedText;
            if (files[i].isFile() & files[i].getName().endsWith(".pdf")) { //this line weeds out other directories/folders
                System.out.println(files[i]);
//sets a limit to documents treated for TESTING purposes
//                if (i == 25) {
//                    break;
//                }
                try {
                    PDDocument pdfDocument = PDDocument.load(files[i]);
                    extractedText = new PDFTextStripper("UTF-8").getText(pdfDocument);
                    extractedText = extractedText.replaceAll("\\p{C}", " ");
                    bufferToWrite.append(extractedText).append("\n");
                    pdfDocument.close();
                } catch (java.io.FileNotFoundException e) {
                } catch (java.io.IOException IO) {
                }

            }
        }
        bw.write(bufferToWrite.toString());
        bw.close();
    }
}
