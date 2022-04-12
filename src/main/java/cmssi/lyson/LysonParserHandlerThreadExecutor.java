/*
 * MIT License
 *
 * Copyright (c) 2019 - 2022  Christophe Munilla
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cmssi.lyson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cmssi.lyson.event.ParsingEvent;


/**
 * Extended {@link ThreadPoolExecutor} allowing to define a {@link ParsingEvent} in each 
 * {@link LysonParserHandlerCallable} of a collection before calling them all
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public final class LysonParserHandlerThreadExecutor extends ThreadPoolExecutor {

	/**
	 * Constructor
	 * 
	 * @param poolSize the thread pool size of the {@link ThreadPoolExecutor}
	 * to be instantiated
	 */
	public LysonParserHandlerThreadExecutor(int poolSize) {
		super(poolSize, poolSize,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
	}
	
	/**
	 * Executes the given tasks parameterized with the specified {@link ParsingEvent}, 
	 * returning a list of Futures holding their status and results when all complete. 
	 * Future.isDone is true for each element of the returned list. Note that a completed 
	 * task could have terminated either normally or by throwing an exception. The results 
	 * of this method are undefined if the given collection is modified while this operation is in progress.
	 *
	 * @param tasks the collection of tasks
	 * @param event the {@link ParsingEvent} parameterizing the tasks
	 * 
	 * @return a list of Futures representing the tasks, in the same sequential order as 
	 * produced by the iterator for the given task list, each of which has completed
	 * 
	 * @throws InterruptedException - if interrupted while waiting, in which case unfinished 
	 * tasks are cancelled
	 */
	public List<Future<Boolean>> invokeAll(Collection<LysonParserHandlerCallable> tasks, ParsingEvent event)
    throws InterruptedException {
        ArrayList<Future<Boolean>> futures = new ArrayList<>(tasks.size());
        try {
            for (LysonParserHandlerCallable t : tasks) {
            	t.setParsingEvent(event);
                RunnableFuture<Boolean> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<Boolean> f = futures.get(i);
                if (!f.isDone()) {
                    try { 
                    	f.get(); 
                    }
                    catch (CancellationException | ExecutionException ignore) {}
                }
            }
            return futures;
        } catch (Throwable t) {  
        	for (int size = futures.size(),j=0; j < size; j++) {
        		futures.get(j).cancel(true);
        	}
            throw t;
        }
    }
}