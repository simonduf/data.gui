/**
 * 
 */
package gui.graph;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;

import data.ConnectionManager;

/**
 * @author Simon Dufour
 *
 */
public class NodeConnetionHandler extends mxConnectionHandler {

	public NodeConnetionHandler(mxGraphComponent graphComponent) {
		super(graphComponent);
	}
	
	@Override
	public String validateConnection(Object source, Object target)
	{
		mxIGraphModel model = graphComponent.getGraph().getModel();
		Object output = model.getValue(source);
		Object input = model.getValue(target);
		//if(  output instanceof data.Output<?> || input instanceof data.Input<?>)
		{
			if( !(output instanceof data.Output<?> && input instanceof data.Input<?>))
				return "";
			
			if( !((data.Output<?>)output).isInputConnectable(( data.Input<?>)input) )
			{
				return "Wrong data Type!";
			}
			
			// if input is connected => invalid
			if( ((data.Input<?>) input).getOutput() != null)
				return "input already connected";
			
//			//if input is connected but edge is being moved
//			if( ((data.Input<?>) input).getOutput() == connections.get(key))
//				return true;
			
			//Unfortunately, input can only be connected to one output for now...
		}
		return super.validateConnection(source, target);
	}

}
