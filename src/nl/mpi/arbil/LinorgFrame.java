package nl.mpi.arbil;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class LinorgFrame extends javax.swing.JFrame {

    private ArbilMenuBar arbilMenuBar;

    public LinorgFrame() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                arbilMenuBar.performCleanExit();
            //super.windowClosing(e);
            }
        });

        initComponents();
        PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel();
        mainSplitPane.setRightComponent(previewSplitPanel);
        ArbilTreePanels arbilTreePanels = new ArbilTreePanels();
        mainSplitPane.setLeftComponent(arbilTreePanels);
        arbilMenuBar = new ArbilMenuBar(previewSplitPanel);
        setJMenuBar(arbilMenuBar);

        mainSplitPane.setDividerLocation(0.25);

        LinorgWindowManager.getSingleInstance().loadGuiState(this);
        setTitle("Arbil (Testing version) " + new LinorgVersion().compileDate);
        setIconImage(ImdiIcons.getSingleInstance().linorgTestingIcon.getImage());
        // load the templates and populate the templates menu
        setVisible(true);
        LinorgWindowManager.getSingleInstance().openIntroductionPage();
        if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
            new LinorgVersionChecker().checkForUpdate(this);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Arbil");

        mainSplitPane.setDividerLocation(100);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setName("mainSplitPane"); // NOI18N
        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    new LinorgFrame();
                } catch (Exception ex) {
                    new LinorgBugCatcher().logError(ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane mainSplitPane;
    // End of variables declaration//GEN-END:variables
}
