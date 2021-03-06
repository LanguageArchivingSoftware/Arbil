package nl.mpi.arbil.wicket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Proxy for the ArbilWicketTreeHelper that is contained in the session that makes
 * the request. To be injected into Arbil core classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeHelperProxy implements TreeHelper {

    private TreeHelper getTreeHelper() {
	// Session storage is retrieved from the request cycle. It is kept there because it gets
	// stored in a thread local variable per request
	return ArbilWicketSession.get().getTreeHelper();
    }

    public void init() {
	getTreeHelper().init();
    }

    public int addDefaultCorpusLocations() {
	return getTreeHelper().addDefaultCorpusLocations();
    }

    public boolean addLocation(URI addedLocation) {
	return getTreeHelper().addLocation(addedLocation);
    }

    public void applyRootLocations() {
	getTreeHelper().applyRootLocations();
    }

    public void deleteNodes(Object sourceObject) {
	getTreeHelper().deleteNodes(sourceObject);
    }

    public ArbilDataNode[] getFavouriteNodes() {
	return getTreeHelper().getFavouriteNodes();
    }

    public DefaultTreeModel getFavouritesTreeModel() {
	return getTreeHelper().getFavouritesTreeModel();
    }

    public ArbilDataNode[] getLocalCorpusNodes() {
	return getTreeHelper().getLocalCorpusNodes();
    }

    public DefaultTreeModel getLocalCorpusTreeModel() {
	return getTreeHelper().getLocalCorpusTreeModel();
    }

    public DefaultTreeModel getLocalDirectoryTreeModel() {
	return getTreeHelper().getLocalDirectoryTreeModel();
    }

    public ArbilDataNode[] getLocalFileNodes() {
	return getTreeHelper().getLocalFileNodes();
    }

    public ArbilDataNode[] getRemoteCorpusNodes() {
	return getTreeHelper().getRemoteCorpusNodes();
    }

    public DefaultTreeModel getRemoteCorpusTreeModel() {
	return getTreeHelper().getRemoteCorpusTreeModel();
    }

    public boolean isInFavouritesNodes(ArbilDataNode dataNode) {
	return getTreeHelper().isInFavouritesNodes(dataNode);
    }

    public boolean isShowHiddenFilesInTree() {
	return getTreeHelper().isShowHiddenFilesInTree();
    }

    public void jumpToSelectionInTree(boolean silent, ArbilDataNode cellDataNode) {
	getTreeHelper().jumpToSelectionInTree(silent, cellDataNode);
    }

    public void loadLocationsList() {
	getTreeHelper().loadLocationsList();
    }

    public boolean locationsHaveBeenAdded() {
	return getTreeHelper().locationsHaveBeenAdded();
    }

    public void removeLocation(ArbilDataNode removeObject) {
	getTreeHelper().removeLocation(removeObject);
    }

    public void removeLocation(URI removeLocation) {
	getTreeHelper().removeLocation(removeLocation);
    }

    public void saveLocations(ArbilDataNode[] nodesToAdd, ArbilDataNode[] nodesToRemove) {
	getTreeHelper().saveLocations(nodesToAdd, nodesToRemove);
    }

    public void setShowHiddenFilesInTree(boolean showState) {
	getTreeHelper().setShowHiddenFilesInTree(showState);
    }

    public boolean addLocationInteractive(URI addableLocation) {
	return getTreeHelper().addLocationInteractive(addableLocation);
    }

    public void clearRemoteLocations() {
	getTreeHelper().clearRemoteLocations();
    }

    public void addLocations(List<URI> locations) {
	getTreeHelper().addLocations(locations);
    }

    public void addLocations(InputStream inputStream) throws IOException {
	getTreeHelper().addLocations(inputStream);
    }

    @Override
    public boolean isGroupFavouritesByType() {
	return getTreeHelper().isGroupFavouritesByType();
    }

    @Override
    public void setGroupFavouritesByType(boolean groupFavouritesByType) {
	getTreeHelper().setGroupFavouritesByType(groupFavouritesByType);
    }
}
