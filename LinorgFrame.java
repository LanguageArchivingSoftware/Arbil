/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 */
package mpi.linorg;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author  petwit
 */
public class LinorgFrame extends javax.swing.JFrame {

    private GuiHelper guiHelper = new GuiHelper();
    ImdiTable previewTable;

    public LinorgFrame() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                performCleanExit();
            //super.windowClosing(e);
            }
        });

        initComponents();

        GuiHelper.treeHelper.setTrees(remoteCorpusTree, localCorpusTree, localDirectoryTree);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(localDirectoryTree);
        ToolTipManager.sharedInstance().registerComponent(localCorpusTree);
        ToolTipManager.sharedInstance().registerComponent(remoteCorpusTree);
        // Enable the tree icons
        localCorpusTree.setCellRenderer(GuiHelper.treeHelper.getImdiTreeRenderer());
        remoteCorpusTree.setCellRenderer(GuiHelper.treeHelper.getImdiTreeRenderer());
        localDirectoryTree.setCellRenderer(GuiHelper.treeHelper.getImdiTreeRenderer());

//        imdiDragDrop.addDrop(localCorpusTree);

        GuiHelper.imdiDragDrop.addDrag(remoteCorpusTree);
        GuiHelper.imdiDragDrop.addDrag(localDirectoryTree);
        GuiHelper.imdiDragDrop.addDrag(localCorpusTree);

        // set the default window dimensions
        // TODO: move this to the sessionstorage and load / save on exit

        previewTable = new ImdiTable(new ImdiTableModel(), null, "Preview");
        rightScrollPane.setViewportView(previewTable);
        mainSplitPane.setDividerLocation(0.25);
        // also set in showSelectionPreviewCheckBoxMenuItemActionPerformed
        rightSplitPane.setDividerLocation(0.1);
        leftSplitPane.setDividerLocation(0.15);
        leftLocalSplitPane.setDividerLocation(0.2);

        //setSize(800, 600);
        //this.setExtendedState(Frame.MAXIMIZED_BOTH);


        GuiHelper.linorgWindowManager.setComponents(windowMenu, this, jDesktopPane1);
        setVisible(true);
        GuiHelper.linorgWindowManager.openIntroductionPage();
        //guiHelper.initViewMenu(viewMenu); // moved to the view menu action

        setTitle("Linorg (Testing version, not for production use) " + new LinorgVersion().compileDate);
    }

    private void performCleanExit() {
        if (GuiHelper.imdiLoader.nodesNeedSave()){
            switch(JOptionPane.showConfirmDialog(this, "Save changes?")){
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.YES_OPTION:
                    GuiHelper.imdiLoader.saveNodesNeedingSave();
                    break;
                default:
                    return;
            }
        }
        guiHelper.saveState();
        System.exit(0);
    }

    private void addLocation(String addableLocation) {
        if (!GuiHelper.treeHelper.addLocation(addableLocation)) {
            // alert the user when the node already exists and cannot be added again
            JOptionPane.showMessageDialog(this, "The location already exists and cannot be added again", "Add location", JOptionPane.INFORMATION_MESSAGE);
        }
        GuiHelper.treeHelper.applyRootLocations();
    //locationSettingsTable.setModel(guiHelper.getLocationsTableModel());
    }

    private void removeSelectedLocation(DefaultMutableTreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            JOptionPane.showMessageDialog(this, "No node selected", "", 0);
        } else {
            GuiHelper.treeHelper.removeLocation(selectedTreeNode.getUserObject());
            GuiHelper.treeHelper.applyRootLocations();
        }
    }

    private Vector getSelectedNodes(JTree[] treesToSearch) {
        Vector selectedNodes = new Vector();
        // iterate over allthe selected nodes in the available trees
        for (int treeCount = 0; treeCount < treesToSearch.length; treeCount++) {
            for (int selectedCount = 0; selectedCount < treesToSearch[treeCount].getSelectionCount(); selectedCount++) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treesToSearch[treeCount].getSelectionPaths()[selectedCount].getLastPathComponent();
                if (parentNode.getUserObject() instanceof ImdiTreeObject) {
                    selectedNodes.add(parentNode.getUserObject());
                }
            }
        }
        return selectedNodes;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treePopupMenu = new javax.swing.JPopupMenu();
        viewSelectedNodesMenuItem = new javax.swing.JMenuItem();
        copyBranchMenuItem = new javax.swing.JMenuItem();
        actorsToGridMenuItem = new javax.swing.JMenuItem();
        searchSubnodesMenuItem = new javax.swing.JMenuItem();
        reloadSubnodesMenuItem = new javax.swing.JMenuItem();
        addMenu = new javax.swing.JMenu();
        deleteMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator1 = new javax.swing.JSeparator();
        copyImdiUrlMenuItem = new javax.swing.JMenuItem();
        viewXmlMenuItem = new javax.swing.JMenuItem();
        viewXmlMenuItem1 = new javax.swing.JMenuItem();
        validateMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator2 = new javax.swing.JSeparator();
        addRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        addDefaultLocationsMenuItem = new javax.swing.JMenuItem();
        removeRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        removeCachedCopyMenuItem = new javax.swing.JMenuItem();
        addLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        removeLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        viewChangesMenuItem = new javax.swing.JMenuItem();
        sendToServerMenuItem = new javax.swing.JMenuItem();
        mainSplitPane = new javax.swing.JSplitPane();
        leftSplitPane = new javax.swing.JSplitPane();
        leftLocalSplitPane = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        localDirectoryTree = new javax.swing.JTree();
        jScrollPane4 = new javax.swing.JScrollPane();
        localCorpusTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        remoteCorpusTree = new javax.swing.JTree();
        rightSplitPane = new javax.swing.JSplitPane();
        rightScrollPane = new javax.swing.JScrollPane();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveFileMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        optionsMenu = new javax.swing.JMenu();
        editLocationsMenuItem = new javax.swing.JMenuItem();
        templatesMenu = new javax.swing.JMenu();
        editFieldViewsMenuItem = new javax.swing.JMenuItem();
        saveWindowsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        showSelectionPreviewCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        introductionMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        featuresMenuItem = new javax.swing.JMenuItem();

        viewSelectedNodesMenuItem.setText("View Selected");
        viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSelectedNodesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewSelectedNodesMenuItem);

        copyBranchMenuItem.setText("Copy Branch to Offline Cache");
        copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyBranchMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(copyBranchMenuItem);

        actorsToGridMenuItem.setText("Search for Actors");
        actorsToGridMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actorsToGridMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(actorsToGridMenuItem);

        searchSubnodesMenuItem.setText("Search");
        searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchSubnodesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(searchSubnodesMenuItem);

        reloadSubnodesMenuItem.setText("Reload");
        reloadSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadSubnodesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(reloadSubnodesMenuItem);

        addMenu.setText("Add");
        addMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                addMenuMenuSelected(evt);
            }
        });
        treePopupMenu.add(addMenu);

        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(deleteMenuItem);
        treePopupMenu.add(treePopupMenuSeparator1);

        copyImdiUrlMenuItem.setText("Copy Location to Clipboard");
        copyImdiUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyImdiUrlMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(copyImdiUrlMenuItem);

        viewXmlMenuItem.setText("View IMDI XML");
        viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewXmlMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewXmlMenuItem);

        viewXmlMenuItem1.setText("View IMDI Formatted");
        viewXmlMenuItem1.setEnabled(false);
        viewXmlMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewXmlXslMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewXmlMenuItem1);

        validateMenuItem.setText("Check IMDI format");
        validateMenuItem.setEnabled(false);
        validateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(validateMenuItem);
        treePopupMenu.add(treePopupMenuSeparator2);

        addRemoteCorpusMenuItem.setText("Add Remote Location");
        addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRemoteCorpusMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addRemoteCorpusMenuItem);

        addDefaultLocationsMenuItem.setText("Add Default Remote Locations");
        addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDefaultLocationsMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addDefaultLocationsMenuItem);

        removeRemoteCorpusMenuItem.setText("Remove Remote Location");
        removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRemoteCorpusMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeRemoteCorpusMenuItem);

        removeCachedCopyMenuItem.setText("Remove Cache Link");
        removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCachedCopyMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeCachedCopyMenuItem);

        addLocalDirectoryMenuItem.setText("Add Local Directory");
        addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocalDirectoryMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addLocalDirectoryMenuItem);

        removeLocalDirectoryMenuItem.setText("Remove Local Directory");
        removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLocalDirectoryMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeLocalDirectoryMenuItem);

        saveMenuItem.setText("Save Changes to Disk");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveNodeMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(saveMenuItem);

        viewChangesMenuItem.setText("View Changes");
        viewChangesMenuItem.setEnabled(false);
        viewChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewChangesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewChangesMenuItem);

        sendToServerMenuItem.setText("Send to Server");
        sendToServerMenuItem.setEnabled(false);
        sendToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToServerMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(sendToServerMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Linorg");

        mainSplitPane.setDividerLocation(100);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        leftSplitPane.setDividerSize(5);
        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setName("leftSplitPane"); // NOI18N

        leftLocalSplitPane.setDividerSize(5);
        leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftLocalSplitPane.setName("leftLocalSplitPane"); // NOI18N

        localDirectoryTree.setModel(GuiHelper.treeHelper.localDirectoryTreeModel);
        localDirectoryTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localDirectoryTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        localDirectoryTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        localDirectoryTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        localDirectoryTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                treeKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(localDirectoryTree);

        leftLocalSplitPane.setBottomComponent(jScrollPane2);

        localCorpusTree.setModel(GuiHelper.treeHelper.localCorpusTreeModel);
        localCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localCorpusTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        localCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        localCorpusTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        localCorpusTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                treeKeyTyped(evt);
            }
        });
        jScrollPane4.setViewportView(localCorpusTree);

        leftLocalSplitPane.setLeftComponent(jScrollPane4);

        leftSplitPane.setBottomComponent(leftLocalSplitPane);

        remoteCorpusTree.setModel(GuiHelper.treeHelper.remoteCorpusTreeModel);
        remoteCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        remoteCorpusTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        remoteCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        remoteCorpusTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        remoteCorpusTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                treeKeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(remoteCorpusTree);

        leftSplitPane.setLeftComponent(jScrollPane3);

        mainSplitPane.setLeftComponent(leftSplitPane);

        rightSplitPane.setDividerSize(5);
        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setName("rightSplitPane"); // NOI18N
        rightSplitPane.setTopComponent(rightScrollPane);
        rightSplitPane.setRightComponent(jDesktopPane1);

        mainSplitPane.setRightComponent(rightSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        saveFileMenuItem.setText("Save Changes");
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveFileMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setEnabled(false);
        jMenuBar1.add(editMenu);

        optionsMenu.setText("Options");

        editLocationsMenuItem.setText("Locations");
        editLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLocationsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editLocationsMenuItem);

        templatesMenu.setText("Templates");
        templatesMenu.setEnabled(false);
        optionsMenu.add(templatesMenu);

        editFieldViewsMenuItem.setText("Field Views");
        editFieldViewsMenuItem.setEnabled(false);
        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFieldViewsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editFieldViewsMenuItem);

        saveWindowsCheckBoxMenuItem.setSelected(true);
        saveWindowsCheckBoxMenuItem.setText("Save Windows on Exit");
        saveWindowsCheckBoxMenuItem.setEnabled(false);
        optionsMenu.add(saveWindowsCheckBoxMenuItem);

        showSelectionPreviewCheckBoxMenuItem.setSelected(true);
        showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
        showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSelectionPreviewCheckBoxMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(showSelectionPreviewCheckBoxMenuItem);

        jMenuBar1.add(optionsMenu);

        viewMenu.setText("View");
        viewMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                viewMenuMenuSelected(evt);
            }
        });
        jMenuBar1.add(viewMenu);

        windowMenu.setText("Window");
        jMenuBar1.add(windowMenu);

        helpMenu.setText("Help");

        introductionMenuItem.setText("Introduction");
        introductionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                introductionMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(introductionMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        featuresMenuItem.setText("Features/Known Bugs");
        featuresMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                featuresMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(featuresMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void editFieldViewsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFieldViewsMenuItemActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_editFieldViewsMenuItemActionPerformed

private void remoteCorpusTreeTreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_remoteCorpusTreeTreeWillExpand
// TODO add your handling code here:
    DefaultMutableTreeNode parentNode = null;
    if (evt.getPath() == null) {
        //There is no selection.
    } else {
        parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
    }
    // load imdi data if not already loaded
    GuiHelper.treeHelper.getImdiChildNodes(parentNode);
//remoteCorpusTree.scrollPathToVisible(evt.getPath());
}//GEN-LAST:event_remoteCorpusTreeTreeWillExpand

private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
// TODO add your handling code here:
    // test if click was over a selected node
    javax.swing.tree.TreePath clickedNodePath = ((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY());

    int clickedNodeInt = ((javax.swing.JTree) evt.getSource()).getClosestRowForLocation(evt.getX(), evt.getY());
    int leadSelectedInt = ((javax.swing.JTree) evt.getSource()).getLeadSelectionRow();

    boolean clickedPathIsSelected = (((javax.swing.JTree) evt.getSource()).isPathSelected(clickedNodePath));
    if (evt.getButton() == 3) {
        // this is simplified and made to match the same type of actions as the imditable 
        if (!evt.isShiftDown() && !evt.isControlDown() && !clickedPathIsSelected) {
            ((javax.swing.JTree) evt.getSource()).clearSelection();
            ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
        }
    }
//    if (evt.getButton() == 3) {
//        if (/*(*/!evt.isControlDown() /*&& evt.getButton() == 1*/ /*&& !evt.isShiftDown())*/ /* || (evt.getButton() == 3 && !clickedPathIsSelected)*/) {
//            System.out.println("alt not down so clearing selection");
//            ((javax.swing.JTree) evt.getSource()).clearSelection();
////        if (evt.getSource() != remoteCorpusTree) {
////            remoteCorpusTree.clearSelection();
////        }
////        if (evt.getSource() != localCorpusTree) {
////            localCorpusTree.clearSelection();
////        }
////        if (evt.getSource() != localDirectoryTree) {
////            localDirectoryTree.clearSelection();
////        }
//            ((javax.swing.JTree) evt.getSource()).setSelectionPath(((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY()));
//        } else if (clickedPathIsSelected) {
//            System.out.println("alt down over selected node");
//            ((javax.swing.JTree) evt.getSource()).removeSelectionPath(clickedNodePath);
//        } else {
//            System.out.println("alt down over unselected node");
//            ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
//        }
//        if (evt.isShiftDown()) {
//            System.out.println("shift down");
//            ((javax.swing.JTree) evt.getSource()).addSelectionInterval(leadSelectedInt, clickedNodeInt);
//        }
//    }
    if (evt.getButton() == 3) {
        boolean showContextMenu = true;
        int selectionCount = ((javax.swing.JTree) evt.getSource()).getSelectionCount();
        int nodeLevel = -1;
        if (selectionCount > 0) {
            nodeLevel = ((javax.swing.JTree) evt.getSource()).getSelectionPath().getPathCount();
        }
        boolean showRemoveLocationsTasks = selectionCount == 1 && nodeLevel == 2;
        boolean showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;
        //System.out.println("path count: " + ((JTree) evt.getSource()).getSelectionPath().getPathCount());
        // set up the contect menu
        removeCachedCopyMenuItem.setVisible(false);
        removeLocalDirectoryMenuItem.setVisible(false);
        addLocalDirectoryMenuItem.setVisible(false);
        removeRemoteCorpusMenuItem.setVisible(false);
        addRemoteCorpusMenuItem.setVisible(false);
        copyBranchMenuItem.setVisible(false);
        copyImdiUrlMenuItem.setVisible(false);
        viewXmlMenuItem.setVisible(false);
        viewXmlMenuItem1.setVisible(false);
        searchSubnodesMenuItem.setVisible(false);
        reloadSubnodesMenuItem.setVisible(false);
        actorsToGridMenuItem.setVisible(false);
        addDefaultLocationsMenuItem.setVisible(false);
        addMenu.setVisible(false);
        deleteMenuItem.setVisible(false);
        viewSelectedNodesMenuItem.setVisible(false);
        saveMenuItem.setVisible(false);
        viewChangesMenuItem.setVisible(false);
        sendToServerMenuItem.setVisible(false);
        validateMenuItem.setVisible(false);

        if (evt.getSource() == remoteCorpusTree) {
            removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
            addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
            copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
        }
        if (evt.getSource() == localCorpusTree) {
            removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
            searchSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            actorsToGridMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            // a corpus can be added even at the root node
            addMenu.setVisible(selectionCount > 0 && /*nodeLevel > 1 &&*/ localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes 
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
            deleteMenuItem.setVisible(nodeLevel > 2);
            boolean nodeIsImdiChild = false;
            Object leadSelectedTreeObject = GuiHelper.treeHelper.getSingleSelectedNode(localCorpusTree);
            if (leadSelectedTreeObject != null && leadSelectedTreeObject instanceof ImdiTreeObject) {
                nodeIsImdiChild = ((ImdiTreeObject)leadSelectedTreeObject).isImdiChild();                
                if (((ImdiTreeObject) leadSelectedTreeObject).imdiNeedsSaveToDisk) {
                    saveMenuItem.setVisible(true);
                } else if (((ImdiTreeObject) leadSelectedTreeObject).needsChangesSentToServer()) {
                    viewChangesMenuItem.setVisible(true);
                    sendToServerMenuItem.setVisible(true);
                }
                viewXmlMenuItem.setVisible(true);
                viewXmlMenuItem1.setVisible(true);
                validateMenuItem.setVisible(true);
            }
            deleteMenuItem.setEnabled(!nodeIsImdiChild && selectionCount == 1);
            showContextMenu = true; //nodeLevel != 1;
        }
        if (evt.getSource() == localDirectoryTree) {
            removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
            addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
        } else {
            copyImdiUrlMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
        }
        viewSelectedNodesMenuItem.setVisible(selectionCount >= 1 && nodeLevel > 1);
        reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);

        // hide show the separators
        treePopupMenuSeparator2.setVisible(nodeLevel != 1 && showRemoveLocationsTasks && evt.getSource() != localDirectoryTree);
        treePopupMenuSeparator1.setVisible(nodeLevel != 1 && evt.getSource() != localDirectoryTree);

        // store the event source
        treePopupMenu.setInvoker((javax.swing.JTree) evt.getSource());

        // show the context menu
        if (showContextMenu) {
            if (evt.getSource() instanceof Component) {
                treePopupMenu.setInvoker((Component) evt.getSource());
            }
            treePopupMenu.show((java.awt.Component) evt.getSource(), evt.getX(), evt.getY());
        }
    }
}//GEN-LAST:event_treeMousePressed

private void actorsToGridMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actorsToGridMenuItemActionPerformed
// TODO add your handling code here:
    ThreadedDialog threadedDialog = new ThreadedDialog(localCorpusTree);
    threadedDialog.searchNodes(getSelectedNodes(new JTree[]{localCorpusTree}), "Actor");
}//GEN-LAST:event_actorsToGridMenuItemActionPerformed

