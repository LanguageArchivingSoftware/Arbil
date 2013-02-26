/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui.favourites;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import nl.mpi.arbil.favourites.FavouritesImportExportException;
import nl.mpi.arbil.favourites.FavouritesImporter;
import nl.mpi.flap.plugin.PluginBugCatcher;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginException;

/**
 * Controller class for handling import requests from the UI
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImportAction extends AbstractAction {

    private final PluginDialogHandler dialogHandler;
    private final PluginBugCatcher bugCatcher;
    private final FavouritesImporter importer;

    public ImportAction(PluginDialogHandler dialogHandler, PluginBugCatcher bugCatcher, FavouritesImporter importer) {
	this.dialogHandler = dialogHandler;
	this.bugCatcher = bugCatcher;
	this.importer = importer;
    }

    public void actionPerformed(ActionEvent e) {
	importFavourites();
	if (e.getSource() instanceof ImportUI) {
	    ((ImportUI) e.getSource()).refresh();
	}
    }

    private void importFavourites() {
	final File[] exportLocation = dialogHandler.showFileSelectBox("Select favourites directory to import", true, false, null, PluginDialogHandler.DialogueType.open, null);
	if (exportLocation != null && exportLocation.length > 0 && exportLocation[0] != null) {
	    try {
		importer.importFavourites(exportLocation[0]);
		dialogHandler.addMessageDialogToQueue("Favourites have been imported", "Import complete");
	    } catch (FavouritesImportExportException ex) {
		//TODO: Wrap exception in PluginException as soon as this is supported
		bugCatcher.logException(new PluginException(ex.getMessage()));
		dialogHandler.addMessageDialogToQueue("An error occurred while importing favourites. See error log for details.", "Error");
	    }
	}
    }
}