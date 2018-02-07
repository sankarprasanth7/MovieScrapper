package com.compare.movie.MovieScrapper.Theatres;

import java.io.FileReader;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.compare.movie.MovieScrapper.Utilities;

public class TicketnewTheatres {
	public static void main(String args[]) {
		JSONObject theaters = new JSONObject();
		JSONParser parser = new JSONParser();
		String data;
		try {
			data = (String) parser.parse(new FileReader("src/main/resources/ticketnew_data.json")).toString();
			JSONObject citiesData = new JSONObject(data);
			Iterator itr = citiesData.keys();
			while (itr.hasNext()) {
				String cityId = (String) itr.next();
				JSONObject obj = citiesData.getJSONObject(cityId);
				String cinemasUrl = "http://www.ticketnew.com/online-advance-booking/Theatres/C/"
						+ obj.getString("city_name");
				Document doc = Utilities.getDataInPage(cinemasUrl);
				Elements cinemas = doc.select("#theatres > div");
				for (Element e : cinemas) {
					JSONObject theatre = new JSONObject();
					String theatreUrl = e.select(".tn-entity-book > a").attr("href");
					String theatreName = e.select(".tn-entity-details > h5").text();
					theatre.put("theatre_url", theatreUrl);
					theatre.put("theatre_name", theatreName);
					theatre.put("city_key", obj.getString("city_key"));
					theatre.put("city_id", cityId);
					String id = UUID.randomUUID().toString();
					theatre.put("_id", id);
					theaters.put(id, theatre);
					System.out.println(theatreUrl);
				}
			}
			System.out.println(theaters);
			Utilities.addToFile(theaters, "src/main/resources/ticketnew_theatres.json", "src/main/resources/");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