private void copyBranchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyBranchMenuItemActionPerformed
// TODO add your handling code here:
    ThreadedDialog threadedDialog = new ThreadedDialog(remoteCorpusTree);
    threadedDialog.copyToCache(getSelectedNodes(new JTree[]{remoteCorpusTree}));
    // update the tree and reload the ui
    GuiHelper.treeHelper.applyRootLocations();
}//GEN-LAST:event_copyBranchMenuItemActionPerformed

private void addLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocalDirectoryMenuItemActionPerformed
// TODO add your handling code here:
    JFileChooser fc = new JFileChooser();
    //fc.setDialogTitle(getResourceMap().getString(name + ".dialogTitle"));
    //String textFilesDesc = getResourceMap().getString("txtFileExtensionDescription");
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int option = fc.showOpenDialog(this);
    if (JFileChooser.APPROVE_OPTION == option) {
        try {
            addLocation(fc.getSelectedFile().getCanonicalPath());
        } catch (IOException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Error adding location: " + ex.getMessage());
        }
    }
}//GEN-LAST:event_addLocalDirectoryMenuItemActionPerformed

private void viewXmlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewXmlMenuItemActionPerformed
// TODO add your handling code here:
    guiHelper.openImdiXmlWindow(GuiHelper.treeHelper.getSingleSelectedNode(treePopupMenu.getInvoker()), false);
}//GEN-LAST:event_viewXmlMenuItemActionPerformed

