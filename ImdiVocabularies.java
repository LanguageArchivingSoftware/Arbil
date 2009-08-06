package mpi.linorg;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiVocabularies
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiVocabularies {

    Hashtable<String, Vocabulary> vocabulariesTable = new Hashtable<String, Vocabulary>();

    // IMDIVocab has been abandoned, the mpi.vocabs.IMDIVocab class is too scary
    // every time this is called (correction its in a static stansa ARRRGGG) it downloads the mpi homepage before anything else 
    // mpi.vocabs.IMDIVocab cv = mpi.vocabs.IMDIVocab.get(vocabularyLocation);
    // cv.cslist2array(vocabularyLocation)
    // the output of the following looks similar however they differ slightly and only a few have comments
//        System.out.println("CVentries: "+cv.getCVentries());
//        System.out.println("NameEntries: "+cv.getNameEntries());
//        System.out.println("ValueEntries: "+cv.getValueEntries()); // this one is has comments
//        System.out.println("VocabHashtable: "+cv.getVocabHashtable());
//        System.out.println("Vocabs: "+cv.getVocabs());
//        System.out.println("DescriptionEntries: "+cv.getDescriptionEntries());
//        System.out.println("Entries: "+cv.getEntries());      
//    }
    static private ImdiVocabularies singleInstance = null;

    static synchronized public ImdiVocabularies getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ImdiVocabularies();
        }
        return singleInstance;
    }

    private ImdiVocabularies() {
    }

    public boolean vocabularyContains(String vocabularyLocation, String valueString) {
        if (!vocabulariesTable.containsKey(vocabularyLocation)) {
            parseRemoteFile(vocabularyLocation);
        }
        Vocabulary tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (tempVocab != null) {
            return (null != tempVocab.findVocabularyItem(valueString));
        } else {
            return false;
        }
    }

    public Vocabulary getVocabulary(ImdiField originatingImdiField, String vocabularyLocation) {
        if (vocabularyLocation == null || vocabularyLocation.length() == 0) {
            return null;
        }
        // testing code for checking the language fields have the required triggers
        if (vocabularyLocation.endsWith("MPI-Languages.xml")) {
            boolean foundTrigger = false;
            System.out.println("vocabularyLocation: " + vocabularyLocation);
            System.out.println("Field: " + originatingImdiField.getFullXmlPath());
            String fieldPath = originatingImdiField.getGenericFullXmlPath();
            for (String[] currentTrigger : originatingImdiField.parentImdi.currentTemplate.triggersArray) {
                if (fieldPath.equals(currentTrigger[0])) {
                    foundTrigger = true;
                }
            }
            if (!foundTrigger) {
                if (!fieldPath.equals(".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language")) {
                    GuiHelper.linorgBugCatcher.logError(new Exception("Missing Field Trigger for: " + fieldPath + " in " + originatingImdiField.parentImdi.getUrlString()));
                }
            }
        }
        if (!vocabulariesTable.containsKey(vocabularyLocation)) {
            parseRemoteFile(vocabularyLocation);
        }
        Vocabulary tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (tempVocab != null) {
            Vocabulary returnValue = null;
            if (tempVocab.vocabularyRedirectField != null) {
                if (tempVocab.vocabularyRedirectField != null) {
                    ImdiField[] tempField = originatingImdiField.getSiblingField(tempVocab.vocabularyRedirectField);
                    if (tempField != null) {
                        String redirectFieldString = tempField[0].toString();
                        // TODO: this may need to put the (\d) back into the (x) as is done for the FieldChangeTriggers
                        Vocabulary tempVocabulary = tempField[0].getVocabulary();
                        VocabularyItem redirectFieldVocabItem = tempVocabulary.findVocabularyItem(redirectFieldString);
                        System.out.println("redirectFieldString: " + redirectFieldString);
                        if (redirectFieldVocabItem != null && redirectFieldVocabItem.followUpVocabulary != null) {
                            System.out.println("redirectFieldVocabItem.followUpVocabulary: " + redirectFieldVocabItem.followUpVocabulary);
                            String correctedUrl = tempVocabulary.resolveFollowUpUrl(redirectFieldVocabItem.followUpVocabulary);
                            returnValue = getVocabulary(originatingImdiField, correctedUrl);
                        }
                    }
                }
            }
            if (returnValue == null) {
                returnValue = tempVocab;
            }
            return returnValue;
        } else {
            System.out.println("vocabulary is null");
            return null;
        }
    }

    synchronized public void parseRemoteFile(String vocabRemoteUrl) {
        if (vocabRemoteUrl != null && !vocabulariesTable.containsKey(vocabRemoteUrl)) {
            String cachePath = LinorgSessionStorage.getSingleInstance().updateCache(vocabRemoteUrl, false, new DownloadAbortFlag());
            if (!new File(cachePath).exists()) {
                String backupPath = "/mpi/linorg/resources/IMDI/FallBack/" + new File(cachePath).getName();
                System.out.println("backupPath: " + backupPath);
                URL backUp = this.getClass().getResource(backupPath);
                if (backUp != null) {
                    cachePath = backUp.getFile();
                }
            }
            System.out.println("parseRemoteFile: " + cachePath);
            Vocabulary vocabulary = new Vocabulary(vocabRemoteUrl);
            vocabulariesTable.put(vocabRemoteUrl, vocabulary);
            try {
                javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
                javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
                org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setFeature("http://xml.org/sax/features/validation", false);
                xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
                xmlReader.setContentHandler(new SaxVocabularyHandler(vocabulary));
                xmlReader.parse(cachePath);
            } catch (Exception ex) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A controlled vocabulary could not be read.\n" + vocabRemoteUrl + "\nSome fields may not show all options.", "Load Controlled Vocabulary");
            }
        }
    }

    private class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

        Vocabulary collectedVocab;
        VocabularyItem currentVocabItem = null;

        public SaxVocabularyHandler(Vocabulary vocabList) {
            super();
            collectedVocab = vocabList;
        }

        @Override
        public void characters(char[] charArray, int start, int length) {
            if (currentVocabItem != null) {
                String nodeContents = "";
                for (int charCounter = start; charCounter < start + length; charCounter++) {
                    nodeContents = nodeContents + charArray[charCounter];
                }
                currentVocabItem.descriptionString = nodeContents;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            currentVocabItem = null;
        }

        @Override
        public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
            if (name.equals("VocabularyRedirect")) { // or should this be Redirect
                // when getting the list check attribute in the field X for the vocab location
                collectedVocab.vocabularyRedirectField = atts.getValue("SourceFieldName");
                System.out.println("VocabularyRedirect: " + collectedVocab.vocabularyRedirectField);
            }
            if (name.equals("Entry")) {
                String vocabName = atts.getValue("Value");
                String vocabCode = atts.getValue("Code");
                String followUpVocab = atts.getValue("FollowUp");
                currentVocabItem = new VocabularyItem(vocabName, vocabCode, followUpVocab);
                collectedVocab.vocabularyItems.add(currentVocabItem);
            }
        }
    }

    public class Vocabulary {

        public Vector<VocabularyItem> vocabularyItems = new Vector<VocabularyItem>();
        public String vocabularyRedirectField = null; // the sibling imdi field that changes this vocabularies location
        public String vocabularyUrlRedirected = null; // the url of the vocabulary indicated by the value of the vocabularyRedirectField
        private String vocabularyUrl = null;

        public Vocabulary(String locationUrl) {
            vocabularyUrl = locationUrl;
        }

        public void addEntry(String entryString) {
            boolean itemExistsInVocab = false;
            for (VocabularyItem currentVocabularyItem : vocabularyItems.toArray(new VocabularyItem[]{})) {
                if (currentVocabularyItem.languageName.equals(entryString)) {
                    itemExistsInVocab = true;
                }
            }
            if (!itemExistsInVocab) {
                vocabularyItems.add(new VocabularyItem(entryString, null, null));
            }
        }
        public VocabularyItem findVocabularyItem(String searchString) {
            for (VocabularyItem currentVocabularyItem : vocabularyItems.toArray(new VocabularyItem[]{})) {
                if (currentVocabularyItem.languageName.equals(searchString)) {
                    return currentVocabularyItem;
                }
            }
            return null;
        }

        public String resolveFollowUpUrl(String folowUpString) {
            vocabularyUrlRedirected = folowUpString;
            String vocabUrlDirectory = vocabularyUrl.substring(0, vocabularyUrl.lastIndexOf("/") + 1);
            System.out.println("vocabUrlDirectory: " + vocabUrlDirectory);
            return (vocabUrlDirectory + folowUpString);
        }
    }
    public class VocabularyItem {

        public String languageName;
        public String languageCode;
        public String followUpVocabulary;
        public String descriptionString;

        public VocabularyItem(String languageNameLocal, String languageCodeLocal, String followUpVocabularyLocal) {
            languageName = languageNameLocal;
            languageCode = languageCodeLocal;
            followUpVocabulary = followUpVocabularyLocal;
        }

        @Override
        public String toString() {
            return languageName;
        }
    }
}
