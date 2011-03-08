/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.util.ArbilActionBuffer;

/**
 * ArbilSubnodesPanel is created for an ArbilDataNode and will contain the
 * ArbilTable for the specified node and recursively add ArbilSubnodesPanels
 * below for all child notes of the specified node. The result is a nested
 * collection of tables for a node and all of its subnodes.
 *
 * Sketch of the layout:
 *
 * +--ArbilSubnodesPanel---------------+
 * |+--++-----Content-----------------+|
 * ||P || TABLE                       ||
 * ||A || +---ArbilSubnodesPanel 1--+ ||
 * ||D || |+--+ +-----Content------+| ||
 * ||D || |+P + + TABLE            || ||
 * ||I || |+A + + {ASnPanel 1.1}   || ||
 * ||N || |+D + + {ASnPanel 1.2}   || ||
 * ||G || |+--+ +------------------+| ||
 * ||  || +-------------------------+ ||
 * ||  || {ArbilSubnodesPanel 2}       ||
 * ||  || {ArbilSubnodesPanel 3}       ||
 * |+--++-----------------------------+|
 * +-----------------------------------+
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @see ArbilTable
 * @see ArbilDataNode
 */
public class ArbilSubnodesPanel extends JPanel implements ArbilDataNodeContainer {

    private static Border levelBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 2, 0, Color.BLACK), // Outer border - black line to the left
            new EmptyBorder(0, 10, 5, 0)); // Inner border - empty white space (inset)

    public ArbilDataNode getDataNode() {
        return this.dataNode;
    }

    public ArbilSubnodesPanel(ArbilDataNode dataNode) {
        // Construct a top-level subnodes panel (with no parent)
        this(dataNode, null);
    }

    @Override
    public String toString() {
        if (dataNode != null) {
            return "ArbilSubnodesPanel " + dataNode.toString();
        }
        return super.toString();
    }

    public boolean isTopLevelPanel() {
        return parent == null;
    }

    private ArbilSubnodesPanel(ArbilDataNode dataNode, ArbilSubnodesPanel parent) {
        super();

        this.dataNode = dataNode;
        this.parent = parent;

        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setOpaque(false);

        addContents();
    }

    final protected void addContents() {
        // Add indent to the left
        this.add(Box.createRigidArea(new Dimension(parent == null ? 5 : 20, 0)));

        contentPanel = createContentPanel(dataNode);
        this.add(contentPanel);

        // Add some padding to the right at top level
        if (parent == null) {
            this.add(Box.createRigidArea(new Dimension(10, 0)));
        }

        // Register top level panel as container for dataNode, so that the entire
        // panel will be notified upon deletion or clearing of a subnode
        dataNode.registerContainer(getTopLevelPanel());
    }

    public void stopAllEditing() {
        if (table != null) {
            TableCellEditor cellEditor = table.getCellEditor();
            if (cellEditor != null) {
                cellEditor.stopCellEditing();
            }
        }
        for (ArbilSubnodesPanel child : children) {
            child.stopAllEditing();
        }
    }

    /**
     * Clears entire panel. Removes all nodes and subnodes from panels and tables
     * as well as other content
     */
    public void clear() {
        for (ArbilSubnodesPanel child : children) {
            // Make child clear its contents
            child.clear();
        }
        // Clear list of children
        children.clear();

        // Remove as container for the datanode
        dataNode.removeContainer(getTopLevelPanel());
        if (table != null) {
            // Remove data node from table
            ((ArbilTableModel) table.getModel()).removeArbilDataNodes(new ArbilDataNode[]{dataNode});
        }
        // Remove all contents from the contentpanel
        if (contentPanel != null) {
            contentPanel.removeAll();
        }
        // Remove all contents from this panel
        this.removeAll();
    }

    /**
     * Content panel contains a table for the node itself (if not empty)
     * and ArbilSubnodesPanels for each child node
     * @param dataNode
     * @param level
     * @return New content panel
     */
    private JPanel createContentPanel(ArbilDataNode dataNode) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        // Components are layed out vertically
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        // Adds a level border, which is a line on the left side of the panel and some added white space
        panel.setBorder(levelBorder);

        // Add title label (with icon) to panel
        titleLabel = new JLabel(dataNode.toString(), ArbilIcons.getSingleInstance().getIconForNode(dataNode), SwingConstants.LEADING);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(titleLabel);

        // Add table for the current node
        if (!dataNode.isEmptyMetaNode()) {
            addTable(panel);
        }

        // Recursively add ArbilSubnodesPanels to the content panel
        for (ArbilDataNode child : dataNode.getChildArray()) {
            addChildPanel(panel, child);
        }
        return panel;
    }

    private void updateTitleLabel() {
        titleLabel.setText(dataNode.toString());
        titleLabel.setIcon(ArbilIcons.getSingleInstance().getIconForNode(dataNode));
    }

    private void addTable(JPanel panel) {
        // Add table to content panel
        table = createArbilTable(dataNode);
        panel.add(table.getTableHeader());
        panel.add(table);
        // Add some padding below table
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void addChildPanel(JPanel panel, ArbilDataNode child) {
        ArbilSubnodesPanel childPanel = new ArbilSubnodesPanel(child, this);
        children.add(childPanel);
        panel.add(childPanel);
        // Add some padding below child
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private ArbilTable createArbilTable(ArbilDataNode dataNode) {
        ArbilTableModel arbilTableModel = new ArbilTableModel();
        arbilTableModel.addArbilDataNodes(new ArbilDataNode[]{dataNode});
        ArbilTable arbilTable = new ArbilTable(arbilTableModel, dataNode.toString());
        arbilTable.getTableHeader().setAlignmentX(LEFT_ALIGNMENT);
        arbilTable.setAlignmentX(LEFT_ALIGNMENT);
        return arbilTable;
    }

    public void dataNodeRemoved(ArbilDataNode dataNode) {
        reloadRunner.requestActionAndNotify();
    }

    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        reloadRunner.requestActionAndNotify();
    }

    protected synchronized void reload() {
        List<ArbilDataNode> nodeChildren = Arrays.asList(dataNode.getChildArray());
        // Initialize nodes to add with contents of node children. Nodes already present will be removed
        List<ArbilDataNode> nodesToAdd = new LinkedList<ArbilDataNode>(nodeChildren);
        // We will detect whether any nodes have been removed
        boolean nodesRemoved = false;

        // Inspect children of this node
        for (ArbilSubnodesPanel child : children) {
            if (nodeChildren.contains(child.getDataNode())) {
                // Node is already on contents panel. Delete from list of nodes to add
                nodesToAdd.remove(child.getDataNode());
                // Reload this child
                child.reload();
            } else {
                // Node is on panel but not a child of data node anymore
                nodesRemoved = true;
                break;
            }
        }

        if (nodesRemoved) {
            // Nodes have been removed. Clear and reload entire node level
            clear();
            addContents();
        } else {
            // Add missing components

            // Check if a table should be added if there is none
            if (table == null && !dataNode.isEmptyMetaNode()) {
                addTable(contentPanel);
            }

            if (!nodesToAdd.isEmpty()) {
                // notesToAdd now contains only dataNodes that are not in the contents panel yet. Add them
                for (ArbilDataNode child : nodesToAdd) {
                    addChildPanel(contentPanel, child);
                }
            }
        }

        // Title text or icon may have changed
        updateTitleLabel();

        if (isTopLevelPanel()) {
            revalidate();
        }
    }

    protected final ArbilSubnodesPanel getTopLevelPanel() {
        if (parent == null) {
            return this;
        } else if (parent.isTopLevelPanel()) {
            return parent;
        } else {
            return parent.getTopLevelPanel();
        }
    }

    private void reloadAll() {
        if (EventQueue.isDispatchThread()) {
            reload();
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    public void run() {
                        reload();
                    }
                });
            } catch (InterruptedException ex) {
                return;
            } catch (InvocationTargetException ex) {
                return;
            }
        }
    }
    /**
     * Action buffer for reloading the panel
     */
    private ArbilActionBuffer reloadRunner = new ArbilActionBuffer("SubnodePanelReload-" + this.hashCode(), 150) {

        @Override
        public void executeAction() {
            reloadAll();
        }
    };
    private JPanel contentPanel;
    private ArbilTable table;
    private JLabel titleLabel;
    protected ArbilDataNode dataNode;
    protected ArbilSubnodesPanel parent;
    final protected ArrayList<ArbilSubnodesPanel> children = new ArrayList<ArbilSubnodesPanel>();
}
