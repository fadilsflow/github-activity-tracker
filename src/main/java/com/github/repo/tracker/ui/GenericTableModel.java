package com.github.repo.tracker.ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Model tabel generik yang dapat dipakai untuk berbagai tipe data.
 * Memanfaatkan Program Generik (syarat 1).
 *
 * @param <T> tipe data baris
 */
public class GenericTableModel<T> extends AbstractTableModel {

    private final String[] columnNames;
    private final Function<T, Object>[] mappers;
    private final List<T> data;

    @SafeVarargs
    public GenericTableModel(String[] columnNames, Function<T, Object>... mappers) {
        this.columnNames = columnNames;
        this.mappers = mappers;
        this.data = new ArrayList<>();
    }

    public void setData(List<T> newData) {
        data.clear();
        data.addAll(newData);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T item = data.get(rowIndex);
        return mappers[columnIndex].apply(item);
    }

    public T getRow(int row) {
        return data.get(row);
    }
} 