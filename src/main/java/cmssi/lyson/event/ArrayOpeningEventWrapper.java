package cmssi.lyson.event;

/**
 * ArrayOpeningEvent implementation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class ArrayOpeningEventWrapper implements ArrayOpeningEvent, ParsingEvent, ParsingEventWrapper {

	private final ParsingEvent event;
	private int innerIndex;

	public ArrayOpeningEventWrapper(ParsingEvent event){
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
	public int getInnerIndex() {
		return this.innerIndex;
	}

	@Override
	public ArrayOpeningEventWrapper withInnerIndex(int innerIndex) {
		this.innerIndex = innerIndex;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.event.toString());
		return builder.toString();
	}
}
