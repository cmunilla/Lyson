package cmssi.lyson.event;


/**
 * JSON parsing event implementation
 *  
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class LysonParsingEvent implements ParsingEvent {
	
    final private int type;
    private String path;

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
	    	default :
	    		break;
    	} 
    	builder.append(String.format("[%s]",this.getPath()));
    	return builder.toString();
    }
}