private void copyImdiUrlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyImdiUrlMenuItemActionPerformed
// TODO add your handling code here:
    //DefaultMutableTreeNode selectedTreeNode = null;
    ImdiTreeObject selectedImdiNode = (ImdiTreeObject) GuiHelper.treeHelper.getSingleSelectedNode(treePopupMenu.getInvoker());

    if (selectedImdiNode == null) {
        if (localCorpusTree.getSelectionPath() != null) {
            JOptionPane.showMessageDialog(this, "Cannot copy from the cache", "", 0);
            guiHelper.copyNodeUrlToClipboard(selectedImdiNode);
        } else {
            JOptionPane.showMessageDialog(this, "No node selected", "", 0);
        }
    } else {
        guiHelper.copyNodeUrlToClipboard(selectedImdiNode);
    }
}//GEN-LAST:event_copyImdiUrlMenuItemActionPerformed

private void addRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRemoteCorpusMenuItemActionPerformed
// TODO add your handling code here:
    String addableLocation = (String) JOptionPane.showInputDialog(this, "Enter the URL");
    if ((addableLocation != null) && (addableLocation.length() > 0)) {
        addLocation(addableLocation);
    }
}//GEN-LAST:event_addRemoteCorpusMenuItemActionPerformed

private void addDefaultLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDefaultLocationsMenuItemActionPerformed
// TODO add your handling code here:
    if (0 < GuiHelper.treeHelper.addDefaultCorpusLocations()) {
        GuiHelper.treeHelper.applyRootLocations();
    } else {
        // alert the user when the node already exists and cannot be added again
        JOptionPane.showMessageDialog(this, "The defalut locations already exists and cannot be added again", "Add default locations", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_addDefaultLocationsMenuItemActionPerformed

private void removeRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRemoteCorpusMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (remoteCorpusTree.getSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) remoteCorpusTree.getSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeRemoteCorpusMenuItemActionPerformed

private void removeCachedCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCachedCopyMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (localCorpusTree.getSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeCachedCopyMenuItemActionPerformed

private void removeLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLocalDirectoryMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (localDirectoryTree.getSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) localDirectoryTree.getSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeLocalDirectoryMenuItemActionPerformed

private void searchSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchSubnodesMenuItemActionPerformed
// TODO add your handling code here:
    ThreadedDialog threadedDialog = new ThreadedDialog(localCorpusTree);
    threadedDialog.searchNodes(getSelectedNodes(new JTree[]{localCorpusTree}), null);
}//GEN-LAST:event_searchSubnodesMenuItemActionPerformed

private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeValueChanged
// TODO add your handling code here:
    if (showSelectionPreviewCheckBoxMenuItem.getState()) {
        // count the total number of selected nodes across all trees
//        int selectedNodesCount = remoteCorpusTree.getSelectionCount();
//        selectedNodesCount += localCorpusTree.getSelectionCount();
//        selectedNodesCount += localDirectoryTree.getSelectionCount();
//
//        // if there are no nodes selected then clear the grid
//        if (0 == selectedNodesCount) {
//            guiHelper.removeAllFromGridData(previewTable.getModel());
//        } else {
//            Vector nodesToRemove = new Vector();
//            Vector nodesToAdd = new Vector();
//            // Make a list of nodes to be removed and a separate list of nodes to ba added
//            // this may not be the quickest way to do this but it will reduce redraws and make the other calls simpler
//            for (int selectedCount = 0; selectedCount < evt.getPaths().length; selectedCount++) {
//                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) evt.getPaths()[selectedCount].getLastPathComponent();
//                // only preview imdi nodes
//                if (parentNode.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
//                    if (((ImdiHelper.ImdiTreeObject) parentNode.getUserObject()).isImdi()) {
//                        if (evt.isAddedPath(selectedCount)) {
//                            System.out.println("adding: " + parentNode.getPath());
//                            nodesToAdd.add(parentNode.getUserObject());
//                        } else {
//                            System.out.println("removing: " + parentNode.getPath());
//                            nodesToRemove.add(parentNode.getUserObject());
//                        }
//                    }
//                }
//            }
//            guiHelper.removeFromGridData(previewTable.getModel(), nodesToRemove);
//            guiHelper.addToGridData(previewTable.getModel(), nodesToAdd);  
//        }
        guiHelper.removeAllFromGridData(previewTable.getModel());
        guiHelper.addToGridData(previewTable.getModel(), GuiHelper.treeHelper.getSingleSelectedNode(evt.getSource()));
    }
}//GEN-LAST:event_jTreeValueChanged

private void showSelectionPreviewCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.linorgWindowManager.saveSplitPlanes(this.getContentPane().getComponent(0));
    if (!showSelectionPreviewCheckBoxMenuItem.getState()) {//GEN-LAST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
            // remove the right split split and show only the jdesktoppane
//            int lastPost = mainSplitPane.getDividerLocation();
            mainSplitPane.remove(rightSplitPane);
            mainSplitPane.setRightComponent(jDesktopPane1);
//            mainSplitPane.setDividerLocation(lastPost);
            // clear the grid to keep things tidy
            guiHelper.removeAllFromGridData(previewTable.getModel());
        } else {
            // put the jdesktoppane and the preview grid back into the right split pane
//            int lastPost = mainSplitPane.getDividerLocation();
            mainSplitPane.remove(jDesktopPane1);
            mainSplitPane.setRightComponent(rightSplitPane);
            rightSplitPane.setTopComponent(rightScrollPane);
            rightSplitPane.setBottomComponent(jDesktopPane1);
            rightSplitPane.setDividerLocation(0.1);
//            mainSplitPane.setDividerLocation(lastPost);
            // update the preview data grid
            guiHelper.removeAllFromGridData(previewTable.getModel());
//            guiHelper.addToGridData(previewTable.getModel(), getSelectedNodes(new JTree[]{remoteCorpusTree, localCorpusTree, localDirectoryTree}));
        }
        GuiHelper.linorgWindowManager.loadSplitPlanes(this.getContentPane().getComponent(0));
    }

