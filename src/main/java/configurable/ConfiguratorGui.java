/**
 * 
 */
package configurable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

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
public class ConfiguratorGui {
	
	public static void showOptionFrame(ConfigurationEditor ce)
	{
		JOptionPane.showMessageDialog(null,getDisplayPane(ce),"Information",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static JPanel getDisplayPane(ConfigurationEditor ce)
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
			panel.add(value,c);
			
			c.gridx=2;
			JButton editBtn = new JButton(CreateEditAction(p));
			panel.add(editBtn,c);
			
			c.gridy++;
			
			//TODO event to update display...
			
			//map.put(p, value);
			//presenter.addPropertyChangeListener(variable, this);
		}
		return panel;
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
				//TODO map of type that can be edited and the associated editor
				if(p.type.equals(Integer.class) || p.type.equals(int.class))
					showIntegerEditor(p);
			}
		}
		
		return new ParameterAction();
	}
	
	
	public static void showIntegerEditor(Parameter p) {
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
	
}
