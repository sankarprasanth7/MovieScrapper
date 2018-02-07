package com.compare.movie.MovieScrapper.trending;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.compare.movie.MovieScrapper.App;
import com.compare.movie.MovieScrapper.Utilities;
import com.compare.movie.MovieScrapper.Helpers.ElasticUtilities;

public class TrendingDataProcessor implements Runnable {
	private String url;
	private String city;
	private String id;
	private Document doc;

	public TrendingDataProcessor(String id, String url, String city, Document doc) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.url = url;
		this.city = city;
		this.doc = doc;
	}

	@Override
	public void run() {
		System.out.println("Job ID : " + this.id + " performed by " + Thread.currentThread().getName() + " URL :" + url
				+ " city :" + city);
		try {
			Thread.sleep(1000);
			//if (LeachUtilities.checkProduct(url))
			getTrendingMoviesBMS(city, doc, url);
			getUpCommingMoviesBMS(city,doc,url);
			// System.out.println("Response :" + response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getName() {
		// TODO Auto-generated method stub
		return "Id: " + id + "URL:" + this.url;
	}
	public static void getTrendingMoviesBMS(String city, Document doc, String url) {
		String updatedProduct = null;
		JSONObject trending = new JSONObject();
		Elements nowShowing = doc
				.select(".carousel-now-showing > .viewport > .banner-container > .movie-card >.card-container");
		for (Element e : nowShowing) {
			JSONObject obj = new JSONObject();
			String movieName = e.select(".detail.detail-scroll > div > a.__movie-name").text();
			String movieKey;
			if(movieName.contains("(")){
					movieKey = movieName.substring(0, movieName.indexOf("(")).replaceAll("[^ _0-9a-zA-Z]+", "").replaceAll("\\s+$", "");
			}else{
				movieKey = movieName.replaceAll("[^ _0-9a-zA-Z]+", "").replaceAll("\\s+$", "").replaceAll(" ", "_").toLowerCase();
			}
			obj.put("movie_name", movieName);
			obj.put("movie_key", movieKey);
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
			if(!App.movies.contains(movieName)){
				App.movies.add(movieName);
				ElasticUtilities.addMovie("movies", "movieInfo", modifiedObject.getString("movie_name"), modifiedObject);
			}
			String bookingUrl = e.select(".book-button > a").attr("href");
			if(bookingUrl.length() > 0){
				trending.put("show_times", "https://in.bookmyshow.com"+bookingUrl);
			}else{
				trending.put("show_times", "https://in.bookmyshow.com"+e.select("a.more-showtimes").attr("href"));
			}
			trending.put("city", city);
			trending.put("poster_image", modifiedObject.getString("poster_image"));
			trending.put("movie_key", modifiedObject.getString("movie_key"));
			trending.put("movie_name", modifiedObject.getString("movie_name"));
			trending.put("percentage", modifiedObject.get("percentage"));
			trending.put("user_rating", modifiedObject.getString("user_rating"));
			trending.put("release_date", modifiedObject.getString("release_date"));
			trending.put("languages", modifiedObject.getString("languages"));
			trending.put("generes", modifiedObject.get("generes"));
			System.out.println(trending);
			ElasticUtilities.addMovie("trending", city, movieKey, trending);
		}
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
			String movieName = e.select(".detail.detail-scroll > div > a.__movie-name").text();
			String movieKey;
			if(movieName.contains("(")){
					movieKey = movieName.substring(0, movieName.indexOf("(")).replaceAll("[^ _0-9a-zA-Z]+", "").replaceAll("\\s+$", "");
			}else{
				movieKey = movieName.replaceAll("[^ _0-9a-zA-Z]+", "").replaceAll("\\s+$", "").replaceAll(" ", "_").toLowerCase();
			}
			obj.put("movie_name", movieName);
			obj.put("movie_key", movieKey);
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