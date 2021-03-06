package nl.mpi.arbil.wicket;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.ProgressListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSessionStorage implements SessionStorage {

    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getApplicationSettingsDirectory() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getProjectDirectory() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getProjectWorkingDirectory() {
	throw new UnsupportedOperationException("Not supported yet.");
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

    public File getSaveLocation(String pathString) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean loadBoolean(String filename, boolean defaultValue) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object loadObject(String filename) throws Exception {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public String loadString(String filename) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] loadStringArray(String filename) throws IOException {
	throw new UnsupportedOperationException("Not supported yet.");
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
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveObject(Serializable object, String filename) throws IOException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirects, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveString(String filename, String storableValue) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveStringArray(String filename, String[] storableValue) throws IOException {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File updateCache(String pathString, int expireCacheDays, boolean followRedirects) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirects, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getTypeCheckerConfig() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getFromCache(String pathString, boolean followRedirect) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
