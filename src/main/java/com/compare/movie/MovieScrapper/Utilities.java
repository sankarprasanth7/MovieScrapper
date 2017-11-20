package com.compare.movie.MovieScrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import org.json.simple.parser.JSONParser;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Utilities {
	public static Document getDataInPage(String url) {
		Document doc = null;
		Connection.Response response = null;
		for (int retryCount = 0; doc == null && retryCount < 3; retryCount++) {
			try {
				Thread.sleep(1000);
				response = Jsoup.connect(url).timeout(6000 * 1000)
						.userAgent(
								"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36")
						.ignoreHttpErrors(true).followRedirects(true).execute();
				if (response.statusCode() == 200) {
					doc = response.parse();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(url);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(url);
			}
		}
		return doc;
	}

	public static void addToFile(Object obj, String Path, String dir) {
		try {
			new File(dir).mkdirs();
			PrintWriter writer = new PrintWriter(Path, "UTF-8");
			writer.println(obj);
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void getlocations(String domain) {
		JSONObject citiesData = new JSONObject();
		if (domain.equals("ticketnew")) {
			Document doc = Utilities.getDataInPage("http://www.ticketnew.com/");
			Elements cities = doc.select("div#allCities > div");
			for (Element e : cities) {
				String mainCity = e.select("p").text().replaceAll(" ", "_").toLowerCase();
				Elements subCities = e.select("ul > li");
				JSONObject obj = new JSONObject();
				for (Element e1 : subCities) {
					obj.put(e1.text().replaceAll(" ", "_").toLowerCase(), e1.select("a").attr("href"));
				}
				citiesData.put(mainCity, obj);
			}
			addToFile(citiesData, "src/main/resources/ticketnew_data.json", "src/main/resources/");
		}
		if (domain.equals("bookmyshow")) {
			citiesData = new JSONObject();
			JSONParser parser = new JSONParser();
			String data;
			try {
				data = (String) parser.parse(new FileReader("src/main/resources/bookmyshow_conifg.json")).toString();
				JSONObject bmsData = new JSONObject(data);
				Iterator itr = bmsData.keys();
				while (itr.hasNext()) {
					String mainCity = (String) itr.next();
					JSONArray bmsCities = bmsData.getJSONArray(mainCity);
					JSONObject obj = new JSONObject();
					for (int i = 0; i < bmsCities.length(); i++) {
						String city = bmsCities.getJSONObject(i).getString("name").replaceAll(" ", "-")
								.replaceAll("\\)", "").replaceAll("\\(", "").toLowerCase();
						obj.put(city.replace("-", "_"), "https://in.bookmyshow.com/" + city);
					}
					citiesData.put(mainCity.replaceAll(" ", "_").toLowerCase(), obj);
					addToFile(citiesData, "src/main/resources/bookmyshow_data.json", "src/main/resources/");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void normalize() {
		JSONObject normalizeData = new JSONObject();
		JSONObject citiesData = new JSONObject();
		JSONParser parser = new JSONParser();
		String data;
		try {
			data = (String) parser.parse(new FileReader("src/main/resources/ticketnew_data.json")).toString();
			citiesData = new JSONObject(data);
			data = (String) parser.parse(new FileReader("src/main/resources/bookmyshow_data.json")).toString();
			JSONObject bmsData = new JSONObject(data);
			Iterator itr = citiesData.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				JSONObject cities = citiesData.getJSONObject(key);
				Iterator itr1 = cities.keys();
				while (itr1.hasNext()) {
					String city = (String) itr1.next();
					if (bmsData.has(key)) {
						if (bmsData.getJSONObject(key).has(city)) {
							JSONObject object = new JSONObject();
							object.put("TicketnewUrl", cities.getString(city));
							object.put("BookmyshowUrl", bmsData.getJSONObject(key).getString(city));
							if(normalizeData.has(key))
								normalizeData.getJSONObject(key).put(city, object);
							else
							normalizeData.put(key, new JSONObject().put(city, object));
						} else {
							if (normalizeData.has(key)){
								if(normalizeData.getJSONObject(key).has(city))
								normalizeData.getJSONObject(key).getJSONObject(city).put("TicketnewUrl", cities.getString(city));
							}
							else{
								normalizeData.put(key, new JSONObject().put(city, new JSONObject().put("TicketnewUrl",cities.getString(city))));
							}
						}
					}else{
						if(normalizeData.has(key)){
							normalizeData.getJSONObject(key).put(city, new JSONObject().put("TicketnewUrl",cities.getString(city)));
						}else{
							normalizeData.put(key, new JSONObject().put(city, new JSONObject().put("TicketnewUrl",cities.getString(city))));
						}
					}
				}
			}
			addToFile(normalizeData, "src/main/resources/mymovieplan_data.json", "src/main/resources/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addDataforNormalization(String provider){
		JSONObject citiesData = new JSONObject();
		JSONParser parser = new JSONParser();
		String data;
		try {
			data = (String) parser.parse(new FileReader("src/main/resources/"+provider+"_data.json")).toString();
			citiesData = new JSONObject(data);
			data = (String) parser.parse(new FileReader("src/main/resources/mymovieplan_data.json")).toString();
			JSONObject mymovieplan = new JSONObject(data);
			Iterator itr = citiesData.keys();
			while(itr.hasNext()){
				String mainCity = (String) itr.next();
				JSONObject cities = citiesData.getJSONObject(mainCity);
				Iterator itr1 = cities.keys();
				while(itr1.hasNext()){
					String city = (String) itr1.next();
					if(mymovieplan.has(mainCity)){
						if(mymovieplan.getJSONObject(mainCity).has(city)){
							mymovieplan.getJSONObject(mainCity).getJSONObject(city).put("BookmyshowUrl", cities.get(city));
						}else{
							mymovieplan.getJSONObject(mainCity).put(city, new JSONObject().put("BookmyshowUrl", cities.get(city)));
						}
					}else{
						mymovieplan.put(mainCity, new JSONObject().put(city, new JSONObject().put("BookmyshowUrl", cities.get(city))));
					}
				}
			}
			addToFile(mymovieplan, "src/main/resources/mymovieplan_data.json", "src/main/resources/");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static String doGet(String requestUrl) throws HttpException, IOException, URISyntaxException {
		String line = "";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(requestUrl);
		request.addHeader("Content-Type", "application/json; charset=utf-8");
		request.addHeader("Accept", "application/json; charset=utf-8");
		int retryCount = 0;
		HttpResponse response = null;
		do {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				if (retryCount > 0) {
					Thread.sleep(1000);
				} else {
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response = client.execute(request);

			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			++retryCount;
		} while (retryCount < 5 && ((response.getStatusLine().getStatusCode() == 504)
				|| (response.getStatusLine().getStatusCode() == 429)
				|| (response.getStatusLine().getStatusCode() == 503)
				|| (response.getStatusLine().getStatusCode() == 404)));
		StringBuffer result = null;
		if (response.getStatusLine().getStatusCode() == 200) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			result = new StringBuffer();

			try {
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		return result.toString();
	}
}
