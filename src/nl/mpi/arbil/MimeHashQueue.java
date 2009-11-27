package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import nl.mpi.arbil.data.ImdiLoader;

/**
 * Document   : MimeHashQueue
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class MimeHashQueue {
    // stored across sessions

    private Hashtable<String, Long> processedFilesMTimes; // make this a vector and maybe remove or maybe make file path and file mtime
    private Hashtable<String, String[]> knownMimeTypes; // imdi path/file path, mime type : maybe sould only be file path
    private Hashtable<String, Vector<String>> md5SumToDuplicates;
    private Hashtable<String, String> pathToMd5Sums;
    // not stored across sessions
    private Vector<ImdiTreeObject> imdiObjectQueue;
//    private Hashtable<String, ImdiTreeObject> currentlyLoadedImdiObjects;
    private boolean continueThread = false;
    private static mpi.bcarchive.typecheck.FileType fileType; //  used to check the file type
    private static mpi.bcarchive.typecheck.DeepFileType deepFileType;
    static private MimeHashQueue singleInstance = null;

    static synchronized public MimeHashQueue getSingleInstance() {
//        System.out.println("MimeHashQueue getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new MimeHashQueue();
        }
        return singleInstance;
    }

    public MimeHashQueue() {
        System.out.println("MimeHashQueue init");
        imdiObjectQueue = new Vector();
        continueThread = true;
        new Thread() {

            public void run() {
                setPriority(Thread.MIN_PRIORITY);
                System.out.println("MimeHashQueue run");
                fileType = new mpi.bcarchive.typecheck.FileType(); //  used to check the file type
                deepFileType = new mpi.bcarchive.typecheck.DeepFileType();
                // load from disk
                loadMd5sumIndex();
                boolean changedSinceLastSave = false;
                while (continueThread) {
                    try {
                        sleep(500);//sleep for 100 ms
                    } catch (InterruptedException ie) {
                        GuiHelper.linorgBugCatcher.logError(ie);
//                        System.err.println("run MimeHashQueue: " + ie.getMessage());
                    }
                    while (imdiObjectQueue.size() > 0) {
                        ImdiTreeObject currentImdiObject = imdiObjectQueue.remove(0);
                        if (currentImdiObject.hasResource() && !currentImdiObject.hasLocalResource()) {
                            checkServerPermissions(currentImdiObject);
                        } else {
//                        System.out.println("MimeHashQueue checking: " + currentImdiObject);
                            String currentPathString = getFilePath(currentImdiObject);
                            if (currentPathString != null && currentPathString.length() > 0) {
                                try {
                                    URL currentNodeUrl = new URL(currentPathString);
                                    // check if this file has been process before and then check its mtime
                                    File currentFile = new File(currentNodeUrl.getFile());
                                    if (currentFile.exists()) {
                                        long previousMTime = 0;
                                        if (processedFilesMTimes.containsKey(currentPathString)) {
                                            previousMTime = processedFilesMTimes.get(currentPathString);
                                        }
                                        long currentMTime = currentFile.lastModified();
//                                System.out.println("run MimeHashQueue mtime: " + currentPathString);
                                        String[] lastCheckedMimeArray = knownMimeTypes.get(currentPathString);
                                        if (previousMTime != currentMTime || lastCheckedMimeArray == null) {
//                                    System.out.println("run MimeHashQueue processing: " + currentPathString);
                                            currentImdiObject.setMimeType(getMimeType(currentNodeUrl, currentPathString));
                                            currentImdiObject.hashString = getHash(currentNodeUrl, currentPathString);
                                            processedFilesMTimes.put(currentPathString, currentMTime); // avoid issues of the file being modified between here and the last mtime check
                                            changedSinceLastSave = true;
                                        } else {
                                            currentImdiObject.hashString = pathToMd5Sums.get(currentPathString);
                                            currentImdiObject.setMimeType(lastCheckedMimeArray);
                                        }
                                        updateAutoFields(currentImdiObject, currentFile);
                                        updateImdiIconsToMatchingFileNodes(currentPathString); //for each node relating to the found sum run getMimeHashResult() or quivalent to update the nodes for the found md5
                                    }
                                } catch (MalformedURLException e) {
                                    //GuiHelper.linorgBugCatcher.logError(currentPathString, e);
                                    System.out.println("MalformedURLException: " + currentPathString + " : " + e);
                                }
                            }
                        }
//                        currentImdiObject.updateLoadingState(-1); // Loading state change dissabled due to performance issues when offline
                        currentImdiObject.clearIcon();
                    }
                    //TODO: take one file from the list and check it is still there and that it has the same mtime and maybe check the md5sum
                    //TODO: when deleting resouce or removing a session or corpus branch containing a session check for links 
                    if (changedSinceLastSave) {
                        saveMd5sumIndex();
                        changedSinceLastSave = false;
                    }
                    // TODO: add check for url in list with different hash which would indicate a modified file and require a red x on the icon
                    // TODO: add check for mtime change and update accordingly
                }
                System.out.println("MimeHashQueue stop");
            }
        }.start();
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
//        // save to disk
//        saveMd5sumIndex(); // this is called by guihelper
        //        ImdiTreeObject.mimeHashQueue.saveMd5sumIndex();
        super.finalize();
    }

    private void loadMd5sumIndex() {
        System.out.println("MimeHashQueue loadMd5sumIndex");
        try {
            knownMimeTypes = (Hashtable) LinorgSessionStorage.getSingleInstance().loadObject("knownMimeTypesV2");
            pathToMd5Sums = (Hashtable) LinorgSessionStorage.getSingleInstance().loadObject("pathToMd5Sums");
            processedFilesMTimes = (Hashtable) LinorgSessionStorage.getSingleInstance().loadObject("processedFilesMTimeV2");
            md5SumToDuplicates = (Hashtable) LinorgSessionStorage.getSingleInstance().loadObject("md5SumToDuplicates");
            System.out.println("loaded md5 and mime from disk");
        } catch (Exception ex) {
            knownMimeTypes = new Hashtable();
            pathToMd5Sums = new Hashtable();
            processedFilesMTimes = new Hashtable();
            md5SumToDuplicates = new Hashtable();
            System.out.println("load loadMd5sumIndex failed: " + ex.getMessage());
        }
    }

    private void saveMd5sumIndex() {
        System.out.println("MimeHashQueue saveMd5sumIndex");
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(knownMimeTypes, "knownMimeTypesV2");
            LinorgSessionStorage.getSingleInstance().saveObject(pathToMd5Sums, "pathToMd5Sums");
            LinorgSessionStorage.getSingleInstance().saveObject(processedFilesMTimes, "processedFilesMTimesV2");
            LinorgSessionStorage.getSingleInstance().saveObject(md5SumToDuplicates, "md5SumToDuplicates");
            System.out.println("saveMd5sumIndex");
        } catch (IOException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("saveMap exception: " + ex.getMessage());
        }
    }

    private void updateAutoFields(ImdiTreeObject currentImdiObject, File resourceFile) {
        Set<String> currentNodeFieldNames = currentImdiObject.getFields().keySet();
        // loop over the auto fields from the template
        for (String[] autoFields : currentImdiObject.currentTemplate.autoFieldsArray) {
            String fieldPath = autoFields[0];
            String fileAttribute = autoFields[1];
            String autoValue = null;
            if (fileAttribute.equals("Size")) {
                if (!currentImdiObject.resourceFileNotFound()) {
                    autoValue = (resourceFile.length() / 1024) + "KB";
                }
            } else if (fileAttribute.equals("MpiMimeType")) {
                autoValue = currentImdiObject.mpiMimeType;
            } else if (fileAttribute.equals("FileType")) {
                autoValue = mpi.bcarchive.typecheck.FileType.resultToMimeType(currentImdiObject.typeCheckerMessage);
                if (autoValue != null) {
                    int indexOfChar = autoValue.indexOf("/");
                    if (indexOfChar > 0) {
                        autoValue = autoValue.substring(0, indexOfChar); // TODO: does the tyoe checker not provide this???
                    }
                }
            }
            if (autoValue == null) {
                autoValue = ""; // clear any fields that have no new data but may be out of date
            }
            if (autoValue != null) {
                // loop over the field names in the imdi tree node
                for (String currentKeyString : currentNodeFieldNames) {
                    // look for the field name at the end of the auto field path
                    if (fieldPath.endsWith(currentKeyString)) {
                        ImdiField[] currentFieldArray = currentImdiObject.getFields().get(currentKeyString);
                        if (currentFieldArray != null) {
                            // verify that the full field path is the same as the auto field path
                            if (currentFieldArray[0].getGenericFullXmlPath().equals(fieldPath)) {
                                // set the value of the fields with the requested data
                                // note that there will usually only be one of each so we could just use the first in the array
                                for (ImdiField currentField : currentFieldArray) {
                                    currentField.setFieldValue(autoValue, true, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateImdiIconsToMatchingFileNodes(String currentPathString) {//for each node relating to the found sum run getMimeHashResult() or quivalent to update the nodes for the found md5
        int matchesInCache = 0;
        int matchesLocalFileSystem = 0;
        int matchesRemote = 0;
        // get the md5sum from the path
        String currentMd5Sum = pathToMd5Sums.get(currentPathString);
        if (currentMd5Sum != null) {
            // loop the paths for the md5sum
            Vector<String> duplicatesPaths = md5SumToDuplicates.get(currentMd5Sum);
            Vector<ImdiTreeObject> relevantImdiObjects = new Vector();
            for (Enumeration<String> duplicatesPathEnum = duplicatesPaths.elements(); duplicatesPathEnum.hasMoreElements();) {
                String currentDupPath = duplicatesPathEnum.nextElement();
                try {
                    File currentFile = new File(new URL(currentDupPath).getFile());
                    if (currentFile.exists()) { // check that the file still exists and has the same mtime otherwise rescan
                        // get the currently loaded imdiobjects for the paths
                        ImdiTreeObject currentImdiObject = ImdiLoader.getSingleInstance().getImdiObjectOnlyIfLoaded(currentDupPath);
                        if (currentImdiObject != null) {
                            relevantImdiObjects.add(currentImdiObject);
                        }
                        if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(currentFile)) {
                            matchesInCache++;
                        } else {
                            matchesLocalFileSystem++;
                        }
                        matchesRemote = 0;// TODO: set up the server md5sum query
                    }
                } catch (Exception e) {
                }
            }
            for (Enumeration<ImdiTreeObject> relevantImdiEnum = relevantImdiObjects.elements(); relevantImdiEnum.hasMoreElements();) {
                ImdiTreeObject currentImdiObject = relevantImdiEnum.nextElement();
                // update the values
                currentImdiObject.matchesInCache = matchesInCache;
                currentImdiObject.matchesLocalFileSystem = matchesLocalFileSystem;
                currentImdiObject.matchesRemote = matchesRemote;
                currentImdiObject.clearIcon();
            }
        }
    }

    private String[] getMimeType(URL fileUrl, String nodePath) {
//        System.out.println("getMimeType: " + fileUrl);
        String mpiMimeType;
        String typeCheckerMessage;
        // here we also want to check the magic number but the mpi api has a function similar to that so we
        // use the mpi.api to get the mime type of the file, if the mime type is not a valid archive format the api will return null
        // because the api uses null to indicate non archivable we cant return other strings
        mpiMimeType = null;//"unreadable";
        typeCheckerMessage = null;
        boolean deep = false;
        if (!new File(fileUrl.getFile()).exists()) {
//            System.out.println("File does not exist: " + fileUrl);
        } else {
            try {
                // this will choke on strings that look url encoded but are not. because it erroneously decodes them
                InputStream inputStream = fileUrl.openStream();
                if (inputStream != null) {
                    String pamperUrl = fileUrl.getFile().replace("//", "/");
                    if (deep) {
                        typeCheckerMessage = deepFileType.checkStream(inputStream, pamperUrl);
                    } else {
                        typeCheckerMessage = fileType.checkStream(inputStream, pamperUrl);
                    }
//                    System.out.println("mpiMimeType: " + typeCheckerMessage);
                }
                mpiMimeType = mpi.bcarchive.typecheck.FileType.resultToMPIType(typeCheckerMessage);
            } catch (Exception ioe) {
//                GuiHelper.linorgBugCatcher.logError(ioe);
                System.out.println("Cannot read file at URL: " + fileUrl + " ioe: " + ioe.getMessage());
            }
            System.out.println(mpiMimeType);
        }
        String[] resultArray = new String[]{mpiMimeType, typeCheckerMessage};
        // if non null then it is an archivable file type
//        if (mpiMimeType != null) {
        knownMimeTypes.put(nodePath, resultArray);
//        } else {
        // because the api uses null to indicate non archivable we cant return other strings
        //knownMimeTypes.put(filePath, "nonarchivable");
//        }
        return resultArray;
    }

    private String getHash(URL fileUrl, String nodePath) {
//        System.out.println("getHash: " + fileUrl);
//        File targetFile = new URL(filePath).getFile();
        String hashString = null;
        // TODO: add hashes for session links 
        // TODO: organise a way to get the md5 sum of files on the server
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            StringBuffer hexString = new StringBuffer();
            FileInputStream is = new FileInputStream(fileUrl.getFile());
            byte[] buff = new byte[1024];
            byte[] md5sum;
            int i = 0;
            while ((i = is.read(buff)) > 0) {
                digest.update(buff, 0, i);
            }
            md5sum = digest.digest();
            for (i = 0; i < md5sum.length; ++i) {
                hexString.append(Integer.toHexString(0x0100 + (md5sum[i] & 0x00FF)).substring(1));
            }
            hashString = hexString.toString();
//                    debugOut("file: " + this.getFile().getAbsolutePath());
//                    debugOut("location: " + getUrl());
//                    debugOut("digest: " + digest.toString());                    
        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logMessage("getHash: " + targetFile);
//            GuiHelper.linorgBugCatcher.logError("getHash: " + fileUrl, ex);
            System.out.println("failed to created hash: " + ex.getMessage());
        }
        // store the url to node mapping. Note that; in the case of a resource line the session node is mapped against the resource url not the imdichildnode for the file
//                urlToNodeHashtable.put(nodeLocation, this);

//        String filePath = fileUrl.getPath();
        if (hashString != null) {
            pathToMd5Sums.put(nodePath, hashString);
            Object matchingNodes = md5SumToDuplicates.get(hashString);
            if (matchingNodes != null) {
//                        debugOut("checking vector for: " + hashString);
                if (!((Vector) matchingNodes).contains(nodePath)) {
//                            debugOut("adding to vector: " + hashString);
                    Enumeration otherNodesEnum = ((Vector) matchingNodes).elements();
                    while (otherNodesEnum.hasMoreElements()) {
                        Object currentElement = otherNodesEnum.nextElement();
                        Object currentNode = processedFilesMTimes.get(currentElement);
                        if (currentNode instanceof ImdiTreeObject) {
                            //debugOut("updating icon for: " + ((ImdiTreeObject) currentNode).getUrl());
                            // clear the icon of the other copies so that they will be updated to indicate the commonality
                            System.out.println("Clearing icon for other node: " + currentNode.toString());
                            ((ImdiTreeObject) currentNode).clearIcon();
                        }
                    }
                    ((Vector) matchingNodes).add(nodePath);
                }
            } else {
                System.out.println("creating new vector for: " + hashString);
                Vector nodeVector = new Vector();
                nodeVector.add(nodePath);
                md5SumToDuplicates.put(hashString, nodeVector);
            }
        }
//            }
//        System.out.println("hashString: " + hashString);
        return hashString;
    }

    private void checkServerPermissions(ImdiTreeObject imdiObject) {
        try {
//            System.out.println("imdiObject: " + imdiObject);
            HttpURLConnection resourceConnection = (HttpURLConnection) new URL(imdiObject.getFullResourcePath()).openConnection();
//            System.out.println("conn: " + resourceConnection.getURL());
            imdiObject.resourceFileServerResponse = resourceConnection.getResponseCode();
            if (imdiObject.resourceFileServerResponse == HttpURLConnection.HTTP_NOT_FOUND || imdiObject.resourceFileServerResponse == HttpURLConnection.HTTP_FORBIDDEN) {
                imdiObject.fileNotFound = true;
            } else {
                imdiObject.fileNotFound = false;
            }
//            System.out.println("ResponseCode: " + resourceConnection.getResponseCode());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private String getFilePath(ImdiTreeObject imdiObject) {
        if (imdiObject.hasResource()) {
            return imdiObject.getFullResourcePath();
        } else {
            return imdiObject.getUrlString();
        }
    }

    public void addToQueue(ImdiTreeObject imdiObject) {
//        System.out.println("MimeHashQueue addToQueue: " + imdiObject);
        // TODO: when removing a directory from the local woking directories or deleting a resource all records of the file should be removed from the objects in this class to prevent bloating
        if (((imdiObject.isLocal() && !imdiObject.isImdi() && !imdiObject.isDirectory()) || (imdiObject.isImdiChild() && imdiObject.hasResource()))) {
//            System.out.println("addToQueue: " + getFilePath(imdiObject));
//            System.out.println("addToQueue session: " + imdiObject.isSession());
//            System.out.println("addToQueue directory: " + imdiObject.isDirectory());
//            System.out.println("addToQueue: " + getFilePath(imdiObject));
//            if (new File(new URL(getFilePath(imdiObject)).getFile().exists()) {// here also check that the destination file exists
            if (!imdiObjectQueue.contains(imdiObject)) {
//                imdiObject.updateLoadingState(+1); // Loading state change dissabled due to performance issues when offline
                imdiObjectQueue.add(imdiObject);
            }
        }
    }
    //    public String getMimeResult(ImdiTreeObject imdiObject) {
//        if (knownMimeTypes != null && imdiObject != null) {
//            Object returnObject = knownMimeTypes.get(getFilePath(imdiObject));
//            if (returnObject != null) {
//                return returnObject.toString();
//            }
//        }
//        return null;
//    }
//    public String getHashResult(ImdiTreeObject imdiObject) {
//        Object returnObject = null;
//        if (pathToMd5Sums != null) {
//            returnObject = pathToMd5Sums.get(getFilePath(imdiObject));
//        }
//        if (returnObject != null) {
//            return returnObject.toString();
//        } else {
//            return null;
//        }
//    }
//    public Enumeration getDuplicateList(String hashString) {
//        Object matchingNodes = md5SumToDuplicates.get(hashString);
//        return ((Vector) matchingNodes).elements();
//    }
//            public void getMimeHashResult() {
//        hashString = mimeHashQueue.getHashResult(this);
//        mpiMimeType = mimeHashQueue.getMimeResult(this);
//
//        // there is no point counting matches when the hash does not exist, ie when there is no file.          
//        if (hashString != null) {
//            //System.out.println("countMatches <<<<<<<<<<< " + this.toString());
//            matchesLocal = 0;
//            matchesRemote = 0;
//            matchesLocalResource = 0;
//            if (hashString != null) {
//                for (Enumeration listOfMatches = mimeHashQueue.getDuplicateList(hashString); listOfMatches.hasMoreElements();) {
//                    String currentUrl = listOfMatches.nextElement().toString();
//                    //System.out.println("currentUrl: " + currentUrl);
//                    if (ImdiTreeObject.isStringLocal(currentUrl)) {
//                        if (ImdiTreeObject.isStringImdiChild(currentUrl)) {
//                            matchesLocalResource++;
//                        } else {
//                            matchesLocal++;
//                        }
//                    } else {
//                        matchesRemote++;
//                    }
//                }
//            //System.out.println(">>> [L:" + matchesLocal + " R:" + matchesRemote + "]");
//            }
//        }
//    }
}
