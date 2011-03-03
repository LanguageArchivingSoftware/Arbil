/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;

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

    public ArbilSubnodesPanel(ArbilDataNode dataNode) {
        this(dataNode, null);
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

    private void clear() {
        for (ArbilSubnodesPanel child : children) {
            child.clear();
        }
        children.clear();
        if (contentPanel != null) {
            contentPanel.removeAll();
        }
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
        JLabel titleLabel = new JLabel(dataNode.toString(), ArbilIcons.getSingleInstance().getIconForNode(dataNode), SwingConstants.LEADING);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(titleLabel);

        // Add table for the current node
        if (!dataNode.isEmptyMetaNode()) {
            // Add table to content panel
            ArbilTable table = createArbilTable(dataNode);
            panel.add(table.getTableHeader());
            panel.add(table);
            // Add some padding below table
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Recursively add ArbilSubnodesPanels to the content panel
        for (ArbilDataNode child : dataNode.getChildArray()) {
            ArbilSubnodesPanel childPanel = new ArbilSubnodesPanel(child, this);
            children.add(childPanel);
            panel.add(childPanel);
            // Add some padding below child
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        return panel;
    }

    private ArbilTable createArbilTable(ArbilDataNode dataNode) {
        ArbilTableModel arbilTableModel = new ArbilTableModel();
        arbilTableModel.addArbilDataNodes(new ArbilDataNode[]{dataNode});
        ArbilTable table = new ArbilTable(arbilTableModel, dataNode.toString());
        table.getTableHeader().setAlignmentX(LEFT_ALIGNMENT);
        table.setAlignmentX(LEFT_ALIGNMENT);
        return table;
    }

    public void dataNodeRemoved(ArbilDataNode dataNode) {
        deleteNodePanels(dataNode);
        doLayout();
    }

    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        requestReload();
    }

    protected void deleteNodePanels(ArbilDataNode dataNode) {
        synchronized (children) {
            for (ArbilSubnodesPanel child : children) {
                if (child.dataNode.equals(dataNode)) {
                    contentPanel.remove(child);
                } else {
                    child.deleteNodePanels(dataNode);
                }
            }
        }
    }

    protected void reloadAll() {
        reloadNode(null);
    }

    protected void reloadNode(ArbilDataNode dataNode) {
        if (dataNode == null || this.dataNode == dataNode) {
            clear();
            addContents();
        } else {
            for (ArbilSubnodesPanel child : children) {
                child.reloadNode(dataNode);
            }
        }
        revalidate();
    }

    protected ArbilSubnodesPanel getTopLevelPanel() {
        if (parent == null) {
            return this;
        } else if (parent.parent == null) {
            return parent;
        } else {
            return parent.getTopLevelPanel();
        }
    }
    private JPanel contentPanel;
    protected ArbilDataNode dataNode;
    protected ArbilSubnodesPanel parent;
    final protected ArrayList<ArbilSubnodesPanel> children = new ArrayList<ArbilSubnodesPanel>();
    private boolean reloadRequested = false;
    private boolean reloadThreadRunning = false;
    final private Object reloadLock = new Object();

    private void requestReload() {
        synchronized (reloadLock) {
            reloadRequested = true;
            if (!reloadThreadRunning) {
                reloadThreadRunning = true;
                EventQueue.invokeLater(new ReloadRunner());
            }
        }
    }

    private class ReloadRunner implements Runnable {

        @SuppressWarnings("SleepWhileHoldingLock")
        public void run() {
            while (reloadRequested) {
                try {
                    // Sleep to allow more requests to come in
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    return;
                }
                synchronized (reloadLock) {
                    reloadRequested = false;
                }
                reloadAll();
            }
            synchronized (reloadLock) {
                reloadThreadRunning = false;
            }
        }
    }
}