package com.compare.movie.MovieScrapper.trending;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.compare.movie.MovieScrapper.App;
import com.compare.movie.MovieScrapper.Utilities;
import com.compare.movie.MovieScrapper.Helpers.ElasticUtilities;
import com.compare.movie.MovieScrapper.upcomming.UpCommingMovies;

public class Trending {
	public static TrendingExecutor executor;

	public Trending() {
		super();
		App.init();
		executor = new TrendingExecutor();
	}

	public static void getTrendingMovies() {
		JSONParser parser = new JSONParser();
		String data;
		try {
			data = (String) parser.parse(new FileReader("src/main/resources/bookmyshow_data.json")).toString();
			JSONObject citiesData = new JSONObject(data);
			Iterator itr = citiesData.keys();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				JSONObject city = citiesData.getJSONObject(key);
					Document doc = Utilities.getDataInPage(city.getString("city_url"));
					executor.submitForProcessing(city.getString("city_url"), city.getString("city_key"), doc);
					// UpCommingMovies.getUpCommingMoviesBMS(city, doc,
					// obj.getString("BookmyshowUrl"));
			}
			System.out.println("getting data for movies is completed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getTrendingMoviesBMS(String city, Document doc, String url) {
		String updatedProduct = null;
		JSONObject trending = new JSONObject();
		Elements nowShowing = doc
				.select(".carousel-now-showing > .viewport > .banner-container > .movie-card >.card-container");
		for (Element e : nowShowing) {
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
			String bookingUrl = e.select(".book-button > a").attr("href");
			if (bookingUrl.length() > 0) {
				trending.put("show_times", "https://in.bookmyshow.com" + bookingUrl);
			} else {
				trending.put("show_times", "https://in.bookmyshow.com" + e.select("a.more-showtimes").attr("href"));
			}
			trending.put("city", city);
			trending.put("poster_image", modifiedObject.getString("poster_image"));
			trending.put("movie_name", modifiedObject.getString("movie_name"));
			trending.put("percentage", modifiedObject.get("percentage"));
			trending.put("user_rating", modifiedObject.getString("user_rating"));
			trending.put("release_date", modifiedObject.getString("release_date"));
			trending.put("languages", modifiedObject.getString("languages"));
			trending.put("generes", modifiedObject.get("generes"));
			System.out.println(trending);
			ElasticUtilities.addMovie("trending", city, modifiedObject.getString("movie_name"), trending);
		}
	}

	public JSONObject getMovieInfo(String url, JSONObject obj) {
		JSONArray cast = new JSONArray();
		JSONArray crew = new JSONArray();
		JSONArray criticReviews = new JSONArray();
		obj.remove("city");
		Document doc = Utilities.getDataInPage(url);
		obj.put("votes", doc.select("div.heart-rating > .__votes").text());
		obj.put("release_date", doc.select("span.__release-date").text());
		String percentage = doc.select("div.heart-rating > .__percentage").text().replaceAll("%", "");
		if (percentage.length() > 0)
			obj.put("percentage", Integer.parseInt(percentage));
		else
			obj.put("percentage", 75);
		obj.put("critic_rating", doc.select(".critic-rating > .__rating > ul").attr("data-value"));
		obj.put("user_rating", doc.select(".user-rating > .__rating > ul").attr("data-value"));
		obj.put("poster_image", doc.select(".poster-container > .poster > meta").attr("content"));
		obj.put("synopsis", doc.select(".synopsis").text());
		obj.put("trailer", doc.select("meta[itemprop=target]").attr("content").replace("watch?v=", "embed/"));
		obj.put("banner",
				StringUtils.substringBetween(doc.select("#imgBanner").attr("style"), "(", ")").replaceAll("'", ""));
		Elements castEle = doc.select("#cast > .cast-members > .showcase-carousel > span");
		Elements crewEle = doc.select("#crew > .cast-members > .showcase-carousel > span");
		for (Element e : castEle) {
			JSONObject object = new JSONObject();
			object.put("actor", e.select(".__cast-member").attr("content"));
			object.put("image", "http:" + e.select(".__cast-image > img").attr("data-lazy"));
			object.put("role", e.select(".__role").text());
			object.put("character_name", e.select(".__characterName").text());
			cast.put(object);
		}
		for (Element e : crewEle) {
			JSONObject object = new JSONObject();
			object.put("actor", e.select(".__cast-member").attr("content"));
			object.put("image", "http:" + e.select(".__cast-image > img").attr("data-lazy"));
			object.put("role", e.select(".__role").text());
			object.put("character_name", e.select(".__characterName").text());
			crew.put(object);
		}
		obj.put("cast", cast);
		obj.put("crew", crew);
		try {
			String reviews = Utilities
					.doGet("https://in.bookmyshow.com/serv/getData.bms?cmd=GETREVIEWSGROUP&eventGroupCode="
							+ obj.getString("event_group_code") + "&type=UR&pageNum=1&perPage=9&sort=POPULAR");
			if (reviews.startsWith("{")) {
				JSONObject reviewsObj = new JSONObject(reviews);
				// System.out.println(reviewsObj);
				if (reviewsObj.get("data") instanceof JSONObject) {
					for (int i = 0; i < reviewsObj.getJSONObject("data").getJSONArray("Reviews").length(); i++) {
						reviewsObj.getJSONObject("data").getJSONArray("Reviews").getJSONObject(i).remove("EventCode");
						reviewsObj.getJSONObject("data").getJSONArray("Reviews").getJSONObject(i).remove("Verified");
						reviewsObj.getJSONObject("data").getJSONArray("Reviews").getJSONObject(i).remove("ReviewId");
					}
					obj.put("user_reviews", reviewsObj.getJSONObject("data").getJSONArray("Reviews"));
				}
			}
			Elements critics = doc.select("#mv-critic > .mv-synopsis-review > div");
			for (Element e : critics) {
				JSONObject critic = new JSONObject();
				critic.put("reviewer_name", e.select(".__reviewer-name-rate > .__reviewer-left > #critic_").text());
				critic.put("reviewer_text", e.select(".__reviewer-text > span").text());
				criticReviews.put(critic);
			}
			obj.put("critic_reviews", criticReviews);
		} catch (HttpException | IOException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return obj;
	}
}
