package com.compare.movie.MovieScrapper.Helpers;

import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.json.JSONObject;

import com.compare.movie.MovieScrapper.App;

public class ElasticUtilities {

	public static void addMovie(String index, String type, String id, JSONObject obj) {
		String updatedProduct = null;
		IndexRequest indexRequest = new IndexRequest(index, type, id.replaceAll(" ", "_")).source(obj.toString());
		UpdateRequest updateRequest = new UpdateRequest(index, type, id.replaceAll(" ", "_")).doc(obj.toString())
				.upsert(indexRequest);
		try {
			updatedProduct = App.getClient().update(updateRequest).get().toString();
			System.out.println("Product Id :" + obj.getString("movie_name"));
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (ExecutionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

}
