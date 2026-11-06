package xs.parser.internal.util;

import java.util.*;

public class DeferredSet<T> extends LinkedHashSet<T> {

	private final Deque<T> deque;

	public DeferredSet() {
		this.deque = Deques.emptyDeque();
	}

	public DeferredSet(final Deque<T> deque) {
		this.deque = Objects.requireNonNull(deque);
	}

	private void addAllElements() {
		if (!deque.isEmpty()) {
			synchronized (this) {
				addAll(deque);
				deque.clear();
			}
		}
	}

	private T poll() {
		synchronized (this) {
			final T element = deque.pollFirst();
			if (element != null) {
				add(element);
			}
			return element;
		}
	}

	@Override
	public int size() {
		addAllElements();
		return super.size();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && deque.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		if (super.contains(o)) {
			return true;
		}
		T element;
		while ((element = poll()) != null) {
			if (element.equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		addAllElements();
		return super.iterator();
	}

	public Deque<T> getDeque() {
		return deque;
	}

}
