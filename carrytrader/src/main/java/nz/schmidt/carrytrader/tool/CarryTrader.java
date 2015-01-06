package nz.schmidt.carrytrader.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CarryTrader {

	public static void main(String[] args) throws IOException {

		CarryTrader javaApiStreaming = new CarryTrader();

		// javaApiStreaming.getinterestRate(account_id, access_token,
		// "EUR_JPY");

		ArrayList<CarryTradeInstrument> carryTradeInstruments = new ArrayList<CarryTradeInstrument>();

		carryTradeInstruments.add(new CarryTradeInstrument("NZD_JPY", "long",
				1000));
		carryTradeInstruments.add(new CarryTradeInstrument("ZAR_JPY", "long",
				1000));
		carryTradeInstruments.add(new CarryTradeInstrument("TRY_JPY", "long",
				1000));

		carryTradeInstruments.add(new CarryTradeInstrument("EUR_NZD", "short",
				1000));
		carryTradeInstruments.add(new CarryTradeInstrument("EUR_ZAR", "short",
				1000));
		carryTradeInstruments.add(new CarryTradeInstrument("EUR_TRY", "short",
				1000));

		javaApiStreaming.start(carryTradeInstruments);
	}

	private void start(ArrayList<CarryTradeInstrument> carryTradeInstruments) {
		while (true) {
			for (CarryTradeInstrument carryTradeInstrument : carryTradeInstruments) {
				carryTradeInstrument.checkAction();
			}
		}
	}

	public void getinterestRate(String account_id, String access_token,
			String instrument_key) {
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {

			// Set these variables to whatever personal ones are preferred
			String domain = ConfigurationFactory.getConfiguration().getBaseUrl();

			HttpUriRequest httpGet = new HttpGet(
					domain
							+ "/v1/instruments?accountId="
							+ account_id
							+ "&instruments="
							+ instrument_key
							+ "&start=2014-06-19T15%3A47%3A40Z&end=2014-06-19T15%3A47%3A50Z");
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
				
				String jsonObject = "";
				
				while ((line = br.readLine()) != null) 
				{
					jsonObject += line;
				}

				
				System.out.println("line " + line);

				Object obj = JSONValue.parse(jsonObject);
				JSONObject tick = (JSONObject) obj;
				// unwrap if necessary

				if (tick != null) {
					System.out.println("start parsing");

					if (tick.containsKey("tick")) {
						tick = (JSONObject) tick.get("tick");
					}
					// ignore heartbeats
					if (tick.containsKey("instrument")) {
						System.out.println("-------");
						String instrument = tick.get("instrument")
								.toString();
						String time = tick.get("time").toString();
						double bid = Double.parseDouble(tick.get("bid")
								.toString());
						double ask = Double.parseDouble(tick.get("ask")
								.toString());
						System.out.println(instrument);
						System.out.println(time);
						System.out.println(bid);
						System.out.println(ask);
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
	}


}
