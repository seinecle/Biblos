/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biblioportrait;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import java.io.*;
import java.util.*;
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

    static void ExtractAuthorsForCircos(String fileName, String dateRange) throws FileNotFoundException, IOException {

        String filePath = "D:\\Docs Pro Clement\\circos\\circos-0.60\\data\\";


        //Extracting the dates of the DATE RANGE
        String[] stringDates = dateRange.split("-");
        int startDate = Integer.parseInt(stringDates[0]);
        int endDate = Integer.parseInt(stringDates[1]);


        HashMultiset<String> multisetAuthors = HashMultiset.create();
        HashMap<String, String> mapAuthorsToColors = new HashMap();
        HashMultimap<String, String> multimapColorsToLinks = HashMultimap.create();
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
        boolean newEntry;

        br = new BufferedReader(new FileReader(fileName));
        bw = new BufferedWriter(new FileWriter("bibTexInTimeRange.txt"));
        boolean goodYear = false;

        LinkedHashMap<Integer, HashMap<String, String>> listLinesOfOriginalFile = new LinkedHashMap();
        while ((currLine = br.readLine()) != null) {
            //System.out.println("currLine is: "+currLine);
            if (currLine.startsWith("@")) {
                currId = Integer.parseInt(currLine.replaceAll("(.*:)(.*),", ("$2")));
                newEntry = true;
                System.out.println("currId: " + currId);
                continue;
            }
            if (currLine.startsWith("author")) {
                currAuthors = currLine.replaceAll("(.*\\{)(.*)(\\}.*)", "$2");
//                System.out.println("currAuthor: " + currAuthor);
                if (currAuthors.contains("and")) {
                    String[] arrayAuthors = currAuthors.split("and");
                    for (String author : arrayAuthors) {
                        currAuthor = author.trim().replaceAll("[ ,]", "_");
                        if (!setCircosColorsIterator.hasNext()) {
                            setCircosColorsIterator = setCircosColors.iterator();
                        }
                        mapAuthorsToColors.put(currAuthor, setCircosColorsIterator.next());

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
                continue;
            }

            if (currLine.startsWith("}") & currYearInt > startDate & currYearInt < endDate) {
                HashMap<String, String> currEntry = new HashMap();
                currEntry.put("authors", currAuthors);
                System.out.println("authors" + currAuthors);
                currEntry.put("title", currTitle);
                currEntry.put("journal", currJournal);
                currEntry.put("year", currYearString);
                System.out.println("currYear" + currYearInt);
                listLinesOfOriginalFile.put(currId, currEntry);
                newEntry = false;
            }

        }





        // CREATES EDGES FOR EACH PAIR OF CO-AUTHOR
        Iterator<Entry<Integer, HashMap<String, String>>> listLinesOfOriginalFileIterator = listLinesOfOriginalFile.entrySet().iterator();
        listLinesOfOriginalFileIterator = listLinesOfOriginalFile.entrySet().iterator();
        while (listLinesOfOriginalFileIterator.hasNext()) {

            Entry<Integer, HashMap<String, String>> currEntry = listLinesOfOriginalFileIterator.next();
            currAuthors = currEntry.getValue().get("authors");
            if (currAuthors.contains("and")) {
                String[] arrayAuthors = currAuthors.split("and");

                for (int i = 0; i < arrayAuthors.length - 1; i++) {
                    for (int j = i + 1; j < arrayAuthors.length; j++) {
                        StringBuilder onePairOfCoAuthors = new StringBuilder();
                        String sourceAuthor = arrayAuthors[i].trim().replaceAll("[ ,]", "_");
                        String targetAuthor = arrayAuthors[j].trim().replaceAll("[ ,]", "_");
                        onePairOfCoAuthors.append(sourceAuthor).append(" 0 ").append(multisetAuthors.count(sourceAuthor)).append(" ").append(targetAuthor).append(" 0 ").append(multisetAuthors.count(targetAuthor)).append("\n");
                        multimapColorsToLinks.put(mapAuthorsToColors.get(sourceAuthor), onePairOfCoAuthors.toString());
                        multisetAuthors.add(sourceAuthor);
                        multisetAuthors.add(targetAuthor);

                    }
                }
            }
        }



        // CREATES THE CIRCOS CONFIG FILE AND ONE FILE PER COLOR OF LINKS
        StringBuilder circosConfig = new StringBuilder();
        Iterator<String> multimapColorsToLinksIterator = multimapColorsToLinks.keySet().iterator();
        circosConfig.append(CircosConfigFile.getConfig());
        circosConfig.append("<links>");
        circosConfig.append("\n");
        while (multimapColorsToLinksIterator.hasNext()) {
            String currColor = multimapColorsToLinksIterator.next();
            String currFileName = currColor + ".txt";

            circosConfig.append("<link>").append("\n");
            circosConfig.append("show         = yes").append("\n");
            circosConfig.append("thickness    = 20").append("\n");
            circosConfig.append("file         = data/").append(currFileName).append("\n");
            circosConfig.append("color        = ").append(currColor).append("\n");
            circosConfig.append("record_limit = 5000").append("\n");
            circosConfig.append("ribbon = no").append("\n");
            circosConfig.append("</link>").append("\n");

            bw = new BufferedWriter(new FileWriter(filePath + currFileName));
            Set<String> setCurrLinks = multimapColorsToLinks.get(currColor);

            Iterator<String> setCurrLinksIterator = setCurrLinks.iterator();
            StringBuilder linksOfOneColorToWrite = new StringBuilder();
            while (setCurrLinksIterator.hasNext()) {
                linksOfOneColorToWrite.append(setCurrLinksIterator.next());
            }
            bw.write(linksOfOneColorToWrite.toString());
            bw.close();
        }
        circosConfig.append("</links>").append("\n");
        circosConfig.append("<<include etc/housekeeping.conf>>");

        bw = new BufferedWriter(new FileWriter("circosConfig.txt"));

        bw.write(circosConfig.toString());
        bw.close();


        // CREATES A LIST OF NODES AND THEIR COLORS
        bw = new BufferedWriter(new FileWriter(filePath + "nodes.txt"));
        Iterator<String> multisetAuthorsIterator = multisetAuthors.elementSet().iterator();
        int authorCounter = 0;

        while (multisetAuthorsIterator.hasNext()) {
            authorCounter++;
            currAuthor = multisetAuthorsIterator.next();
            stringBuilderAuthors.append("chr - ").append(currAuthor).append(" ").append(currAuthor).append(" 0 ").append(multisetAuthors.count(currAuthor)).append(" ").append(mapAuthorsToColors.get(currAuthor)).append("\n");

        }

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
