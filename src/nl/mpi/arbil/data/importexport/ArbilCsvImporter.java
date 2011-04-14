package nl.mpi.arbil.data.importexport;

import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : ArbilCsvImporter
 * Created on : Nov 16, 2009, 10:34:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilCsvImporter {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance){
        bugCatcher = bugCatcherInstance;
    }

    private ArbilDataNode destinationCorpusNode;

    public ArbilCsvImporter(ArbilDataNode destinationCorpusNodeLocal) {
        destinationCorpusNode = destinationCorpusNodeLocal;
    }

    public void doImport() {
        File[] selectedFiles = messageDialogHandler.showFileSelectBox("Import CSV", false, true, false);
        if (selectedFiles != null && selectedFiles.length > 0) {
//                return "CSV File (comma or tab separated values)";
//                return selectedFile.getName().toLowerCase().endsWith(".csv");
            for (File currentFile : selectedFiles) {
                processCsvFile(currentFile);
            }
        }
    }

    private void cleanQuotes(String[] arrayToClean, String fileType) {
        if (arrayToClean.length > 0) {
            if (fileType.indexOf("\"") != -1) {
                arrayToClean[0] = arrayToClean[0].replaceAll("^\"", "");
                arrayToClean[arrayToClean.length - 1] = arrayToClean[arrayToClean.length - 1].replaceAll("\"$", "");
            }
        }
    }

    private void processCsvFile(File inputFile) {
        String csvHeaders[] = null;
        String fileType = ",";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            String currentLine = "";
            String remainderOfLastLine = "";
            StringTokenizer stringTokeniser = null;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (csvHeaders == null) {
                    fileType = "\"\\t\"";
                    csvHeaders = currentLine.split(fileType);
                    if (csvHeaders.length == 1) {
                        fileType = "\t";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = "\",\"";
                        csvHeaders = currentLine.split(fileType);
                    }
                    if (csvHeaders.length == 1) {
                        fileType = ",";
                        csvHeaders = currentLine.split(fileType);
                    }
                    cleanQuotes(csvHeaders, fileType);
                } else {
                    boolean skipLine = false;
                    if (fileType.contains("\"")) {
                        // some fields will contain line breaks and they, must be reassembled here
                        // but this can only be done if the file contains quotes around each feild
                        if (!currentLine.endsWith("\"")) {
                            remainderOfLastLine = remainderOfLastLine + "\n" + currentLine;
                            skipLine = true;
                        } else if (remainderOfLastLine.length() > 0) {
                            currentLine = remainderOfLastLine + "\n" + currentLine;
                            remainderOfLastLine = "";
                        }
                    }
                    if (!skipLine) {
                        String nodeType = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session";
                        ArbilDataNode addedImdiObject = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new MetadataBuilder().addChildNode(destinationCorpusNode, nodeType, null, null, null));
                        addedImdiObject.waitTillLoaded();
                        Hashtable<String, ArbilField[]> addedNodesFields = addedImdiObject.getFields();
                        String[] currentLineArray = currentLine.split(fileType);
                        cleanQuotes(currentLineArray, fileType);
                        for (int columnCounter = 0; columnCounter < csvHeaders.length && columnCounter < currentLineArray.length; columnCounter++) {
                            System.out.println(csvHeaders[columnCounter] + " : " + currentLineArray[columnCounter]);
                            ArbilField[] currentFieldArray = addedNodesFields.get(csvHeaders[columnCounter]);
                            if (currentFieldArray != null) {
                                // TODO: check that the field does not already have a value and act accordingly (add new description?) if it does
                                currentFieldArray[0].setFieldValue(currentLineArray[columnCounter], false, true);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            bugCatcher.logError(ex);
        }
    }
}