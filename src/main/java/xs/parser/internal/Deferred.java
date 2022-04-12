package xs.parser.internal;

import java.util.*;
import java.util.function.*;

public interface Deferred<T> {

	public static final Deferred<?> NONE = Deferred.value(null);

	@SuppressWarnings("unchecked")
	public static <U> Deferred<U> none() {
		return (Deferred<U>) NONE;
	}

	public T get();

	public default <U> Deferred<U> map(Function<T, U> mapper) {
		return Deferred.of(() -> mapper.apply(get()));
	}

	public static <T> Deferred<T> of(final Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		return new Deferred<T>() {

			volatile Supplier<T> s = supplier;
			volatile T value;

			@Override
			public T get() {
				if (s != null) {
					synchronized (this) {
						if (s != null) {
							value = s.get();
							s = null;
						}
					}
				}
				return value;
			}

		};
	}

	public static <T> Deferred<T> value(final T value) {
		return new Deferred<T>() {

			@Override
			public T get() {
				return value;
			}

		};
	}

}