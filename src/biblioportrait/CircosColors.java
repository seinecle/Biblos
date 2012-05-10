/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biblioportrait;

import java.util.HashSet;

/**
 *
 * @author C. Levallois
 */
public class CircosColors {


    
    static HashSet<String> getSetBasicColors (){
        
    HashSet<String> basicColors = new HashSet();
    basicColors.add("green");
    basicColors.add("red");
    basicColors.add("blue");
    basicColors.add("orange");
    basicColors.add("purple");
    basicColors.add("yellow");
    
        
    return basicColors;    
    }
    
}
