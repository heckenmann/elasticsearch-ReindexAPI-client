/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.heckenmann.esreindex.gui;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author heckenmann
 */
public class FieldsTableModel extends DefaultTableModel {

    public FieldsTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column >= 2;
    }
}
