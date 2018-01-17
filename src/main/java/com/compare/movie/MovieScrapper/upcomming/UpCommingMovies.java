package com.compare.movie.MovieScrapper.upcomming;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.compare.movie.MovieScrapper.App;
import com.compare.movie.MovieScrapper.Utilities;
import com.compare.movie.MovieScrapper.Helpers.ElasticUtilities;
import com.compare.movie.MovieScrapper.trending.Trending;

public class UpCommingMovies {
	public static void main(String args[]){
//		Utilities.getlocations("ticketnew");
//		Utilities.getlocations("bookmyshow");
//		Utilities.normalize();
//		Utilities.addDataforNormalization("bookmyshow");
		Trending.getTrendingMovies();
	}
	public void getUpCommingMovies(){
		
	}
	public static void getUpCommingMoviesBMS(String city, Document doc, String url){
		String html = doc.select("#coming-soon-carousel > script").html().replaceAll("var comingSoonCardsDOM = ", "").replace("\\", "");
		JSONObject upcomming = new JSONObject();
//		System.out.println(html);
		Document doc1 = Jsoup.parse(html.substring(1, html.length()-3));
		Elements commingSoon = doc
				.select(".banner-container > .movie-card >.card-container");
		for (Element e : commingSoon) {
			JSONObject obj = new JSONObject();
			String movieName = e.select(".detail.detail-scroll > div > a.__movie-name").text().toLowerCase()
					.replaceAll(" ", "_");
			obj.put("movie_name", movieName);
			String infoUrl = "https://in.bookmyshow.com"
					+ e.select(".detail.detail-scroll > div > a.__movie-name").attr("href");
			obj.put("info_url", infoUrl);
			String languages = e.select(".detail.detail-scroll > div > ul > li.__language").text();
			obj.put("languages", languages);
			obj.put("event_group_code",
					e.select(".poster-container > .stats-wrapper > .stats > div").attr("data-event-group"));
			Elements generesEle = e.select(".detail.detail-scroll > div.genre-list > a > div");
			JSONArray generes = new JSONArray();
			for (Element e1 : generesEle) {
				generes.put(e1.text());
			}
			obj.put("city", city);
			obj.put("generes", generes);
			Trending t = new Trending();
			JSONObject modifiedObject = t.getMovieInfo(infoUrl, obj);
			if (!App.movies.contains(movieName)) {
				App.movies.add(movieName);
				ElasticUtilities.addMovie("movies", "movieInfo", modifiedObject.getString("movie_name"),
						modifiedObject);
			}
			upcomming.put("city", city);
			upcomming.put("poster_image", modifiedObject.getString("poster_image"));
			upcomming.put("movie_name", modifiedObject.getString("movie_name"));
			upcomming.put("percentage", modifiedObject.get("percentage"));
			upcomming.put("user_rating", modifiedObject.getString("user_rating"));
			upcomming.put("release_date", modifiedObject.getString("release_date"));
			upcomming.put("languages", modifiedObject.getString("languages"));
			upcomming.put("generes", modifiedObject.get("generes"));
			System.out.println(upcomming);
			ElasticUtilities.addMovie("upcomming", city, modifiedObject.getString("movie_name"), upcomming);
		}
	}
}
