package nl.mpi.arbil.wicket;

import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.AbstractTreeHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;

/**
 * TreeHelper that is tied to a specific session storage
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeHelper extends AbstractTreeHelper {

    private SessionStorage sessionStorage;
    private ArbilWicketTreeModel localCorpusTreeModel;
    private ArbilWicketTreeModel remoteCorpusTreeModel;
    private ArbilWicketTreeModel localDirectoryTreeModel;
    private ArbilWicketTreeModel favouritesTreeModel;

    /**
     *
     * @param sessionStorage Storage to tie treehelper to
     */
    public ArbilWicketTreeHelper(MessageDialogHandler dialogHandler, SessionStorage sessionStorage) {
	super(dialogHandler);
	this.sessionStorage = sessionStorage;

    }

    public void init() {
	initTrees();
	loadLocationsList();
    }

    @Override
    protected synchronized SessionStorage getSessionStorage() {
	return sessionStorage;
    }

    @Override
    public void applyRootLocations() {
	localCorpusTreeModel.setRootNodeChildren(getLocalCorpusNodes());
	localCorpusTreeModel.requestResort();

	remoteCorpusTreeModel.setRootNodeChildren(getRemoteCorpusNodes());
	remoteCorpusTreeModel.requestResort();

	localDirectoryTreeModel.setRootNodeChildren(getLocalFileNodes());
	localDirectoryTreeModel.requestResort();

	favouritesTreeModel.setRootNodeChildren(getFavouriteNodes());
	favouritesTreeModel.requestResort();
    }

    @Override
    protected void initTreeModels() {
	localCorpusTreeModel = new ArbilWicketTreeModel(new DefaultMutableTreeNode(localCorpusRootNodeObject));
	remoteCorpusTreeModel = new ArbilWicketTreeModel(new DefaultMutableTreeNode(remoteCorpusRootNodeObject));
	localDirectoryTreeModel = new ArbilWicketTreeModel(new DefaultMutableTreeNode(localDirectoryRootNodeObject));
	favouritesTreeModel = new ArbilWicketTreeModel(new DefaultMutableTreeNode(favouritesRootNodeObject));
    }

    @Override
    public void deleteNodes(Object sourceObject) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArbilWicketTreeModel getRemoteCorpusTreeModel() {
	return remoteCorpusTreeModel;
    }

    @Override
    public ArbilWicketTreeModel getLocalCorpusTreeModel() {
	return localCorpusTreeModel;
    }

    @Override
    public ArbilWicketTreeModel getLocalDirectoryTreeModel() {
	return localDirectoryTreeModel;
    }

    @Override
    public ArbilWicketTreeModel getFavouritesTreeModel() {
	return favouritesTreeModel;
    }
}
