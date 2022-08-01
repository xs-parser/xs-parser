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
		final class State {

			final Supplier<T> supplier;
			final T value;

			State(final Supplier<T> supplier, final T value) {
				this.supplier = supplier;
				this.value = value;
			}

		}

		Objects.requireNonNull(supplier);
		final AtomicReference<State> state = new AtomicReference<>(new State(supplier, null));
		return () -> state.updateAndGet(s -> s.supplier == null ? s : new State(null, s.supplier.get())).value;
	}

}
