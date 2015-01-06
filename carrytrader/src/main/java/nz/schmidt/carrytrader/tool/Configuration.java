package nz.schmidt.carrytrader.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "configuration")
public class Configuration 
{

	private String _access_token;
	private String _account_id;

	public Configuration(String access_token, String account_id) 
	{
		this._access_token = access_token;
		this._account_id = account_id;
	}

	public Configuration() {
	}

	public String get_access_token() {
		return _access_token;
	}

	public void set_access_token(String _access_token) {
		this._access_token = _access_token;
	}

	public String get_account_id() {
		return _account_id;
	}

	public void set_account_id(String _account_id) {
		this._account_id = _account_id;
	}
	
	
	public String getBaseUrl() {
		return "https://api-fxpractice.oanda.com";
	}

	public String getStreamBaseUrl() {
		String domain = "https://stream-fxpractice.oanda.com";
		return domain;
	}
	
	private void save(String filename) 
	{
		try {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
	    Marshaller m = context.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

	    // Write to System.out
	    m.marshal(this, System.out);

	    // Write to File
			m.marshal(this, new File(filename));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Configuration load(String filename) 
	{
		JAXBContext context;
		Configuration configuration = null;
		try {
			context = JAXBContext.newInstance(Configuration.class);

			Unmarshaller um = context.createUnmarshaller();
			configuration = (Configuration) um.unmarshal(new FileReader(filename));
			
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return configuration;
	}
	
	public static void main(String[] args) 
	{
		Configuration configuration = new Configuration("oanda_code", "repository number");
		configuration.save("c:/tmp/test.conf");
	}
}
