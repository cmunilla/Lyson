package cmssi.lyson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;
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
 * @version 0.1
 */
public class LysonParser {

	private static final Logger LOG = Logger.getLogger(LysonParser.class.getName());
	
	private static final int BUFFER_SIZE = 1024*60;
	private static final char EOF  = '\0';
	
    private Reader reader;
    
	private char[] buffer = new char[BUFFER_SIZE];
	private int pos = 0;
	private int line = 0;
	private int column = 0;
	private int length = 0;	
	
    private Stack<ParsingEvent> stack;
    
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
        this.stack = new Stack<ParsingEvent>();
    }
    
    /**
     * Parses the input string (or stream) using the 
     * {@link LysonParserHandler} passed as parameter to handle
     * parsing events, potential errors, and defining whether 
     * the parsing can be carried on or not
     * 
     * @param handler the {@link LysonParserHandler} used for
     * the parsing
     */
    public void parse(LysonParserHandler handler) {
    	try { 
            while (handler.handle(read()));
        } catch (LysonParsingException e) {
        	handler.handle(e);
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
    
    private ParsingEvent read() throws LysonException {
        char c = nextChar();
        if(c == 0) {
        	return null;
        }
        if (this.stack.isEmpty()) {
            ParsingEvent co = checkOpening(c, "/", null);
            if (co != null) {
                return co;
            }
            return null;
        }  
        ParsingEvent lastToken = this.stack.pop();        
        String path = lastToken.getPath();
        int index = 0;
        
        if(lastToken instanceof ArrayOpeningEvent) {
        	index =  ((ArrayOpeningEvent)lastToken).getInnerIndex();
        }
        
        if (lastToken != null && 
        	(lastToken.getType() == ParsingEvent.JSON_ARRAY_OPENING || 
        	 lastToken.getType() == ParsingEvent.JSON_OBJECT_OPENING )) {
            this.stack.push(lastToken);
        }
        switch (lastToken.getType()) {
            case ParsingEvent.JSON_OBJECT_OPENING:
            	ParsingEvent cc = checkClosing(c, path);
                if (cc != null) {
                    return cc;
                }
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
                    default:
                        break;
                }
                if (value == null){
                    ParsingEvent co = checkOpening(c, path , key );
                    if (co != null) {
                        return co;
                    }
                    StringBuffer sb = new StringBuffer();
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
                    default:
                        break;
                }
                ParsingEvent ev = new LysonParsingEvent(ParsingEvent.JSON_OBJECT_ITEM
	                ).withPath(new StringBuilder().append(path).append(path.endsWith("/")
	                	?"":"/").append(key).toString());    
                return new KeyValueEventWrapper(ev).withValue(value).withKey(key);                
            case ParsingEvent.JSON_ARRAY_OPENING:
                cc = checkClosing(c, path);
                if (cc != null) {
                    return cc;
                }
                index+=1;
                ((ArrayOpeningEvent)lastToken).withInnerIndex(index);
                value = null;
                switch (c) {
                    case ';':
                    case ',':
                    	ev = new LysonParsingEvent(ParsingEvent.JSON_ARRAY_ITEM
        	                ).withPath(new StringBuilder().append(path).append("["
        	                ).append(index).append("]").toString());
                    	return new ValuableEventWrapper(new IndexedEventWrapper(ev
        	                	).withIndex(index));
                    case '"':
                    case '\'':
                        value = readString(c);
                    default:
                        break;
                }
                if (value == null) {
                    ParsingEvent co = checkOpening(c, path, index);
                    if (co != null) {
                        return co;
                    }
                    StringBuffer sb = new StringBuffer();
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
                    default:
                    	break;
                }
                ev = new LysonParsingEvent(ParsingEvent.JSON_ARRAY_ITEM).withPath(
                	new StringBuilder().append(path).append("[").append(index).append("]"
                		).toString());                
            	return new ValuableEventWrapper(new IndexedEventWrapper(ev
	                	).withIndex(index)).withValue(value);
            case ParsingEvent.JSON_OBJECT_CLOSING:
            case ParsingEvent.JSON_OBJECT_ITEM:
            case ParsingEvent.JSON_ARRAY_CLOSING:
            case ParsingEvent.JSON_ARRAY_ITEM:
            default:
                break;
        }
        return null;
    }
    
    private void checkClosingArray() throws LysonException {
        if (this.stack.isEmpty()) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
        ParsingEvent previousToken = this.stack.pop();
        if (previousToken.getType()!=ParsingEvent.JSON_ARRAY_OPENING) {
            throw new LysonParsingException("Unexpected array closing", line, column);
        }
    }

    private void checkClosingObject() throws LysonException {
        if (this.stack.isEmpty()) {
            throw new LysonParsingException("Unexpected object closing", line,  column);
        }
        ParsingEvent previousToken = this.stack.pop();
        if (previousToken.getType()!= ParsingEvent.JSON_OBJECT_OPENING) {
            throw new LysonParsingException("Unexpected object closing", line, column);
        }
    }

    private ParsingEvent checkClosing(char c, String path) throws LysonException {
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
	                ).append("[").append((Number)key).append("]"
	                		).toString());
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
    	this.stack.push(o);
        moveOn();
        return o;
    }

    private char currentChar() throws LysonException {
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

    private char nextChar() throws LysonException {
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
        StringBuffer sb = new StringBuffer();
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
        if (s.equals("")) {
            return s;
        }
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
                if (b == '0') {
                	try {
                        if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {                            
                                return Integer.parseInt(s.substring(2), 16);
                        } else {
                                return Integer.parseInt(s, 8);
                        }
                	} catch(Exception ex ){
             			LOG.log(Level.FINEST, ex.getMessage(), ex);
             		}
                }
                Number num;      
                if(s.indexOf('.') < 0) {
                	num = new BigInteger(s);
                	if(((BigInteger)num).compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
                    	Long myLong = Long.valueOf(s);
                        if (myLong.longValue() == myLong.intValue()) {
                            num = myLong.intValue();
                        } else {
                            num = myLong;
                        }
                	}
                	return num;
                } else {
                    num = new BigDecimal(s);
                    if((((BigDecimal)num).compareTo(new BigDecimal(Double.MAX_VALUE)) <= 0
                    	&& ((BigDecimal)num).compareTo(new BigDecimal(Double.MIN_VALUE)) >= 0)
                    	||  ((BigDecimal)num).intValue() == 0) {
	                    num = Double.valueOf(s);
                    }
                    return num;
                }
            }
 		} catch(Exception ex) {
 			LOG.log(Level.FINEST, ex.getMessage(), ex);
 		}
        return s;
    }
    
    public static void main(String[] args){
    	new LysonParser("[8,{\"fst\": 5,\"array\":[8,2,1,{\"thd\" => \"more\"}],\"snd\":\"another\",\"frth\":{\"last\":45}}]") .valid();
    }
}