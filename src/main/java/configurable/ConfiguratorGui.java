/**
 * 
 */
package configurable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import configurable.ConfigurationEditor.Parameter;

/**
 * @author Simon Dufour
 *
 */
public class ConfiguratorGui implements PropertyChangeListener {
	
	private static Map<Class<?>, Editor> editors = new HashMap<>();
	static {
		editors.put(Integer.class, new IntegerEditor());
		editors.put(int.class, new IntegerEditor());
	}

	private final ConfigurationEditor ce;
	private final Map<String,JLabel> labels = new HashMap<>();
	
	public ConfiguratorGui(ConfigurationEditor ce)
	{
		this.ce = ce;
	}
	
	public void showOptionFrame()
	{
		ce.parameters.forEach(p-> p.addPropertyChangeListener(p.name, this) );
		JOptionPane.showMessageDialog(null,getDisplayPane(),"Information",JOptionPane.INFORMATION_MESSAGE);
		ce.parameters.forEach(p-> p.removePropertyChangeListener(p.name, this) );
	}
	
	public JPanel getDisplayPane()
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(ce.object.toString()));
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		
		c.gridy=0;
		
		for(Parameter p : ce.parameters)
		{
			c.insets = new Insets(5, 5, 5, 5);
			c.gridx=0;
			panel.add(new JLabel(p.name),c);
			
			c.gridx=1;
			JLabel value = new JLabel(p.get().toString());
			labels.put(p.name, value);
			panel.add(value,c);
			
			c.gridx=2;
			JButton editBtn = new JButton(CreateEditAction(p));
			panel.add(editBtn,c);
			
			c.gridy++;
		}
		return panel;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		labels.get(event.getPropertyName()).setText(String.valueOf(event.getNewValue()));
	}
	
	private static Action CreateEditAction(Parameter p)
	{
		@SuppressWarnings("serial")
		class ParameterAction extends AbstractAction {
			public ParameterAction() {
				super("Edit");
				putValue(SHORT_DESCRIPTION, "Edit the value");
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				
				Editor editor = editors.get(p.type);
				
				if(editor!=null)
					editor.showDialog(p);
			}
		}
		
		return new ParameterAction();
	}
	
	public static interface Editor
	{
		void showDialog(Parameter p);
		JPanel getPanel(Parameter p);
	}
	
	public static class IntegerEditor implements Editor
	{
		@Override
		public void showDialog(Parameter p) {
			try {
				
				String result = JOptionPane.showInputDialog(null,
				        "enter new value",
				        p.name,
				        JOptionPane.INFORMATION_MESSAGE,
				        null,
				        null,
				        p.get() == null? null : (Integer) p.get() ).toString();
				if(result == null || result.isEmpty())
					return;
				
				int ans = Integer.parseInt(result);
				p.set(ans);
			}
			catch(NumberFormatException e)
			{
				//TODO switch to logger
				//logger.
				
				System.out.println("Error when parsing the result, new value not assigned");
				System.out.println(e);
			}
		}

		@Override
		public JPanel getPanel(Parameter p) {
			return null;
		}
		
	}
}
