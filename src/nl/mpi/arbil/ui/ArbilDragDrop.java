package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ImdiTableModel;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.data.importexport.ImportExportDialog;
import nl.mpi.arbil.data.ArbilNodeObject;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.MetadataBuilder;

/**
 * Document   :  ArbilDragDrop
 * Created on :  Tue Sep 09 15:02:56 CEST 2008
 * @author Peter.Withers@mpi.nl
 */
public class ArbilDragDrop {

    private static ArbilDragDrop singleInstance = null;

    static synchronized public ArbilDragDrop getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ArbilDragDrop();
        }
        return singleInstance;
    }

    private ArbilDragDrop() {
        imdiObjectFlavour = new DataFlavor(ArbilNodeObject.class, "ImdiTreeObject");
        imdiObjectSelection = new ImdiObjectSelection();
    }
    // There are numerous limitations of drag and drop in 1.5 and to overcome the resulting issues we need to share the same transferable object on both the drag source and the drop target
    public DataFlavor imdiObjectFlavour;
    public ImdiObjectSelection imdiObjectSelection;

    public void addDrag(JTable tableSource) {
        tableSource.setDragEnabled(true);
        tableSource.setTransferHandler(imdiObjectSelection);
    }

    public void addDrag(JTree treeSource) {
        treeSource.setDragEnabled(true);
        treeSource.setTransferHandler(imdiObjectSelection);
        treeSource.addTreeSelectionListener(imdiObjectSelection);
        DropTarget target = treeSource.getDropTarget();
        try {
            target.addDropTargetListener(new DropTargetAdapter() {

                public void dragOver(DropTargetDragEvent dtdEvent) {
                    System.out.println("imdiObjectSelection.dropAllowed: " + imdiObjectSelection.dropAllowed);
                    if (imdiObjectSelection.dropAllowed) {
                        dtdEvent.acceptDrag(dtdEvent.getDropAction());
                    } else {
                        dtdEvent.rejectDrag();
                    }
                }

                public void drop(DropTargetDropEvent e) {
                    // handled by the TransferHandler
                }
            });
        } catch (java.util.TooManyListenersException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public void addDrag(JList listSource) {
        listSource.setDragEnabled(true);
        listSource.setTransferHandler(imdiObjectSelection);
    }

    public void addTransferHandler(JComponent targetComponent) {
        //targetComponent.setDragEnabled(true);
        targetComponent.setTransferHandler(imdiObjectSelection);
    }

    public class ImdiObjectSelection extends TransferHandler implements Transferable, javax.swing.event.TreeSelectionListener {

        long dragStartMilliSeconds;
        DataFlavor flavors[] = {imdiObjectFlavour};
        ArbilNodeObject[] draggedImdiObjects;
        DefaultMutableTreeNode[] draggedTreeNodes;
        public boolean selectionContainsArchivableLocalFile = false;
        public boolean selectionContainsLocalFile = false;
        public boolean selectionContainsLocalDirectory = false;
        public boolean selectionContainsImdiResource = false;
        public boolean selectionContainsImdiCorpus = false;
        public boolean selectionContainsImdiInCache = false;
        public boolean selectionContainsImdiCatalogue = false;
        public boolean selectionContainsImdiSession = false;
        public boolean selectionContainsCmdiMetadata = false;
        public boolean selectionContainsImdiChild = false;
        public boolean selectionContainsLocal = false;
        public boolean selectionContainsRemote = false;
        public boolean selectionContainsFavourite = false;
        private JComponent currentDropTarget = null;
        public boolean dropAllowed = false;

        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
            if (evt.getSource() == currentDropTarget) {
                System.out.println("Drag target selection change: " + evt.getSource().toString());
                if (evt.getSource() instanceof ArbilTree) {
                    dropAllowed = canDropToTarget((ArbilTree) evt.getSource());
//                    DropTarget dropTarget = dropTree.getDropTarget();                    
                }
            }
        }

        private boolean canDropToTarget(ArbilTree dropTree) {
            ArbilNodeObject currentLeadSelection = dropTree.getLeadSelectionNode();
            if (currentLeadSelection == null) {
                // this check is for the root node of the trees
                if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(currentDropTarget)) {
                    // allow drop to the favourites tree even when no selection is made
                    // allow drop to only the root node of the favourites tree
                    System.out.println("favourites tree check");
                    return !selectionContainsFavourite;
                } else if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(currentDropTarget)) {
                    //if (dropTree.getSelectionPath().getPathCount() == 1) {
                    // allow import to local tree if no nodes are selected
                    // allow drop to the root node if it is an import
                    System.out.println("local corpus tree check");
                    // todo: enable drag to rootnode from favourites but this change also needs to be done in the context menu
                    return (!selectionContainsFavourite && (selectionContainsImdiCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession || selectionContainsCmdiMetadata));
                }
                System.out.println("no tree check");
                return false;
            } else {
                // this check is for the child nodes of the trees
                System.out.println("currentLeadSelection: " + currentLeadSelection.toString());
//                todo: prevent dragging to self but allow dragging to other branch of parent session
//                todo: look for error dragging actor from favourites
//                todo: look for error in field triggers when merging from favourite (suppress trtiggeres when merging)
                if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(currentDropTarget)) {
                    if (currentLeadSelection.isCmdiMetaDataNode()) {
                        if (currentLeadSelection.getParentDomNode().nodeTemplate == null) {
                            System.out.println("no template for drop target node");
                            return false;
                        }
                        System.out.println("Drop to CMDI: " + currentLeadSelection.getURI().getFragment());
                        String nodePath = currentLeadSelection.getURI().getFragment();
                        if (nodePath == null) {
                            // todo: consider making sure that the dom parent node always has a path
                            nodePath = ".CMD.Components." + currentLeadSelection.getParentDomNode().nodeTemplate.loadedTemplateName;
                        }
                        System.out.println("nodePath:" + nodePath);
                        return (currentLeadSelection.getParentDomNode().nodeTemplate.pathCanHaveResource(nodePath));
                    } else if (currentLeadSelection.isDirectory) {
                        return false; // nothing can be dropped to a directory
                    } else if (currentLeadSelection.isCorpus()) {
                        if ((selectionContainsImdiCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession) && !selectionContainsCmdiMetadata) {
                            return true;
                        }
                    } else if (currentLeadSelection.isCatalogue()) {
                        return false; // nothing can be dropped to a catalogue
                    } else if (currentLeadSelection.isSession()) {
                        if (selectionContainsArchivableLocalFile || (selectionContainsImdiChild && selectionContainsFavourite)) {
                            return true;
                        }
                    } else if (currentLeadSelection.isImdiChild()) {
                        // TODO: in this case we should loop over the dragged nodes and check each one for compatability
                        if (selectionContainsLocalFile || (selectionContainsImdiChild && selectionContainsFavourite)) { // TODO: allow drag drop of appropriate imdi child nodes to sessions and compatable subnodes
                            return true;
                        }
                    }
//        public boolean selectionContainsArchivableLocalFile = false;
//        public boolean selectionContainsLocalFile = false;
//        public boolean selectionContainsLocalDirectory = false;
//        public boolean selectionContainsImdiResource = false;
//        public boolean selectionContainsImdiCorpus = false;
//        public boolean selectionContainsImdiInCache = false;
//        public boolean selectionContainsImdiCatalogue = false;
//        public boolean selectionContainsImdiSession = false;
//        public boolean selectionContainsImdiChild = false;
//        public boolean selectionContainsLocal = false;
//        public boolean selectionContainsRemote = false;
                    return false;
                } else {
                    return false;
                }
            }
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
            System.out.println("exportToClipboard: " + comp);
            createTransferable(null); // clear the transfer objects
            if (comp instanceof ArbilTree) {
                ArbilTree sourceTree = (ArbilTree) comp;
                ArbilNodeObject[] selectedImdiNodes = sourceTree.getSelectedNodes();
                if (selectedImdiNodes != null) {
                    sourceTree.copyNodeUrlToClipboard(selectedImdiNodes);
                }
            } else if (comp instanceof ArbilTable) {
                ArbilTable sourceTable = (ArbilTable) comp;
                sourceTable.copySelectedTableRowsToClipBoard();
            } else {
                super.exportToClipboard(comp, clip, action);
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            if ((c instanceof JTree) || (c instanceof JTable) || (c instanceof JList)) {
                return TransferHandler.COPY;
            } else {
                return TransferHandler.NONE;
            }
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor flavor[]) {
            System.out.println("canImport: " + comp);
            currentDropTarget = null;
            dropAllowed = false;
            if (comp instanceof JTree) {
                if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(comp)) {
                    System.out.println("localcorpustree so can drop here");
                    if (selectionContainsArchivableLocalFile
                            || //selectionContainsLocalFile ||
                            //selectionContainsLocalDirectory ||
                            //selectionContainsImdiResource ||
                            //selectionContainsLocal ||
                            //selectionContainsRemote ||
                            selectionContainsImdiCorpus
                            || selectionContainsImdiCatalogue
                            || selectionContainsImdiSession
                            || selectionContainsCmdiMetadata
                            || selectionContainsImdiChild) {
                        System.out.println("dragged contents are acceptable");
                        currentDropTarget = comp; // store the source component for the tree node sensitive drop
                        dropAllowed = canDropToTarget((ArbilTree) comp);
                        return true;
                    }
                }
                if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(comp)) {
                    System.out.println("favourites tree so can drop here");
                    if (//selectionContainsArchivableLocalFile &&
                            //selectionContainsLocalFile ||
                            //selectionContainsLocalDirectory &&
                            //selectionContainsImdiResource ||
                            //selectionContainsLocal ||
                            //selectionContainsRemote ||
                            //selectionContainsImdiCorpus ||
                            selectionContainsImdiCatalogue
                            || selectionContainsImdiSession
                            || selectionContainsCmdiMetadata
                            || selectionContainsImdiChild) {
                        System.out.println("dragged contents are acceptable");
                        currentDropTarget = comp; // store the source component for the tree node sensitive drop
                        dropAllowed = canDropToTarget((ArbilTree) comp);
                        return true;
                    }
                }
            } else {
                // search through al the parent nodes to see if we can find a drop target
                dropAllowed = (null != findImdiDropableTarget(comp));
                System.out.println("dropAllowed: " + dropAllowed);
                return dropAllowed;
            }
            System.out.println("canImport false");
            return false;
        }

        private Container findImdiDropableTarget(Container tempCom) {
            while (tempCom != null) {
                if (tempCom instanceof ArbilSplitPanel || tempCom instanceof JDesktopPane) {
                    System.out.println("canImport true");
                    return tempCom;
                }
                tempCom = tempCom.getParent();
            }
            return null;
        }

        @Override
        public Transferable createTransferable(JComponent comp) {
            dragStartMilliSeconds = System.currentTimeMillis();
            draggedImdiObjects = null;
            draggedTreeNodes = null;
            selectionContainsArchivableLocalFile = false;
            selectionContainsLocalFile = false;
            selectionContainsLocalDirectory = false;
            selectionContainsImdiResource = false;
            selectionContainsImdiCorpus = false;
            selectionContainsImdiCatalogue = false;
            selectionContainsImdiSession = false;
            selectionContainsCmdiMetadata = false;
            selectionContainsImdiChild = false;
            selectionContainsLocal = false;
            selectionContainsRemote = false;
            selectionContainsFavourite = false;
//         if (comp != null)  { System.out.println("createTransferable: " + comp.toString()); }
            if (comp instanceof ArbilTree) {
                ArbilTree draggedTree = (ArbilTree) comp;

                // Prevent root node from being dragged
                if (!(draggedTree.getSelectionCount() > 1 || (draggedTree.getSelectionCount() == 1 && draggedTree.getSelectionPath().getPathCount() > 1))) {
                    return null;
                }

                //System.out.println("selectedCount: " + draggedTree.getSelectionCount());
                draggedImdiObjects = new ArbilNodeObject[draggedTree.getSelectionCount()];
                draggedTreeNodes = new DefaultMutableTreeNode[draggedTree.getSelectionCount()];
                for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
                    //System.out.println("parentNode: " + parentNode.toString());
                    if (parentNode.getUserObject() instanceof ArbilNodeObject) {
                        //System.out.println("DraggedImdi: " + parentNode.getUserObject().toString());
                        draggedImdiObjects[selectedCount] = (ArbilNodeObject) (parentNode.getUserObject());
                        draggedTreeNodes[selectedCount] = parentNode;
                    } else {
                        draggedImdiObjects[selectedCount] = null;
                        draggedTreeNodes[selectedCount] = null;
                    }
                }
                classifyTransferableContents();
                return this;
            } else if (comp instanceof ArbilTable) {

                draggedImdiObjects = ((ArbilTable) comp).getSelectedRowsFromTable();
                classifyTransferableContents();
                return this;
            } else if (comp instanceof JList) {
                Object[] selectedValues = ((JList) comp).getSelectedValues();
                //System.out.println("selectedValues: " + selectedValues);
                draggedImdiObjects = new ArbilNodeObject[selectedValues.length];
                for (int selectedNodeCounter = 0; selectedNodeCounter < selectedValues.length; selectedNodeCounter++) {
                    if (selectedValues[selectedNodeCounter] instanceof ArbilNodeObject) {
                        draggedImdiObjects[selectedNodeCounter] = (ArbilNodeObject) selectedValues[selectedNodeCounter];
                    }
                }
                classifyTransferableContents();
                return this;
            }
            return null;
        }

        private void classifyTransferableContents() {
            System.out.println("classifyTransferableContents");
            // classify the draggable bundle to help matching drop targets
            for (ArbilNodeObject currentDraggedObject : draggedImdiObjects) {
                if (currentDraggedObject != null) {
                    if (currentDraggedObject.isLocal()) {
                        selectionContainsLocal = true;
                        System.out.println("selectionContainsLocal");
                        if (currentDraggedObject.isDirectory()) {
                            selectionContainsLocalDirectory = true;
                            System.out.println("selectionContainsLocalDirectory");
                        } else {
                            if (!currentDraggedObject.isMetaDataNode()) {
                                selectionContainsLocalFile = true;
                                System.out.println("selectionContainsLocalFile");
                                if (currentDraggedObject.isArchivableFile()) {
                                    selectionContainsArchivableLocalFile = true;
                                    System.out.println("selectionContainsArchivableLocalFile");
                                }

                            }
                        }
                    } else {
                        selectionContainsRemote = true;
                        System.out.println("selectionContainsRemote");
                    }
                    if (currentDraggedObject.isMetaDataNode()) {
                        if (currentDraggedObject.isLocal() && ArbilSessionStorage.getSingleInstance().pathIsInsideCache(currentDraggedObject.getFile())) {
                            selectionContainsImdiInCache = true;
                            System.out.println("selectionContainsImdiInCache");
                        }
                        if (currentDraggedObject.isImdiChild()) {
                            selectionContainsImdiChild = true;
                            System.out.println("selectionContainsImdiChild");
                            // only an imdichild will contain a resource
                            if (currentDraggedObject.hasResource()) {
                                selectionContainsImdiResource = true;
                                System.out.println("selectionContainsImdiResource");
                            }
                        } else if (currentDraggedObject.isSession()) {
                            selectionContainsImdiSession = true;
                            System.out.println("selectionContainsImdiSession");
                        } else if (currentDraggedObject.isCmdiMetaDataNode()) {
                            selectionContainsCmdiMetadata = true;
                            System.out.println("selectionContainsCmdiMetadata");
                        } else if (currentDraggedObject.isCatalogue()) {
                            selectionContainsImdiCatalogue = true;
                            System.out.println("selectionContainsImdiCatalogue");
                        } else if (currentDraggedObject.isCorpus()) {
                            selectionContainsImdiCorpus = true;
                            System.out.println("selectionContainsImdiCorpus");
                        }
                        if (currentDraggedObject.isFavorite()) {
                            selectionContainsFavourite = true;
                            System.out.println("selectionContainsFavourite");
                        }
                    }
                }
            }
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            // due to the swing api being far to keen to do a drag drop action on the windows platform users frequently loose nodes by dragging them into random locations
            // so to avoid this we check the date time from when the transferable was created and if less than x seconds reject the drop
            if (System.currentTimeMillis() - dragStartMilliSeconds < (100 * 1)) {
                // todo: (has beed reduced to 100 * 1 from 100 * 3) this may be too agressive and preventing valid drag events, particularly since "improveddraggesture" property is now set.
                return false;
            }
            try {
                System.out.println("importData: " + comp.toString());
                if (comp instanceof ArbilTable && draggedImdiObjects == null) {
                    ((ArbilTable) comp).pasteIntoSelectedTableRowsFromClipBoard();
                } else {
                    //System.out.println("draggedImdiObjects: " + draggedImdiObjects);
                    if (draggedImdiObjects != null) {
                        if (comp instanceof JTree && canDropToTarget((ArbilTree) comp)) {
                            System.out.println("comp: " + comp.getName());
                            for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                            }
                            if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(currentDropTarget)) {
                                boolean resultValue = ArbilFavourites.getSingleInstance().toggleFavouritesList(draggedImdiObjects, true);
                                createTransferable(null); // clear the transfer objects
                                return resultValue;
                            } else {
                                ArbilTree dropTree = (ArbilTree) comp;
                                DefaultMutableTreeNode targetNode = TreeHelper.getSingleInstance().getLocalCorpusTreeSingleSelection();
//                                TreeHelper.getSingleInstance().addToSortQueue(targetNode);
                                Object dropTargetUserObject = targetNode.getUserObject();
                                Vector<ArbilNodeObject> importNodeList = new Vector<ArbilNodeObject>();
                                Hashtable<ArbilNodeObject, Vector<ArbilNodeObject>> imdiNodesDeleteList = new Hashtable<ArbilNodeObject, Vector<ArbilNodeObject>>();
                                System.out.println("to: " + dropTargetUserObject.toString());
//                     TODO: add drag to local corpus tree
//                     TODO: consider adding a are you sure you want to move that node into this node ...
//                     TODO: must prevent parent nodes being dragged into lower branches of itself
                                if (dropTargetUserObject instanceof ArbilNodeObject) {
                                    //TODO: this should also allow drop to the root node
//                        if (((ImdiTreeObject) dropTargetUserObject).isImdiChild()) {
//                            dropTargetUserObject = ((ImdiTreeObject) dropTargetUserObject).getParentDomNode();
//                        }
                                    if (((ArbilNodeObject) dropTargetUserObject).getParentDomNode().isCmdiMetaDataNode() || ((ArbilNodeObject) dropTargetUserObject).getParentDomNode().isSession()/* || ((ImdiTreeObject) dropTargetUserObject).isImdiChild()*/) {
                                        //TODO: for now we do not allow drag on to imdi child nodes
                                        if (selectionContainsArchivableLocalFile == true
                                                && selectionContainsLocalFile == true
                                                && selectionContainsLocalDirectory == false
                                                && selectionContainsImdiResource == false
                                                && selectionContainsImdiCorpus == false
                                                && selectionContainsImdiSession == false
                                                && selectionContainsImdiChild == false
                                                && selectionContainsLocal == true
                                                && selectionContainsRemote == false) {
                                            System.out.println("ok to add local file");
                                            for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                                System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                                                new MetadataBuilder().requestAddNode((ArbilNodeObject) dropTargetUserObject, "Resource", draggedImdiObjects[draggedCounter]);
                                            }
                                            createTransferable(null); // clear the transfer objects
                                            return true; // we have achieved the drag so return true
                                        }
                                    }
                                }
                                // allow drop to the root node wich will not be an imditreeobject
//                    if (!(dropTargetUserObject instanceof ImdiTreeObject) || ((ImdiTreeObject) dropTargetUserObject).isCorpus()) {
                                if (selectionContainsArchivableLocalFile == false
                                        // selectionContainsLocalFile == true &&
                                        && selectionContainsLocalDirectory == false
                                        && selectionContainsImdiResource == false
                                        && (selectionContainsImdiCorpus == false || selectionContainsImdiSession == false) //&&
                                        //(selectionContainsImdiChild == false || GuiHelper.imdiSchema.nodeCanExistInNode((ImdiTreeObject) dropTargetUserObject, (ImdiTreeObject) draggedImdiObjects[draggedCounter]))// &&
                                        //                                    selectionContainsLocal == true &&
                                        //                                    selectionContainsRemote == false
                                        ) {
                                    System.out.println("ok to move local IMDI");
                                    for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                        System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                                        if (!((ArbilNodeObject) draggedImdiObjects[draggedCounter]).isImdiChild() || MetadataReader.getSingleInstance().nodeCanExistInNode((ArbilNodeObject) dropTargetUserObject, (ArbilNodeObject) draggedImdiObjects[draggedCounter])) {
                                            //((ImdiTreeObject) dropTargetUserObject).requestAddNode(GuiHelper.imdiSchema.getNodeTypeFromMimeType(draggedImdiObjects[draggedCounter].mpiMimeType), "Resource", null, draggedImdiObjects[draggedCounter].getUrlString(), draggedImdiObjects[draggedCounter].mpiMimeType);

                                            // check that the node has not been dragged into itself
                                            boolean draggedIntoSelf = false;
                                            DefaultMutableTreeNode ancestorNode = targetNode;
                                            while (ancestorNode != null) {
                                                if (draggedTreeNodes[draggedCounter].equals(ancestorNode)) {
                                                    draggedIntoSelf = true;
                                                    System.out.println("found ancestor: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);
                                                }
//                                        System.out.println("checking: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);
                                                ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
                                            }
                                            // todo: test for dragged to parent session
                                            //System.out.println("found ancestor: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);

                                            if (!draggedIntoSelf) {
                                                if (((ArbilNodeObject) draggedImdiObjects[draggedCounter]).isFavorite()) {
                                                    //  todo: this does not allow the adding of favourites to the root node, note that that would need to be changed in the add menu also
                                                    new MetadataBuilder().requestAddNode((ArbilNodeObject) dropTargetUserObject, ((ArbilNodeObject) draggedImdiObjects[draggedCounter]).toString(), ((ArbilNodeObject) draggedImdiObjects[draggedCounter]));
                                                } else if (!(((ArbilNodeObject) draggedImdiObjects[draggedCounter]).isLocal() && ArbilSessionStorage.getSingleInstance().pathIsInsideCache(((ArbilNodeObject) draggedImdiObjects[draggedCounter]).getFile()))) {
                                                    importNodeList.add((ArbilNodeObject) draggedImdiObjects[draggedCounter]);
                                                } else {
                                                    String targetNodeName;
                                                    if (dropTargetUserObject instanceof ArbilNodeObject) {
                                                        targetNodeName = targetNode.getUserObject().toString();
                                                    } else {
                                                        targetNodeName = ((JLabel) targetNode.getUserObject()).getText();
                                                    }
//                                        if (draggedTreeNodes[draggedCounter].getUserObject())
                                                    int detailsOption = JOptionPane.showOptionDialog(ArbilWindowManager.getSingleInstance().linorgFrame,
                                                            "Move " + draggedTreeNodes[draggedCounter].getUserObject().toString()
                                                            + /*" from " + ((DefaultMutableTreeNode) ancestorNode.getParent()).getUserObject().toString() +*/ " to " + targetNodeName,
                                                            "Arbil",
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.PLAIN_MESSAGE,
                                                            null,
                                                            new Object[]{"Move", "Cancel"},
                                                            "Cancel");
                                                    if (detailsOption == 0) {
                                                        boolean addNodeResult = true;
                                                        if (dropTargetUserObject instanceof ArbilNodeObject) {
                                                            addNodeResult = ((ArbilNodeObject) dropTargetUserObject).addCorpusLink(draggedImdiObjects[draggedCounter]);
                                                        } else {
                                                            addNodeResult = TreeHelper.getSingleInstance().addLocation(draggedImdiObjects[draggedCounter].getURI());
                                                        }
                                                        if (addNodeResult) {
                                                            if (draggedTreeNodes[draggedCounter] != null) {
                                                                if (draggedTreeNodes[draggedCounter].getParent().equals(draggedTreeNodes[draggedCounter].getRoot())) {
                                                                    System.out.println("dragged from root");
                                                                    TreeHelper.getSingleInstance().removeLocation(draggedImdiObjects[draggedCounter]);
                                                                    TreeHelper.getSingleInstance().applyRootLocations();
                                                                } else {
                                                                    ArbilNodeObject parentImdi = (ArbilNodeObject) ((DefaultMutableTreeNode) draggedTreeNodes[draggedCounter].getParent()).getUserObject();
                                                                    System.out.println("removeing from parent: " + parentImdi);
                                                                    // add the parent and the child node to the deletelist
                                                                    if (!imdiNodesDeleteList.containsKey(parentImdi)) {
                                                                        imdiNodesDeleteList.put(parentImdi, new Vector());
                                                                    }
                                                                    imdiNodesDeleteList.get(parentImdi).add(draggedImdiObjects[draggedCounter]);
//                                                            System.out.println("delete list: " + imdiNodesDeleteList.get(parentImdi).size());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (importNodeList.size() > 0) {
//                                  TODO: finish this import code
                                        try {
                                            ImportExportDialog importExportDialog = new ImportExportDialog(dropTree);
                                            if (dropTargetUserObject instanceof ArbilNodeObject) {
                                                importExportDialog.setDestinationNode(((ArbilNodeObject) dropTargetUserObject));
                                            }
                                            importExportDialog.copyToCache(importNodeList);
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                    for (ArbilNodeObject currentParent : imdiNodesDeleteList.keySet()) {
                                        System.out.println("deleting by corpus link");
                                        ArbilNodeObject[] imdiNodeArray = ((Vector<ArbilNodeObject>) imdiNodesDeleteList.get(currentParent)).toArray(new ArbilNodeObject[]{});
                                        currentParent.deleteCorpusLink(imdiNodeArray);
                                    }
                                    if (dropTargetUserObject instanceof ArbilNodeObject) {
                                        // TODO: this save is required to prevent user data loss, but the save and reload process may not really be required here
//                                        ((ImdiTreeObject) dropTargetUserObject).saveChangesToCache(false);
                                        ((ArbilNodeObject) dropTargetUserObject).reloadNode();
                                    } else {
                                        TreeHelper.getSingleInstance().applyRootLocations();
                                    }
                                    createTransferable(null); // clear the transfer objects
                                    return true; // we have achieved the drag so return true
                                }
                            }
                        } else {
                            Container imdiSplitPanel = findImdiDropableTarget(comp);
                            if (imdiSplitPanel instanceof ArbilSplitPanel) {
                                ArbilSplitPanel targetPanel = (ArbilSplitPanel) imdiSplitPanel;
                                ImdiTableModel dropTableModel = (ImdiTableModel) targetPanel.imdiTable.getModel();
                                dropTableModel.addImdiObjects(draggedImdiObjects);
                                createTransferable(null); // clear the transfer objects
                                return true; // we have achieved the drag so return true
                            } else if (imdiSplitPanel instanceof JDesktopPane) {
                                ArbilWindowManager.getSingleInstance().openFloatingTableOnce(draggedImdiObjects, null);
                                createTransferable(null); // clear the transfer objects
                                return true; // we have achieved the drag so return true
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
            createTransferable(null); // clear the transfer objects
            return false;
        }

        public Object getTransferData(DataFlavor flavor) {
            System.out.println("getTransferData");
            if (isDataFlavorSupported(flavor)) {
                return draggedImdiObjects;
            }
            return null;
        }

        public DataFlavor[] getTransferDataFlavors() {
            System.out.println("getTransferDataFlavors");
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            System.out.println("isDataFlavorSupported");
            return flavors[0].equals(flavor);
        }
    }
}
