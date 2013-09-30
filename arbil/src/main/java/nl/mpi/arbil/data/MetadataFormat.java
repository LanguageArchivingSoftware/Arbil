/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.clarin.HandleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : MetadataFormat <br>Created on : Aug 26, 2011, 10:19:34 AM Author :
 * Peter Withers
 */
public class MetadataFormat {

    private final static Logger logger = LoggerFactory.getLogger(MetadataFormat.class);

    static class FormatType {

        private FileType fileType;
        private String suffixString;
        private String metadataStartXpath;
        private ImageIcon imageIcon;

        public FormatType(String suffixString, String metadataStartXpath, ImageIcon imageIcon, FileType fileType) {
            this.fileType = fileType;
            this.suffixString = suffixString;
            this.metadataStartXpath = metadataStartXpath;
            this.imageIcon = imageIcon;
        }
    }

    public enum FileType {

        IMDI,
        CMDI,
        KMDI,
        FILE,
        DIRECTORY,
        CHILD,
        UNKNOWN
    }
    private final static Collection<FormatType> knownFormats = new CopyOnWriteArraySet<FormatType>(Arrays.asList(new FormatType[]{
        new FormatType(".imdi", ".METATRANSCRIPT", null, FileType.IMDI),
        // todo: the filter strings used by the cmdi templates and metadata loading process should be reading the metadataStartXpath from here instead
        new FormatType(".cmdi", ".CMD.Components", ArbilIcons.clarinIcon, FileType.CMDI),
        // Generic XML
        new FormatType(".xml", "", ArbilIcons.clarinIcon, FileType.CMDI), // Clarin icon is not really appropriate
        // KMDI, Kinship metadata
        new FormatType(".kmdi", ".Kinnate.CustomData", ArbilIcons.kinOathIcon, FileType.KMDI),
        // TLA test results
        new FormatType(".trx", "", ArbilIcons.clarinIcon, FileType.CMDI)})); // Clarin icon is not really appropriate

//    private static MetadataFormat singleInstance = null;
//    static synchronized public MetadataFormat getSingleInstance() {
////        logger.debug("LinorgWindowManager getSingleInstance");
//        if (singleInstance == null) {
//            singleInstance = new MetadataFormat();
//        }
//        return singleInstance;
//    }
    public FileType getFileType(URI targetUri) {
//        final URI targetUri = new HandleUtils().resolveHandle(inputUri);
        String urlString = targetUri.toString();
        if (isStringChildNode(urlString)) {
            return FileType.CHILD;
        }
        File localFile = getFile(targetUri);
        if (localFile != null && localFile.isDirectory()) {
            return FileType.DIRECTORY;
        }
        for (FormatType formatType : knownFormats) {
            if (urlString.endsWith(formatType.suffixString)) {
                return formatType.fileType;
            }
        }
        if (localFile != null && localFile.isFile()) {
            return FileType.FILE;
        }
        if (urlString.lastIndexOf(".") > urlString.length() - 6) {
            // if the file name has a suffix and has passed through the known metadata suffixes then we assume it is a file and dont bother reading the remote file
            logger.info("Presuming the URI points to a file based on its suffix: " + urlString);
            return FileType.FILE;
        }
        // the file is remote so we need to do a deep check
        return deepCheck(targetUri);
    }

    private FileType deepCheck(URI targetUri) {
        try {
            int bytesToRead = 1024;
            final URI resolveHandle = new HandleUtils().resolveHandle(targetUri);
            URLConnection uRLConnection = resolveHandle.toURL().openConnection();
            ((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
            final String contentType = uRLConnection.getContentType();
            logger.info("ContentType: " + contentType); // sadly we dont have a useful mime type here
            if (contentType != null && contentType.contains("text/xml")) {
                Scanner scanner = new Scanner(uRLConnection.getInputStream());
                Pattern pattern = Pattern.compile("<[\\S]*");
                boolean keepSearching = true;
                while (keepSearching) {
                    String found = scanner.findWithinHorizon(pattern, bytesToRead);
                    logger.info("found: " + found);
                    if ("<?xml".equals(found)) {
                        continue;
                    } else if ("<!--".equals(found)) {
                        continue;
                    }
                    for (FormatType formatType : knownFormats) {
                        if (formatType.metadataStartXpath != null && formatType.metadataStartXpath.length() > 0) {
                            final String foundTag = found.substring(1);
                            if (formatType.metadataStartXpath.startsWith(foundTag, 1)) {
                                return formatType.fileType;
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            logger.info("Could not get remote file type: ", exception);
            return FileType.UNKNOWN;
        }
        return FileType.FILE;
    }

    public static boolean isPathImdi(String urlString) {
        for (FormatType formatType : knownFormats) {
            if (urlString.endsWith(formatType.suffixString)) {
                return formatType.fileType == FileType.IMDI;
            }
        }
        return false;
    }

    public static boolean isPathCmdi(String urlString) {
        for (FormatType formatType : knownFormats) {
            if (urlString.endsWith(formatType.suffixString)) {
                return formatType.fileType == FileType.CMDI;
            }
        }
        return false;
    }

    public static ImageIcon getFormatIcon(FileType fileType) {
        for (FormatType formatType : knownFormats) {
            if (formatType.fileType == fileType) {
                return formatType.imageIcon;
            }
        }
        return null;
    }

    public static String getMetadataStartPath(String urlString) {
        for (FormatType formatType : knownFormats) {
            if (urlString.endsWith(formatType.suffixString)) {
                return formatType.metadataStartXpath;
            }
        }
        return null;
    }

    public boolean isMetaDataNode(FileType fileType) {
        return (MetadataFormat.FileType.CHILD == fileType
                || MetadataFormat.FileType.IMDI == fileType
                || MetadataFormat.FileType.CMDI == fileType
                || MetadataFormat.FileType.KMDI == fileType);
    }

    static public boolean isPathMetadata(String urlString) {
        return isPathImdi(urlString) || isPathCmdi(urlString); // change made for clarin
    }

    static public boolean isStringChildNode(String urlString) {
        // todo: this seems not to cause any issues but might it mistake a file for a child node?
        return urlString.contains("#."); // anything with a fragment is a sub node //urlString.contains("#.METATRANSCRIPT") || urlString.contains("#.CMD"); // change made for clarin
    }

    /**
     * Create File object for URI if it represents a file
     *
     * @param nodeUri URI of node to get file for
     * @return null if URI is not a file
     * @throws IllegalArgumentException thrown by constructor of File if URI
     * does not meet requirements
     * @see File#File(java.net.URI)
     */
    public static File getFile(URI nodeUri) throws IllegalArgumentException {
        if (nodeUri.getScheme().toLowerCase().equals("file")) {
            return new File(URI.create(nodeUri.toString().split("#")[0] /* fragment removed */));
        }
        return null;
    }
}
