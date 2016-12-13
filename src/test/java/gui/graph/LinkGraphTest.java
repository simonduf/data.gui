package gui.graph;

import static org.junit.Assert.*;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.junit.Test;

import configurable.Getter;
import configurable.Setter;
import data.node.ConnectionManager;
import data.node.Input;
import data.node.Node;
import data.node.Output;

public class LinkGraphTest {



	public static class IntegerNode implements Node {
		public Input<?> input = new Input<Integer>(this::processData){};
		public Output<?> output = new Output<Integer>(){};
		public String name;
		private void processData(Integer d){	}
		
		public IntegerNode(String name)
		{ this.name = name;}
		
		int i =2;
		@Setter(name = "Integer")
		public void setInt(int i){ this.i = i; }
		
		@Getter(name = "Integer")
		public int getInt(){return i;}
		
		
		public String getNodeName() {
			return name;
		}
	};
	
	public static class DoubleNode implements Node {
		public Input<?> input = new Input<Double>(this::processData){};
		public Output<?> output = new Output<Double>(){};
		public String name;
		
		public DoubleNode(String name)
		{ this.name = name;}
		
		private void processData(Double d){	}
		public String getNodeName() {
			return name;
		}
	};
	
	@Test
	public void test() throws InterruptedException {
		ConnectionManager cm = new ConnectionManager();
		
		IntegerNode intnode = new IntegerNode("intnode");
		
		IntegerNode anotherIntNode = new IntegerNode("anotherIntNode");
		
		
		cm.add(intnode);
		cm.add(anotherIntNode);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				LinkGraph graph = new LinkGraph(cm);
				graph.pack();
				graph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				graph.setVisible(true);
				cm.addListener(graph);
			}
		});
		
		
		Thread.sleep(1000);
		
		//Test Connection
		{
			NonModalJOptionPane dialog = new NonModalJOptionPane( "Connect intnode's output to anothorIntNode's input.");
			
			while(!intnode.output.getConnectedInputs().contains(anotherIntNode.input) )
			{
				Thread.sleep(100);
				dialog.failOnCancellAndReshowIfClosed();
			}
			assertTrue(intnode.output.getConnectedInputs().contains(anotherIntNode.input));
			dialog.dialog.setVisible(false);
		}
		
		
		//Test disconnection
		{
			NonModalJOptionPane dialog = new NonModalJOptionPane( "Disconnect intnode's output from anothorIntNode's input by selecting the connection and pressing the DEL key.");
			
			while(intnode.output.getConnectedInputs().contains(anotherIntNode.input) )
			{
				Thread.sleep(100);
				dialog.failOnCancellAndReshowIfClosed();

			}
			assertTrue(!intnode.output.getConnectedInputs().contains(anotherIntNode.input));
			dialog.dialog.setVisible(false);
		}
		
		//Test setting value
		{
			NonModalJOptionPane dialog = new NonModalJOptionPane( "Right click on intNode and set the Integer value to 3");
			
			while(intnode.i != 3)
			{
				Thread.sleep(100);
				dialog.failOnCancellAndReshowIfClosed();
			}
			
			assertTrue(intnode.i == 3);
			dialog.setVisible(false);
		}
		
		DoubleNode  doubleNode= new DoubleNode("doubleNode");
		DoubleNode anotherDoubleNode = new DoubleNode("anotherDoubleNode");
		
		cm.add(doubleNode);
		cm.add(anotherDoubleNode);
		
		{
			NonModalJOptionPane dialog = new NonModalJOptionPane( "Click OK if there is two extra nodes.");
			while(dialog.getValue() == JOptionPane.UNINITIALIZED_VALUE)
				Thread.sleep(100);
			assertEquals( JOptionPane.OK_OPTION, dialog.getValue() );
			dialog.setVisible(false);
		}

	}
	
	@SuppressWarnings("serial")
	public static class NonModalJOptionPane extends JOptionPane
	{
		public JDialog dialog;
		NonModalJOptionPane(String message)
		{
			super(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			dialog = createDialog(null, "");
			dialog.setModal(false); // this says not to block background components
			dialog.setVisible(true);
		}
		
		public void failOnCancellAndReshowIfClosed()
		{
			if(getValue() instanceof Integer && (Integer)getValue() == JOptionPane.CANCEL_OPTION)
				fail();
			
			if(getValue() != JOptionPane.UNINITIALIZED_VALUE)
			{
				setValue(JOptionPane.UNINITIALIZED_VALUE);
				dialog.setVisible(true);
			}
		}
	}
	public static void main(String[] args)
	{
		ConnectionManager cm = new ConnectionManager();
		

		
		IntegerNode intnode = new IntegerNode("intnode");
		DoubleNode  doubleNode= new DoubleNode("doubleNode");
		IntegerNode anotherIntNode = new IntegerNode("anotherIntNode");
		DoubleNode anotherDoubleNode = new DoubleNode("anotherDoubleNode");
		
		cm.add(intnode);
		cm.add(doubleNode);
		cm.add(anotherIntNode);
		cm.add(anotherDoubleNode);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				LinkGraph graph = new LinkGraph(cm);
				graph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				graph.setVisible(true);
			}
		});
	}
}
