/**
 * 
 */
package gui.graph;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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

import configurable.ConfigurationEditor;
import configurable.ConfiguratorGui;
import data.node.ConnectionManager;
import data.node.ConnectionManager.NodeEvent;
import data.node.ConnectionManager.NodeEventListener;
import data.node.Input;
import data.node.Node;
import data.node.Output;


/**
 * @author simon
 *
 */
public class LinkGraph extends JFrame implements NodeEventListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	Map<mxCell,Connection > connections = new HashMap<>();
	Map<Object, mxCell> mapObjectToMxCell = new HashMap<Object, mxCell>();

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
			
			
			// Overrides method to provide a cell label in the display
			public String convertValueToString(Object cell) {
				if (cell instanceof mxCell) {
					Object value = ((mxCell) cell).getValue();

					if (value instanceof Node)
						return ((Node)value).getNodeName();
					
					if (value instanceof Input )
						return manager.getName((Input<?>)value);
					
					if (value instanceof Output)
						return manager.getName((Output<?>) value);
					
				}

				return super.convertValueToString(cell);
			}
			
			public boolean isCellEditable(Object cell)
			{
				return getModel().isEdge(cell);
			}
		};
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();

		try
		{
			
			
			for(Node n : manager.getNodes())
				addNodeIfNeeded(n);
			
			for(Node n : manager.getNodes())
				for(Output<?> o : manager.getOutputs(n).values())
					for(Input<?> connectedInput : o.getConnectedInputs())
						createConnection(connectedInput, o);
	
			
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
		
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{
		
			public void mouseReleased(MouseEvent e)
			{
				if(!SwingUtilities.isRightMouseButton(e))
						return;
				
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				
				if (cell != null && cell instanceof mxCell)
				{
					System.out.println("Editing cell "+graph.getLabel(cell));
					Object value = ((mxCell) cell).getValue();
					new ConfiguratorGui(new ConfigurationEditor(value)).showOptionFrame();
				}
			}
		});
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
		//graphComponent.setToolTips(true); //override public String getToolTipForCell(Object cell) to get interesting results
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
		@SuppressWarnings("unchecked")
		public void invoke(Object sender, mxEventObject event) {
			mxCell edge = (mxCell) event.getProperty("edge");
			boolean source = (Boolean) event.getProperty("source");
			Object terminal = event.getProperty("terminal");
			
			
			mxICell sourceCell = edge.getSource();// gives the source cell
			mxICell targetCell = edge.getTarget();//gives the target cell
			
			mxIGraphModel model = graph.getModel();
			Output<?> output = (Output<?>) model.getValue(sourceCell);
			Input<?> input = ( Input<?>) model.getValue(targetCell);
			
			//If already existing connection moved
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

			//SyncUp new connection with the underlying object if needed
			if(!output.getConnectedInputs().contains(input))
				manager.connect((Input<Object>)input, (Output<Object>)output);
			
			//Add a new connection object for future reference
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
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((destination == null) ? 0 : destination.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Connection other = (Connection) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (destination == null) {
				if (other.destination != null)
					return false;
			} else if (!destination.equals(other.destination))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}
		private LinkGraph getOuterType() {
			return LinkGraph.this;
		}
		
	}
	
	public static void newFrame(ConnectionManager cm )
	{
		LinkGraph frame = new LinkGraph( cm);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 640);
		frame.setVisible(true);
	}

	@Override
	public void accept(NodeEvent e) {
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater( ()-> accept(e));
			return;
		}
			
		graph.getModel().beginUpdate();

		try
		{
			if(NodeEvent.NEW_NODE.equals(e.event))
				if(!mapObjectToMxCell.containsKey(e.sourceNode))
					addNodeIfNeeded(e.sourceNode);
			
			if(NodeEvent.CONNNECTION.equals(e.event))
					createConnection(e.destInput, e.sourceOutput);
			
//			if(NodeEvent.DISCONNECTION.equals(e.event))
//				if(!mapObjectToMxCell.containsKey(e.sourceNode))
//					addNodeIfNeeded(e.sourceNode);
//			
//			if(NodeEvent.DISPOSE_NODE.equals(e.event))
//				if(!mapObjectToMxCell.containsKey(e.sourceNode))
//					dispose(e.sourceNode);
			
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		
		
	}
	
	private void addNodeIfNeeded(Node n)
	{
		//TODO factory for GUI node!!! reference should not escape constructor!
		new GuiNode(n, graph, mapObjectToMxCell, manager);
	}
	
	private void createConnection(Input i, Output o)
	{
		Object v1;
		if(mapObjectToMxCell.containsKey(o))
			v1 = mapObjectToMxCell.get(o);
		else
			throw new RuntimeException( "object not in the map: " + o);
		
		Object v2;
		if(mapObjectToMxCell.containsKey(i))
			v2 = mapObjectToMxCell.get(i);
		else
			throw new RuntimeException( "object not in the map: " + i);
		
		//Change name to type? to connectedInput.getInputType().getName()^
		Connection connection = new Connection( "" , o, i) ;
		if(!connections.containsValue(connection))
			connections.put((mxCell) graph.insertEdge(graph.getDefaultParent(), null,connection , v1, v2), connection);
	}
}
