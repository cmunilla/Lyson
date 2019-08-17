package cmssi.lyson.event;

public class KeyValueEventWrapper extends ValuableEventWrapper implements KeyValueEvent, ParsingEvent {

	private String key;
	
	public KeyValueEventWrapper(ParsingEvent event){
		super(event);
	}
	
	@Override
	public KeyValueEventWrapper withKey(String key) {
		this.key = key;
		return this;
	}

	@Override
	public KeyValueEventWrapper withValue(Object value) {
		super.withValue(value);
		return this;
	}
	
	@Override
	public String getKey(){
		return this.key;
	}
}
