package com.compare.movie.MovieScrapper;

import java.util.concurrent.BlockingQueue;

import com.compare.movie.MovieScrapper.Helpers.CustomThreadPoolExecutor;

public class MovieScrapperApp {
	public static Boolean iscomplete = false;
	public static CustomThreadPoolExecutor ex = null;
	public static BlockingQueue<Runnable> blockingQueue = null;
}
