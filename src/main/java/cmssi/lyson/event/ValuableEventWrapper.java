package cmssi.lyson.event;

/**
 * ValuableEvent implementation
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.1
 */
public class ValuableEventWrapper implements ValuableEvent, ParsingEvent, ParsingEventWrapper {

	private final ParsingEvent event;
	private Object value;

	public ValuableEventWrapper(ParsingEvent event){
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
		return  this;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	@Override
	public ValuableEventWrapper withValue(Object value) {
		this.value = value;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.event.toString());
		if(this.value != null) {
			builder.append(String.format("[%s]",this.value));
		}
		return builder.toString();
	}
}
