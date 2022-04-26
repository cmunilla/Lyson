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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import cmssi.lyson.handler.validation.ValidationHandler;

/**
 * Lazy JSON Parser
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class LysonParser {

	private static final Logger LOG = Logger.getLogger(LysonParser.class.getName());
	
	/**
     * Assumes that the String passed as parameter represents a numeric
     * value. Try to convert it to a Number instance and returns it
     * 
     * @param s the String to convert into a Number instance
     * 
     * @return a new Number instance based on the specified numeric value
     * represented as a String 
     */
	public static Number numberFromString(String s) {
    	Number num = null;
    	if(s.indexOf('.') < 0) {
        	if (s.charAt(0) == '0') {
            	try {
                    if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))                             
                        num = Integer.parseInt(s.substring(2), 16);
                    else 
                        num = Integer.parseInt(s, 8);                    
                    num = Integer.valueOf((int)num);
            	} catch(Exception ex ){
         			LOG.log(Level.FINEST, ex.getMessage(), ex);
         		}
            } else {
            	num = new BigInteger(s);
            	if(((BigInteger)num).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {                    	
            		Long myLong = Long.valueOf(s);
                    if (myLong.longValue() == myLong.intValue()) {
                        num = Integer.valueOf(myLong.intValue());
                    } else {
                        num = myLong;
                    }
            	}
            }
        } else {
            num = new BigDecimal(s);
            if(((BigDecimal)num).compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) <= 0
            	&& ((BigDecimal)num).compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) >= 0) {
                num = ((BigDecimal)num).doubleValue();
            }
        }
        return num;
    }
	
	public static final int MAX_THREAD = 100;
	public static final int BUFFER_SIZE = 1024*60;
	
	public static final char EOF  = '\0';

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
    	
    	int length = handlers==null?0:handlers.length;
    	if(length == 0) 
    		return;
    	if(length > MAX_THREAD) 
    		length = MAX_THREAD;
    	
    	LysonParserHandlerThreadExecutor executor = new LysonParserHandlerThreadExecutor(length);
    	
    	try { 
    		LinkedList<LysonParserHandlerCallable> callables = new LinkedList<>();
    		Arrays.stream(handlers).forEach(h -> {
    			callables.add(new LysonParserHandlerCallable(h));
    		});
    		while(true) {
	            List<Future<Boolean>> futures = executor.invokeAll(callables, read());
	            int offset = 0;
	            for(int pos = 0; pos < futures.size(); pos++) {
	            	try {
						if(futures.get(pos).get().booleanValue()) 
							continue;
					} catch (ExecutionException e) {
						if(LOG.isLoggable(Level.SEVERE)) 
							LOG.log(Level.SEVERE,e.getMessage(),e);
					}
					callables.remove(pos-offset);
					offset+=1;
	            }
	            if(callables.isEmpty())
	            	break;
    		}
        } catch (LysonParsingException e) {        	
        	Arrays.stream(handlers).forEach(h -> {
    			h.handle(e);
    		});
    		if(LOG.isLoggable(Level.SEVERE)) 
    			LOG.log(Level.SEVERE,e.getMessage(),e);    		
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		} finally {
			executor.shutdownNow();
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

    private ParsingEvent read() {
        char c = nextChar();
        if(c == 0) {
        	if(!this.queue.isEmpty()) {        		
        		ParsingEvent lastToken = this.queue.pop();        		
        		if (lastToken.getType() == ParsingEvent.JSON_ARRAY_OPENING ) 
        			throw new LysonParsingException("Json array closing expected", line,column);
        		
        		else if(lastToken.getType() == ParsingEvent.JSON_OBJECT_OPENING ) 
        			throw new LysonParsingException("Json object closing expected", line,column);        	    
        	}
        	return null;
        }
        if (this.queue.isEmpty()) {
            ParsingEvent co = checkOpening(c, "/", null);
            if (co != null)
                return co;
            
            return null;
        }  
        ParsingEvent lastToken = this.queue.pop();        
        String path = lastToken.getPath();
        int index = 0;        
        
        if(lastToken instanceof ArrayOpeningEvent) 
        	index =  ((ArrayOpeningEvent)lastToken).getInnerIndex();
        
        if ((lastToken.getType() & ParsingEvent.OPENING) == ParsingEvent.OPENING )
            this.queue.push(lastToken);
       
        switch (lastToken.getType()) {
            case ParsingEvent.JSON_OBJECT_OPENING:
            	ParsingEvent cc = checkClosing(c, path);
                if (cc != null) 
                    return cc;
                
            	return parseInJsonObject(c, path);               
            case ParsingEvent.JSON_ARRAY_OPENING:
                cc = checkClosing(c, path);
                if (cc != null) 
                    return cc;
                
                index+=1;
                ((ArrayOpeningEvent)lastToken).withInnerIndex(index);
                return parseInJsonArray(index, c, path);
            case ParsingEvent.JSON_OBJECT_CLOSING:
            case ParsingEvent.JSON_OBJECT_ITEM:
            case ParsingEvent.JSON_ARRAY_CLOSING:
            case ParsingEvent.JSON_ARRAY_ITEM:
            	break;
            default:
                break;
        }
        return null;
    }
    
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
        if (c == ':')
            moveOn();        
        else if (c == '='){
            moveOn();
            if (currentChar() == '>')
                moveOn();                    	
            else 
                throw new LysonParsingException("Expected a ':' or '=>' after a key", line, column);
        } else 
            throw new LysonParsingException("Expected a ':' or '=>' after a key", line, column);
        
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
            if (co != null) 
                return co;
            
            StringBuilder sb = new StringBuilder();
            while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
                sb.append(c);
                moveOn();
                c = currentChar();
            }
            String s = sb.toString().trim();
            if (s.equals(""))
                throw new LysonParsingException("Missing value", line, column);
            
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

    private void checkClosingArray() {
        if (this.queue.isEmpty()) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
        ParsingEvent previousToken = this.queue.pop();
        if (previousToken.getType()!=ParsingEvent.JSON_ARRAY_OPENING) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
    }

    private void checkClosingObject() {
        if (this.queue.isEmpty()) {
            throw new LysonParsingException("Unexpected object closing", line,  column);
        }
        ParsingEvent previousToken = this.queue.pop();
        if (previousToken.getType()!= ParsingEvent.JSON_OBJECT_OPENING) {
            throw new LysonParsingException("Unexpected object closing", line, column);
        }
    }

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

    private void moveOn(){
        pos+=1;
        column+=1;
    }

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
                return numberFromString(s);
            }
 		} catch(Exception ex) {
 			LOG.log(Level.FINEST, ex.getMessage(), ex);
 		}
        return s;
    }  
}