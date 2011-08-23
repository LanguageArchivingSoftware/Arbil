package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.MockSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilComponentBuilderTest extends ArbilTest {

    ArbilComponentBuilder componentBuilder;
    boolean firstrun = true;

    @Before
    public synchronized void setUp() throws Exception {
	if (firstrun) {
	    inject();
	    componentBuilder = new ArbilComponentBuilder();
	    firstrun = false;
	}
    }

    @Test
    public void testInsertResourceProxies() throws Exception {
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();

	addResourceToNode(node, childNode, resourceLinks);
    }

    @Test
    public void testRemoveResourceProxies() throws Exception {
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();
	ArbilDataNode resourceNode = addResourceToNode(node, childNode, resourceLinks);

	// Remove one link ref
	removeResourceProxyFromNode(node, resourceNode);
	// Proxy should still be there
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());

	// Remove final link ref
	removeResourceProxyFromNode(childNode, resourceNode);
	// Proxy should be removed
	assertEquals(resourceLinks, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
    }

    private ArbilDataNode getMdChildNode(ArbilDataNode node) {
	ArbilDataNode childNode = node.getChildByPath(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	assertNotNull(childNode);
	childNode.waitTillLoaded();
	return childNode;
    }

    private ArbilDataNode getMdInstanceNode() throws InterruptedException, IOException, URISyntaxException {
	addToLocalTreeFromURI(copyOfResource(uriFromResource("/nl/mpi/arbil/data/example-md-instance.cmdi")));
	ArbilDataNode node = getTreeHelper().getLocalCorpusNodes()[0];
	assertNotNull(node);
	assertTrue(node.isCmdiMetaDataNode());
	assertTrue(node.isLocal());
	assertTrue(node.isEditable());
	return node;
    }

    private void addResourceProxyToNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	assertNotNull(componentBuilder.insertResourceProxy(node, resourceNode));
	node.reloadNode();
	waitForNodeToLoad(node);
    }

    private void removeResourceProxyFromNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	assertTrue(componentBuilder.removeResourceProxyReferences(node, Collections.singleton(resourceNode.getUrlString())));
	node.reloadNode();
	waitForNodeToLoad(node);
    }

    private ArbilDataNode addResourceToNode(ArbilDataNode node, ArbilDataNode childNode, int resourceLinks) throws URISyntaxException {
	// Add new resource to file
	ArbilDataNode resourceNode = getDataNodeLoader().getArbilDataNodeWithoutLoading(uriFromResource("/nl/mpi/arbil/data/resources/arbil.jpg"));
	addResourceProxyToNode(node, resourceNode);
	// A resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
	// One reference occurences should be present
	assertEquals(1, node.getCmdiComponentLinkReader().getResourceLink(node.getCmdiComponentLinkReader().getProxyId(resourceNode.getUrlString())).getReferencingNodesCount());

	// Add existing resource to child node
	addResourceProxyToNode(childNode, resourceNode);
	// One resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
	// Two reference occurences should be present
	assertEquals(2, node.getCmdiComponentLinkReader().getResourceLink(node.getCmdiComponentLinkReader().getProxyId(resourceNode.getUrlString())).getReferencingNodesCount());
	return resourceNode;
    }

    @Override
    protected SessionStorage newSessionStorage() {
	return new MockSessionStorage() {

	    @Override
	    public boolean pathIsInsideCache(File fullTestFile) {
		return true;
	    }
	};
    }
}