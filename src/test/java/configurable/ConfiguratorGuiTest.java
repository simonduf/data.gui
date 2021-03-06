package configurable;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class ConfiguratorGuiTest {

	@Test
	@Ignore("Test combined with LinkGraphTest")
	public void test() throws InterruptedException {
		
		class DummyClass {
			int i =2;
			@Setter(name = "Integer")
			public void setInt(int i)
			{
				this.i = i;
			}
			
			@Getter(name = "Integer")
			public int getInt(){return i;}
		}
		
		Object o = new DummyClass();
		ConfigurationEditor cfgEdit = new ConfigurationEditor(o);
		new ConfiguratorGui(cfgEdit).showOptionFrame();
		Thread.sleep(10000);
	}

}
