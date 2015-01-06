package nz.schmidt.carrytrader.tool;

public class ConfigurationFactory 
{
	
	public static Configuration getConfiguration()
	{
		return Configuration.load("c:/tmp/configurationTest.conf");
	}
}
