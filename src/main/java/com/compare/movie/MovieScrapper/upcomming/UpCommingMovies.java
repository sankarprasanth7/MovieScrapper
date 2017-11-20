package com.compare.movie.MovieScrapper.upcomming;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.compare.movie.MovieScrapper.Utilities;
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
		Document doc1 = Jsoup.parse(html.substring(0, html.length()-1));
//		System.out.println(doc1);
		Elements e = doc.select(".banner-container");
		System.out.println(e);
		
	}
}
