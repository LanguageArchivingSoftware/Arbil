package nl.mpi.arbil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JLabel;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DownloadAbortFlag;

public class MockSessionStorage implements SessionStorage {

    private static final Logger log = Logger.getLogger(MockSessionStorage.class.toString());
    private File localCacheDirectory = null;

    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
	log.log(Level.INFO, "changeCacheDirectory({0},{1})", new Object[]{preferedCacheDirectory, moveFiles});
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
//		try {
//		    File localWorkingDirectory = (File) loadObject("cacheDirectory");
//		    localCacheDirectory = localWorkingDirectory;
//		} catch (Exception exception) {
		    if (new File(getStorageDirectory(), "imdicache").exists()) {
			localCacheDirectory = new File(getStorageDirectory(), "imdicache");
		    } else {
			localCacheDirectory = new File(getStorageDirectory(), "ArbilWorkingFiles");
		    }
//		}
		saveString("cacheDirectory", localCacheDirectory.getAbsolutePath());
	    }
	    boolean cacheDirExists = localCacheDirectory.exists();
	    if (!cacheDirExists) {
		if (!localCacheDirectory.mkdirs()) {
		    log.severe("Could not create cache directory");
		    return null;
		}
	    }
	}
	return localCacheDirectory;
    }

    public File getExportPath(String pathString, String destinationDirectory) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getFavouritesDir() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getLocationOptions() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getNewArbilFileName(File parentDirectory, String nodeType) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI getOriginatingUri(URI locationInCacheURI) {
	throw new UnsupportedOperationException("Not supported yet.");
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
	    log.log(Level.SEVERE, null, uee);
	}
	pathString = pathString.replace("//", "/");
	for (String searchString : new String[]{getCacheDirectory().toString()}) {
	    if (pathString.indexOf(searchString) > -1) {
		log.log(Level.SEVERE, "Recursive path error (about to be corrected) in: {0}", pathString);
		pathString = pathString.substring(pathString.lastIndexOf(searchString) + searchString.length());
	    }
	}
	String cachePath = pathString.replace(":/", "/").replace("//", "/");
	while (cachePath.contains(":")) { // todo: this may not be the only char that is bad on file systems and this will cause issues reconstructing the url later
	    cachePath = cachePath.replace(":", "_");
	}
	// make the xsd path tidy for viewing in an editor durring testing
	cachePath = cachePath.replaceAll("/xsd$", ".xsd");
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
    private HashMap<String, Object> saveMap = new HashMap<String, Object>();

    public boolean loadBoolean(String filename, boolean defaultValue) {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof Boolean) {
		    return (Boolean) object;
		}
	    }
	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return defaultValue;
    }

    public Object loadObject(String filename) throws Exception {
	return saveMap.get(filename);
    }

    public String loadString(String filename) {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof String) {
		    return (String) object;
		}
	    }

	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return null;
    }

    public String[] loadStringArray(String filename) throws IOException {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof String[]) {
		    return (String[]) object;
		}
	    }

	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return null;
    }

    public boolean pathIsInFavourites(File fullTestFile) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean pathIsInsideCache(File fullTestFile) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean replaceCacheCopy(String pathString) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveBoolean(String filename, boolean storableValue) {
	saveMap.put(filename, storableValue);
    }

    public void saveObject(Serializable object, String filename) throws IOException {
	saveMap.put(filename, object);
    }

    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag, JLabel progressLabel) {
	boolean downloadSucceeded = false;
//        String targetUrlString = getFullResourceURI();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
//        System.out.println("saveRemoteResource: " + targetUrlString);
//        System.out.println("destinationPath: " + destinationPath);
//        File destinationFile = new File(destinationPath);
	if (destinationFile.length() == 0) {
	    // todo: check the file size on the server and maybe its date also
	    // if the file is zero length then is presumably should either be replaced or the version in the jar used.
	    destinationFile.delete();
	}
	String fileName = destinationFile.getName();
	if (destinationFile.exists() && !expireCacheCopy && destinationFile.length() > 0) {
	    System.out.println("this resource is already in the cache");
	} else {
	    try {
		URLConnection urlConnection = targetUrl.openConnection();
		HttpURLConnection httpConnection = null;
		if (urlConnection instanceof HttpURLConnection) {
		    httpConnection = (HttpURLConnection) urlConnection;
//                    httpConnection.setFollowRedirects(false); // this is done when this class is created because it is a static call
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
		if (httpConnection != null && httpConnection.getResponseCode() != 200) { // if the url points to a file on disk then the httpconnection will be null, hence the response code is only relevant if the connection is not null
		    if (httpConnection == null) {
			System.out.println("httpConnection is null, hence this is a local file and we should not have been testing the response code");
		    } else {
			System.out.println("non 200 response, skipping file");
		    }
		} else {
		    File tempFile = File.createTempFile(destinationFile.getName(), "tmp", destinationFile.getParentFile());
		    tempFile.deleteOnExit();
		    int bufferLength = 1024 * 3;
		    FileOutputStream outFile = new FileOutputStream(tempFile); //targetUrlString
		    System.out.println("getting file");
		    InputStream stream = urlConnection.getInputStream();
		    byte[] buffer = new byte[bufferLength]; // make this 1024*4 or something and read chunks not the whole file
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
			if (progressLabel != null) {
			    progressLabel.setText(fileName + " : " + totalRead / 1024 + " Kb");
			}
		    }
		    outFile.close();
		    if (tempFile.length() > 0 && !abortFlag.abortDownload) { // TODO: this should check the file size on the server
			if (destinationFile.exists()) {
			    destinationFile.delete();
			}
			tempFile.renameTo(destinationFile);
			downloadSucceeded = true;
		    }
		    System.out.println("Downloaded: " + totalRead / (1024 * 1024) + " Mb");
		}
	    } catch (Exception ex) {
		log.log(Level.SEVERE, null, ex);
//                System.out.println(ex.getMessage());
	    }
	}
	return downloadSucceeded;
    }

    public void saveString(String filename, String storableValue) {
	saveMap.put(filename, storableValue);
    }

    public void saveStringArray(String filename, String[] storableValue) throws IOException {
	saveMap.put(filename, storableValue);
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
	return updateCache(pathString, null, fileNeedsUpdate, new DownloadAbortFlag(), null);
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag, JLabel progressLabel) {
	// to expire the files in the cache set the expireCacheCopy flag.
	File cachePath = getSaveLocation(pathString);
	try {
	    saveRemoteResource(new URL(pathString), cachePath, shibbolethNegotiator, expireCacheCopy, abortFlag, progressLabel);
	} catch (MalformedURLException mul) {
	    log.log(Level.SEVERE, null, new Exception(pathString, mul));
	}
	return cachePath;
    }
    private File tempDir;

    public File getStorageDirectory() {
	if (tempDir == null) {
	    try {
		tempDir = File.createTempFile("arbil", Long.toString(System.nanoTime()));
		tempDir.delete();
		tempDir.mkdir();
		tempDir.deleteOnExit();
		log.log(Level.INFO, "Create temp dir " + tempDir.getAbsolutePath());
	    } catch (IOException ex) {
		log.log(Level.SEVERE, null, ex);
	    }
	}
	return tempDir;
    }

    public boolean isTrackTableSelection() {
	return false;
    }

    public void setTrackTableSelection(boolean trackTableSelection) {
	log.log(Level.INFO, "setTrackTableSelection");
    }

    public boolean isUseLanguageIdInColumnName() {
	return false;
    }

    public void setUseLanguageIdInColumnName(boolean useLanguageIdInColumnName) {
	log.log(Level.INFO, "setUseLanguageIdInColumnName");
    }
}