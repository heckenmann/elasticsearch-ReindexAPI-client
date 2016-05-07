package de.heckenmann.esreindex;

import de.heckenmann.esreindex.gui.MainFrame;
import de.heckenmann.esreindex.rest.RestController;
import javax.swing.JOptionPane;

/**
 *
 * @author heckenmann
 */
public class ESReindex {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainFrame f = new MainFrame(new RestController());
                JOptionPane.showMessageDialog(f, "Should be tested for your purposes at first in your testing environment.\n"
                        + "Developed with elasticsearch version 2.3.2.\n"
                        + "For full functionality \"script.inline: true\" should be set in elasticsearch.yml.", "Read first!!!", JOptionPane.WARNING_MESSAGE);
                f.setVisible(true);
            }
        });
    }
}