private void viewSelectedNodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSelectedNodesMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.linorgWindowManager.openFloatingTable(getSelectedNodes(new JTree[]{((JTree) (treePopupMenu.getInvoker()))}).elements(), "Selection");
}//GEN-LAST:event_viewSelectedNodesMenuItemActionPerformed

private void editLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLocationsMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.treeHelper.showLocationsDialog();
}//GEN-LAST:event_editLocationsMenuItemActionPerformed

private void viewMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_viewMenuMenuSelected
// TODO add your handling code here:
    guiHelper.initViewMenu(viewMenu);
}//GEN-LAST:event_viewMenuMenuSelected

private void addMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_addMenuMenuSelected
// TODO add your handling code here:
    guiHelper.initAddMenu(addMenu, GuiHelper.treeHelper.getSingleSelectedNode(localCorpusTree));
}//GEN-LAST:event_addMenuMenuSelected

private void viewXmlXslMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewXmlXslMenuItemActionPerformed
// TODO add your handling code here:
    guiHelper.openImdiXmlWindow(GuiHelper.treeHelper.getSingleSelectedNode(treePopupMenu.getInvoker()), true);
}//GEN-LAST:event_viewXmlXslMenuItemActionPerformed

private void treeMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseDragged
// TODO add your handling code here:
    System.out.println("jTree1MouseDragged");
    JComponent c = (JComponent) evt.getSource();
    TransferHandler th = c.getTransferHandler();
    th.exportAsDrag(c, evt, TransferHandler.COPY);
}//GEN-LAST:event_treeMouseDragged

