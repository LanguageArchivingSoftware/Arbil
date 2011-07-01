package nl.mpi.arbil.ui;

import java.util.Enumeration;
import javax.swing.table.DefaultTableModel;

/**
 * Document   : ArbilFieldViewTableModel
 * This table model is used to edit the field view for a given imdi table model
 * Split from ImdiFieldViews on : Dec 16, 2008, 10:27:30 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFieldViewTableModel extends DefaultTableModel {

    private ArbilTableModel imdiTableModel;

    public ArbilFieldViewTableModel(ArbilTableModel localImdiTableModel) {
	imdiTableModel = localImdiTableModel;
	//System.out.println("setting to: " + viewLabel);
	// "Column Description",  is not relevant here because this is a list of column names not imdi fields
	setColumnIdentifiers(new String[]{"Column Name", "Show Only", "Hide", "Column width"}); //, "Always Display"
	// we want a table model even if it has no rows
	ArbilFieldView currentView = imdiTableModel.getFieldView();
	if (currentView != null) {
	    // loop the known columns
	    for (Enumeration knownColumnNames = ((ArbilFieldView) currentView).getKnownColumns(); knownColumnNames.hasMoreElements();) {
		String currentFieldName = knownColumnNames.nextElement().toString();
		this.addRow(new Object[]{currentFieldName,
			    //GuiHelper.imdiSchema.getHelpForField(currentFieldName),
			    // set the show only fields
			    ((ArbilFieldView) currentView).isShowOnlyColumn(currentFieldName),
			    // set the hidden fields
			    ((ArbilFieldView) currentView).isHiddenColumn(currentFieldName),
			    // set width
			    widthToString(((ArbilFieldView) currentView).getColumnWidth(currentFieldName))
			//,
			// set alays show fields
			//((LinorgFieldView) currentView).isAlwaysShowColumn(currentFieldName)
			});
	    }
	}
    }
    Class[] types = new Class[]{
	java.lang.String.class, /*java.lang.String.class,*/ java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class
    };
    private int showOnlyEnabledCount = -1;
    private final int showOnlyColumn = 1;
    private final int fieldNameColumn = 0;
    private final int hideColumn = 2;
    private final int widthColumn = 3;

    @Override
    public Class getColumnClass(int columnIndex) {
	return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
//        System.out.println("isCellEditable showOnlyEnabledCount: " + showOnlyEnabledCount);
	if (column == hideColumn) {
	    if (showOnlyEnabledCount < 0) { // count the show only selection
		showOnlyEnabledCount = 0;
		for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
		    //if (((Boolean) getValueAt(rowCounter, showOnlyColumn)) == true) {
		    if (getValueAt(rowCounter, showOnlyColumn).equals(true)) {
			showOnlyEnabledCount++;
		    }
		}

	    }
	    return showOnlyEnabledCount == 0;
//                    return (getValueAt(row, showOnlyColumn).equals(true) || showOnlyEnabledCount == 0);
	}
	return (column == showOnlyColumn || column == hideColumn || column == widthColumn);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
	super.setValueAt(aValue, row, column);
//        System.out.println("setValueAt showOnlyEnabledCount reset");
	// clear the show only count to retrigger the checking process
	showOnlyEnabledCount = -1;
//                fireTableDataChanged();
	boolean booleanState = aValue.equals(true);
	String targetColumnName = getValueAt(row, fieldNameColumn).toString();
	switch (column) {
	    case showOnlyColumn:
		if (booleanState) {
		    imdiTableModel.getFieldView().addShowOnlyColumn(targetColumnName);
		} else {
		    imdiTableModel.getFieldView().removeShowOnlyColumn(targetColumnName);
		}
		break;
	    case hideColumn:
		if (booleanState) {
		    imdiTableModel.getFieldView().addHiddenColumn(targetColumnName);
		} else {
		    imdiTableModel.getFieldView().removeHiddenColumn(targetColumnName);
		}
		break;
	    case widthColumn:
		if (aValue != null) {
		    if (aValue instanceof String) {
			String intString = ((String) aValue).trim();
			if (intString.length() > 0) {
			    try {
				Integer width = Integer.parseInt((String) aValue);
				imdiTableModel.setPreferredColumnWidth(targetColumnName, width);
			    } catch (NumberFormatException ex) {
				ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Invalid width for column " + targetColumnName, "Column width invalid");
			    }
			    break;
			}
		    }
		}
		// Empty or invalid column width
		imdiTableModel.setPreferredColumnWidth(targetColumnName, null);
		break;
//            case 4:
//                if (booleanState) {
//                    imdiTableModel.getFieldView().addAlwaysShowColumn(targetColumnName);
//                } else {
//                    imdiTableModel.getFieldView().removeAlwaysShowColumn(targetColumnName);
//                }
//                break;
	}
	imdiTableModel.requestReloadTableData();
	fireTableStructureChanged();
    }//returnTableModel.setRowCount(0);

    private String widthToString(Integer width) {
	if (width == null) {
	    return "";
	} else {
	    return String.valueOf(width);
	}
    }
}