/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import mpi.linorg.ImdiHelper.ImdiTreeObject;

/**
 *
 * @author petwit
 */
public class ImdiTableModel extends AbstractTableModel {

    private boolean showIcons = false;
    private Hashtable imdiObjectHash = new Hashtable();
    private Hashtable allColumnNames = new Hashtable();
    FieldView tableFieldView;
    private int[] maxColumnWidths;
    int sortColumn = -1;

    public ImdiTableModel() {
        tableFieldView = GuiHelper.imdiFieldViews.getCurrentGlobalView().clone();
    }

    public void setCurrentView(FieldView localFieldView) {
        tableFieldView = localFieldView.clone();
        reloadTableData();
    }

    public ImdiHelper.ImdiTreeObject[] getSelectedImdiNodes(int[] selectedRows) {
        ImdiHelper.ImdiTreeObject[] selectedNodesArray = new ImdiHelper.ImdiTreeObject[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            selectedNodesArray[selectedRowCounter] = (ImdiHelper.ImdiTreeObject) ((ImdiHelper.ImdiField)data[selectedRows[selectedRowCounter]][columnNames.length-1]).parentImdi;
        }
        return selectedNodesArray;
    }

    public Enumeration getImdiNodes() {
        return imdiObjectHash.elements();
    }

    public void setShowIcons(boolean localShowIcons) {
        showIcons = localShowIcons;
    }

    public void addImdiObjects(ImdiTreeObject[] nodesToAdd) {
        for (int draggedCounter = 0; draggedCounter < nodesToAdd.length; draggedCounter++) {
            addImdiObject(nodesToAdd[draggedCounter]);
        }
        reloadTableData();
    }

    public void addImdiObjects(Enumeration nodesToAdd) {
        while (nodesToAdd.hasMoreElements()) {
            addImdiObject((ImdiTreeObject) nodesToAdd.nextElement());
        }
        reloadTableData();
    }

    public void addSingleImdiObject(ImdiTreeObject imdiTreeObject) {
        addImdiObject(imdiTreeObject);
        reloadTableData();
    }

    private void addImdiObject(ImdiTreeObject imdiTreeObject) {
        imdiObjectHash.put(imdiTreeObject.getUrl(), imdiTreeObject);
        Enumeration fieldNames = imdiTreeObject.getFields().keys();
        while (fieldNames.hasMoreElements()) {
            String currentColumnName = fieldNames.nextElement().toString();
            // keep track of the number of times that columns are used by updating the column use count hashtable
            Object currentColumnUse = allColumnNames.get(currentColumnName);
            int currentColumnUseCount = 0;
            if (currentColumnUse == null) {
                currentColumnUseCount = 1;
                tableFieldView.addKnownColumn(currentColumnName);
            } else {
                currentColumnUseCount = ((Integer) currentColumnUse) + 1;
            }
            allColumnNames.put(currentColumnName, currentColumnUseCount);
        }

//            Enumeration tempColoumnEnum = columnNameHash.keys();
//            while (tempColoumnEnum.hasMoreElements()) {
//                System.out.println("column: " + tempColoumnEnum.nextElement());
//            }

//            Vector vecSort = new Vector(columnNameHash.keySet());
//            Collections.sort(vecSort, Collections.reverseOrder());
//            Iterator it = vecSort.iterator();
//            while (it.hasNext()) {
//                String element = (String) it.next();
//                System.out.println(element);
//            }
    }

    public void removeAllImdiRows() {
        imdiObjectHash.clear();
        allColumnNames.clear();
        columnNames = new String[0];
        data = new Object[0][0];
        // add the icon column if icons are to be displayed
        setShowIcons(showIcons);
        reloadTableData();
    }

