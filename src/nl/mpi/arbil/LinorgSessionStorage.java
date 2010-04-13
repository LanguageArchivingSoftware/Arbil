package nl.mpi.arbil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JOptionPane;
import nl.mpi.arbil.importexport.ShibbolethNegotiator;

/**
 * Document   : LinorgSessionStorage
 * use to save and load objects from disk and to manage items in the local cache
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgSessionStorage {

    public File storageDirectory = null;
    private File localCacheDirectory = null;
    static private LinorgSessionStorage singleInstance = null;
//    JDialog settingsjDialog;

    static synchronized public LinorgSessionStorage getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new LinorgSessionStorage();
        }
        return singleInstance;
    }

    private LinorgSessionStorage() {

        String storageDirectoryArray[] = getLocationOptions();

        // look for an existing storage directory
        for (String currentStorageDirectory : storageDirectoryArray) {
            File storageFile = new File(currentStorageDirectory);
            if (storageFile.exists()) {
                System.out.println("existing storage directory found: " + currentStorageDirectory);
                storageDirectory = storageFile;
                break;
            }
        }

        String testedStorageDirectories = "";
        if (storageDirectory == null) {
            for (String currentStorageDirectory : storageDirectoryArray) {
                if (!currentStorageDirectory.startsWith("null")) {
                    File storageFile = new File(currentStorageDirectory);
                    if (!storageFile.exists()) {
                        storageFile.mkdir();
                        if (!storageFile.exists()) {
                            testedStorageDirectories = testedStorageDirectories + currentStorageDirectory + "\n";
                            System.out.println("failed to create: " + currentStorageDirectory);
                        } else {
                            System.out.println("created new storage directory: " + currentStorageDirectory);
                            storageDirectory = storageFile;
                            break;
                        }
                    }
                }
            }
        }
        if (storageDirectory == null) {
            //LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not create a working directory.\n" + testedStorageDirectories + "There may be issues creating, editing and saving.", null);
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create a working directory in any of the potential location:\n" + testedStorageDirectories + "Please check that you have write permissions in at least one of these locations.\nThe application will now exit.", "Arbil Critical Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        System.out.println("storageDirectory: " + storageDirectory);
    }

    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
        File fromDirectory = getCacheDirectory();
        if (!preferedCacheDirectory.getAbsolutePath().contains("ArbilWorkingFiles") && !preferedCacheDirectory.getAbsolutePath().contains(".arbil/imdicache") && !localCacheDirectory.getAbsolutePath().contains(".linorg/imdicache")) {
            preferedCacheDirectory = new File(preferedCacheDirectory, "ArbilWorkingFiles");
        }
        if (!moveFiles || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame,
                "Moving files from:\n" + fromDirectory + "\nto:\n" + preferedCacheDirectory + "\n"
                + "Arbil will need to close in order to change the working directory.\nDo you wish to continue?", "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            if (moveFiles) {
                saveString("cacheDirectory", preferedCacheDirectory.getAbsolutePath());
                // move the files
                changeStorageDirectory(fromDirectory, preferedCacheDirectory);
            } else {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not change to the requested location.", null);
            }
        }
    }

    public void changeStorageDirectory(String preferedDirectory) {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Arbil will need to close in order to move the storage directory.\nDo you wish to continue?", "Arbil", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            File fromDirectory = storageDirectory;
            File toDirectory = new File(preferedDirectory);
            storageDirectory = new File(preferedDirectory);
            changeStorageDirectory(fromDirectory, toDirectory);
        }
    }

    // Move the storage directory and change the local corpus tree links to the new directory.
    // After completion the application will be closed!
    private void changeStorageDirectory(File fromDirectory, File toDirectory) {
        String toDirectoryUriString = toDirectory.toURI().toString().replaceAll("/$", "");
        String fromDirectoryUriString = fromDirectory.toURI().toString().replaceAll("/$", "");
        System.out.println("toDirectoryUriString: " + toDirectoryUriString);
        System.out.println("fromDirectoryUriString: " + fromDirectoryUriString);
        try {
            toDirectoryUriString = URLDecoder.decode(toDirectoryUriString, "UTF-8");
            fromDirectoryUriString = URLDecoder.decode(fromDirectoryUriString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            GuiHelper.linorgBugCatcher.logError(uee);
        }
        boolean success = fromDirectory.renameTo(toDirectory);
        if (!success) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not move the existing files to the requested location.", null); //\nThe files will need to be moved manually from:\n" + fromDirectory + "\nto:\n" + toDirectory, null);
        } else {
            try {
                Vector<String> locationsList = new Vector<String>();
                for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{TreeHelper.getSingleInstance().remoteCorpusNodes, TreeHelper.getSingleInstance().localCorpusNodes, TreeHelper.getSingleInstance().localFileNodes, TreeHelper.getSingleInstance().favouriteNodes}) {
                    for (ImdiTreeObject currentLocation : currentTreeArray) {
                        String currentLocationString = URLDecoder.decode(currentLocation.getUrlString(), "UTF-8");
                        System.out.println("currentLocationString: " + currentLocationString);
                        System.out.println("prefferedDirectoryUriString: " + toDirectoryUriString);
                        System.out.println("storageDirectoryUriString: " + fromDirectoryUriString);
                        locationsList.add(currentLocationString.replace(fromDirectoryUriString, toDirectoryUriString));
                    }
                }
                //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
                LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));

                System.out.println("updated locationsList");
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
            }
            TreeHelper.getSingleInstance().loadLocationsList();
            JOptionPane.showOptionDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The requested files have been moved, Arbil will now exit.", "Arbil", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Exit"}, "Exit");
            System.exit(0); // TODO: this exit might be unrequired
        }
    }

    public String[] getLocationOptions() {
//        for (Map.Entry<?, ?> e : System.getProperties().entrySet()) {
//            System.out.println(String.format("%s = %s", e.getKey(), e.getValue()));
//        }
//        System.out.println("HOMEDRIVE" + System.getenv("HOMEDRIVE"));
//        System.out.println("HOMEPATH" + System.getenv("HOMEPATH"));
        String[] locationOptions = new String[]{
            // System.getProperty("user.dir") is unreliable in the case of Vista and possibly others
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6519127
            System.getProperty("user.home") + File.separatorChar + "Local Settings" + File.separatorChar + "Application Data" + File.separatorChar + ".arbil" + File.separatorChar,
            System.getenv("APPDATA") + File.separatorChar + ".arbil" + File.separatorChar,
            //                    System.getProperty("user.home") + File.separatorChar + "directory with spaces" + File.separatorChar + ".arbil" + File.separatorChar,
            System.getProperty("user.home") + File.separatorChar + ".arbil" + File.separatorChar,
            System.getenv("USERPROFILE") + File.separatorChar + ".arbil" + File.separatorChar,
            System.getProperty("user.dir") + File.separatorChar + ".arbil" + File.separatorChar,
            // keep checking for linorg for users with old data
            System.getenv("APPDATA") + File.separatorChar + ".linorg" + File.separatorChar,
            System.getProperty("user.home") + File.separatorChar + ".linorg" + File.separatorChar,
            System.getenv("USERPROFILE") + File.separatorChar + ".linorg" + File.separatorChar,
            System.getProperty("user.dir") + File.separatorChar + ".linorg" + File.separatorChar
        };
        for (String currentLocationOption : locationOptions) {
            System.out.println("LocationOption: " + currentLocationOption);
        }
        return locationOptions;
    }

//    public void showDirectorySelectionDialogue() {
//        settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame));
//        settingsjDialog.setLocationRelativeTo(LinorgWindowManager.getSingleInstance().linorgFrame);
//        JPanel optionsPanel = new JPanel();
//        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
//        ButtonGroup group = new ButtonGroup();
//        for (String currentLocation : getLocationOptions()) {
//            if (!currentLocation.startsWith("null")) {
//                JRadioButton locationButton = new JRadioButton(currentLocation);
//                locationButton.setActionCommand(currentLocation);
//                locationButton.setSelected(storageDirectory.equals(currentLocation));
//                group.add(locationButton);
//                optionsPanel.add(locationButton);
////            birdButton.addActionListener(this);
//            }
//        }
//        JPanel buttonsPanel = new JPanel();
//        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
//        JButton moveButton = new JButton("Move");
//        moveButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        moveButton.setEnabled(false);
//        buttonsPanel.add(moveButton);
//        buttonsPanel.add(cancelButton);
//        optionsPanel.add(buttonsPanel);
//        settingsjDialog.add(optionsPanel);
////        optionsPanel.setBackground(Color.BLUE);
////        buttonsPanel.setBackground(Color.GREEN);
//        settingsjDialog.setTitle("Storage Directory Location");
////        settingsjDialog.setMinimumSize(new Dimension(400, 300));
//        settingsjDialog.pack();
//        settingsjDialog.setVisible(true);
//    }
    /**
     * Tests if the a string points to a file that is in the favourites directory.
     * @return Boolean
     */
    public boolean pathIsInFavourites(File fullTestFile) { //todo: test me
        String favouritesString = "favourites";
        int foundPos = fullTestFile.getPath().indexOf(favouritesString) + favouritesString.length();
        if (foundPos == -1) {
            return false;
        }
        if (foundPos > fullTestFile.getPath().length()) {
            return false;
        }
        File testFile = new File(fullTestFile.getPath().substring(0, foundPos));
        return getFavouritesDir().equals(testFile);
    }

    public URI getOriginatingUri(URI locationInCacheURI) {
        URI returnUri = null;
        String uriPath = locationInCacheURI.getPath();
//        System.out.println("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);
        System.out.println("uriPath: " + uriPath);
        int foundPos = uriPath.indexOf("imdicache");
        if (foundPos == -1) {
            return null;
        }
        uriPath = uriPath.substring(foundPos);
        String[] uriParts = uriPath.split("/", 4);
        try {
            if (uriParts[1].toLowerCase().equals("http")) {
                returnUri = new URI(uriParts[1], uriParts[2], "/" + uriParts[3], null); // [0] will be "imdicache"
                System.out.println("returnUri: " + returnUri);
            }
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        }
        return returnUri;
    }

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     * @return Boolean
     */
    public boolean pathIsInsideCache(File fullTestFile) {
        File cacheDirectory = getCacheDirectory();
        File testFile = fullTestFile;
        while (testFile != null) {
            if (testFile.equals(cacheDirectory)) {
                return true;
            }
            testFile = testFile.getParentFile();
        }
        return false;
    }

    /**
     * Checks for the existance of the favourites directory exists and creates it if it does not.
     * @return File pointing to the favourites directory
     */
    public File getFavouritesDir() {
        File favDirectory = new File(storageDirectory, "favourites"); // storageDirectory already has the file separator appended
        boolean favDirExists = favDirectory.exists();
        if (!favDirExists) {
            favDirExists = favDirectory.mkdir();
        }
        return favDirectory;
    }

    /**
     * Tests that the cache directory exists and creates it if it does not.
     * @return Boolean
     */
    public File getCacheDirectory() {
        if (localCacheDirectory == null) {
            // load from the text based properties file
            String localCacheDirectoryPathString = loadString("cacheDirectory");
            if (localCacheDirectoryPathString != null) {
                localCacheDirectory = new File(localCacheDirectoryPathString);
            } else {
                // otherwise load from the to be replaced binary based storage file
                try {
                    File localWorkingDirectory = (File) loadObject("cacheDirectory");
                    localCacheDirectory = localWorkingDirectory;
                } catch (Exception exception) {
                    localCacheDirectory = new File(storageDirectory, "imdicache"); // storageDirectory already has the file separator appended
                }
                saveString("cacheDirectory", localCacheDirectory.getAbsolutePath());
            }
            boolean cacheDirExists = localCacheDirectory.exists();
            if (!cacheDirExists) {
                cacheDirExists = localCacheDirectory.mkdirs();
            }
        }
        return localCacheDirectory;
    }

    /**
     * Serialises the passed object to a file in the linorg storage directory so that it can be retrieved on application restart.
     * @param object The object to be serialised
     * @param filename The name of the file the object is to be serialised into
     * @throws java.io.IOException
     */
    public void saveObject(Serializable object, String filename) throws IOException {
        System.out.println("saveObject: " + filename);
        ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(new File(storageDirectory, filename)));
        objstream.writeObject(object);
        objstream.close();
    }

    /**
     * Deserialises the file from the linorg storage directory into an object. Use to recreate program state from last save.
     * @param filename The name of the file containing the serialised object
     * @return The deserialised object
     * @throws java.lang.Exception
     */
    public Object loadObject(String filename) throws Exception {
        System.out.println("loadObject: " + filename);
        Object object = null;
        // this must be allowed to throw so don't do checks here
        ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(new File(storageDirectory, filename)));
        object = objstream.readObject();
        objstream.close();
        if (object == null) {
            throw (new Exception("Loaded object is null"));
        }
        return object;
    }

    public String[] loadStringArray(String filename) {
        // read the location list from a text file that admin-users can read and hand edit if they really want to
        try {
            ArrayList<String> stringArrayList = new ArrayList<String>();
            FileInputStream fstream = new FileInputStream(new File(storageDirectory, filename + ".config"));
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                stringArrayList.add(strLine);
            }
            in.close();
            return stringArrayList.toArray(new String[]{});
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        return null;

//        String[] stringProperty = {};
//        Properties propertiesObject = new Properties();
//        try {
//            // load the file
//            FileInputStream propertiesInStream = new FileInputStream(new File(storageDirectory, filename + ".config"));
//            propertiesObject.load(propertiesInStream);
//            // load all the values into an array
//            stringProperty = propertiesObject.values().toArray(new String[]{});
//            // close the file
//            propertiesInStream.close();
//        } catch (IOException ioe) {
//            // file not found so create the file
//            saveStringArray(filename, stringProperty);
//        }
//        return stringProperty;
    }

    public void saveStringArray(String filename, String[] storableValue) {
        // save the location list to a text file that admin-users can read and hand edit if they really want to
        try {
            FileWriter fstream = new FileWriter(new File(storageDirectory, filename + ".config"));
            BufferedWriter out = new BufferedWriter(fstream);
            for (String currentString : storableValue) {
                out.write(currentString + "\n");
            }
            out.close();
        } catch (Exception exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
//        try {
//            Properties propertiesObject = new Properties();
//            FileOutputStream propertiesOutputStream = new FileOutputStream(new File(storageDirectory, filename + ".config"));
//            for (int valueCounter = 0; valueCounter < storableValue.length; valueCounter++) {
//                propertiesObject.setProperty("nl.mpi.arbil." + filename + "." + valueCounter, storableValue[valueCounter]);
//            }
//            propertiesObject.store(propertiesOutputStream, null);
//            propertiesOutputStream.close();
//        } catch (IOException ioe) {
//            GuiHelper.linorgBugCatcher.logError(ioe);
//        }
    }

    public String loadString(String filename) {
        Properties configObject = getConfig();
        String stringProperty = configObject.getProperty("nl.mpi.arbil." + filename);
        return stringProperty;
    }

    public void saveString(String filename, String storableValue) {
        Properties configObject = getConfig();
        configObject.setProperty("nl.mpi.arbil." + filename, storableValue);
        saveConfig(configObject);
    }

    public boolean loadBoolean(String filename, boolean defaultValue) {
        Properties configObject = getConfig();
        String stringProperty = configObject.getProperty("nl.mpi.arbil." + filename);
        if (stringProperty == null) {
            stringProperty = new Boolean(defaultValue).toString();
            saveBoolean(filename, defaultValue);
        }
        return stringProperty.equalsIgnoreCase("true");
    }

    public void saveBoolean(String filename, boolean storableValue) {
        Properties configObject = getConfig();
        configObject.setProperty("nl.mpi.arbil." + filename, new Boolean(storableValue).toString());
        saveConfig(configObject);
    }

    private Properties getConfig() {
        Properties propertiesObject = new Properties();
        try {
            FileInputStream propertiesInStream = new FileInputStream(new File(storageDirectory, "arbil.config"));
            propertiesObject.load(propertiesInStream);
            propertiesInStream.close();
        } catch (IOException ioe) {
            // file not found so create the file
            saveConfig(propertiesObject);
        }
        return propertiesObject;
    }

    private void saveConfig(Properties configObject) {
        try {
            FileOutputStream propertiesOutputStream = new FileOutputStream(new File(storageDirectory, "arbil.config"));
            configObject.store(propertiesOutputStream, null);
            propertiesOutputStream.close();
        } catch (IOException ioe) {
            GuiHelper.linorgBugCatcher.logError(ioe);
        }
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @param expireCacheDays Number of days old that a file can be before it is replaced.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, int expireCacheDays) { // update if older than the date - x
        File targetFile = getSaveLocation(pathString);
        boolean fileNeedsUpdate = !targetFile.exists();
        if (!fileNeedsUpdate) {
            Date lastModified = new Date(targetFile.lastModified());
            Date expireDate = new Date(System.currentTimeMillis());
            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expireDate);
            calendar.add(Calendar.DATE, -expireCacheDays);
            expireDate.setTime(calendar.getTime().getTime());

            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());

            fileNeedsUpdate = expireDate.after(lastModified);
            System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        }
        System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        return updateCache(pathString, null, fileNeedsUpdate, new DownloadAbortFlag());
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag) {
        //TODO: There will need to be a way to expire the files in the cache.
        File cachePath = getSaveLocation(pathString);
        try {
            saveRemoteResource(new URL(pathString), cachePath, shibbolethNegotiator, expireCacheCopy, abortFlag);
        } catch (MalformedURLException mul) {
            GuiHelper.linorgBugCatcher.logError(mul);
        }
        return cachePath;
    }

    public boolean replaceCacheCopy(String pathString) {
        File cachePath = getSaveLocation(pathString);
        boolean fileDownloadedBoolean = false;
        try {
            fileDownloadedBoolean = saveRemoteResource(new URL(pathString), cachePath, null, true, new DownloadAbortFlag());
        } catch (MalformedURLException mul) {
            GuiHelper.linorgBugCatcher.logError(mul);
        }
        return fileDownloadedBoolean;
    }

    /**
     * Removes the cache path component from a path string and appends it to the destination directory.
     * Then tests for and creates the directory structure in the destination directory if requred.
     * @param pathString Path of a file within the cache.
     * @param destinationDirectory Path of the destination directory.
     * @return The path of the file in the destination directory.
     */
    public File getExportPath(String pathString, String destinationDirectory) {
        System.out.println("pathString: " + pathString);
        System.out.println("destinationDirectory: " + destinationDirectory);
        String cachePath = pathString;
        for (String testDirectory : new String[]{"imdicache", "ArbilWorkingFiles"}) {
            if (pathString.contains(testDirectory)) {
                cachePath = destinationDirectory + cachePath.substring(cachePath.lastIndexOf(testDirectory) + testDirectory.length()); // this path must be inside the cache for this to work correctly
            }
        }
        File returnFile = new File(cachePath);
        if (!returnFile.getParentFile().exists()) {
            returnFile.getParentFile().mkdirs();
        }
        return returnFile;
    }

    public URI getNewImdiFileName(File parentDirectory, String nodeType) {
        String suffixString;
        if (nodeType.toLowerCase().contains("clarin")) {
            suffixString = ".cmdi";
        } else {
            suffixString = ".imdi";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        int fileCounter = 0;
        File returnFile = new File(parentDirectory, formatter.format(new Date()) + suffixString);
        while (returnFile.exists()) {
            returnFile = new File(parentDirectory, formatter.format(new Date()) + (fileCounter++) + suffixString);
        }
        return returnFile.toURI();
    }

    /**
     * Converts a String path from the remote location to the respective location in the cache.
     * Then tests for and creates the directory structure in the cache if requred.
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    public File getSaveLocation(String pathString) {
        try {
            pathString = URLDecoder.decode(pathString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            GuiHelper.linorgBugCatcher.logError(uee);
        }
        pathString = pathString.replace("//", "/");
        for (String searchString : new String[]{".linorg/imdicache", ".arbil/imdicache", ".linorg\\imdicache", ".arbil\\imdicache", "ArbilWorkingFiles"}) {
            if (pathString.indexOf(searchString) > -1) {
                GuiHelper.linorgBugCatcher.logError(new Exception("Recursive path error (about to be corrected) in: " + pathString));
                pathString = pathString.substring(pathString.lastIndexOf(searchString) + searchString.length());
            }
        }
        String cachePath = pathString.replace(":/", "/").replace("//", "/");
        while (cachePath.contains(":")) { // todo: this may not be the only char that is bad on file systems and this will cause issues reconstructing the url later
            cachePath = cachePath.replace(":", "_");
        }
        if (cachePath.matches(".*/[^.]*$")) {
            // rest paths will create files and then require directories of the same name and this must be avoided
            cachePath = cachePath + ".dat";
        }
        File returnFile = new File(getCacheDirectory(), cachePath);
        if (!returnFile.getParentFile().exists()) {
            returnFile.getParentFile().mkdirs();
        }
        return returnFile;
    }

    /**
     * Copies a remote file over http and saves it into the cache.
     * @param targetUrlString The URL of the remote file as a string
     * @param destinationPath The local path where the file should be saved
     * @return boolean true ony if the file was downloaded, this will be false if the file exists but was not re-downloaded or if the dowload failed
     */
    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag) {
        boolean downloadSucceeded = false;
//        String targetUrlString = getFullResourceURI();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
//        System.out.println("saveRemoteResource: " + targetUrlString);
//        System.out.println("destinationPath: " + destinationPath);
//        File destinationFile = new File(destinationPath);
        if (destinationFile.length() == 0) {
            // if the file is zero length then is presumably should either be replaced or the version in the jar used.
            destinationFile.delete();
        }
        if (destinationFile.exists() && !expireCacheCopy && destinationFile.length() > 0) {
            System.out.println("this resource is already in the cache");
        } else {
            try {
                URLConnection urlConnection = targetUrl.openConnection();
                HttpURLConnection httpConnection = null;
                if (urlConnection instanceof HttpURLConnection) {
                    httpConnection = (HttpURLConnection) urlConnection;
                    if (shibbolethNegotiator != null) {
                        httpConnection = shibbolethNegotiator.getShibbolethConnection((HttpURLConnection) urlConnection);
//                        if (httpConnection.getResponseCode() != 200 && targetUrl.getProtocol().equals("http")) {
//                            // work around for resources being https when under shiboleth
//                            // try https after http failed
//                            System.out.println("Code: " + httpConnection.getResponseCode() + ", Message: " + httpConnection.getResponseMessage());
//                            System.out.println("trying https");
//                            targetUrl = new URL(targetUrl.toString().replace("http://", "https://"));
//                            urlConnection = targetUrl.openConnection();
//                            httpConnection = shibbolethNegotiator.getShibbolethConnection((HttpURLConnection) urlConnection);
//                        }
                    }
                    //h.setFollowRedirects(false);
                    System.out.println("Code: " + httpConnection.getResponseCode() + ", Message: " + httpConnection.getResponseMessage());
                }
                if (httpConnection != null && httpConnection.getResponseCode() != 200) {
                    System.out.println("non 200 response, skipping file");
                } else {
                    File tempFile = File.createTempFile(destinationFile.getName(), "tmp", destinationFile.getParentFile());
                    tempFile.deleteOnExit();
                    int bufferLength = 1024 * 3;
                    FileOutputStream outFile = new FileOutputStream(tempFile); //targetUrlString
                    System.out.println("getting file");
                    InputStream stream = urlConnection.getInputStream();
                    byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
                    int bytesread = 0;
                    int totalRead = 0;
                    while (bytesread >= 0 && !abortFlag.abortDownload) {
                        bytesread = stream.read(buffer);
                        totalRead += bytesread;
//                        System.out.println("bytesread: " + bytesread);
//                        System.out.println("Mbs totalRead: " + totalRead / 1048576);
                        if (bytesread == -1) {
                            break;
                        }
                        outFile.write(buffer, 0, bytesread);
                    }
                    outFile.close();
                    if (tempFile.length() > 0 && !abortFlag.abortDownload) { // TODO: this should check the file size on the server
                        if (destinationFile.exists()) {
                            destinationFile.delete();
                        }
                        tempFile.renameTo(destinationFile);
                        downloadSucceeded = true;
                    }
                    System.out.println("Downloaded: " + totalRead / 1048576 + " Mb");
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//                System.out.println(ex.getMessage());
            }
        }
        return downloadSucceeded;
    }
}
