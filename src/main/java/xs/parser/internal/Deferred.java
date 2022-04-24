package xs.parser.internal;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

@FunctionalInterface
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

	public static <T> Deferred<T> value(final T value) {
		return () -> value;
	}

}