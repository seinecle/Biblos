/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biblioportrait;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author C. Levallois
 */
public class BiblioPortrait {

    /**
     * @param args the command line arguments
     */
    public static String nameOfAuthor = "Bosch, F.A.J. van den";
    public static String field_delimiter = "\t";
    public static String rangeDates;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //Utils.ExtractAuthors("bibtex.txt");
        rangeDates = "1995-2000";
//        int startDate = 1979;
//        int endDate;
//        for (int i = 0; i < 33; i = i + 5) {
//            endDate = startDate+i;
//            rangeDates = startDate+"-"+endDate;
            Utils.ExtractAuthorsForCircos("bibtex.txt", rangeDates, field_delimiter);
//            startDate = endDate;
//        }
        //Utils.ExtractTitles("bibtex.txt");
        //Utils.ExtractTextFromPdFFolder("pdfs\\");
    }
}
