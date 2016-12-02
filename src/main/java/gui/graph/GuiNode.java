/**
 * 
 */
package gui.graph;

import java.util.Collection;
import java.util.Map;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import data.node.ConnectionManager;
import data.node.Input;
import data.node.Node;
import data.node.Output;


/**
 * @author simon
 *
 */
public class GuiNode
{
	
	final int	PORT_DIAMETER	= 20;
	final int	PORT_RADIUS		= PORT_DIAMETER / 2;
	
	GuiNode(Node n, mxGraph graph, Map<Object, mxCell> mapA, ConnectionManager cm)
	{
		Collection<Input> inputs = cm.getInputs(n).values();
		Collection<Output> outputs = cm.getOutputs(n).values();
		
		mxCell main = createMainCell(n, graph, graph.getDefaultParent(), inputs.size(),outputs.size() );
		mapA.put(n, main);
		double count = 1;
		
		for (Input<?> i : inputs)
		{
			double pos = count++ / (double) (inputs.size() + 1);
			mxCell c =  createInput(pos,i, graph, main);
			mapA.put(i, c);
		}
		count = 1;
		
		for (Output<?> o : outputs)
		{
			double pos = count++ / (double) (outputs.size() + 1);
			mxCell c = createOutput(pos,o, graph, main);
			mapA.put(o, c);
		}
		
	}
	
	private mxCell createMainCell(Node n, mxGraph graph, Object parent, int inputSize, int outputSize)
	{
		int height = Math.max( 25 * Math.max(inputSize, outputSize), 50);
		
		mxCell v1 = (mxCell) graph.insertVertex(parent, null, n, 20, 20, 150, 50, "");
		v1.setConnectable(false);
		v1.setCollapsed(true);
		mxGeometry geo = graph.getModel().getGeometry(v1);
		// The size of the rectangle when the minus sign is clicked
		geo.setAlternateBounds(new mxRectangle(20, 20, 200, height));
		return v1;
	}
	
	private mxCell createInput(double pos, Input<?> i, mxGraph graph, Object parent)
	{
		mxGeometry geo1 = new mxGeometry(0, pos, PORT_DIAMETER, PORT_DIAMETER);
		// Because the origin is at upper left corner, need to translate to
		// position the center of port correctly
		geo1.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo1.setRelative(true);
		
		mxCell port1 = new mxCell(i, geo1, "shape=ellipse;perimter=ellipsePerimeter");
		port1.setVertex(true);
		graph.addCell(port1, parent);
		return port1;
	}
	
	private mxCell createOutput(double pos,Output<?> o, mxGraph graph, Object parent)
	{
		mxGeometry geo2 = new mxGeometry(1.0, pos, PORT_DIAMETER, PORT_DIAMETER);
		geo2.setOffset(new mxPoint(-PORT_RADIUS, -PORT_RADIUS));
		geo2.setRelative(true);
		
		mxCell port2 = new mxCell(o, geo2, "shape=ellipse;perimter=ellipsePerimeter");
		port2.setVertex(true);
		graph.addCell(port2, parent);
		return port2;
	}
}
