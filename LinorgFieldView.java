/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpi.linorg;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author petwit
 */
class LinorgFieldView implements Serializable {

    private Vector hiddenColumns = new Vector();
    private Vector showOnlyColumns = new Vector();
    private Vector knownColumns = new Vector();
    private Vector alwaysShowColumns = new Vector();

    public void showState() {
        System.out.println("knownColumns: " + knownColumns);
        System.out.println("hiddenColumns: " + hiddenColumns);
        System.out.println("showOnlyColumns: " + showOnlyColumns);
        System.out.println("alwaysShowColumns: " + alwaysShowColumns);
    }

    public void setAlwaysShowColumns(Vector alwaysShowColumns) {
        this.alwaysShowColumns = alwaysShowColumns;
    }

    public Enumeration getAlwaysShowColumns() {
        return this.alwaysShowColumns.elements();
    }

    public void setHiddenColumns(Vector hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    private void setKnownColumns(Vector knownColumns) {
        this.knownColumns = knownColumns;
    }

    public void setShowOnlyColumns(Vector showOnlyColumns) {
        this.showOnlyColumns = showOnlyColumns;
    }

    public LinorgFieldView clone() {
        LinorgFieldView returnFieldView = new LinorgFieldView();
        returnFieldView.setAlwaysShowColumns((Vector) alwaysShowColumns.clone());
        returnFieldView.setHiddenColumns((Vector) hiddenColumns.clone());
        returnFieldView.setKnownColumns((Vector) knownColumns.clone());
        returnFieldView.setShowOnlyColumns((Vector) showOnlyColumns.clone());
        return returnFieldView;
    }

    public void addKnownColumn(String columnName) {
        if (!knownColumns.contains(columnName)) {
            knownColumns.add(columnName);
        }
    }

    public void setShowOnlyColumns(String[] columnsToShow) {
        showOnlyColumns.clear();
        for (int columnCounter = 0; columnCounter < columnsToShow.length; columnCounter++) {
            showOnlyColumns.add(columnsToShow[columnCounter]);
        }
    }

    public void addAlwaysShowColumn(String columnName) {
        System.out.println("addAlwaysShowColumn");
        alwaysShowColumns.add(columnName);
        showState();
    }

    public void removeAlwaysShowColumn(String columnName) {
        System.out.println("removeAlwaysShowColumn");
        alwaysShowColumns.remove(columnName);
        showState();
    }

    public void addShowOnlyColumn(String columnName) {
        System.out.println("addShowOnlyColumn");
        showOnlyColumns.add(columnName);
        showState();
    }

    public void removeShowOnlyColumn(String columnName) {
        System.out.println("removeShowOnlyColumn");
        showOnlyColumns.remove(columnName);
        showState();
    }

    public void addHiddenColumn(String columnName) {
        System.out.println("addHiddenColumn");
        hiddenColumns.add(columnName);
        showState();
    }

    public void removeHiddenColumn(String columnName) {
        System.out.println("removeHiddenColumn");
        hiddenColumns.remove(columnName);
        showState();
    }

    public boolean viewShowsColumn(String currentColumnString) {
        boolean showColumn = true;
//    hiddenColumns, showOnlyColumns, knownColumns, alwaysShowColumns
        if (showOnlyColumns.size() > 0) {
            // set to true if it is in the show only list
            showColumn = showOnlyColumns.contains(currentColumnString);
        }
        if (showColumn) {
            // set to false if in the hidden list
            showColumn = !hiddenColumns.contains(currentColumnString);
        }
        if (!showColumn) {
            // set to true if in the always show list
            showColumn = alwaysShowColumns.contains(currentColumnString);
        }
        return showColumn;
    }

    public Enumeration getKnownColumns() {
        return knownColumns.elements();
    }

    public boolean isShowOnlyColumn(String columnString) {
        return showOnlyColumns.contains(columnString);
    }

    public boolean isHiddenColumn(String columnString) {
        return hiddenColumns.contains(columnString);
    }

    public boolean isAlwaysShowColumn(String columnString) {
        return alwaysShowColumns.contains(columnString);
    }
}
