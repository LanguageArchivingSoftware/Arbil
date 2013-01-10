/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.RemoteServerSearchTerm;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : RemoteServerSearchTermPanel
 * Created on : Sept 22, 2010, 15:31:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class RemoteServerSearchTermPanel extends JPanel implements RemoteServerSearchTerm {

    private ArbilNodeSearchPanel parentPanel;
    private JTextField searchField;
    private JLabel resultCountLabel;
    private ArbilRemoteSearch remoteSearch = new ArbilRemoteSearch();
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }

    public RemoteServerSearchTermPanel(ArbilNodeSearchPanel parentPanelLocal) {
	parentPanel = parentPanelLocal;
	searchField = new javax.swing.JTextField(valueFieldMessage);
	searchField.setForeground(Color.lightGray);

	this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

	searchField.addFocusListener(new FocusListener() {

	    public void focusGained(FocusEvent e) {
		if (searchField.getText().equals(valueFieldMessage)) {
		    searchField.setText("");
		    searchField.setForeground(Color.BLACK);
		}
	    }

	    public void focusLost(FocusEvent e) {
		if (searchField.getText().length() == 0) {
		    searchField.setText(valueFieldMessage);
		    searchField.setForeground(Color.lightGray);
		}
	    }
	});

	searchField.addKeyListener(new java.awt.event.KeyAdapter() {

	    @Override
	    public void keyReleased(java.awt.event.KeyEvent evt) {
		parentPanel.stopSearch();
	    }
	});
	resultCountLabel = new JLabel();
	this.add(searchField);
	this.add(resultCountLabel);
    }

    @Override
    public URI[] getServerSearchResults(ArbilDataNode[] arbilDataNodeArray) {
	final String searchFieldText = searchField.getText();
	if (ArbilRemoteSearch.isEmptyQuery(searchFieldText)) {
	    dialogHandler.addMessageDialogToQueue("No remote search term provided, cannot search remotely", "Remote Search");
	}

	URI[] searchResult = remoteSearch.getServerSearchResults(searchFieldText, arbilDataNodeArray);
	resultCountLabel.setText(searchResult.length + " found on server ");
	return searchResult;
    }
}
