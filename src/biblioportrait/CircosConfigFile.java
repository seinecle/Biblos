/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package biblioportrait;

/**
 *
 * @author C. Levallois
 */
public class CircosConfigFile {

    static String getConfig() {
        StringBuilder circosConfig = new StringBuilder();
        String ln = "\n";
        circosConfig.append("<<include etc/colors_fonts_patterns.conf>>");
        circosConfig.append(ln);
        circosConfig.append("<<include ideogram.conf>>>");
        circosConfig.append(ln);
        circosConfig.append("<<include ticks.conf>>");
        circosConfig.append(ln);
        circosConfig.append("<image>");
        circosConfig.append(ln);
        circosConfig.append("<<include etc/image.conf>>");
        circosConfig.append(ln);
        circosConfig.append("</image>");
        circosConfig.append(ln);
        circosConfig.append("chromosomes_units           = 100");
        circosConfig.append(ln);
        circosConfig.append("chromosomes_display_default = yes");
        circosConfig.append(ln);
        circosConfig.append("karyotype = data/nodes.txt");
        circosConfig.append(ln);

        return circosConfig.toString();
    }
}
