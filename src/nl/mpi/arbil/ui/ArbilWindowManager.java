package nl.mpi.arbil.ui;

import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.data.ImdiTableModel;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.data.ArbilNodeObject;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ImdiLoader;

/**
 * Document   : LinorgWindowManager
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilWindowManager {

    Hashtable<String, Component[]> windowList = new Hashtable<String, Component[]>();
    Hashtable windowStatesHashtable;
    public JDesktopPane desktopPane; //TODO: this is public for the dialog boxes to use, but will change when the strings are loaded from the resources
    public JFrame linorgFrame;
    int nextWindowX = 50;
    int nextWindowY = 50;
    int nextWindowWidth = 800;
    int nextWindowHeight = 600;
    float fontScale = 1;
    private Hashtable<String, String> messageDialogQueue = new Hashtable<String, String>();
    private boolean messagesCanBeShown = false;
    boolean showMessageThreadrunning = false;
    static private ArbilWindowManager singleInstance = null;

    static synchronized public ArbilWindowManager getSingleInstance() {
//        System.out.println("LinorgWindowManager getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new ArbilWindowManager();
        }
        return singleInstance;
    }

    private ArbilWindowManager() {
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new java.awt.Color(204, 204, 204));
        ArbilDragDrop.getSingleInstance().addTransferHandler(desktopPane);
    }

    public void loadGuiState(JFrame linorgFrameLocal) {
        linorgFrame = linorgFrameLocal;
        try {
            // load the saved states
            windowStatesHashtable = (Hashtable) ArbilSessionStorage.getSingleInstance().loadObject("windowStates");
            // set the main window position and size
            linorgFrame.setExtendedState((Integer) windowStatesHashtable.get("linorgFrameExtendedState"));
            if (linorgFrame.getExtendedState() == JFrame.ICONIFIED) {
                // start up iconified is just too confusing to the user
                linorgFrame.setExtendedState(JFrame.NORMAL);
            }
            // if the application was maximised when it was last closed then these values will not be set and this will through setting the size in the catch
            Object linorgFrameBounds = windowStatesHashtable.get("linorgFrameBounds");
            linorgFrame.setBounds((Rectangle) linorgFrameBounds);
            if (windowStatesHashtable.containsKey("ScreenDeviceCount")) {
                int screenDeviceCount = ((Integer) windowStatesHashtable.get("ScreenDeviceCount"));
                if (screenDeviceCount > GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length) {
                    linorgFrame.setLocationRelativeTo(null);
                    // make sure the main frame is visible. for instance when a second monitor has been removed.
                    Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
                    if (linorgFrame.getBounds().intersects(new Rectangle(screenDimension))) {
                        linorgFrame.setBounds(linorgFrame.getBounds().intersection(new Rectangle(screenDimension)));
                    } else {
                        linorgFrame.setBounds(0, 0, 800, 600);
                        linorgFrame.setLocationRelativeTo(null);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("load windowStates failed: " + ex.getMessage());
            System.out.println("setting default windowStates");
            windowStatesHashtable = new Hashtable();
            linorgFrame.setBounds(0, 0, 800, 600);
            linorgFrame.setLocationRelativeTo(null);
            linorgFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        // set the split pane positions
        loadSplitPlanes(linorgFrame.getContentPane().getComponent(0));
    }

    public void openAboutPage() {
        ArbilVersion linorgVersion = new ArbilVersion();
        String messageString = "Archive Builder\n"
                + "A local tool for organising linguistic data.\n"
                + "Max Planck Institute for Psycholinguistics\n"
                + "Application design and programming by Peter Withers\n"
                + "Arbil also uses components of the IMDI API and Lamus Type Checker\n"
                + "Version: " + linorgVersion.currentMajor + "." + linorgVersion.currentMinor + "." + linorgVersion.currentRevision + "\n"
                + linorgVersion.lastCommitDate + "\n"
                + "Compile Date: " + linorgVersion.compileDate + "\n";
        JOptionPane.showMessageDialog(linorgFrame, messageString, "About Arbil", JOptionPane.PLAIN_MESSAGE);
    }

    public void offerUserToSaveChanges() throws Exception {
        if (ImdiLoader.getSingleInstance().nodesNeedSave()) {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ArbilWindowManager.getSingleInstance().linorgFrame,
                    "There are unsaved changes.\nSave now?", "Save Changes",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
            } else {
                throw new Exception("user canceled save action");
            }
        }
    }

    public File showEmptyExportDirectoryDialogue(String titleText) {
        boolean fileSelectDone = false;
        try {
            while (!fileSelectDone) {
                File[] selectedFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox(titleText + " Destination Directory", true, false, false);
                if (selectedFiles != null && selectedFiles.length > 0) {
                    File destinationDirectory = selectedFiles[0];
                    if (!destinationDirectory.exists()/* && parentDirectory.getParentFile().exists()*/) {
                        // create the directory provided that the parent directory exists
                        // ths is here due the the way the mac file select gui leads the user to type in a new directory name
                        destinationDirectory.mkdirs();
                    }
                    if (!destinationDirectory.exists()) {
                        JOptionPane.showMessageDialog(linorgFrame, "The export directory\n\"" + destinationDirectory + "\"\ndoes not exist.\nPlease select or create a directory.", titleText, JOptionPane.PLAIN_MESSAGE);
                    } else {
//                        if (!createdDirectory) {
//                            String newDirectoryName = JOptionPane.showInputDialog(linorgFrame, "Enter Export Name", titleText, JOptionPane.PLAIN_MESSAGE, null, null, "arbil_export").toString();
//                            try {
//                                destinationDirectory = new File(parentDirectory.getCanonicalPath() + File.separatorChar + newDirectoryName);
//                                destinationDirectory.mkdir();
//                            } catch (Exception e) {
//                                JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create the export directory + \'" + newDirectoryName + "\'", titleText, JOptionPane.PLAIN_MESSAGE);
//                            }
//                        }
                        if (destinationDirectory != null && destinationDirectory.exists()) {
                            if (destinationDirectory.list().length == 0) {
                                fileSelectDone = true;
                                return destinationDirectory;
                            } else {
                                if (showMessageDialogBox("The selected export directory is not empty.\nTo continue will merge and may overwrite files.\nDo you want to continue?", titleText)) {
                                    return destinationDirectory;
                                }
                                //JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The export directory must be empty", titleText, JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                    }
                } else {
                    fileSelectDone = true;
                }
            }
        } catch (Exception e) {
            System.out.println("aborting export: " + e.getMessage());
        }
        return null;
    }

    public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, boolean requireMetadataFiles) {
        // test for os: if mac or file then awt else for other and directory use swing
        // save/load last directory accoring to the title of the dialogue
        //Hashtable<String, File> fileSelectLocationsHashtable;
        File workingDirectory = null;
        String workingDirectoryPathString = ArbilSessionStorage.getSingleInstance().loadString("fileSelect." + titleText);
        if (workingDirectoryPathString == null) {
            workingDirectory = new File(System.getProperty("user.home"));
        } else {
            workingDirectory = new File(workingDirectoryPathString);
        }
        File lastUsedWorkingDirectory;

        File[] returnFile;
        boolean isMac = true; // TODO: set this correctly
        boolean useAtwSelect = false; //directorySelectOnly && isMac && !multipleSelect;
        if (useAtwSelect) {
            if (directorySelectOnly) {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
            } else {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
            FileDialog fileDialog = new FileDialog(linorgFrame);
            if (requireMetadataFiles) {
                fileDialog.setFilenameFilter(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".imdi");
                    }
                });
            }
            fileDialog.setDirectory(workingDirectory.getAbsolutePath());
            fileDialog.setVisible(true);
            String selectedFile = fileDialog.getFile();

            lastUsedWorkingDirectory = new File(fileDialog.getDirectory());
            if (selectedFile != null) {
                returnFile = new File[]{new File(selectedFile)};
            } else {
                returnFile = null;
            }
        } else {
            JFileChooser fileChooser = new JFileChooser();
            if (requireMetadataFiles) {
                FileFilter imdiFileFilter = new FileFilter() {

                    public String getDescription() {
                        return "IMDI";
                    }

                    @Override
                    public boolean accept(File selectedFile) {
                        // the test for exists is unlikey to do anything here, paricularly regarding the Mac dialogues text entry field
                        return (selectedFile.exists() && (selectedFile.isDirectory() || selectedFile.getName().toLowerCase().endsWith(".imdi")));
                    }
                };
                fileChooser.addChoosableFileFilter(imdiFileFilter);
            }
            if (directorySelectOnly) {
                // this filter is only cosmetic but gives the user an indication of what to select
                FileFilter imdiFileFilter = new FileFilter() {

                    public String getDescription() {
                        return "Directories";
                    }

                    @Override
                    public boolean accept(File selectedFile) {
                        return (selectedFile.exists() && selectedFile.isDirectory());
                    }
                };
                fileChooser.addChoosableFileFilter(imdiFileFilter);
            }
            if (directorySelectOnly) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            }
            fileChooser.setCurrentDirectory(workingDirectory);
            fileChooser.setMultiSelectionEnabled(multipleSelect);
            if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(ArbilWindowManager.getSingleInstance().linorgFrame, titleText)) {
                returnFile = fileChooser.getSelectedFiles();
                if (returnFile.length == 0) {
                    returnFile = new File[]{fileChooser.getSelectedFile()};
                }
            } else {
                returnFile = null;
            }
            if (returnFile != null && returnFile.length == 1 && !returnFile[0].exists()) {
                // if the selected file does not exist then the "unusable" mac file select is usually to blame so try to clean up
                returnFile[0] = returnFile[0].getParentFile();
                // if the result still does not exist then abort the select by returning null
                if (!returnFile[0].exists()) {
                    returnFile = null;
                }
            }
            lastUsedWorkingDirectory = fileChooser.getCurrentDirectory();
        }
        // save last use working directory
        ArbilSessionStorage.getSingleInstance().saveString("fileSelect." + titleText, lastUsedWorkingDirectory.getAbsolutePath());
        return returnFile;
    }

    public boolean showMessageDialogBox(String messageString, String messageTitle) {
        if (messageTitle == null) {
            messageTitle = "Arbil";
        }
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ArbilWindowManager.getSingleInstance().linorgFrame,
                messageString, messageTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            return true;
        } else {
            return false;
        }
    }

    public void addMessageDialogToQueue(String messageString, String messageTitle) {
        if (messageTitle == null) {
            messageTitle = "Arbil";
        }
        String currentMessage = messageDialogQueue.get(messageTitle);
        if (currentMessage != null) {
            messageString = messageString + "\n-------------------------------\n" + currentMessage;
        }
        messageDialogQueue.put(messageTitle, messageString);
        showMessageDialogQueue();
    }

    private synchronized void showMessageDialogQueue() {
        if (!showMessageThreadrunning) {
            new Thread("showMessageThread") {

                public void run() {
                    try {
                        sleep(100);
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                    showMessageThreadrunning = true;
                    if (messagesCanBeShown) {
                        while (messageDialogQueue.size() > 0) {
                            String messageTitle = messageDialogQueue.keys().nextElement();
                            String messageText = messageDialogQueue.remove(messageTitle);
                            if (messageText != null) {
                                JOptionPane.showMessageDialog(ArbilWindowManager.getSingleInstance().linorgFrame, messageText, messageTitle, JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                    }
                    showMessageThreadrunning = false;
                }
            }.start();
        }
    }

    public void openIntroductionPage() {
        // open the introduction page
        // TODO: always get this page from the server if available, but also save it for off line use
//        URL introductionUrl = this.getClass().getResource("/nl/mpi/arbil/resources/html/Introduction.html");
//        openUrlWindowOnce("Introduction", introductionUrl);
//        get remote file to local disk
//        if local file exists then open that
//        else open the one in the jar file
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  The features html file has been limited to the version in the jar (not the server), so that it is specific to the version of linorg in the jar. //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        String remoteUrl = "http://www.mpi.nl/tg/j2se/jnlp/linorg/Features.html";
//        String cachePath = GuiHelper.linorgSessionStorage.updateCache(remoteUrl, true);
//        System.out.println("cachePath: " + cachePath);
//        URL destinationUrl = null;
//        try {
//            if (new File(cachePath).exists()) {
//                destinationUrl = new File(cachePath).toURL();
//            }
//        } catch (Exception ex) {
//        }
//        if (destinationUrl == null) {
//        destinationUrl = this.getClass().getResource("/nl/mpi/arbil/resources/html/Features.html");
////        }
//        System.out.println("destinationUrl: " + destinationUrl);
//        openUrlWindowOnce("Features/Known Bugs", destinationUrl);

        try {
            // load the saved windows
            Hashtable windowListHashtable = (Hashtable) ArbilSessionStorage.getSingleInstance().loadObject("openWindows");
            for (Enumeration windowNamesEnum = windowListHashtable.keys(); windowNamesEnum.hasMoreElements();) {
                String currentWindowName = windowNamesEnum.nextElement().toString();
                System.out.println("currentWindowName: " + currentWindowName);
                Vector imdiURLs = (Vector) windowListHashtable.get(currentWindowName);
//                System.out.println("imdiEnumeration: " + imdiEnumeration);
                ArbilNodeObject[] imdiObjectsArray = new ArbilNodeObject[imdiURLs.size()];
                for (int arrayCounter = 0; arrayCounter < imdiObjectsArray.length; arrayCounter++) {
                    try {
                        imdiObjectsArray[arrayCounter] = (ImdiLoader.getSingleInstance().getImdiObject(null, new URI(imdiURLs.elementAt(arrayCounter).toString())));
                    } catch (URISyntaxException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
                openFloatingTable(imdiObjectsArray, currentWindowName);
                //openFloatingTable(null, currentWindowName);
            }
            System.out.println("done loading windowStates");
        } catch (Exception ex) {
            windowStatesHashtable = new Hashtable();
            System.out.println("load windowStates failed: " + ex.getMessage());
        }

        if (!TreeHelper.getSingleInstance().locationsHaveBeenAdded()) {
            System.out.println("no local locations found, showing help window");
            ArbilHelp helpComponent = ArbilHelp.getSingleInstance();
            if (null == focusWindow(ArbilHelp.helpWindowTitle)) {
                createWindow(ArbilHelp.helpWindowTitle, helpComponent);
            }
            helpComponent.setCurrentPage(ArbilHelp.IntroductionPage);
        }
        startKeyListener();
        messagesCanBeShown = true;
        showMessageDialogQueue();
    }

    public void loadSplitPlanes(Component targetComponent) {
        //System.out.println("loadSplitPlanes: " + targetComponent);
        if (targetComponent instanceof JSplitPane) {
            System.out.println("loadSplitPlanes: " + targetComponent.getName());
            Object linorgSplitPosition = windowStatesHashtable.get(targetComponent.getName());
            if (linorgSplitPosition instanceof Integer) {
                System.out.println(targetComponent.getName() + ": " + linorgSplitPosition);
                ((JSplitPane) targetComponent).setDividerLocation((Integer) linorgSplitPosition);
            } else {
                if (targetComponent.getName().equals("rightSplitPane")) {
                    ((JSplitPane) targetComponent).setDividerLocation(150);
                } else {
                    //leftSplitPane  leftLocalSplitPane rightSplitPane)
                    ((JSplitPane) targetComponent).setDividerLocation(200);
                }
            }
            for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
                loadSplitPlanes(childComponent);
            }
        }
        if (targetComponent instanceof JPanel) {
            for (Component childComponent : ((JPanel) targetComponent).getComponents()) {
                loadSplitPlanes(childComponent);
            }
        }
    }

    public void saveSplitPlanes(Component targetComponent) {
        //System.out.println("saveSplitPlanes: " + targetComponent);
        if (targetComponent instanceof JSplitPane) {
            System.out.println("saveSplitPlanes: " + targetComponent.getName());
            windowStatesHashtable.put(targetComponent.getName(), ((JSplitPane) targetComponent).getDividerLocation());
            for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
                saveSplitPlanes(childComponent);
            }
        }
        if (targetComponent instanceof JPanel) {
            for (Component childComponent : ((JPanel) targetComponent).getComponents()) {
                saveSplitPlanes(childComponent);
            }
        }
    }

    public void saveWindowStates() {
        // loop windowList and make a hashtable of window names with a vector of the imdinodes displayed, then save the hashtable
        try {
            // collect the main window size and position for saving
            if (linorgFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                windowStatesHashtable.put("linorgFrameBounds", linorgFrame.getBounds());
            }
            windowStatesHashtable.put("ScreenDeviceCount", GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length);
            windowStatesHashtable.put("linorgFrameExtendedState", linorgFrame.getExtendedState());
            // collect the split pane positions for saving
            saveSplitPlanes(linorgFrame.getContentPane().getComponent(0));
            // save the collected states
            ArbilSessionStorage.getSingleInstance().saveObject(windowStatesHashtable, "windowStates");
            // save the windows
            Hashtable windowListHashtable = new Hashtable();
            //(Hashtable) windowList.clone();
            for (Enumeration windowNamesEnum = windowList.keys(); windowNamesEnum.hasMoreElements();) {
                String currentWindowName = windowNamesEnum.nextElement().toString();
                System.out.println("currentWindowName: " + currentWindowName);
                // set the value of the windowListHashtable to be the imdi urls rather than the windows
                Object windowObject = ((Component[]) windowList.get(currentWindowName))[0];
                try {
                    if (windowObject != null) {
                        Object currentComponent = ((JInternalFrame) windowObject).getContentPane().getComponent(0);
                        if (currentComponent != null && currentComponent instanceof ArbilSplitPanel) {
                            // if this table has no nodes then don't save it
                            if (0 < ((ArbilSplitPanel) currentComponent).imdiTable.getRowCount()) {
//                System.out.println("windowObject: " + windowObject);
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane());
//                System.out.println("getComponent: " + ((JInternalFrame) windowObject).getComponent(0));
//                System.out.println("LinorgSplitPanel: " + ((LinorgSplitPanel)((JInternalFrame) windowObject).getContentPane()));
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane().getComponent(0));                                           
                                Vector currentNodesVector = new Vector();
                                for (String currentUrlString : ((ImdiTableModel) ((ArbilSplitPanel) currentComponent).imdiTable.getModel()).getImdiNodesURLs()) {
                                    currentNodesVector.add(currentUrlString);
                                }
                                windowListHashtable.put(currentWindowName, currentNodesVector);
                                System.out.println("saved");
                            }
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println("Exception: " + ex.getMessage());
                }
            }
            // save the windows
            ArbilSessionStorage.getSingleInstance().saveObject(windowListHashtable, "openWindows");

            System.out.println("saved windowStates");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save windowStates exception: " + ex.getMessage());
        }
    }

    private String addWindowToList(String windowName, JInternalFrame windowFrame) {
        int instanceCount = 0;
        String currentWindowName = windowName;
        while (windowList.containsKey(currentWindowName)) {
            currentWindowName = windowName + "(" + ++instanceCount + ")";
        }
        JMenuItem windowMenuItem = new JMenuItem();
        windowMenuItem.setText(currentWindowName);
        windowMenuItem.setName(currentWindowName);
        windowFrame.setName(currentWindowName);
        windowMenuItem.setActionCommand(currentWindowName);
        windowMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    focusWindow(evt.getActionCommand());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        windowFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                String windowName = e.getInternalFrame().getName();
                System.out.println("Closing window: " + windowName);
                Component[] windowAndMenu = (Component[]) windowList.get(windowName);
                if (ArbilMenuBar.windowMenu != null && windowAndMenu != null) {
                    ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
                }
                windowList.remove(windowName);
                super.internalFrameClosed(e);
            }
        });
        windowList.put(currentWindowName, new Component[]{windowFrame, windowMenuItem});
        if (ArbilMenuBar.windowMenu != null) {
            ArbilMenuBar.windowMenu.add(windowMenuItem);
        }
        return currentWindowName;
    }

    public void stopEditingInCurrentWindow() {
        // when saving make sure the current editing table or long field editor saves its data first
        Component focusedComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        while (focusedComponent != null) {
            if (focusedComponent instanceof ArbilLongFieldEditor) {
                ((ArbilLongFieldEditor) focusedComponent).storeChanges();
            }
            focusedComponent = focusedComponent.getParent();
        }
    }

    public void closeAllWindows() {
        for (JInternalFrame focusedWindow : desktopPane.getAllFrames()) {
            if (focusedWindow != null) {
                String windowName = focusedWindow.getName();
                Component[] windowAndMenu = (Component[]) windowList.get(windowName);
                if (windowAndMenu != null && ArbilMenuBar.windowMenu != null) {
                    ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
                }
                windowList.remove(windowName);
                desktopPane.remove(focusedWindow);
            }
        }
        desktopPane.repaint();
    }

    public JInternalFrame focusWindow(String windowName) {
        if (windowList.containsKey(windowName)) {
            Object windowObject = ((Component[]) windowList.get(windowName))[0];
            try {
                if (windowObject != null) {
                    ((JInternalFrame) windowObject).setIcon(false);
                    ((JInternalFrame) windowObject).setSelected(true);
                    return (JInternalFrame) windowObject;
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
            }
        }
        return null;
    }

    private void startKeyListener() {

//        desktopPane.addKeyListener(new KeyAdapter() {
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("keyPressed");
//                if (e.VK_W == e.getKeyCode()){
//                    System.out.println("VK_W");
//                }
//                super.keyPressed(e);
//            }
//        
//        });

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            public void eventDispatched(AWTEvent e) {
                boolean isKeybordRepeat = false;
                if (e instanceof KeyEvent) {
                    // only consider key release events
                    if (e.getID() == KeyEvent.KEY_RELEASED) {
                        // work around for jvm in linux
                        // due to the bug in the jvm for linux the keyboard repeats are shown as real key events, so we attempt to prevent ludicrous key events being used here
                        KeyEvent nextPress = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
                        if (nextPress != null) {
                            // the next key event is at the same time as this event
                            if ((nextPress.getWhen() == ((KeyEvent) e).getWhen())) {
                                // the next key code is the same as this event                                
                                if (((nextPress.getKeyCode() == ((KeyEvent) e).getKeyCode()))) {
                                    isKeybordRepeat = true;
                                }
                            }
                        }
                        // end work around for jvm in linux
                        if (!isKeybordRepeat) {
//                            System.out.println("KeyEvent.paramString: " + ((KeyEvent) e).paramString());
//                            System.out.println("KeyEvent.getWhen: " + ((KeyEvent) e).getWhen());
                            if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_W) {
                                JInternalFrame[] windowsToClose;
                                if (((KeyEvent) e).isShiftDown()) {
                                    windowsToClose = desktopPane.getAllFrames();
                                } else {
                                    windowsToClose = new JInternalFrame[]{desktopPane.getSelectedFrame()};
                                }
                                for (JInternalFrame focusedWindow : windowsToClose) {
                                    if (focusedWindow != null) {
                                        String windowName = focusedWindow.getName();
                                        Component[] windowAndMenu = (Component[]) windowList.get(windowName);
                                        if (windowAndMenu != null && ArbilMenuBar.windowMenu != null) {
                                            ArbilMenuBar.windowMenu.remove(windowAndMenu[1]);
                                        }
                                        windowList.remove(windowName);
                                        desktopPane.remove(focusedWindow);
                                        try {
                                            JInternalFrame[] allWindows = desktopPane.getAllFrames();
                                            if (allWindows.length > 0) {
                                                JInternalFrame topMostWindow = allWindows[0];
                                                if (topMostWindow != null) {
                                                    System.out.println("topMostWindow: " + topMostWindow);
                                                    topMostWindow.setIcon(false);
                                                    topMostWindow.setSelected(true);
                                                }
                                            }
                                        } catch (Exception ex) {
                                            GuiHelper.linorgBugCatcher.logError(ex);
//                                        System.out.println(ex.getMessage());
                                        }
                                    }
                                }
                                desktopPane.repaint();
                            }
                            if ((((KeyEvent) e).getKeyCode() == KeyEvent.VK_TAB && ((KeyEvent) e).isControlDown())) {
                                // the [meta `] is consumed by the operating system, the only way to enable the back quote key for window switching is to use separate windows and rely on the OS to do the switching
                                // || (((KeyEvent) e).getKeyCode() == KeyEvent.VK_BACK_QUOTE && ((KeyEvent) e).isMetaDown())
                                try {
                                    JInternalFrame[] allWindows = desktopPane.getAllFrames();
                                    int targetLayerInt;
                                    if (((KeyEvent) e).isShiftDown()) {
                                        allWindows[0].moveToBack();
                                        targetLayerInt = 1;
                                    } else {
                                        targetLayerInt = allWindows.length - 1;
                                    }
                                    allWindows[targetLayerInt].setIcon(false);
                                    allWindows[targetLayerInt].setSelected(true);
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
//                                    System.out.println(ex.getMessage());
                                }
                            }
                            if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && (((KeyEvent) e).getKeyCode() == KeyEvent.VK_MINUS || ((KeyEvent) e).getKeyCode() == KeyEvent.VK_EQUALS || ((KeyEvent) e).getKeyCode() == KeyEvent.VK_PLUS)) {
                                if (((KeyEvent) e).getKeyCode() != KeyEvent.VK_MINUS) {
                                    fontScale = fontScale + (float) 0.1;
                                } else {
                                    fontScale = fontScale - (float) 0.1;
                                }
                                if (fontScale < 1) {
                                    fontScale = 1;
                                }
                                System.out.println("fontScale: " + fontScale);
                                UIDefaults defaults = UIManager.getDefaults();
                                Enumeration keys = defaults.keys();
                                while (keys.hasMoreElements()) {
                                    Object key = keys.nextElement();
                                    Object value = defaults.get(key);
                                    if (value != null && value instanceof Font) {
                                        UIManager.put(key, null);
                                        Font font = UIManager.getFont(key);
                                        if (font != null) {
                                            float size = font.getSize2D();
                                            UIManager.put(key, new FontUIResource(font.deriveFont(size * fontScale)));
                                        }
                                    }
                                }
                                SwingUtilities.updateComponentTreeUI(desktopPane.getParent().getParent());
                            }
                            if ((((KeyEvent) e).isMetaDown() || ((KeyEvent) e).isControlDown()) && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_F) {
                                JInternalFrame windowToSearch = desktopPane.getSelectedFrame();
                                //System.out.println(windowToSearch.getContentPane());
                                for (Component childComponent : windowToSearch.getContentPane().getComponents()) {
                                    // loop through all the child components in the window (there will probably only be one)
                                    if (childComponent instanceof ArbilSplitPanel) {
                                        ((ArbilSplitPanel) childComponent).showSearchPane();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }

    public JInternalFrame createWindow(String windowTitle, Component contentsComponent) {
        JInternalFrame currentInternalFrame = new javax.swing.JInternalFrame();
        currentInternalFrame.setLayout(new BorderLayout());
        //        GuiHelper.arbilDragDrop.addTransferHandler(currentInternalFrame);
        currentInternalFrame.add(contentsComponent, BorderLayout.CENTER);
        windowTitle = addWindowToList(windowTitle, currentInternalFrame);

        // set the new window size to be fully visible
        int tempWindowWidth, tempWindowHeight;
        if (desktopPane.getWidth() > nextWindowWidth) {
            tempWindowWidth = nextWindowWidth;
        } else {
            tempWindowWidth = desktopPane.getWidth() - 50;
        }
        if (desktopPane.getHeight() > nextWindowHeight) {
            tempWindowHeight = nextWindowHeight;
        } else {
            tempWindowHeight = desktopPane.getHeight() - 50;
        }
        if (tempWindowHeight < 100) {
            tempWindowHeight = 100;
        }
        currentInternalFrame.setSize(tempWindowWidth, tempWindowHeight);

        currentInternalFrame.setClosable(true);
        currentInternalFrame.setIconifiable(true);
        currentInternalFrame.setMaximizable(true);
        currentInternalFrame.setResizable(true);
        currentInternalFrame.setTitle(windowTitle);
        currentInternalFrame.setToolTipText(windowTitle);
        currentInternalFrame.setName(windowTitle);
        currentInternalFrame.setVisible(true);

//        selectedFilesFrame.setSize(destinationComp.getWidth(), 300);
//        selectedFilesFrame.setRequestFocusEnabled(false);
//        selectedFilesFrame.getContentPane().add(selectedFilesPanel, java.awt.BorderLayout.CENTER);
//        selectedFilesFrame.setBounds(0, 0, 641, 256);
//        destinationComp.add(selectedFilesFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        // set the window position so that they are cascaded
        currentInternalFrame.setLocation(nextWindowX, nextWindowY);
        nextWindowX = nextWindowX + 10;
        nextWindowY = nextWindowY + 10;
        // TODO: it would be nice to use the JInternalFrame's title bar height to increment the position
        if (nextWindowX + tempWindowWidth > desktopPane.getWidth()) {
            nextWindowX = 0;
        }
        if (nextWindowY + tempWindowHeight > desktopPane.getHeight()) {
            nextWindowY = 0;
        }
        desktopPane.add(currentInternalFrame, 0);
        try {
            // prevent the frame focus process consuming mouse events that should be recieved by the jtable etc.
            currentInternalFrame.setSelected(true);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
        }
        return currentInternalFrame;
    }

    public JEditorPane openUrlWindowOnce(String frameTitle, URL locationUrl) {
        JEditorPane htmlDisplay = new JEditorPane();
        htmlDisplay.setEditable(false);
        htmlDisplay.setContentType("text/html");
        try {
            htmlDisplay.setPage(locationUrl);
            htmlDisplay.addHyperlinkListener(new ArbilHyperlinkListener());

            //gridViewInternalFrame.setMaximum(true);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
        }

        JInternalFrame existingWindow = focusWindow(frameTitle);
        if (existingWindow == null) {
//            return openUrlWindow(frameTitle, htmlDisplay);
            JScrollPane jScrollPane6;
            jScrollPane6 = new javax.swing.JScrollPane();
            jScrollPane6.setViewportView(htmlDisplay);
            createWindow(frameTitle, jScrollPane6);
        } else {
            ((JScrollPane) existingWindow.getContentPane().getComponent(0)).setViewportView(htmlDisplay);
        }
        return htmlDisplay;
    }

    public void openSearchTable(ArbilNodeObject[] selectedNodes, String frameTitle) {
        ImdiTableModel resultsTableModel = new ImdiTableModel();
        ArbilTable imdiTable = new ArbilTable(resultsTableModel, frameTitle);
        ArbilSplitPanel imdiSplitPanel = new ArbilSplitPanel(imdiTable);
        JInternalFrame searchFrame = this.createWindow(frameTitle, imdiSplitPanel);
        searchFrame.add(new ArbilNodeSearchPanel(searchFrame, resultsTableModel, selectedNodes), BorderLayout.NORTH);
        imdiSplitPanel.setSplitDisplay();
        imdiSplitPanel.addFocusListener(searchFrame);
        searchFrame.pack();
    }

    public ImdiTableModel openFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
        ArbilNodeObject[] tableNodes = new ArbilNodeObject[rowNodesArray.length];
        ArrayList<String> fieldPathsToHighlight = new ArrayList<String>();
        for (int arrayCounter = 0; arrayCounter < rowNodesArray.length; arrayCounter++) {
            try {
                if (rowNodesArray[arrayCounter] != null) {
                    ArbilNodeObject parentNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(rowNodesArray[arrayCounter].toString().split("#")[0]));
//                parentNode.waitTillLoaded();
                    String fieldPath = rowNodesArray[arrayCounter].getFragment();
                    String parentNodeFragment;
                    if (parentNode.nodeTemplate == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception("nodeTemplate null in: " + parentNode.getUrlString()));
                        parentNodeFragment = "";
                    } else {
                        parentNodeFragment = parentNode.nodeTemplate.getParentOfField(fieldPath);
                    }
                    URI targetNode;
                    // note that the url has already be encoded and so we must not use the separate parameter version of new URI otherwise it would be encoded again which we do not want
                    if (parentNodeFragment.length() > 0) {
                        targetNode = new URI(rowNodesArray[arrayCounter].toString().split("#")[0] + "#" + parentNodeFragment);
                    } else {
                        targetNode = new URI(rowNodesArray[arrayCounter].toString().split("#")[0]);
                    }
                    tableNodes[arrayCounter] = ImdiLoader.getSingleInstance().getImdiObject(null, targetNode);
                    fieldPathsToHighlight.add(fieldPath);
                }
            } catch (URISyntaxException ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
        ImdiTableModel targetTableModel = openFloatingTableOnce(tableNodes, frameTitle);
        targetTableModel.highlightMatchingFieldPaths(fieldPathsToHighlight.toArray(new String[]{}));
        return targetTableModel;
    }

    public ImdiTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
        HashSet<ArbilNodeObject> tableNodes = new HashSet();
        for (int arrayCounter = 0; arrayCounter < rowNodesArray.length; arrayCounter++) {
//            try {
            ArbilNodeObject currentNode = ImdiLoader.getSingleInstance().getImdiObject(null, rowNodesArray[arrayCounter]);
            tableNodes.add(currentNode);
            for (ArbilNodeObject currentChildNode : currentNode.getAllChildren()) {
                tableNodes.add(currentChildNode);
            }
//            } catch (URISyntaxException ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
//            }
        }
        return openFloatingTableOnce(tableNodes.toArray(new ArbilNodeObject[]{}), frameTitle);
    }

    public ImdiTableModel openFloatingTableOnce(ArbilNodeObject[] rowNodesArray, String frameTitle) {
        if (rowNodesArray.length == 1 && rowNodesArray[0] != null && rowNodesArray[0].isInfoLink) {
            try {
                if (rowNodesArray[0].getUrlString().toLowerCase().endsWith(".html") || rowNodesArray[0].getUrlString().toLowerCase().endsWith(".txt")) {
                    openUrlWindowOnce(rowNodesArray[0].toString(), rowNodesArray[0].getURI().toURL());
                    return null;
                }
            } catch (MalformedURLException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
        }
        // open find a table containing exactly the same nodes as requested or create a new table
        for (Component[] currentWindow : windowList.values().toArray(new Component[][]{})) {
            // loop through all the windows
            for (Component childComponent : ((JInternalFrame) currentWindow[0]).getContentPane().getComponents()) {
                // loop through all the child components in the window (there will probably only be one)
                if (childComponent instanceof ArbilSplitPanel) {
                    // only consider components with a LinorgSplitPanel
                    ImdiTableModel currentTableModel = (ImdiTableModel) ((ArbilSplitPanel) childComponent).imdiTable.getModel();
                    if (currentTableModel.getImdiNodeCount() == rowNodesArray.length) {
                        // first check that the number of nodes in the table matches
                        boolean tableMatches = true;
                        for (ArbilNodeObject currentItem : rowNodesArray) {
                            // compare each node for a verbatim match
                            if (!currentTableModel.containsImdiNode(currentItem)) {
//                              // ignore this window because the nodes do not match
                                tableMatches = false;
                                break;
                            }
                        }
                        if (tableMatches) {
//                            System.out.println("tableMatches");
                            try {
                                ((JInternalFrame) currentWindow[0]).setIcon(false);
                                ((JInternalFrame) currentWindow[0]).setSelected(true);
                                return currentTableModel;
                            } catch (Exception ex) {
                                GuiHelper.linorgBugCatcher.logError(ex);
                            }
                        }
                    }
                }
            }
        }
        // if through the above process a table containing all and only the nodes requested has not been found then create a new table
        return openFloatingTable(rowNodesArray, frameTitle);
    }

    public ImdiTableModel openFloatingTable(ArbilNodeObject[] rowNodesArray, String frameTitle) {
        if (frameTitle == null) {
            if (rowNodesArray.length == 1) {
                frameTitle = rowNodesArray[0].toString();
            } else {
                frameTitle = "Selection";
            }
        }
        ImdiTableModel imdiTableModel = new ImdiTableModel();
        ArbilTable imdiTable = new ArbilTable(imdiTableModel, frameTitle);
        ArbilSplitPanel imdiSplitPanel = new ArbilSplitPanel(imdiTable);
        imdiTableModel.addImdiObjects(rowNodesArray);
        imdiSplitPanel.setSplitDisplay();
        JInternalFrame tableFrame = this.createWindow(frameTitle, imdiSplitPanel);
        imdiSplitPanel.addFocusListener(tableFrame);
        return imdiTableModel;
    }
}
