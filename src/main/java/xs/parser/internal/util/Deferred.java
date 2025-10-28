package xs.parser.internal.util;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

@FunctionalInterface
public interface Deferred<T> {

	public T get();

	public default <U> Deferred<U> map(final Function<T, U> mapper) {
		Objects.requireNonNull(mapper);
		return Deferred.of(() -> mapper.apply(get()));
	}

	public default <U> Deque<U> mapToDeque(final Function<T, ? extends Deque<U>> mapper) {
		return new DeferredArrayDeque<>(map(mapper));
	}

	public static <T> Deferred<T> of(final Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		final AtomicBoolean empty = new AtomicBoolean(true);
		final AtomicReference<T> value = new AtomicReference<>();
		return () -> {
			if (empty.get()) {
				synchronized (value) {
					if (empty.get()) {
						final T val = supplier.get();
						value.set(val);
						empty.set(false);
						return val;
					}
				}
			}
			return value.get();
		};
	}

}
