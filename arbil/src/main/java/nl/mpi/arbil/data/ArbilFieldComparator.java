/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.util.Comparator;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldComparator implements Comparator<ArbilField> {

    public int compare(ArbilField firstColumn, ArbilField secondColumn) {
        try {
            int baseIntA = ((ArbilField) firstColumn).getFieldOrder();
            int comparedIntA = ((ArbilField) secondColumn).getFieldOrder();
            int returnValue = baseIntA - comparedIntA;
            if (returnValue == 0) {
                // if the xml node order is the same then also sort on the strings
                String baseStrA = firstColumn.getFieldValue();
                String comparedStrA = secondColumn.getFieldValue();
                returnValue = baseStrA.compareToIgnoreCase(comparedStrA);
            }
            return returnValue;
        } catch (Exception ex) {
            //bugCatcher.logError(ex);
            return 1;
        }
    }
}