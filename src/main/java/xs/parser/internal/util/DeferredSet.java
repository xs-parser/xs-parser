package xs.parser.internal.util;

import java.util.*;

public class DeferredSet<T> extends AbstractCollection<T> implements Set<T> {

	private final Deque<T> deque;

	public DeferredSet() {
		this.deque = Deques.emptyDeque();
	}

	public DeferredSet(final Deque<T> deque) {
		this.deque = Objects.requireNonNull(deque);
	}

	@Override
	public int size() {
		assert deque.stream().distinct().count() == deque.size();
		return deque.size();
	}

	@Override
	public boolean isEmpty() {
		return deque.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return deque.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		assert deque.stream().distinct().count() == deque.size();
		return deque.iterator();
	}

	public Deque<T> asDeque() {
		return deque;
	}

}
