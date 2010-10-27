package nl.mpi.arbil.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.ArbilEntityResolver;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiVocabularies;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.clarin.CmdiProfileReader;
import nl.mpi.arbil.clarin.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * CmdiTemplate.java
 * Created on March 10, 2010, 17:34:45 AM
 * @author Peter.Withers@mpi.nl
 */
public class CmdiTemplate extends ArbilTemplate {

    String nameSpaceString;
    String filterString[] = {".CMD.Resources.", ".CMD.Header."};

    private class ArrayListGroup {

        public ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
        public ArrayList<String[]> addableComponentPathsList = new ArrayList<String[]>();
        public ArrayList<String[]> resourceNodePathsList = new ArrayList<String[]>();
        public ArrayList<String[]> fieldConstraintList = new ArrayList<String[]>();
        public ArrayList<String[]> displayNamePreferenceList = new ArrayList<String[]>();
        public ArrayList<String[]> fieldUsageDescriptionList = new ArrayList<String[]>();
    }

    public void loadTemplate(String nameSpaceStringLocal) {
        // testing only
//        new TestAnnotationsReader().Test();
        //super.readTemplate(new File(""), "template_cmdi");
        vocabularyHashTable = new Hashtable<String, ImdiVocabularies.Vocabulary>();
        nameSpaceString = nameSpaceStringLocal;
        // construct the template from the XSD
        try {
            // get the name of this profile
            CmdiProfile cmdiProfile = CmdiProfileReader.getSingleInstance().getProfile(nameSpaceString);
            if (cmdiProfile != null) {
                loadedTemplateName = cmdiProfile.name;// this could be null
            } else {
                loadedTemplateName = nameSpaceString.substring(nameSpaceString.lastIndexOf("/") + 1);
            }

            // create a temp file of the read template data so that it can be compared to a hand made version
            File debugTempFile = File.createTempFile("templatetext", ".tmp");
            debugTempFile.deleteOnExit();
            BufferedWriter debugTemplateFileWriter = new BufferedWriter(new FileWriter(debugTempFile));

            ArrayListGroup arrayListGroup = new ArrayListGroup();
            URI xsdUri = new URI(nameSpaceString);
            readSchema(xsdUri, arrayListGroup);
            childNodePaths = arrayListGroup.childNodePathsList.toArray(new String[][]{});
            templatesArray = arrayListGroup.addableComponentPathsList.toArray(new String[][]{});
            resourceNodePaths = arrayListGroup.resourceNodePathsList.toArray(new String[][]{});
            fieldConstraints = arrayListGroup.fieldConstraintList.toArray(new String[][]{});
            fieldUsageArray = arrayListGroup.fieldUsageDescriptionList.toArray(new String[][]{});
            makeGuiNamesUnique();

            // sort and construct the preferredNameFields array
            String[][] tempSortableArray = arrayListGroup.displayNamePreferenceList.toArray(new String[][]{});
            Arrays.sort(tempSortableArray, new Comparator<String[]>() {

                public int compare(String[] o1, String[] o2) {
                    return Integer.valueOf(o1[1]) - Integer.valueOf(o2[1]);
                }
            });
            preferredNameFields = new String[tempSortableArray.length];
            for (int nameFieldCounter = 0; nameFieldCounter < preferredNameFields.length; nameFieldCounter++) {
                preferredNameFields[nameFieldCounter] = tempSortableArray[nameFieldCounter][0];
            }
            // end sort and construct the preferredNameFields array

//            if (preferredNameFields.length < 1) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No preferred field names have been specified, some nodes will not display correctly", "Clarin Profile Error");
//            }
//            if (fieldUsageArray.length < 1) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No field descriptions have been provided in the profile, as a result no information about each fields intended use can be provided to users of this profile", "Clarin Profile Error");
//            }
            for (String[] currentArray : templatesArray) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<TemplateComponent FileName=\"" + currentArray[0] + "\" DisplayName=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String[] currentArray : childNodePaths) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<ChildNodePath ChildPath=\"" + currentArray[0] + "\" SubNodeName=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String[] currentArray : resourceNodePaths) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<ResourceNodePath RefPath=\"" + currentArray[0] + "\" RefNodeName=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String[] currentArray : fieldConstraints) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<FieldConstraint FieldPath=\"" + currentArray[0] + "\" Constraint=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String currentArray : preferredNameFields) {
                System.out.println("loadTemplate: " + currentArray);
                // node that this is not a FieldsShortName but a full field path but the code now supports both while the xml file implies only short
                debugTemplateFileWriter.write("<TreeNodeNameField FieldsShortName==\"" + currentArray + "\" />\r\n");
            }
            for (String[] currentArray : fieldUsageArray) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<FieldUsage FieldPath=\"" + currentArray[0] + "\" FieldDescription=\"" + currentArray[1] + "\" />\r\n");
            }
            debugTemplateFileWriter.close();
            // lanunch the hand made template and the generated template for viewing
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nameSpaceString, debugTempFile.toURL());
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatejar", CmdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/template_cmdi.xml"));
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatejar", CmdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"));
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        } catch (IOException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        }
        // this should be adequate for cmdi templates
        //templatesArray = childNodePaths;
        // TODO: complete these
        requiredFields = new String[]{};
        fieldTriggersArray = new String[][]{};
        autoFieldsArray = new String[][]{};
        genreSubgenreArray = new String[][]{};
    }

    private void makeGuiNamesUnique() {
        // template array is the super set while childnodes array is shorter
        boolean allGuiNamesUnique = false;
        while (!allGuiNamesUnique) {
            allGuiNamesUnique = true;
            for (String[] currentTemplate : templatesArray) {
                String currentTemplateGuiName = currentTemplate[1];
                String currentTemplatePath = currentTemplate[0];
                for (String[] secondTemplate : templatesArray) {
                    String secondTemplateGuiName = secondTemplate[1];
                    String secondTemplatePath = secondTemplate[0];
//                    System.out.println("currentTemplateGuiName: " + currentTemplateGuiName);
//                    System.out.println("secondTemplateGuiName: " + secondTemplateGuiName);
                    if (!currentTemplatePath.equals(secondTemplatePath)) {
                        if (currentTemplateGuiName.equals(secondTemplateGuiName)) {
                            allGuiNamesUnique = false;
                            for (String[] templateToChange : templatesArray) {
                                String templateToChangeGuiName = templateToChange[1];
                                String templateToChangePath = templateToChange[0];
                                if (templateToChangeGuiName.equals(currentTemplateGuiName)) {
                                    int pathCount = templateToChangeGuiName.split("\\.").length;
                                    String[] templateToChangePathParts = templateToChangePath.split("\\.");
                                    templateToChange[1] = templateToChangePathParts[templateToChangePathParts.length - pathCount - 1] + "." + templateToChangeGuiName;
//                                    System.out.println("templateToChangeGuiName: " + templateToChangeGuiName);
//                                    System.out.println("templateToChangePath: " + templateToChangePath);
//                                    System.out.println("new templateToChange[1]: " + templateToChange[1]);
                                }
                            }
                            for (String[] templateToChange : childNodePaths) {
                                String templateToChangeGuiName = templateToChange[1];
                                String templateToChangePath = templateToChange[0];
                                if (templateToChangeGuiName.equals(currentTemplateGuiName)) {
                                    int pathCount = templateToChangeGuiName.split("\\.").length;
                                    String[] templateToChangePathParts = templateToChangePath.split("\\.");
                                    templateToChange[1] = templateToChangePathParts[templateToChangePathParts.length - pathCount - 1] + "." + templateToChangeGuiName;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // get the xpath of the target node
        String targetNodeXpath = ((ImdiTreeObject) targetNodeUserObject).getURI().getFragment();
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        boolean isComponentPath = false;
        if (targetNodeXpath != null) {
            isComponentPath = targetNodeXpath.endsWith(")");
            // remove the extraneous node name for a meta node
//            targetNodeXpath = targetNodeXpath.replaceAll("\\.[^\\.]+[^\\)]$", "");
            // remove the sibling indexes
            targetNodeXpath = targetNodeXpath.replaceAll("\\(\\d+\\)", "");
        }
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        Vector<String[]> childTypes = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            for (String[] childPathString : templatesArray) {
//                System.out.println("Testing: " + childPathString[1] + childPathString[0]);
                boolean allowEntry = false;
                if (targetNodeXpath == null) {
//                    System.out.println("allowing due to null path: " + childPathString[0]);
                    allowEntry = true;
                } else if (childPathString[0].startsWith(targetNodeXpath)) {
//                    System.out.println("allowing: " + childPathString[0]);
                    allowEntry = true;
                }
                if (childPathString[0].equals(targetNodeXpath) && isComponentPath) {
//                    System.out.println("disallowing addint to itself: " + childPathString[0]);
                    allowEntry = false;
                }
                for (String currentFilter : filterString) {
                    if (childPathString[0].startsWith(currentFilter)) {
                        allowEntry = false;
                    }
                }
                if (allowEntry) {
//                    System.out.println("allowing: " + childPathString[0]);
                    childTypes.add(new String[]{childPathString[1], childPathString[0]});
                }
            }
            String[][] childTypesArray = childTypes.toArray(new String[][]{});
            childTypes.removeAllElements();
            for (String[] currentChildType : childTypesArray) {
                boolean keepChildType = true;
//                System.out.println("currentChildType: " + currentChildType[1]);
                for (String[] subChildType : childTypesArray) {
//                    System.out.println("subChildType: " + subChildType[1]);
                    if (currentChildType[1].startsWith(subChildType[1])) {
                        if (currentChildType[1].length() != subChildType[1].length()) {
                            keepChildType = false;
//                            System.out.println("removing: " + currentChildType[1]);
//                            System.out.println("based on: " + subChildType[1]);
                        }
                    }
                }
                if (keepChildType) {
//                    System.out.println("keeping: : " + currentChildType[1]);
                    childTypes.add(currentChildType);
                }
            }
            Collections.sort(childTypes, new Comparator() {

                public int compare(Object o1, Object o2) {
                    String value1 = ((String[]) o1)[0];
                    String value2 = ((String[]) o2)[0];
                    return value1.compareTo(value2);
                }
            });
        }
        return childTypes.elements();
    }

    private void readSchema(URI xsdFile, ArrayListGroup arrayListGroup) {
        File schemaFile;
        if (xsdFile.getScheme().equals("file")) {
            schemaFile = new File(xsdFile);
        } else {
            schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 100);
        }
        templateFile = schemaFile; // store the template file for later use such as adding child nodes
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setCharacterEncoding("UTF-8");
            xmlOptions.setEntityResolver(new ArbilEntityResolver(xsdFile));
//            xmlOptions.setCompileDownloadUrls();
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, xmlOptions)}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
//            System.out.println("XmlObject.Factory:" + XmlObject.Factory.class.toString());
            SchemaType schemaType = sts.documentTypes()[0];
            constructXml(schemaType, arrayListGroup, "");
//            for (SchemaType schemaType : sts.documentTypes()) {
////                System.out.println("T-documentTypes:");
//                constructXml(schemaType, arrayListGroup, "", "");
//                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
//            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(templateFile.getName(), e);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not open the required template file: " + templateFile.getName(), "Load Clarin Template");
        } catch (XmlException e) {
            GuiHelper.linorgBugCatcher.logError(templateFile.getName(), e);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not read the required template file: " + templateFile.getName(), "Load Clarin Template");
        }
    }

    private int constructXml(SchemaType schemaType, ArrayListGroup arrayListGroup, String pathString) {
//        System.out.println("sub element count: " + pathString.split("\\.").length);
//        System.out.println("sub element list: " + pathString);
//        if (pathString.split("\\.").length > 20) {
//            // todo: look into recursion issue
//            return 0;
//        }
//        System.out.println("constructXml: " + pathString);
//        if (pathString.startsWith(".CMD.Components.test-profile-book.Authors.Author.")) {
//            System.out.println("Author");
//        }
//        System.out.println("schemaType: " + schemaType.getName());
        int childCount = 0;
//        boolean hasMultipleElementsInOneNode = false;
        int subNodeCount = 0;
        readControlledVocabularies(schemaType, pathString);
        readFieldConstrains(schemaType, pathString, arrayListGroup.fieldConstraintList);

        // search for annotations
        SchemaParticle topParticle = schemaType.getContentModel();
        searchForAnnotations(topParticle, pathString, arrayListGroup);
        // end search for annotations

        SchemaProperty[] schemaPropertyArray = schemaType.getElementProperties();
//        boolean currentHasMultipleNodes = schemaPropertyArray.length > 1;
        int currentNodeChildCount = 0;
        for (SchemaProperty schemaProperty : schemaPropertyArray) {
            childCount++;
            String localName = schemaProperty.getName().getLocalPart();
            String currentPathString = pathString + "." + localName;
            String currentNodeMenuName;
            if (localName != null) {
                currentNodeChildCount++;

                // while keeping the .cmd.components part filter out all unrequired path component for use in the menus
//            if (currentHasMultipleNodes || filterString.startsWith(currentPathString)) {
//                currentNodeMenuName = nodeMenuName + "." + localName;
//            } else {
//                currentNodeMenuName = nodeMenuName;
//            }
//                  currentNodeMenuName = localName;
//            currentNodeMenuName = currentNodeMenuName.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");
                boolean canHaveMultiple = true;
                if (schemaProperty.getMaxOccurs() == null) {
                    // absence of the max occurs also means multiple
                    canHaveMultiple = true;
                    // todo: also check that min and max are the same because there may be cases of zero required but only one can be added
                } else if (schemaProperty.getMaxOccurs().toString().equals("unbounded")) {
                    canHaveMultiple = true;
                } else {
                    // todo: take into account max occurs in the add menu
                    canHaveMultiple = schemaProperty.getMaxOccurs().intValue() > 1;
                }
                if (!canHaveMultiple) {
                    // todo: limit the number of instances that can be added to a xml file basedon the max bounds
                    canHaveMultiple = schemaProperty.getMinOccurs().intValue() != schemaProperty.getMaxOccurs().intValue();
                }
//            boolean hasSubNodes = false;
                // start: temp code for extracting all field names
//                System.out.println("Found template element: " + currentPathString);
//                boolean foundEntry = false;
//                for (String[] currentEntry : arrayListGroup.fieldUsageDescriptionList) {
//                    if (currentPathString.startsWith(currentEntry[0])) {
//                        currentEntry[0] = currentPathString;
//                        foundEntry = true;
//                        break;
//                    }
//                }
//                if (!foundEntry) {
//                    arrayListGroup.fieldUsageDescriptionList.add(new String[]{currentPathString, ""});
//                }
                // end: temp code for extracting all field names
                SchemaType currentSchemaType = schemaProperty.getType();
//            String nodeMenuNameForChild;
//            if (canHaveMultiple) {
//                // reset the node menu name when traversing through into a subnode
//                nodeMenuNameForChild = localName;
////                nodeMenuName = nodeMenuName + "." + localName;
//            } else {
//                nodeMenuName = nodeMenuName + "." + localName;
//                nodeMenuNameForChild = nodeMenuName;
//            }
//            nodeMenuNameForChild = "";
                currentNodeMenuName = localName;
                // boolean childHasMultipleElementsInOneNode =
                subNodeCount = constructXml(currentSchemaType, arrayListGroup, currentPathString);
//            if (!hasMultipleElementsInOneNode) {
//                hasMultipleElementsInOneNode = childHasMultipleElementsInOneNode;
//            }

//            System.out.println("childNodeChildCount: " + childCount + " : " + hasMultipleElementsInOneNode + " : " + currentPathString);

//            nodeMenuNameForChild = nodeMenuNameForChild.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");
//            boolean hasMultipleSubNodes = childCount < childNodeChildCount - 1; // todo: complete or remove this hasSubNodes case
                if (canHaveMultiple && subNodeCount > 0) {
//                todo check for case of one or only single sub element and when found do not add as a child path
                    arrayListGroup.childNodePathsList.add(new String[]{currentPathString, currentNodeMenuName});
                }// else if (canHaveMultiple) {
//                    System.out.println("Skipping sub node path: " + currentPathString + " : " + currentNodeMenuName);
//                }
                if (canHaveMultiple) {
                    arrayListGroup.addableComponentPathsList.add(new String[]{currentPathString, currentNodeMenuName});
                }
                boolean hasResourceAttribute = false;
                for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
                    if (attributesProperty.getName().getLocalPart().equals("ref")) {
                        hasResourceAttribute = true;
                        break;
                    }
                }
                if (hasResourceAttribute) {
                    arrayListGroup.resourceNodePathsList.add(new String[]{currentPathString, localName});
                }
            }
        }
//        if (childCount > 1) {
//            hasMultipleElementsInOneNode = true;
//        }
        subNodeCount = subNodeCount + currentNodeChildCount;
        return subNodeCount;
    }

//    SchemaParticle topParticle = schemaType.getContentModel();
    private void searchForAnnotations(SchemaParticle schemaParticle, String nodePath, ArrayListGroup arrayListGroup) {
//        System.out.println("searchForAnnotations" + nodePath);
        if (schemaParticle != null) {
            switch (schemaParticle.getParticleType()) {
                case SchemaParticle.SEQUENCE:
                    for (SchemaParticle schemaParticleChild : schemaParticle.getParticleChildren()) {
                        if (schemaParticleChild.getName() != null) {
                            nodePath = nodePath + "." + schemaParticleChild.getName().getLocalPart();
                        } else {
                            GuiHelper.linorgBugCatcher.logError(new Exception("unnamed node at: " + nodePath));
                            nodePath = nodePath + ".unnamed";
                        }
                        searchForAnnotations(schemaParticleChild, nodePath, arrayListGroup);
                    }
                    break;
                case SchemaParticle.ELEMENT:
                    SchemaLocalElement schemaLocalElement = (SchemaLocalElement) schemaParticle;
                    saveAnnotationData(schemaLocalElement, nodePath, arrayListGroup);
                    break;
            }
        }
    }

    private void saveAnnotationData(SchemaLocalElement schemaLocalElement, String nodePath, ArrayListGroup arrayListGroup) {
        SchemaAnnotation schemaAnnotation = schemaLocalElement.getAnnotation();
        if (schemaAnnotation != null) {
//            System.out.println("getAttributes length: " + schemaAnnotation.getAttributes().length);
            for (SchemaAnnotation.Attribute annotationAttribute : schemaAnnotation.getAttributes()) {
                System.out.println("  Annotation: " + annotationAttribute.getName() + " : " + annotationAttribute.getValue());
                //Annotation: {ann}documentation : the title of the book
                //Annotation: {ann}displaypriority : 1
                // todo: the url here could be removed provided that it does not make it to unspecific
                if ("{http://www.clarin.eu}displaypriority".equals(annotationAttribute.getName().toString())) {
                    arrayListGroup.displayNamePreferenceList.add(new String[]{nodePath, annotationAttribute.getValue()});
                }
                if ("{http://www.clarin.eu}documentation".equals(annotationAttribute.getName().toString())) {
                    arrayListGroup.fieldUsageDescriptionList.add(new String[]{nodePath, annotationAttribute.getValue()});
                }
            }
        }
    }

    private void readFieldConstrains(SchemaType schemaType, String nodePath, ArrayList<String[]> fieldConstraintList) {
        switch (schemaType.getBuiltinTypeCode()) {
            case SchemaType.BTC_STRING:
//                System.out.println("BTC_STRING");
                // no constraint relevant for string
                break;
            case SchemaType.BTC_DATE:
//                System.out.println("BTC_DATE");
                fieldConstraintList.add(new String[]{nodePath, "([0-9][0-9][0-9][0-9])((-[0-1][0-9])(-[0-3][0-9])?)?"});// todo: complete this regex
                break;
            case SchemaType.BTC_BOOLEAN:
//                System.out.println("BTC_BOOLEAN");
                fieldConstraintList.add(new String[]{nodePath, "true|false"});// todo: complete this regex
                break;
            case SchemaType.BTC_ANY_URI:
//                System.out.println("BTC_ANY_URI");
                fieldConstraintList.add(new String[]{nodePath, "[^\\d]+://.*"});// todo: complete this regex
                break;
//                case SchemaType. XML object???:
//                    System.out.println("");
//                    fieldConstraintList.add(new String[]{currentPathString, "[^\\d]+://.*"});// todo: complete this regex
//                    break;
            case 0:
                // no constraint relevant
                break;
            default:
//                System.out.println("uknown");
                break;
        }
    }

    private void readControlledVocabularies(SchemaType schemaType, String nodePath) {
        if (schemaType.getEnumerationValues() != null) {
//            System.out.println("Controlled Vocabulary: " + schemaType.toString());
//            System.out.println("Controlled Vocabulary: " + schemaType.getName());

            ImdiVocabularies.Vocabulary vocabulary = ImdiVocabularies.getSingleInstance().getEmptyVocabulary(nameSpaceString + "#" + schemaType.getName());

            for (XmlAnySimpleType anySimpleType : schemaType.getEnumerationValues()) {
//                System.out.println("Value List: " + anySimpleType.getStringValue());
                vocabulary.addEntry(anySimpleType.getStringValue(), null);
                // todo: get the ann:label
//
//                SchemaLocalElement schemaLocalElement = (SchemaLocalElement) schemaType;
//                        SchemaAnnotation schemaAnnotation = schemaLocalElement.getAnnotation();
//        if (schemaAnnotation != null) {
////            System.out.println("getAttributes length: " + schemaAnnotation.getAttributes().length);
//            for (SchemaAnnotation.Attribute annotationAttribute : schemaAnnotation.getAttributes()) {
//                System.out.println("  Annotation: " + annotationAttribute.getName() + " : " + annotationAttribute.getValue());
//                //Annotation: {ann}documentation : the title of the book
//                //Annotation: {ann}displaypriority : 1
//                // todo: the url here could be removed provided that it does not make it to unspecific
////                if ("{http://www.clarin.eu}displaypriority".equals(annotationAttribute.getName().toString())) {
////                    arrayListGroup.displayNamePreferenceList.add(new String[]{nodePath, annotationAttribute.getValue()});
////                }
////                if ("{http://www.clarin.eu}documentation".equals(annotationAttribute.getName().toString())) {
////                    arrayListGroup.fieldUsageDescriptionList.add(new String[]{nodePath, annotationAttribute.getValue()});
////                }
//            }
//        }


//                <item AppInfo="Central Sudanic languages"
//                ConceptLink="http://cdb.iso.org/lg/CDB-00138674-001">csu</item>
//
//                used to be transformed into:
//
//                <xs:enumeration value="csu"
//                dcr:datcat="http://cdb.iso.org/lg/CDB-00138674-001">
//                     <xs:annotation>
//                      <xs:appinfo>Central Sudanic languages</xs:appinfo>
//                     </xs:annotation>
//                </xs:enumeration>
//
//                and now it becomes:
//
//                <xs:enumeration value="csu"
//                dcr:datcat="http://cdb.iso.org/lg/CDB-00138674-001" ann:label="Central
//                Sudanic languages"/>

            }
            System.out.println("vocabularyHashTable.put: " + nodePath);
            vocabularyHashTable.put(nodePath, vocabulary);
        }
    }

//    public static void printPropertyInfo(SchemaProperty p) {
//        System.out.println("Property name=\"" + p.getName() + "\", type=\"" + p.getType().getName()
//                + "\", maxOccurs=\""
//                + (p.getMaxOccurs() != null ? p.getMaxOccurs().toString() : "unbounded") + "\"");
//    }
//    public void getAnnotations(SchemaType schemaType) {
//        SchemaParticle typeParticle = schemaType.getContentModel();
//        if (typeParticle == null) {
//            return;
//        }
//        SchemaParticle[] childParts =
//                typeParticle.getParticleChildren();
//        if (childParts == null) {
//            return;
//        }
//        for (SchemaParticle part : childParts) {
//            /* I know my property is of element type */
//            if (part.getParticleType() == SchemaParticle.ELEMENT) {
////                if (part.getName().equals(prop.getName())) {
//                SchemaAnnotation ann = ((SchemaLocalElement) schemaType.getContentModel()).getAnnotation();
//                System.out.println("SchemaAnnotation: " + ann);
//
//                System.out.println("SchemaLocalElement: " + ((SchemaLocalElement) part).getAnnotation());
////                }
//            }
//        }
//    }
//
//    public void getAnnotations(SchemaType schemaType, SchemaProperty prop) {
//        SchemaParticle typeParticle = schemaType.getContentModel();
//        if (typeParticle == null) {
//            //return null;
//        }
//        SchemaParticle[] childParts =
//                typeParticle.getParticleChildren();
//        if (childParts == null) {
//            //return null;
//        }
//        for (SchemaParticle part : childParts) {
//            /* I know my property is of element type */
//            if (part.getParticleType() == SchemaParticle.ELEMENT) {
//                if (part.getName().equals(prop.getName())) {
//                    System.out.println("SchemaLocalElement: " + ((SchemaLocalElement) part).getAnnotation());
//                }
//            }
//        }
//    }
//
//    public void navigateParticle(SchemaParticle p) {
//        switch (p.getParticleType()) {
//            case SchemaParticle.ALL:
//            case SchemaParticle.CHOICE:
//            case SchemaParticle.SEQUENCE:
//                // These are "container" particles, so iterate over their children
//                SchemaParticle[] children = p.getParticleChildren();
//                for (int i = 0; i < children.length; i++) {
//                    navigateParticle(children[i]);
//                }
//                break;
//            case SchemaParticle.ELEMENT:
//                printElementInfo((SchemaLocalElement) p);
//                break;
//            default:
//            // There can also be "wildcards" corresponding to <xs:any> elements in the Schema
//        }
//    }
//
//    public void printElementInfo(SchemaLocalElement e) {
//        System.out.println("Element name=\"" + e.getName() + "\", type=\"" + e.getType().getName()
//                + "\", maxOccurs=\""
//                + (e.getMaxOccurs() != null ? e.getMaxOccurs().toString() : "unbounded") + "\"");
//        SchemaAnnotation annotation = e.getAnnotation();
//        if (annotation != null) {
//            SchemaAnnotation.Attribute[] att = annotation.getAttributes();
//            if (att != null && att.length > 0) {
//                System.out.println("  Annotation: " + att[0].getName() + "=\""
//                        + att[0].getValue() + "\"");
//            }
//        }
//    }
    public static void main(String args[]) {
//        new CmdiTemplate().loadTemplate("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1272022528355/xsd");
        new CmdiTemplate().loadTemplate("file:/Users/petwit/Desktop/LocalProfiles/clarin.eu_annotation-test_1272022528355.xsd");
    }
}
