/**
 * 
 */
package gui.graph;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;

import data.node.ConnectionManager;
import data.node.Input;
import data.node.Output;


/**
 * @author Simon Dufour
 *
 */
public class NodeConnetionHandler extends mxConnectionHandler {

	protected final ConnectionManager cm;
	public NodeConnetionHandler(mxGraphComponent graphComponent, ConnectionManager cm) {
		super(graphComponent);
		this.cm = cm;
	}
	
	@Override
	public String validateConnection(Object source, Object target)
	{
		mxIGraphModel model = graphComponent.getGraph().getModel();
		Object output = model.getValue(source);
		Object input = model.getValue(target);
		//if(  output instanceof data.Output<?> || input instanceof data.Input<?>)
		{
			if( !(output instanceof Output<?> && input instanceof Input<?>))
				return "";
			
			if( !((Output<?>)output).isInputConnectable((Input<?>)input) )
			{
				return "Wrong data Type!";
			}
			
			// if input is connected => invalid
			if( ((Input<?>) input).getOutput() != null)
				return "input already connected";
			
//			//if input is connected but edge is being moved
//			if( ((data.Input<?>) input).getOutput() == connections.get(key))
//				return true;
			
			//Unfortunately, input can only be connected to one output for now...
		}
		return super.validateConnection(source, target);
	}

}
