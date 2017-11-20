package com.compare.movie.MovieScrapper.trending;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;

import com.compare.movie.MovieScrapper.Helpers.CustomThreadPoolExecutor;

import com.compare.movie.MovieScrapper.MovieScrapperApp;

public class TrendingExecutor extends MovieScrapperApp {
	public TrendingExecutor()
	{
		blockingQueue = new ArrayBlockingQueue<Runnable>(6);
		ex = new CustomThreadPoolExecutor(2, 4, 1000000, TimeUnit.MILLISECONDS, MovieScrapperApp.blockingQueue);
		ex.setRejectedExecutionHandler(new RejectedExecutionHandler(){
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				System.out.println("Lazada Api Executor Thread Pool Rejected : " + ((TrendingDataProcessor) r).getName());
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				System.out.println("Lets add another time : " + ((TrendingDataProcessor)r).getName());
				MovieScrapperApp.ex.execute(r);
			}
		});
	}
			
		static int apiCalls = 0;
		public static void submitForProcessing(String url, String city,Document doc) 
		{
			ex.execute(new TrendingDataProcessor(++apiCalls + "", url, city,doc));
		}

}
