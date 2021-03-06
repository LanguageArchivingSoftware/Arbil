package nl.mpi.arbil.wicket.model;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeModel extends LoadableDetachableModel<ArbilDataNode>{

    private URI uri;
    
    public ArbilDataNodeModel(ArbilDataNode dataNode){
	super(dataNode);
	this.uri = dataNode.getUri();
    }
    
    public ArbilDataNodeModel(URI uri){
	super();
	this.uri = uri;
    }
    
    @Override
    protected ArbilDataNode load() {
	return ArbilWicketSession.get().getDataNodeLoader().getArbilDataNode(null, uri);
    }
}
