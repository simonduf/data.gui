package gui.graph;

import static org.junit.Assert.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Test;

import configurable.Getter;
import configurable.Setter;
import data.node.ConnectionManager;
import data.node.Input;
import data.node.Node;
import data.node.Output;

public class LinkGraphTest {

//	@Test
//	public void test() {
//		fail("Not yet implemented");
//	}

	public static class IntegerNode implements Node {
		public Input<?> input = new Input<Integer>(this::processData){};
		public Output<?> output = new Output<Integer>(){};
		private void processData(Integer d){	}
		
		int i =2;
		@Setter(name = "Integer")
		public void setInt(int i){ this.i = i; }
		
		@Getter(name = "Integer")
		public int getInt(){return i;}
		
		
		public String getNodeName() {
			return "The Integer Node!";
		}
	};
	
	public static class DoubleNode implements Node {
		public Input<?> input = new Input<Double>(this::processData){};
		public Output<?> output = new Output<Double>(){};
		
		private void processData(Double d){	}
		public String getNodeName() {
			return "The Double Node!";
		}
	};
	
	
	public static void main(String[] args)
	{
		ConnectionManager cm = new ConnectionManager();
		

		IntegerNode intnode = new IntegerNode();
		DoubleNode  doubleNode= new DoubleNode();
		IntegerNode anotherIntNode = new IntegerNode();
		DoubleNode anotherDoubleNode = new DoubleNode();
		
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