    // each row contains its relevant imdinodeobject in the last cell which is not displayed
    public void removeImdiRows(int[] selectedRows) {
        Vector nodesToRemove = new Vector();
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("removing: " + selectedRowCounter);
            nodesToRemove.add(((ImdiHelper.ImdiField)data[selectedRows[selectedRowCounter]][columnNames.length-1]).parentImdi);
        }
        removeImdiObjects(nodesToRemove.elements());
    }

    public void copyImdiRows(int[] selectedRows, ClipboardOwner clipBoardOwner) {
        String copiedString = "";
        int firstColumn = 0;
        if (showIcons) {
            firstColumn = 1;
        }
        // add the headers
        for (int selectedColCounter = firstColumn; selectedColCounter < getColumnCount(); selectedColCounter++) {
            copiedString = copiedString + getColumnName(selectedColCounter) + ",";
        }
        copiedString = copiedString + "\n";
        // add the cell data
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("copying row: " + selectedRowCounter);
            for (int selectedColCounter = firstColumn; selectedColCounter < getColumnCount(); selectedColCounter++) {
                copiedString = copiedString + "\"" + data[selectedRows[selectedRowCounter]][selectedColCounter].toString().replace("\"", "\"\"") + "\",";
            }
            copiedString = copiedString + "\n";
        }
        System.out.println("copiedString: " + copiedString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(copiedString);
        clipboard.setContents(stringSelection, clipBoardOwner);
    }

    public void removeImdiObjects(Enumeration nodesToRemove) {
        while (nodesToRemove.hasMoreElements()) {
            Object currentObject = nodesToRemove.nextElement();
            if (currentObject instanceof ImdiTreeObject) {
                ImdiTreeObject imdiTreeObject = (ImdiTreeObject) currentObject;
                System.out.println("removing: " + imdiTreeObject.toString());
                // remove the node
                imdiObjectHash.remove(imdiTreeObject.getUrl());
                // update the used columns
                Enumeration fieldNames = imdiTreeObject.getFields().keys();
                while (fieldNames.hasMoreElements()) {
                    String currentColumnName = fieldNames.nextElement().toString();
                    //System.out.println("currentColumnName: " + currentColumnName);
                    int currentColumnUse = (Integer) allColumnNames.get(currentColumnName);
                    currentColumnUse--;
                    if (currentColumnUse == 0) {
                        allColumnNames.remove(currentColumnName);
                    } else {
                        allColumnNames.put(currentColumnName, currentColumnUse);
                    }
                }
            }
        }
        // refresh the table data
        reloadTableData();
    }

    public void reloadTableData() {
        if (imdiObjectHash.size() > 1) {
            // display the grid view

            // calculate which of the available columns to show
            Enumeration columnNameEnum = allColumnNames.keys();
            Vector displayedColumnNames = new Vector();
            while (columnNameEnum.hasMoreElements()) {
                String currentColumnString = (String) columnNameEnum.nextElement();
                if (tableFieldView.viewShowsColumn(currentColumnString)) {
                    displayedColumnNames.add(currentColumnString);
                }
            }
            // end calculate which of the available columns to show

            // set the column offset to accomadate the icon which is not in the column hashtable
            int firstFreeColumn = 0;
            if (showIcons) {
                System.out.println("showing icon");
                // this assumes that the icon will always be in the leftmost column
                firstFreeColumn = 1;
            }

            // create and populate the colomn names array and prepend the icon and append the imdinode
            columnNames = new String[displayedColumnNames.size() + firstFreeColumn];
            int columnPopulateCounter = firstFreeColumn;
            columnNames[0] = " "; // make sure the the icon column is shown its string is not null
            for (Enumeration currentColumnEnum = displayedColumnNames.elements(); currentColumnEnum.hasMoreElements();) {
                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
                columnNames[columnPopulateCounter] = currentColumnEnum.nextElement().toString();
                columnPopulateCounter++;
            }
            // end create the colomn names array and prepend the icon and append the imdinode

            maxColumnWidths = new int[columnNames.length];

            data = new Object[imdiObjectHash.size()][columnNames.length];
            Enumeration imdiRowsEnum = imdiObjectHash.elements();
            int rowCounter = 0;
            while (imdiRowsEnum.hasMoreElements()) {
                ImdiTreeObject currentNode = (ImdiTreeObject) imdiRowsEnum.nextElement();
                System.out.println("currentNode: " + currentNode.toString());
                Hashtable fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    data[rowCounter][0] = currentNode;
                    maxColumnWidths[0] = currentNode.toString().length();
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNames.length; columnCounter++) {
                    //System.out.println("columnNames[columnCounter]: " + columnNames[columnCounter] + " : " + columnCounter);
                    Object currentValue = fieldsHash.get(columnNames[columnCounter]);
                    if (currentValue != null) {
                        data[rowCounter][columnCounter] = currentValue;
                    } else {
                        data[rowCounter][columnCounter] = "";
                    }

                    //record the column string lengths 
                    int currentLength = (data[rowCounter][columnCounter].toString()).length();
                    if (maxColumnWidths[columnCounter] < currentLength) {
                        maxColumnWidths[columnCounter] = currentLength;
                    }
                }
                rowCounter++;
            }
//            // display the column names use count for testing only
//            Enumeration tempEnum = columnNameHash.elements();
//            int tempColCount = 0;
//            while (tempEnum.hasMoreElements()){
//                data[0][tempColCount] = tempEnum.nextElement().toString();
//                tempColCount++;
//            }
        } else {
            // display the single node view
            maxColumnWidths = new int[2];
            columnNames = new String[]{"Name", "Value"};
            if (imdiObjectHash.size() == 0) {
                data = new Object[0][2];
            } else {
                Enumeration imdiRowsEnum = imdiObjectHash.elements();
                if (imdiRowsEnum.hasMoreElements()) {
                    Hashtable fieldsHash = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getFields();
                    data = new Object[fieldsHash.size()][2];
                    Enumeration labelsEnum = fieldsHash.keys();
                    Enumeration valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    while (labelsEnum.hasMoreElements() && valuesEnum.hasMoreElements()) {
                        data[rowCounter][0] = labelsEnum.nextElement();
                        data[rowCounter][1] = valuesEnum.nextElement();

                        //record the column string lengths 
                        int currentLength = (data[rowCounter][0].toString()).length();
                        if (maxColumnWidths[0] < currentLength) {
                            maxColumnWidths[0] = currentLength;
                        }
                        currentLength = (data[rowCounter][1].toString()).length();
                        if (maxColumnWidths[1] < currentLength) {
                            maxColumnWidths[1] = currentLength;
                        }
                        rowCounter++;
                    }
                }
            }
        }
        // update the table model, note that this could be more specific, ie. just row or all it the columns have changed
        //fireTableDataChanged();
        fireTableStructureChanged();
    }
    private String[] columnNames = new String[0];
    private Object[][] data = new Object[0][0];

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getColumnLength(int col) {
        return maxColumnWidths[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    // JTable uses this method to determine the default renderer
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        boolean returnValue = false;
        if(data[row][col] instanceof ImdiHelper.ImdiField){
            returnValue = ((ImdiHelper.ImdiField)data[row][col]).parentImdi.isLocal();
        }
        System.out.println("Cell is ImdiField: " + returnValue);
//        System.out.println("result: " + (data[row][col] instanceof ImdiHelper.ImdiField));
        return (returnValue);
    }

    public void setValueAt(Object value, int row, int col) {
        if (data[row][col] instanceof ImdiHelper.ImdiField) {
            ImdiHelper.ImdiField currentField = ((ImdiHelper.ImdiField) data[row][col]);
            if (GuiHelper.linorgJournal.saveJournalEntry(currentField.parentImdi.getUrl(), currentField.xmlPath, currentField.fieldValue, value.toString())) {
                currentField.fieldValue = value.toString();
            }
        } else {
            data[row][col] = value;
        }
        fireTableCellUpdated(row, col);
    }

    public void sortByColumn(int columnIndex) {
        // TODO: sort columns
        System.out.println("sortByColumn: " + columnIndex);
        sortColumn = columnIndex;
    //fireTableStructureChanged();
    }

    public void hideColumn(int columnIndex) {
        System.out.println("hideColumn: " + columnIndex);
        // TODO: hide column
        System.out.println("hideColumn: " + getColumnName(columnIndex));
        tableFieldView.addHiddenColumn(getColumnName(columnIndex));
        reloadTableData();
    }

    public void showOnlyCurrentColumns() {
        tableFieldView.setShowOnlyColumns(columnNames);
    }

    public FieldView getFieldView() {
        return tableFieldView;
    }

    public void addChildTypeToDisplay(String childType) {
        System.out.println("addChildTypeToDisplay: " + childType);
    // TODO: add child type as column
    }

    public Object[] getChildNames() {
        Vector childNames = new Vector();
        Enumeration imdiRowsEnum = imdiObjectHash.elements();
        while (imdiRowsEnum.hasMoreElements()) {
            Enumeration childEnum = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getChildEnum();
            while (childEnum.hasMoreElements()) {
                // TODO: maybe check the children for children before adding them to this list
                String currentChildName = childEnum.nextElement().toString();
                if (!childNames.contains(currentChildName)) {
                    childNames.add(currentChildName);
                }
            }
        }
        return childNames.toArray();
    }

    public Vector getMatchingRows(int sampleRowNumber) {
        System.out.println("MatchingRows for: " + sampleRowNumber);
        Vector matchedRows = new Vector();
        if (sampleRowNumber > -1 && sampleRowNumber < getRowCount()) {
            for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
                boolean rowMatches = true;
                for (int colCounter = 0; colCounter < getColumnCount(); colCounter++) {
                    if (!getValueAt(rowCounter, colCounter).toString().equals(getValueAt(sampleRowNumber, colCounter).toString())) {
                        rowMatches = false;
                        break;
                    }
                }
                //System.out.println("Checking: " + getValueAt(sampleRowNumber, 0) + " : " + getValueAt(rowCounter, 0));
                if (rowMatches) {
                    //System.out.println("Matched: " + rowCounter + " : " + getValueAt(rowCounter, 0));
                    matchedRows.add(rowCounter);
                }
            }
        }
        return matchedRows;
    }
}
