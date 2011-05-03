package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Component;
import java.net.URI;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilDataNodeContainer;

/**
 * Document   : ArbilNodeSearchPanel
 * Created on : Feb 17, 2009, 4:42:59 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ArbilNodeSearchPanel extends javax.swing.JPanel implements ArbilDataNodeContainer {

    private ArbilNodeSearchPanel thisPanel = this;
    private JInternalFrame parentFrame;
    private ArbilTableModel resultsTableModel;
    private ArbilDataNode[] selectedNodes;
    private JPanel searchTermsPanel;
    private JPanel inputNodePanel;
    private JProgressBar searchProgressBar;
    private JButton searchButton;
    private JButton stopButton;
    
    private boolean stopSearch = false;
    private int totalNodesToSearch = -1;
    private RemoteServerSearchTerm remoteServerSearchTerm = null;

    public ArbilNodeSearchPanel(JInternalFrame parentFrameLocal, ArbilTableModel resultsTableModelLocal, ArbilDataNode[] localSelectedNodes) {
        parentFrame = parentFrameLocal;
        resultsTableModel = resultsTableModelLocal;
        selectedNodes = localSelectedNodes;
        searchTermsPanel = new JPanel();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        initNodePanel();
        add(inputNodePanel);

        initSearchTermsPanel();
        add(searchTermsPanel);

        JPanel buttonsProgressPanel = createButtonsProgressPanel();
        add(buttonsProgressPanel);
        
        hideFirstBooleanOption();
        parentFrame.pack();
    }

    private JPanel createButtonsProgressPanel(){
        JPanel buttonsProgressPanel = new JPanel();
        buttonsProgressPanel.setLayout(new BoxLayout(buttonsProgressPanel, BoxLayout.LINE_AXIS));

        JButton addButton = new JButton();
        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    System.out.println("adding new term");
                    stopSearch();
                    getSearchTermsPanel().add(new ArbilNodeSearchTerm(thisPanel));
                    hideFirstBooleanOption();
//                searchTermsPanel.revalidate();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        buttonsProgressPanel.add(addButton);

        searchProgressBar = new JProgressBar();
        searchProgressBar.setString("");
        searchProgressBar.setStringPainted(true);
        buttonsProgressPanel.add(searchProgressBar);

        stopButton = new JButton();
        stopButton.setText("stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    stopSearch();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        stopButton.setEnabled(false);
        buttonsProgressPanel.add(stopButton);

        searchButton = new JButton();
        searchButton.setText("search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    startSearch();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        buttonsProgressPanel.add(searchButton);
        return buttonsProgressPanel;
    }

    private void initSearchTermsPanel() {
        searchTermsPanel.setLayout(new BoxLayout(searchTermsPanel, BoxLayout.PAGE_AXIS));
        // check if this search includes remote nodes
        boolean remoteSearch = false;
        for (ArbilDataNode arbilDataNode : selectedNodes) {
            if (!arbilDataNode.isLocal()) {
                remoteSearch = true;
                break;
            }
        }
        if (remoteSearch) {
            remoteServerSearchTerm = new RemoteServerSearchTerm(this);
            this.add(remoteServerSearchTerm);
        }
        searchTermsPanel.add(new ArbilNodeSearchTerm(this));
    }

    private void initNodePanel() {
        inputNodePanel = new JPanel();
        inputNodePanel.setLayout(new java.awt.GridLayout());
        for (ArbilDataNode currentNode : selectedNodes) {
            JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
            inputNodePanel.add(currentLabel);
        }
    }

    private void hideFirstBooleanOption() {
        boolean firstTerm = true;
        for (Component currentTermComp : searchTermsPanel.getComponents()) {
            ((ArbilNodeSearchTerm) currentTermComp).setBooleanVisible(!firstTerm);
            firstTerm = false;
        }
        searchTermsPanel.revalidate();
    }

    public void stopSearch() {
        System.out.println("stop search");
        hideFirstBooleanOption();
        stopSearch = true;
    }

    public void startSearch() {
        System.out.println("start search");
        stopSearch = false;
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);
        resultsTableModel.removeAllArbilDataNodeRows();
        performSearch();
    }

    private void performSearch() {
        Thread thread = new Thread(new SearchThread(), "performSearch");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        thread.start();
    }

    /**
     * @return the searchTermsPanel
     */
    public JPanel getSearchTermsPanel() {
        return searchTermsPanel;
    }

    private class SearchThread implements Runnable {

        @Override
        public void run() {
            Vector<ArbilDataNode> foundNodes = new Vector();
            try {
//                    if (totalNodesToSearch == -1) {
//                        searchProgressBar.setIndeterminate(true);
//                    } else {
                searchProgressBar.setIndeterminate(false);
                searchProgressBar.setMinimum(0);
                searchProgressBar.setMaximum(totalNodesToSearch);
                searchProgressBar.setValue(0);
//                    }
                for (Component currentTermComp : getSearchTermsPanel().getComponents()) {
                    ((ArbilNodeSearchTerm) currentTermComp).populateSearchTerm();
                }
                int totalSearched = 0;
                Vector<ArbilDataNode> localSearchNodes = new Vector<ArbilDataNode>();
                Vector<ArbilDataNode> remoteSearchNodes = new Vector<ArbilDataNode>();
                for (ArbilDataNode arbilDataNode : selectedNodes) {
                    if (arbilDataNode.isLocal()) {
                        localSearchNodes.add(arbilDataNode);
                    } else {
                        remoteSearchNodes.add(arbilDataNode);
                    }
                }
                if (remoteServerSearchTerm != null) {
                    searchProgressBar.setIndeterminate(true);
                    searchProgressBar.setString("connecting to server");
                    for (URI serverFoundUrl : remoteServerSearchTerm.getServerSearchResults(remoteSearchNodes.toArray(new ArbilDataNode[]{}))) {
                        System.out.println("remote node found: " + serverFoundUrl);
                        localSearchNodes.add(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, serverFoundUrl));
                    }
                    searchProgressBar.setString("");
                    searchProgressBar.setIndeterminate(false);
                }
                while (localSearchNodes.size() > 0 && !stopSearch) {
                    System.out.println("parentFrame: " + parentFrame.isVisible());
                    Object currentElement = localSearchNodes.remove(0);
                    if (currentElement instanceof ArbilDataNode) {
                        ArbilDataNode currentDataNode = (ArbilDataNode) currentElement;
                        if (!currentDataNode.isChildNode() && (currentDataNode.isLoading() || !currentDataNode.isDataLoaded())) {
                            System.out.println("searching: " + currentDataNode.getUrlString());
                            System.out.println("still loading so putting back into the list: " + currentDataNode);
                            if (!currentDataNode.fileNotFound) {
                                currentDataNode.registerContainer(ArbilNodeSearchPanel.this); // this causes the node to be loaded
                                localSearchNodes.add(currentDataNode);
                            }
                        } else {
                            // perform the search
                            System.out.println("searching: " + currentDataNode);
                            // add the child nodes
                            if (currentDataNode.isLocal() || !currentDataNode.isCorpus()) {
                                // don't search remote corpus
                                for (ArbilDataNode currentChildNode : currentDataNode.getChildArray()) {
                                    System.out.println("adding to search list: " + currentChildNode);
                                    currentChildNode.registerContainer(ArbilNodeSearchPanel.this); // this causes the node to be loaded
                                    localSearchNodes.add(currentChildNode);
                                }
                            }
                            boolean nodePassedFilter = true;
                            for (Component currentTermComponent : getSearchTermsPanel().getComponents()) {
                                ArbilNodeSearchTerm currentTermPanel = (ArbilNodeSearchTerm) currentTermComponent;
                                boolean termPassedFilter = true;
                                // filter by the node type if entered
                                if (currentTermPanel.nodeType.equals("Corpus")) {
                                    termPassedFilter = currentDataNode.isCorpus();
                                } else if (currentTermPanel.nodeType.equals("Session")) {
                                    termPassedFilter = currentDataNode.isSession();
                                } else if (currentTermPanel.nodeType.equals("Catalogue")) {
                                    termPassedFilter = currentDataNode.isCatalogue();
                                } else if (!currentTermPanel.nodeType.equals("All")) {
                                    termPassedFilter = currentDataNode.getUrlString().matches(".*" + currentTermPanel.nodeType + "\\(\\d*?\\)$");
                                }
                                if (currentTermPanel.searchFieldName.length() > 0) {// filter by the feild name and search string if entered
                                    termPassedFilter = termPassedFilter && (currentDataNode.containsFieldValue(currentTermPanel.searchFieldName, currentTermPanel.searchString));
                                } else if (currentTermPanel.searchString.length() > 0) { // filter by the search string if entered
                                    termPassedFilter = termPassedFilter && (currentDataNode.containsFieldValue(currentTermPanel.searchString));
                                }
                                // invert based on the == / != selection
                                termPassedFilter = currentTermPanel.notEqual != termPassedFilter;
                                // apply the and or booleans against the other search terms
                                if (!currentTermPanel.booleanAnd && nodePassedFilter) {
                                    // we have moved into an OR block so if we already have a positive result then exit the term checking loop
                                    break;
                                }
                                if (currentTermPanel.booleanAnd) {
                                    nodePassedFilter = (nodePassedFilter && termPassedFilter);
                                } else {
                                    nodePassedFilter = (nodePassedFilter || termPassedFilter);
                                }
                            }
                            totalSearched++;
                            // if the node has no fields it should still be added since it will only pass a search if for instance the search is for actors and in that case it should be shown even if blank
                            if (nodePassedFilter) {
                                foundNodes.add(currentDataNode);
                                resultsTableModel.addSingleArbilDataNode(currentDataNode);
                            } else {
                                currentDataNode.removeContainer(ArbilNodeSearchPanel.this);
                            }
                            if (totalNodesToSearch < totalSearched + localSearchNodes.size()) {
                                totalNodesToSearch = totalSearched + localSearchNodes.size();
                            }
                            searchProgressBar.setMaximum(totalNodesToSearch);
                            searchProgressBar.setValue(totalSearched);
                            // todo: indicate how many metadata files searched rather than sub nodes
                            searchProgressBar.setString("searched: " + totalSearched + "/" + totalNodesToSearch + " found: " + foundNodes.size());
                        }
                    }
                    if (!parentFrame.isVisible()) {
                        // in the case that the user has closed the search window we want to stop the thread
                        stopSearch = true;
                    }
                }

                if (stopSearch) {
                    searchProgressBar.setString("search canceled");
                } else {
                    // collect the max nodes found only if the search completed
                    totalNodesToSearch = totalSearched;
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
            searchProgressBar.setIndeterminate(false);
            searchProgressBar.setValue(0);
            searchProgressBar.setMaximum(1000);
            searchButton.setEnabled(true);
            stopButton.setEnabled(false);
            // add the results to the table
            resultsTableModel.addArbilDataNodes(foundNodes.elements());
            foundNodes.removeAllElements();
        }
    }

    /**
     * Data node is to be removed from the container
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilDataNode dataNode) {
        // Nothing to do, but this is implements  ArbilDataNodeContainer
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        // Nothing to do
    }
}
