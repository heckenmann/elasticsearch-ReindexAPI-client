/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.heckenmann.esreindex.gui;

/**
 *
 * @author heckenmann
 */
public class GuiHelper {

    public static Object[][] convertFieldsForTable(String[] fields) {
        Object[][] content = new Object[fields.length][4];
        for (int i = 0; i < content.length; i++) {
            content[i][0] = fields[i];
            content[i][1] = "->";
            content[i][2] = fields[i];
        }
        return content;
    }
}
