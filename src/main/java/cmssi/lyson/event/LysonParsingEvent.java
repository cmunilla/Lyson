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
package cmssi.lyson.event;

/**
 * JSON parsing event implementation
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.3
 */
public class LysonParsingEvent implements ParsingEvent {
	
    final private int type;
    private String path;

	/**
	 * Constructor
	 * 
	 * @param type the int type of the LysonParsingEvent to
	 * be instantiated
	 */
    public LysonParsingEvent(int type) {
    	this.type = type;
    }

    @Override
	public int getType() {
		return this.type;
	}
	
	@Override
    public String getPath() {
        return this.path;
    }

	@Override
    public ParsingEvent withPath(String path) {
        this.path = path;
        return this;
    }

	@Override
	public <P extends ParsingEvent> P adapt(Class<P> type){
		if(type.isAssignableFrom(getClass())) {
			return (P)this;
		}
		return null;
	}
    
    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	switch(getType()) {
	    	case ParsingEvent.JSON_OBJECT_OPENING :
	    		builder.append("[JSON_OBJECT_OPENING]");
	    		break;
	    	case ParsingEvent.JSON_OBJECT_CLOSING  :
	    		builder.append("[JSON_OBJECT_CLOSING]"); 
	    		break;
	    	case ParsingEvent.JSON_OBJECT_ITEM :
	    		builder.append("[JSON_OBJECT_ITEM]");
	    		break;
	    	case ParsingEvent.JSON_ARRAY_OPENING :
	    		builder.append("[JSON_ARRAY_OPENING ]");
	    		break;
	    	case ParsingEvent.JSON_ARRAY_CLOSING :
	    		builder.append("[JSON_ARRAY_CLOSING]");
	    		break;
	    	case ParsingEvent.JSON_ARRAY_ITEM :
	    		builder.append("[JSON_ARRAY_ITEM]");
	    		break;
	    	default :
	    		break;
    	} 
    	builder.append(String.format("[%s]",this.getPath()));
    	return builder.toString();
    }
}