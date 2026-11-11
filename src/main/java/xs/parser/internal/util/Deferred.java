package xs.parser.internal.util;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

@FunctionalInterface
public interface Deferred<T> {

	public T get();

	public default <U> Deferred<U> map(final Function<T, U> mapper) {
		Objects.requireNonNull(mapper);
		return Deferred.of(() -> mapper.apply(get()));
	}

	public default <U> Deque<U> expand(final Function<T, ? extends Deque<U>> mapper) {
		return new DeferredDeque<>(map(mapper));
	}

	public static <T> Deferred<T> of(final Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		final ReentrantLock lock = new ReentrantLock();
		final AtomicReference<T> value = new AtomicReference<>();
		return () -> {
			T val = value.get();
			if (val == null) {
				lock.lock();
				try {
					val = value.get();
					if (val == null) {
						val = supplier.get();
						value.set(val);
					}
				} finally {
					lock.unlock();
				}
			}
			return val;
		};
	}

}
