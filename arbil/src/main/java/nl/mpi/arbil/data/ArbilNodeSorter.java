package nl.mpi.arbil.data;

import java.io.Serializable;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * ArbilTreeNodeSorter.java
 * Created on Aug 11, 2009, 11:08:48 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSorter extends NumberedStringComparator implements  Serializable {

    public int compare(Object object1, Object object2) {
	Object userObject1;
	Object userObject2;
	if (object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode) {
	    userObject1 = ((DefaultMutableTreeNode) object1).getUserObject();
	    userObject2 = ((DefaultMutableTreeNode) object2).getUserObject();
	} else if (object1 instanceof ArbilDataNode && object2 instanceof ArbilDataNode) {
	    userObject1 = ((ArbilDataNode) object1);
	    userObject2 = ((ArbilDataNode) object2);
	} else {
	    throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
	}
	if (userObject1 instanceof ArbilDataNode && userObject2 instanceof ArbilDataNode) {
	    final int typeIndex1 = getTypeIndex((ArbilDataNode) userObject1);
	    final int typeIndex2 = getTypeIndex((ArbilDataNode) userObject2);
	    // sort by catalogue then corpus then session etc. then by the text order
	    if (typeIndex1 == typeIndex2) {
		return compareSameTypeNodes((ArbilDataNode) userObject1, (ArbilDataNode) userObject2);
	    } else {
		return typeIndex1 - typeIndex2;
	    }
	} else {
	    //return userObject1.toString().compareToIgnoreCase(object2.toString());
	    throw new IllegalArgumentException("not a ArbilDataNode object: " + object1.toString() + " : " + object2.toString());
	}
    }

    private int compareSameTypeNodes(final ArbilDataNode userObject1, final ArbilDataNode userObject2) {
	final String string1 = userObject1.toString();
	final String string2 = userObject2.toString();

	Integer resultInt = compareNumberedStrings(string1, string2);
	if (resultInt == null) {
	    resultInt = string1.compareToIgnoreCase(string2);
	    if (resultInt == 0) { // make sure that to objects dont get mistaken to be the same just because the string lebels are the same
		resultInt = ((ArbilDataNode) userObject1).getUrlString().compareToIgnoreCase(((ArbilDataNode) userObject2).getUrlString());
	    }
	}
	return resultInt;
    }

    protected int getTypeIndex(ArbilDataNode targetDataNode) {
	if (targetDataNode.isInfoLink) {
	    return 1;
	}
	if (targetDataNode.isDataLoaded()) { // caution: this sort can cause the tree to collapse when nodes reload because the nodes will be removed if not in order
	    if (targetDataNode.isCorpus()) {
		return 2;
	    } else if (targetDataNode.isCatalogue()) {
		return 3;
	    } else if (targetDataNode.isSession()) {
		return 4;
	    } else if (targetDataNode.isChildNode()) {
		return 5;
	    } else if (targetDataNode.isDirectory()) {
		return 6;
	    } else {
		return 7;
	    }
	} else {
	    // put the loading nodes at the end to help the tree sorting and rendering process
	    return 10;
	}
    }
}
