package com.compare.movie.MovieScrapper.Helpers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
	public static CountLatch numRunningTasks = new CountLatch(0);
	public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> blockingQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, blockingQueue);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		numRunningTasks.increment();
		System.out.println("Starting Data Processor"+t.getName());
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		 numRunningTasks.decrement();
		 System.out.println("**************num Of Running Tasks in ThreadPool:::::"+numRunningTasks.toInt());
		if (t != null) {
			System.out.println("Perform exception handler logic"+t.getMessage());
		}
		System.out.println("Success fully finished :: Active Count"+getActiveCount() +" ::Task Count :"+ getTaskCount()+"::Completed Count:"+getCompletedTaskCount());
	}
}

