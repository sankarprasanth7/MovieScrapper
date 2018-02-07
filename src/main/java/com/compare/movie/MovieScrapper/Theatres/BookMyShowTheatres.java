package com.compare.movie.MovieScrapper.Theatres;

import java.io.FileReader;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

public class BookMyShowTheatres {
	public static void main(String args[]){
		JSONObject theaters = new JSONObject();
		JSONParser parser = new JSONParser();
		String data;
		try {
			data = (String) parser.parse(new FileReader("src/main/resources/bookmyshow_theatres.json")).toString();
			JSONObject citiesData = new JSONObject(data);
			Iterator itr = citiesData.keys();
			while (itr.hasNext()) {
				String cityId = (String) itr.next();
				JSONObject obj = citiesData.getJSONObject(cityId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
