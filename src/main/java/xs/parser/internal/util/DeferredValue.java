package xs.parser.internal.util;

public class DeferredValue<V> implements Deferred<V> {

	private V value;

	public DeferredValue() { }

	public DeferredValue(final V value) {
		this.value = value;
	}

	public V set(final V value) {
		this.value = value;
		return value;
	}

	@Override
	public V get() {
		return value;
	}

}