private void treeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeKeyTyped
// TODO add your handling code here:
    if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
        GuiHelper.linorgWindowManager.openFloatingTable(getSelectedNodes(new JTree[]{(JTree) evt.getSource()}).elements(), "Selection");
    }
}//GEN-LAST:event_treeKeyTyped

private void saveNodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveNodeMenuItemActionPerformed
// TODO add your handling code here:
//    for (Enumeration nodesEnum = getSelectedNodes(new JTree[]{(JTree) evt.getSource()}).elements();nodesEnum.hasMoreElements();){

    for (Enumeration nodesEnum = getSelectedNodes(new JTree[]{localCorpusTree}).elements(); nodesEnum.hasMoreElements();) {
        Object userObject = nodesEnum.nextElement();
        System.out.println("userObject: " + userObject);
        if (userObject instanceof ImdiTreeObject) {
            ((ImdiTreeObject) userObject).saveChangesToCache();
        }
    }
}//GEN-LAST:event_saveNodeMenuItemActionPerformed

private void sendToServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToServerMenuItemActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_sendToServerMenuItemActionPerformed

private void viewChangesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewChangesMenuItemActionPerformed
// TODO add your handling code here:
    
}//GEN-LAST:event_viewChangesMenuItemActionPerformed

private void validateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateMenuItemActionPerformed
// TODO add your handling code here:
    
}//GEN-LAST:event_validateMenuItemActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
// TODO add your handling code here:
    performCleanExit();
}//GEN-LAST:event_exitMenuItemActionPerformed

