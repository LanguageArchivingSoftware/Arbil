/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.util;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public interface TableManager {

    void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openSearchTable(ArbilNode[] selectedNodes, String frameTitle);
}
