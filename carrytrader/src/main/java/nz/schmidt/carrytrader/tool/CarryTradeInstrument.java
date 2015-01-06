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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CarryTradeInstrument {
	ArrayList<Trade> trades;
	private String _key;
	private String _longorshort;
	private int _volume;
	int maximumNumberofActiveTrades = 3;

	public CarryTradeInstrument(String key, String longorshort, int volume) {
		this._key = key;
		this._longorshort = longorshort;
		this._volume = volume;
		trades = new ArrayList<Trade>();
	}

	public void checkAction() 
	{
		int openTrades = 0;
		
		for (Trade trade : trades) 
		{
			if (trade.isActive())
			{
				
			}
		}
		
		if (trades.size() <= maximumNumberofActiveTrades) 
		{
			buyInstrument(_key, _volume, ConfigurationFactory.getConfiguration().get_account_id(), ConfigurationFactory.getConfiguration().get_access_token());
		}

		checkStopLoss(ConfigurationFactory.getConfiguration().get_account_id(), ConfigurationFactory.getConfiguration().get_access_token());
	}

	private void checkStopLoss(String account_id, String access_token) 
	{
		for (Trade trade : trades) 
		{
			double currentPrice = getPrice(account_id, access_token, _key);
			double price = trade.get_price();

			double profit = 0.0;
			
			if (_longorshort.equals("long"))
			{
				profit = currentPrice - price;
			}
			else
			{
				profit = price - currentPrice;
			}

			double profitInPercent = profit/price;

			if (profitInPercent > 0.5)
			{
				double profitInPercentStop = profitInPercent/2;

				double stopPrice = 0.0;
				
				if (_longorshort.equals("long"))
				{
					stopPrice = price + (price * profitInPercentStop);
				}
				else
				{
					stopPrice = price - (price * profitInPercentStop);
				}
				
				if (trade.isStopPriceImprovement(stopPrice))
				{
					trade.setNewStopPrice(stopPrice);
				}
			}
		}
	}

	private void buyInstrument(String _key2, int _volume2, String account_id,
			String access_token) {

		HttpClient httpClient = HttpClientBuilder.create().build();

		try {

			// Set these variables to whatever personal ones are preferred
			String domain = ConfigurationFactory.getConfiguration().getBaseUrl();

			HttpPost httpPost = new HttpPost(domain + "/v1/accounts/"
					+ account_id + "/orders");

			ArrayList<NameValuePair> postParameters;
			postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("instrument", _key));

			if (_longorshort.equals("short"))
			{
				postParameters.add(new BasicNameValuePair("side", "sell"));
			}
			else
			{
				postParameters.add(new BasicNameValuePair("side", "buy"));
			}
			
			
			postParameters.add(new BasicNameValuePair("units", "" + _volume));
			postParameters.add(new BasicNameValuePair("type", "market"));

			httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

			httpPost.setHeader(new BasicHeader("Authorization", "Bearer "
					+ access_token));

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

				String id = "";
				String price = "";
				String starttime = "";

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
					
					if (tick.containsKey("tradeOpened")) 
					{
						JSONObject tradeOpenend = (JSONObject) tick.get("tradeOpened");
						
						id = tradeOpenend.get("id").toString();
						
					}
					// ignore heartbeats
					if (tick.containsKey("instrument")) {
						System.out.println("-------");
						price = tick.get("price")
								.toString();
						starttime = tick.get("time")
								.toString();
					}
				}

				System.out.println("id = " + id);
				System.out.println("price = " + price);
				System.out.println("starttime = " + starttime);
				
				trades.add(new Trade(id, price, starttime, _longorshort));
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

	public double getPrice(String account_id, String access_token,
			String instrument_key) {
		HttpClient httpClient = HttpClientBuilder.create().build();

		double returnValue = -.999;

		try {

			// Set these variables to whatever personal ones are preferred
			String domain = ConfigurationFactory.getConfiguration().getStreamBaseUrl();

			HttpUriRequest httpGet = new HttpGet(domain
					+ "/v1/prices?accountId=" + account_id + "&instruments="
					+ instrument_key);
			httpGet.setHeader(new BasicHeader("Authorization", "Bearer "
					+ access_token));
			System.out
					.println("Executing request: " + httpGet.getRequestLine());
			HttpResponse resp = httpClient.execute(httpGet);
			HttpEntity entity = resp.getEntity();
			if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {
				InputStream stream = entity.getContent();
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(
						stream));
				if ((line = br.readLine()) != null) {
					Object obj = JSONValue.parse(line);
					JSONObject tick = (JSONObject) obj;
					// unwrap if necessary
					if (tick.containsKey("tick")) {
						tick = (JSONObject) tick.get("tick");
					}
					// ignore heartbeats
					if (tick.containsKey("instrument")) {
						System.out.println("-------");
						String instrument = tick.get("instrument").toString();
						String time = tick.get("time").toString();
						double bid = Double.parseDouble(tick.get("bid")
								.toString());
						double ask = Double.parseDouble(tick.get("ask")
								.toString());

						if (_longorshort.equals("long")) {
							returnValue = ask;
						} else {
							returnValue = bid;
						}
					}
				}
			} else {
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

		return returnValue;

	}


}
