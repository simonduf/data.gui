/**
 * 
 */
package gui.graph;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectionHandler;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

import data.node.ConnectionManager;
import data.node.Input;
import data.node.Node;
import data.node.Output;


/**
 * @author simon
 *
 */
public class LinkGraph extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	Map<mxCell,Connection > connections = new HashMap<>();

	private ConnectionManager manager;
	mxGraph graph;

	public LinkGraph(ConnectionManager manager)
	{
		super("Link Graph");
		this.manager = manager;
		graph = new mxGraph(){
			@Override
			public boolean isValidConnection(Object source, Object target)
			{
				if( !super.isValidConnection(source, target) )
					return false;
				
				mxIGraphModel model = getModel();
				Object output = model.getValue(source);
				Object input = model.getValue(target);
				
				if( !(output instanceof Output<?>))
					return false;
				
				if (!(input instanceof Input<?>))
					return false;
				
				if( !manager.isConnectable( ( Input<?>)input ,  (Output<?>)output) )
					return false;

				return true;
			
			}
		};
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();

		try
		{
			Map<Object, mxCell> mapA = new HashMap<Object, mxCell>();
			
			for(Node n : manager.getNodes())
			{
				new GuiNode(n, graph, mapA, manager);
//				if(!manager.getkeeplist().contains(n))
//					manager.keep(n);
			}
			
			for(Node n : manager.getNodes())
			{
				for(Output<?> o : manager.getOutputs(n).values())
				{
					for(Input<?> connectedInput : o.getConnectedInputs())
					{
						Object v1;
						if(mapA.containsKey(o))
						{
							v1 = mapA.get(o);
						}
						else
						{
							throw new RuntimeException( "object not in the map: " + n);
						}
						
						Object v2;
						if(mapA.containsKey(connectedInput))
						{
							v2 = mapA.get(connectedInput);
						}
						else
						{
							throw new RuntimeException( "object not in the map: " + n);
						}
						
						//Change name to type? to connectedInput.getInputType().getName()^
						Connection connection = new Connection( "" , o, connectedInput) ;
						connections.put((mxCell) graph.insertEdge(parent, null,connection , v1, v2), connection);
						
					}
				}
				
				
			}
			
			mxIGraphLayout layout = new mxFastOrganicLayout(graph);
			//mxIGraphLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
			//mxIGraphLayout layout = new mxEdgeLabelLayout(graph);
			
            layout.execute(parent);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph){
			@Override
			protected mxConnectionHandler createConnectionHandler()
			{
				return new NodeConnetionHandler(this, manager);
			}
		};
		getContentPane().add(graphComponent);
		
//		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
//		{
//		
//			public void mouseReleased(MouseEvent e)
//			{
//				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
//				
//				if (cell != null)
//				{
//					System.out.println("cell="+graph.getLabel(cell));
//				}
//			}
//		});
 		graph.setCellsEditable(false);//TODO implements, disabled for now
	    graph.setAllowDanglingEdges(false);
	    graph.setAllowLoops(false);
//	    graph.setCellsDeletable(false);//TODO now cells are also deleted from UI but not manager
//	    graph.setCellsCloneable(false);
//	    graph.setCellsDisconnectable(false);
	    graph.setDropEnabled(false);
	   
//	    graph.setSplitEnabled(false);
//	    graph.setCellsBendable(false);
		graphComponent.setImportEnabled(false);
		graphComponent.setDragEnabled(false);
		//graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT, new EdgeConnectListener());
		//graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT_CELL, new graphConnectListener());
		graph.addListener(mxEvent.CELL_CONNECTED, new graphConnectListener());
		graph.addListener(mxEvent.CELLS_REMOVED, new GraphDeletionListener());
		
		
		// to delete using the keyboard
		new mxKeyboardHandler( graphComponent);

	}
	

	//Event occurs when new edge are created : may be used to set user object?
	private class EdgeConnectListener implements mxIEventListener {
		public void invoke(Object sender, mxEventObject event) {
			mxCell edge = (mxCell) event.getProperty("cell");
			
			mxICell source = edge.getSource();// gives the source cell
			mxICell target = edge.getTarget();//gives the target cell
			
			System.out.println("Connection from " + source + " to " + target);
			
		}
	}
	
	//process mxEvent. or mxEvent.REMOVE_CELLS? or CELLS_REMOVED from the graph
	//Those events contains: 
	//	edge : the edge cell (arrow)
	//	source : boolean indicating the side of the arrow that changed
	//	terminal : ?
	public class GraphDeletionListener implements mxIEventListener {
		public void invoke(Object sender, mxEventObject event) {
			Object[] cells = (Object[]) event.getProperty("cells");
			
			mxIGraphModel model = graph.getModel();
			
			for( Object obj: cells)
			{
				mxCell cell = (mxCell) obj;
				if(!cell.isEdge())
				{
					manager.dispose((Node) model.getValue(cell));
					continue;
				}
				
				
				mxICell sourceCell = cell.getSource();// gives the source cell
				mxICell targetCell = cell.getTarget();//gives the target cell
				
				
				Output<?> output = (Output<?>) model.getValue(sourceCell);
				Input<?> input = ( Input<?>) model.getValue(targetCell);
				
				
				
				
				Input<?> oldInput = connections.get(cell).destination;
				Output<?> oldOutput = connections.get(cell).source;
				
				if(output != oldOutput)
					System.out.println( "output mismatch: " + oldOutput + " !=  " + output);
				
				if(input != oldInput)
					System.out.println( "input mismatch: " + oldInput + " !=  " + input);
				
				if( manager.getOutput(input) != oldOutput)
				{
					System.out.println( "connection mismatch: " + manager.getOutput(oldInput) + " !=  " + oldOutput);
					return;
				}
				
				manager.disconnect(oldInput, oldOutput);//oldOutput.disconect(oldInput);
				connections.remove(cell);
				//Do not dispose node!!
				System.out.println("Disconnecting " + oldOutput + " and " + oldInput);
			}
		}
	}
	
	//process mxEvent.CONNECT_CELL or mxEvent.CELL_CONNECTED from the graph
	//Those events contains: 
	//	edge : the edge cell (arrow)
	//	source : boolean indicating the side of the arrow that changed
	//	terminal : ?
	public class graphConnectListener implements mxIEventListener {
		public void invoke(Object sender, mxEventObject event) {
			mxCell edge = (mxCell) event.getProperty("edge");
			boolean source = (Boolean) event.getProperty("source");
			Object terminal = event.getProperty("terminal");
			
			
			mxICell sourceCell = edge.getSource();// gives the source cell
			mxICell targetCell = edge.getTarget();//gives the target cell
			
			mxIGraphModel model = graph.getModel();
			Output<?> output = (Output<?>) model.getValue(sourceCell);
			Input<?> input = ( Input<?>) model.getValue(targetCell);
			
			Connection c = connections.get(edge);
			if(c != null)
			{
				Input<?> oldInput = connections.get(edge).destination;
				Output<?> oldOutput = connections.get(edge).source;
				
				
				if(input!=oldInput || output!=oldOutput)
				{
					if ( manager.getOutput(oldInput)  != oldOutput)
					{
						System.out.println("?input not connected to what it was supposed to...?");
					}
					manager.disconnect(oldInput, oldOutput);//oldOutput.disconect(oldInput);
					connections.remove(edge);
					//Do not dispose node!!
					System.out.println("Disconnecting " + oldOutput + " and " + oldInput);
				}
			}
			
			//When new edge are created, two event are generated: one when the drag start, one when it finishes.
			//We only process the completed connections.
			if(input == null)
			{
				return;
			}

			
			manager.connect(input, output);
			connections.put(edge, new Connection("", output, input));
			
			
			System.out.println("Connection from " +output  + " to " + input);
			
		}
	}
	
	public class Connection
	{
		public String name;
		public Output<?> source;
		public Input<?> destination;
		protected Connection(String name, Output<?> source, Input<?> destination) {
			this.name = name;
			this.source = source;
			this.destination = destination;
		}
		
	}
	
	public static void newFrame(ConnectionManager cm )
	{
		LinkGraph frame = new LinkGraph( cm);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 640);
		frame.setVisible(true);
	}
}
