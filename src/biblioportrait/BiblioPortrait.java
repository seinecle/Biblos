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
    public static void main(String[] args) throws FileNotFoundException, IOException {
         //Utils.ExtractAuthors("bibtex.txt");
          Utils.ExtractAuthorsForCircos("bibtex.txt","1994-1997");
        //Utils.ExtractTitles("bibtex.txt");
        //Utils.ExtractTextFromPdFFolder("pdfs\\");
    }
}
