package nz.schmidt.carrytrader.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Trade {
	private String _id;
	private double _price = 0.0;
	long starttime = 0;
	long endtime = 0;
	long profitableTime = 0;
	double profit = 0.00;
	boolean active = true;
	private String _longorshort;
	private double _stopPrice = -0.99;

	public Trade(String id, String price2, String starttime2, String longorshort) {
		this._id = id;
		this._longorshort = longorshort;
		this._price = Double.parseDouble(price2);
		
		double newstopPrice = -0.99;
		if (_longorshort.equals("long"))
		{
			newstopPrice =  _price - (_price * 0.02);
		}
		else
		{
			newstopPrice =  _price + (_price * 0.02);
		}
		
		setNewStopPrice(newstopPrice);
	}

	public double get_price() {
		return _price;
	}

	public void set_price(double _price) {
		this._price = _price;
	}

	public boolean isStopPriceImprovement(double stopPrice) {
		boolean isimprovement = false;

		if (_stopPrice < 0.00) 
		{
			isimprovement = true;
		} else {

			if (_longorshort.equals("long")) 
			{
				if (_stopPrice < stopPrice)
				{
					isimprovement = true;
				}
			} 
			else 
			{
				if (_stopPrice > stopPrice)
				{
					isimprovement = true;
				}
			}
		}
		
		return isimprovement;
	}

	public void setNewStopPrice(double stopPrice) 
	{

			HttpClient httpClient = HttpClientBuilder.create().build();

			try {

				// Set these variables to whatever personal ones are preferred
				String domain = ConfigurationFactory.getConfiguration().getBaseUrl();

				HttpPatch httpPost = new HttpPatch(domain + "/v1/accounts/"
						+ ConfigurationFactory.getConfiguration().get_account_id() + "/trades/" + _id);

				ArrayList<NameValuePair> postParameters;
				postParameters = new ArrayList<NameValuePair>();
				System.out.println("New Stop Loss price " + stopPrice);
								
				postParameters.add(new BasicNameValuePair("stopLoss", "" + round(stopPrice, 3)));

				httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

				httpPost.setHeader(new BasicHeader("Authorization", "Bearer "
						+ ConfigurationFactory.getConfiguration().get_access_token()));

				System.out.println("Executing request: "
						+ httpPost.getRequestLine());
				HttpResponse resp = httpClient.execute(httpPost);
				HttpEntity entity = resp.getEntity();
				if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
					System.out.println("all okay");

					InputStream stream = entity.getContent();
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(
							stream));

					String newStopPrice = "";

					String jsonObject = "";
					
					while ((line = br.readLine()) != null) 
					{
						jsonObject  += line;
					}

					Object obj = JSONValue.parse(jsonObject);
					JSONObject tick = (JSONObject) obj;
					// unwrap if necessary

					if (tick != null) {
						System.out.println("start parsing");
						
						// ignore heartbeats
						if (tick.containsKey("stopLoss")) {
							newStopPrice = tick.get("stopLoss")
									.toString();
						}
					}

					System.out.println("stopLoss = " + newStopPrice);
					
					_stopPrice = Double.parseDouble(newStopPrice);
				}

				else {
					// print error message
					String responseString = EntityUtils.toString(entity, "UTF-8");
					System.out.println(responseString);
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	public boolean isActive() 
	{
		return active;
	}
	
}

