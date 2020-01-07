/*
 * MIT License
 *
 * Copyright (c) 2019 Christophe Munilla
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cmssi.lyson.event.ArrayOpeningEvent;
import cmssi.lyson.event.ArrayOpeningEventWrapper;
import cmssi.lyson.event.IndexedEventWrapper;
import cmssi.lyson.event.KeyValueEventWrapper;
import cmssi.lyson.event.LysonParsingEvent;
import cmssi.lyson.event.ParsingEvent;
import cmssi.lyson.event.ValuableEventWrapper;
import cmssi.lyson.exception.LysonException;
import cmssi.lyson.exception.LysonParsingException;
import cmssi.lyson.handler.LysonParserHandler;
import cmssi.lyson.handler.ValidationHandler;

/**
 * Lazy JSON Parser
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.3
 */
public class LysonParser {

	private static final Logger LOG = Logger.getLogger(LysonParser.class.getName());
	
	private class LysonParserHandlerThreadExecutor extends ThreadPoolExecutor {

		/**
		 * Constructor
		 * 
		 * @param poolSize the thread pool size of the {@link ThreadPoolExecutor}
		 * to be instantiated
		 */
		LysonParserHandlerThreadExecutor(int poolSize) {
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
		List<Future<Boolean>> invokeAll(Collection<LysonParserHandlerCallable> tasks, ParsingEvent event)
	    throws InterruptedException {
	        if (tasks == null)
	            throw new NullPointerException();
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
	
	private final class LysonParserHandlerCallable implements Callable<Boolean> {
		
		private ParsingEvent parsingEvent;
		private LysonParserHandler handler;

		LysonParserHandlerCallable(LysonParserHandler handler){
			this.handler = handler;
		}
		
		void setParsingEvent(ParsingEvent parsingEvent) {
			this.parsingEvent = parsingEvent;
		}
		
		@Override
		public Boolean call() throws Exception {
			if(this.parsingEvent == null) {
				return false;
			}
			return this.handler.handle(this.parsingEvent);
		}
	};
	
	
	private static final int MAX_THREAD = 100;
	private static final int BUFFER_SIZE = 1024*60;
	
	private static final char EOF  = '\0';

    private Reader reader;
    
	private char[] buffer = new char[BUFFER_SIZE];
	private int pos = 0;
	private int line = 0;
	private int column = 0;
	private int length = 0;	
	
    private Deque<ParsingEvent> queue;
    
    /**
     * Constructor
     *
     * @param s the String to be parsed by the
     * LysonParser to be instantiated
     */
    public LysonParser (String s) {
        this(new StringReader(s));
    }
    
   /**
    * Constructor
    *
    * @param input the {@link InputStream} to be parsed by 
    * the LysonParser to be instantiated
    */
   public LysonParser (InputStream input) {
       this(new InputStreamReader(input));
   }
    
    /**
     * Constructor
     *
     * @param reader the used {@link Reader} on the String 
     * or InputStream by the LysonParser to be instantiated
     */
    public LysonParser (Reader reader) {
        this.reader = reader;
        this.queue = new LinkedList<>();
    }
    
    /**
     * Parses the input string (or stream) and propagates parsing 
     * events, including potential error ones to the set of {@link 
     * LysonParserHandler}s passed as parameter, and defining whether 
     * the parsing can be carried on or not
     * 
     * @param handlers the {@link LysonParserHandler}s used for the 
     * parsing
     */
    public void parse(LysonParserHandler... handlers) {
    	try { 
    		int length = handlers==null?0:handlers.length;
    		if(length == 0) {
    			return; 
    		}
    		if(length > MAX_THREAD) {
    			length = MAX_THREAD;
    		}
	    	var callables = new LinkedList<LysonParserHandlerCallable>();
    		Arrays.stream(handlers).forEach(h -> {
    			callables.add(new LysonParserHandlerCallable(h));
    		});
    		LysonParserHandlerThreadExecutor executor = new LysonParserHandlerThreadExecutor(length);
    		while(true) {
	            List<Future<Boolean>> futures = executor.invokeAll(callables, read());
	            int offset = 0;
	            for(int pos = 0; pos < futures.size(); pos++) {
	            	try {
						if(futures.get(pos).get(1,TimeUnit.SECONDS).booleanValue()) {
							continue;
						}
					} catch (ExecutionException | TimeoutException e) {
						if(LOG.isLoggable(Level.SEVERE)) {
							LOG.log(Level.SEVERE,e.getMessage(),e);
						}
					}
					callables.remove(pos-offset);
					offset+=1;
	            }
	            if(callables.isEmpty()) {
	            	break;
	            }
    		}
        } catch (LysonParsingException e) {        	
        	Arrays.stream(handlers).forEach(h -> {
    			h.handle(e);
    		});
    		if(LOG.isLoggable(Level.SEVERE)) {
    			LOG.log(Level.SEVERE,e.getMessage(),e);
    		}
        } catch (InterruptedException e) {
    		if(LOG.isLoggable(Level.SEVERE)) {
    			LOG.log(Level.SEVERE,e.getMessage(),e);
    		}
        	Thread.currentThread().interrupt();
		}
    }
    
    /**
     * Returns true if the chars sequence read by this 
     * LysonParser describes a valid JSON Object or Array
     * 
     * @return 
     * <ul>
     * 	<li>true if the specified String describes a valid
     * 		JSON Object or Array </li>
     * 	<li>false otherwise</li>
     * </ul>
     */
    public boolean valid() {
        ValidationHandler handler = new ValidationHandler();
        parse(handler);
        return handler.valid();
    }
    
    /**
     * @return
     * @throws LysonException
     */
    private ParsingEvent read() {
        char c = nextChar();
        if(c == 0) {
        	if(!this.queue.isEmpty()) {
        		ParsingEvent lastToken = this.queue.pop();
        		if (lastToken.getType() == ParsingEvent.JSON_ARRAY_OPENING ) {
        			throw new LysonParsingException("Json array closing expected",
        				line,column);
        		} else if(lastToken.getType() == ParsingEvent.JSON_OBJECT_OPENING ) {
        			throw new LysonParsingException("Json object closing expected",
        				line,column);
        	    }
        	}
        	return null;
        }
        if (this.queue.isEmpty()) {
            ParsingEvent co = checkOpening(c, "/", null);
            if (co != null) {
                return co;
            }
            return null;
        }  
        ParsingEvent lastToken = this.queue.pop();        
        String path = lastToken.getPath();
        int index = 0;
        
        if(lastToken instanceof ArrayOpeningEvent) {
        	index =  ((ArrayOpeningEvent)lastToken).getInnerIndex();
        }        
        if (lastToken.getType() == ParsingEvent.JSON_ARRAY_OPENING || 
        	 lastToken.getType() == ParsingEvent.JSON_OBJECT_OPENING ) {
            this.queue.push(lastToken);
        }
        switch (lastToken.getType()) {
            case ParsingEvent.JSON_OBJECT_OPENING:
            	ParsingEvent cc = checkClosing(c, path);
                if (cc != null) {
                    return cc;
                }
            	return parseInJsonObject(c, path);               
            case ParsingEvent.JSON_ARRAY_OPENING:
                cc = checkClosing(c, path);
                if (cc != null) {
                    return cc;
                }
                index+=1;
                ((ArrayOpeningEvent)lastToken).withInnerIndex(index);
                return parseInJsonArray(index, c, path);
            case ParsingEvent.JSON_OBJECT_CLOSING:
            case ParsingEvent.JSON_OBJECT_ITEM:
            case ParsingEvent.JSON_ARRAY_CLOSING:
            case ParsingEvent.JSON_ARRAY_ITEM:
            default:
                break;
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * Last register event was a JSON object opening one - 
     * Continue to parse knowing that
     * 
     * @param c the last read character
     * @param path the String current path
     * 
     * @return the next {link ParsinEvent}
     */
    private ParsingEvent parseInJsonObject(char c, String path) {    	
        String key = null;
        Object value = null;
        switch (c) {
            case '\'':
            case '"':
                key = readString(c);
                break;
            default:
                throw new LysonParsingException("Expected String delimiter", line, column);
        }                
        c = nextChar();
        if (c == ':'){
            moveOn();
        }else if (c == '='){
            moveOn();
            if (currentChar() == '>'){
                moveOn();                    	
            } else {
                throw new LysonParsingException("Expected a ':' or '=>' after a key", line, column);
            }
        } else {
            throw new LysonParsingException("Expected a ':' or '=>' after a key", line, column);
        }
        c = nextChar();
        switch (c){
            case '"':
            case '\'':
                value = readString(c);
                break;
            default:
                break;
        }
        if (value == null){
            ParsingEvent co = checkOpening(c, path , key );
            if (co != null) {
                return co;
            }
            StringBuilder sb = new StringBuilder();
            while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                sb.append(c);
                moveOn();
                c = currentChar();
            }
            String s = sb.toString().trim();
            if (s.equals("")){
                throw new LysonParsingException("Missing value", line, column);
            }
            value = readObject(s);
        }
        c = nextChar();
        switch (c) {
            case ';':
            case ',':
                moveOn();
                break;
            default:
                break;
        }
        ParsingEvent ev = new LysonParsingEvent(ParsingEvent.JSON_OBJECT_ITEM
            ).withPath(new StringBuilder().append(path).append(path.endsWith("/")
            	?"":"/").append(key).toString());    
        return new KeyValueEventWrapper(ev).withValue(value).withKey(key);  
    	
    }
    
    /*
     * (non-Javadoc)
     * 
     * Last register event was a JSON array opening one - 
     * Continue to parse knowing that
     * 
     * @param index the current int index in the parsed array
     * @param c the last read character
     * @param path the String current path
     * 
     * @return the next {link ParsinEvent}
     */
    private ParsingEvent parseInJsonArray(int index, char c, String path) {
    	Object value = null;
        switch (c) {
            case ';':
            case ',':
            	moveOn();
            	ParsingEvent ev = new LysonParsingEvent(ParsingEvent.JSON_ARRAY_ITEM
	                ).withPath(new StringBuilder().append(path).append(path.endsWith("/")
	                    ?"":"/").append("[").append(index).append("]").toString());
            	return new ValuableEventWrapper(new IndexedEventWrapper(ev
	                	).withIndex(index));
            case '"':
            case '\'':
                value = readString(c);
                break;
            default:
                break;
        }
    	if (value == null) {
            ParsingEvent co = checkOpening(c, path, index);
            if (co != null) {
                return co;
            }
            StringBuilder sb = new StringBuilder();
            while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                sb.append(c);
                moveOn();
                c = currentChar();
            }
            String s = sb.toString().trim();
            if (s.equals("")) {
                throw new LysonParsingException("Missing value", line, column);
            }
            value = readObject(s);
        }
        c = nextChar();
        switch (c) {
            case ';':
            case ',':
                moveOn();
                break;
            default:
            	break;
        }
        ParsingEvent ev = new LysonParsingEvent(ParsingEvent.JSON_ARRAY_ITEM).withPath(
        	new StringBuilder().append(path).append(path.endsWith("/")?"":"/").append("["
        		).append(index).append("]").toString());                
    	return new ValuableEventWrapper(new IndexedEventWrapper(ev
            	).withIndex(index)).withValue(value);
    }
    
    /**
     * @throws LysonException
     */
    private void checkClosingArray() {
        if (this.queue.isEmpty()) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
        ParsingEvent previousToken = this.queue.pop();
        if (previousToken.getType()!=ParsingEvent.JSON_ARRAY_OPENING) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
    }

    /**
     * @throws LysonException
     */
    private void checkClosingObject() {
        if (this.queue.isEmpty()) {
            throw new LysonParsingException("Unexpected object closing", line,  column);
        }
        ParsingEvent previousToken = this.queue.pop();
        if (previousToken.getType()!= ParsingEvent.JSON_OBJECT_OPENING) {
            throw new LysonParsingException("Unexpected object closing", line, column);
        }
    }

    /**
     * @param c
     * @param path
     * @return
     * @throws LysonException
     */
    private ParsingEvent checkClosing(char c, String path) {
        LysonParsingEvent cc = null;
        switch (c) {
            case 0:
                throw new LysonParsingException("Unexpected end of stream", line, column);
            case '}':
                checkClosingObject();
                cc = new LysonParsingEvent(ParsingEvent.JSON_OBJECT_CLOSING);
                break;
            case ']':
                checkClosingArray();
                cc = new LysonParsingEvent(ParsingEvent.JSON_ARRAY_CLOSING);
                break;
            default:
                return cc;
        }
    	moveOn();
        c = nextChar();
        switch (c) {
            case ';':
            case ',':
                moveOn();
                break;
            default:
                break;
        }
        return cc.withPath(path);
    }

    /**
     * @param c
     * @param path
     * @param key
     * @return
     */
    private ParsingEvent checkOpening(char c, String path, Object key) {
        ParsingEvent o = null;
        int tokenType = -1;                      
        switch (c) {
            case '{':
            	tokenType = ParsingEvent.JSON_OBJECT_OPENING;
                break;
            case '[':
            	tokenType = ParsingEvent.JSON_ARRAY_OPENING;
                break;
            default:
            	return null;
        }
    	if(key != null) {
	    	if(Number.class.isAssignableFrom(key.getClass())){
	    		ParsingEvent ev = new LysonParsingEvent(tokenType
	                ).withPath(new StringBuilder().append(path
	                ).append(path.endsWith("/")?"":"/").append("["
	                ).append((Number)key).append("]").toString());
	    		o = new IndexedEventWrapper(ev).withIndex(((Number)key).intValue());
	    	} else {	    		
	    		ParsingEvent ev = new LysonParsingEvent(tokenType
		            ).withPath(new StringBuilder().append(path
		            ).append(path.endsWith("/")?"":"/").append(key
		            ).toString());
	    		o = new KeyValueEventWrapper(ev).withKey(String.valueOf(key));
	    	}
	    }  else {
	    	o = new LysonParsingEvent(tokenType).withPath(path);
	    }
    	if(tokenType == ParsingEvent.JSON_ARRAY_OPENING) {
    		o = new ArrayOpeningEventWrapper(o).withInnerIndex(-1);
    	}
    	this.queue.push(o);
        moveOn();
        return o;
    }

    /**
     * @return
     * @throws LysonException
     */
    private char currentChar() {
    	if(pos >= length) {
    		length = -1;
            try {
                length = this.reader.read(buffer, 0, BUFFER_SIZE);
                pos = 0;
                if(length == -1) {
                	this.reader.close();
                	return EOF;
                }
            } catch (IOException exc) {
            	LOG.log(Level.FINE, exc.getMessage(), exc);
            	return 0;
            }
    	}
        char c = buffer[pos];
        return c;
    }

    /* (non-Javadoc)
     * 
     * @return
     * @throws LysonException
     */
    /* (non-Javadoc)
     * 
     * Makes the inner buffer cursor move  
     */
    private char nextChar() {
        for ( ; ; ) {
            char c = currentChar();
            if (c == 0 || c > ' ') {
                return c;
            }
            if(c == '\n' || c == '\r') {
            	line+=1;
            	column = 0;
            }
            moveOn();
        }
    }

    /* (non-Javadoc)
     * 
     * Makes the inner buffer cursor move  
     */
    private void moveOn(){
        pos+=1;
        column+=1;
    }

    /*
     * (non-Javadoc)
     * 
     * Reads the String between the quoted char passed as parameter
     * 
     * @param q the quote char 
     * 
     * @return the String between the specified quoted char
     * 
     * @throws LysonException if an error occurred while reading the string 
     */
    private String readString(char q) throws LysonException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            moveOn();
            c = currentChar(); 
            switch (c) {
            	case EOF:
                case '\n':
                case '\r':
                throw new LysonParsingException("Unterminated string", line, column);                	
                case '\\':
                    moveOn();
                    c = currentChar();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                        	char[] unicode = new char[4];
                            int offset = 0;
                            while (offset < 4){
                               moveOn();
                               if((unicode[offset] = currentChar())== EOF) {
                            	   break;
                               }
                               offset+=1;
                            }
                            if (offset < 4) {
                              throw new LysonParsingException("Substring bounds error", line, column);
                            }
                            sb.append((char) Integer.parseInt(new String(unicode), 16));
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw new LysonParsingException("Illegal escape", line, column);
                    }
                    break;
                default:
                    if (c == q) {
                    	moveOn();
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }
    
    /* (non-Javadoc)
     * 
     * Tries to identify the boolean or numeric value the String argument
     * represents, to convert it into the appropriate type and to return
     * the converted value
     *  
     * @param s the String formated object to be converted
     * 
     * @return the appropriate boolean, numeric or string object, according 
     * the s string argument
     */
    private Object readObject(String s) {    	
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return null;
        }
        try {  	
     		char b = s.charAt(0);                
     		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {                
                Number num = null;      
                if(s.indexOf('.') < 0) {
                	if (b == '0') {
                    	try {
                            if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {                            
                                    num = Integer.parseInt(s.substring(2), 16);
                            } else {
                                    num = Integer.parseInt(s, 8);
                            }
                    	} catch(Exception ex ){
                 			LOG.log(Level.FINEST, ex.getMessage(), ex);
                 		}
                    } else {
	                	num = new BigInteger(s);
	                	if(((BigInteger)num).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {                    	
	                		Long myLong = Long.valueOf(s);
	                        if (myLong.longValue() == myLong.intValue()) {
	                            num = myLong.intValue();
	                        } else {
	                            num = myLong;
	                        }
	                	}
                    }
                } else {
                    num = new BigDecimal(s);
                    if((((BigDecimal)num).compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
                    	&& ((BigDecimal)num).compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0)
                    	||  ((BigDecimal)num).intValue() == 0) {
	                    num = Double.valueOf(s);
                    }
                }
                return num;
            }
 		} catch(Exception ex) {
 			LOG.log(Level.FINEST, ex.getMessage(), ex);
 		}
        return s;
    }    
}