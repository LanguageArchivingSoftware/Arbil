package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.TreeHelper;
import javax.swing.JTabbedPane;

/**
 * ArbilTreePanels.java
 * Created on Jul 14, 2009, 2:30:03 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreePanels extends javax.swing.JSplitPane {

    public ArbilTreePanels() {
        leftLocalSplitPane = new javax.swing.JSplitPane();
        localDirectoryScrollPane = new javax.swing.JScrollPane();
        localCorpusScrollPane = new javax.swing.JScrollPane();
        remoteCorpusScrollPane = new javax.swing.JScrollPane();
        favouritesScrollPane = new javax.swing.JScrollPane();

        this.setDividerSize(5);
        this.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        this.setName("ArbilTreePanels"); // NOI18N

        leftLocalSplitPane.setDividerSize(5);
        leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftLocalSplitPane.setName("ArbilTreePanelsInner"); // NOI18N

        remoteCorpusTree = new ArbilTree();
        localDirectoryTree = new ArbilTree();
        localCorpusTree = new ArbilTree();
        favouritesTree = new ArbilTree();

        remoteCorpusTree.setModel(TreeHelper.getSingleInstance().remoteCorpusTreeModel);
        remoteCorpusScrollPane.setViewportView(remoteCorpusTree);

        localCorpusTree.setModel(TreeHelper.getSingleInstance().localCorpusTreeModel);
        localCorpusScrollPane.setViewportView(localCorpusTree);

        localDirectoryTree.setModel(TreeHelper.getSingleInstance().localDirectoryTreeModel);
        localDirectoryScrollPane.setViewportView(localDirectoryTree);

        favouritesTree.setModel(TreeHelper.getSingleInstance().favouritesTreeModel);
        favouritesScrollPane.setViewportView(favouritesTree);

        JTabbedPane treeTabPane = new JTabbedPane();
        treeTabPane.add("Files", localDirectoryScrollPane);
        treeTabPane.add("Favourites", favouritesScrollPane);

        leftLocalSplitPane.setBottomComponent(treeTabPane);
        leftLocalSplitPane.setLeftComponent(localCorpusScrollPane);

        this.setBottomComponent(leftLocalSplitPane);
        this.setLeftComponent(remoteCorpusScrollPane);

        TreeHelper.getSingleInstance().setTrees(this);
        setDefaultTreePaneSize();
    }

    public void setDefaultTreePaneSize() {
        setDividerLocation(0.33);
        leftLocalSplitPane.setDividerLocation(0.5);
    }

    public ArbilTree[] getTreeArray() {
        return new ArbilTree[]{localCorpusTree, localDirectoryTree, remoteCorpusTree, favouritesTree};
    }
    
    private javax.swing.JScrollPane localDirectoryScrollPane;
    private javax.swing.JScrollPane remoteCorpusScrollPane;
    private javax.swing.JScrollPane localCorpusScrollPane;
    private javax.swing.JScrollPane favouritesScrollPane;
    private javax.swing.JSplitPane leftLocalSplitPane;
    public ArbilTree localCorpusTree;
    public ArbilTree localDirectoryTree;
    public ArbilTree remoteCorpusTree;
    public ArbilTree favouritesTree;
}