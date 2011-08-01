package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilDataNodeLoaderThreadManager;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.LoaderThreadManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTestInjector extends ArbilInjector {

    public static synchronized void injectHandlers() {
	final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
	injectBugCatcher(bugCatcher);
	
	final LoaderThreadManager loaderThreadManager = new ArbilDataNodeLoaderThreadManager();
	injectLoaderThreadManager(loaderThreadManager);

	final MessageDialogHandler messageDialogHandler = new MockDialogHandler();
	injectDialogHandler(messageDialogHandler);

	final WindowManager windowManager = new MockWindowManager();
	injectWindowManager(windowManager);

	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);

	final SessionStorage sessionStorage = new MockSessionStorage();
	ArbilTestInjector.injectSessionStorage(sessionStorage);

	final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
	injectTreeHelper(treeHelper);
    }
}
