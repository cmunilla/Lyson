package cmssi.lyson.event;

/**
 * IndexedEvent implementation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class IndexedEventWrapper implements IndexedEvent, ParsingEvent, ParsingEventWrapper {

	private final ParsingEvent event;
	private int index;

	public IndexedEventWrapper(ParsingEvent event){
		this.event = event;
	}
	
	@Override
	public ParsingEvent getEvent() {
		return this.event;
	}
	
	@Override
	public int getType() {
		return this.event.getType();
	}

	@Override
	public String getPath() {
		return this.event.getPath();
	}

	@Override
	public ParsingEvent withPath(String path) {
		this.event.withPath(path);
		return this;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public IndexedEventWrapper withIndex(int index) {
		this.index = index;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.event.toString());
		return builder.toString();
	}
}
