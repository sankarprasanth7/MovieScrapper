package com.compare.movie.MovieScrapper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.compare.movie.MovieScrapper.trending.Trending;

/**
 * Hello world!
 *
 */
public class App 
{
	static Client transportClient = null;
	public static ArrayList<String> movies = new ArrayList<String>();
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
		Settings settings = Settings.builder()
		        .put("cluster.name", "movie-cluster").put("transport.tcp.port", "9300").build();
		try {
			transportClient = new PreBuiltTransportClient(settings)
			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("35.196.31.116"), 9300))
			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Trending t = new Trending();
		Trending.getTrendingMovies();
    }
	public static Client getClient() {
		return transportClient;
	}
	public static void init() {

	}
}