private void reloadSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadSubnodesMenuItemActionPerformed
// TODO add your handling code here:
    // TODO: this is inadequate and needs to be updated
    ((ImdiTreeObject) GuiHelper.treeHelper.getSingleSelectedNode(treePopupMenu.getInvoker())).reloadNode(true);
}//GEN-LAST:event_reloadSubnodesMenuItemActionPerformed

private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.treeHelper.deleteNode(treePopupMenu.getInvoker());
}//GEN-LAST:event_deleteMenuItemActionPerformed

private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.linorgWindowManager.openAboutPage();
}//GEN-LAST:event_aboutMenuItemActionPerformed

private void featuresMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_featuresMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.linorgWindowManager.openUrlWindowOnce("Features/Known Bugs", this.getClass().getResource("/mpi/linorg/resources/html/Features.html"));
}//GEN-LAST:event_featuresMenuItemActionPerformed

private void introductionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_introductionMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.linorgWindowManager.openUrlWindowOnce("Introduction", this.getClass().getResource("/mpi/linorg/resources/html/Introduction.html"));
}//GEN-LAST:event_introductionMenuItemActionPerformed

private void saveFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.imdiLoader.saveNodesNeedingSave();
}//GEN-LAST:event_saveFileMenuItemActionPerformed

private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
// TODO add your handling code here:
    saveFileMenuItem.setEnabled(GuiHelper.imdiLoader.nodesNeedSave());
}//GEN-LAST:event_fileMenuMenuSelected

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
            java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try{
                    new LinorgFrame();
                } catch (Exception ex) {
                    new LinorgBugCatcher().logError(ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem actorsToGridMenuItem;
    private javax.swing.JMenuItem addDefaultLocationsMenuItem;
    private javax.swing.JMenuItem addLocalDirectoryMenuItem;
    private javax.swing.JMenu addMenu;
    private javax.swing.JMenuItem addRemoteCorpusMenuItem;
    private javax.swing.JMenuItem copyBranchMenuItem;
    private javax.swing.JMenuItem copyImdiUrlMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem editFieldViewsMenuItem;
    private javax.swing.JMenuItem editLocationsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem featuresMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem introductionMenuItem;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane leftLocalSplitPane;
    private javax.swing.JSplitPane leftSplitPane;
    private javax.swing.JTree localCorpusTree;
    private javax.swing.JTree localDirectoryTree;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem reloadSubnodesMenuItem;
    private javax.swing.JTree remoteCorpusTree;
    private javax.swing.JMenuItem removeCachedCopyMenuItem;
    private javax.swing.JMenuItem removeLocalDirectoryMenuItem;
    private javax.swing.JMenuItem removeRemoteCorpusMenuItem;
    private javax.swing.JScrollPane rightScrollPane;
    private javax.swing.JSplitPane rightSplitPane;
    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private javax.swing.JMenuItem searchSubnodesMenuItem;
    private javax.swing.JMenuItem sendToServerMenuItem;
    private javax.swing.JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private javax.swing.JMenu templatesMenu;
    private javax.swing.JPopupMenu treePopupMenu;
    private javax.swing.JSeparator treePopupMenuSeparator1;
    private javax.swing.JSeparator treePopupMenuSeparator2;
    private javax.swing.JMenuItem validateMenuItem;
    private javax.swing.JMenuItem viewChangesMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewSelectedNodesMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItem1;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables

}
