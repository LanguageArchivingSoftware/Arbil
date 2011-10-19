package nl.mpi.arbil.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Logic for carrying out a search on a remote corpus
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilRemoteSearch {
    
    protected String lastSearchString = null;
    protected ArbilDataNode[] lastSearchNodes = null;
    protected URI[] searchResults = null;
    
    public static boolean isEmptyQuery(String queryText) {
	return RemoteServerSearchTerm.valueFieldMessage.equals(queryText) || "".equals(queryText);
    }
    
    public URI[] getServerSearchResults(final String queryText, ArbilDataNode[] searchNodes) {
	if (queryText == null || isEmptyQuery(queryText)) {
	    return new URI[]{};
	} else {
	    if (queryText.equals(lastSearchString) && Arrays.equals(searchNodes, lastSearchNodes)) {
		System.out.println("remote search term unchanged, returning last server response");
		return searchResults;
	    } else {
		ArrayList<URI> foundNodes = new ArrayList<URI>();
		lastSearchString = queryText;
		lastSearchNodes = searchNodes.clone();
		for (String resultString : performSearch(lastSearchString, searchNodes)) {
		    try {
			foundNodes.add(new URI(resultString));
		    } catch (URISyntaxException exception) {
			GuiHelper.linorgBugCatcher.logError(exception);
		    }
		}
		searchResults = foundNodes.toArray(new URI[]{});
		return searchResults;
	    }
	}
    }
    
    protected String[] performSearch(String searchString, ArbilDataNode[] arbilDataNodeArray) {
	ArrayList<String> returnArray = new ArrayList<String>();
	int maxResultNumber = 1000;
	try {
	    String fullQueryString = constructSearchQuery(arbilDataNodeArray, searchString, maxResultNumber);
	    System.out.println("QueryString: " + fullQueryString);
	    Document resultsDocument = getSearchResults(fullQueryString);
	    NodeList domIdNodeList = XPathAPI.selectNodeList(resultsDocument, RemoteServerSearchTerm.IMDI_RESULT_URL_XPATH);
	    for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
		Node urlNode = domIdNodeList.item(nodeCounter);
		if (urlNode != null) {
		    System.out.println(urlNode.getTextContent());
		    returnArray.add(urlNode.getTextContent());
		}
	    }
	} catch (DOMException exception) {
	    GuiHelper.linorgBugCatcher.logError(exception);
	} catch (IOException exception) {
	    GuiHelper.linorgBugCatcher.logError(exception);
	} catch (ParserConfigurationException exception) {
	    GuiHelper.linorgBugCatcher.logError(exception);
	} catch (SAXException exception) {
	    GuiHelper.linorgBugCatcher.logError(exception);
	} catch (TransformerException exception) {
	    GuiHelper.linorgBugCatcher.logError(exception);
	}
	if (returnArray.size() >= maxResultNumber) {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Found more results than can be displayed, only showing the first " + maxResultNumber + " results", "Remote Search");
	}
	return returnArray.toArray(new String[]{});
    }
    
    private static String constructSearchQuery(ArbilDataNode[] arbilDataNodeArray, String searchString, int maxResultNumber) {
	String encodedQuery;
	try {
	    encodedQuery = URLEncoder.encode(searchString, "UTF-8");
	} catch (UnsupportedEncodingException ex) {
	    throw new RuntimeException(ex);
	}
	
	String fullQueryString = RemoteServerSearchTerm.IMDI_SEARCH_BASE;
	fullQueryString += "&num=" + maxResultNumber;
	fullQueryString += "&query=" + encodedQuery;
	fullQueryString += "&type=simple";
	fullQueryString += "&includeUrl=true";
	for (ArbilDataNode arbilDataNode : arbilDataNodeArray) {
	    if (arbilDataNode.archiveHandle != null) {
		fullQueryString += "&nodeid=" + arbilDataNode.archiveHandle;
	    } else {
		ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot search \"" + arbilDataNode + "\" because it does not have an archive handle", "Remote Search");
	    }
	}
	// to search a branch we need the node id and to get that we need to have the handle and that might not exist, also to do any of that we would need to use an xmlrpc and include the lamusapi jar file to all versions of the application, so we will just search the entire archive since that takes about the same time to return the results
	//fullQueryString += "&nodeid=" + nodeidString; //MPI77915%23
	// &nodeid=MPI556280%23&nodeid=MPI84114%23&nodeid=MPI77915%23
	fullQueryString += "&returnType=xml";
	return fullQueryString;
    }
    
    private Document getSearchResults(String fullQueryString) throws SAXException, ParserConfigurationException, IOException {
	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	documentBuilderFactory.setValidating(false);
	documentBuilderFactory.setNamespaceAware(true);
	DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	Document resultsDocument = documentBuilder.parse(fullQueryString);
	return resultsDocument;
    }
}