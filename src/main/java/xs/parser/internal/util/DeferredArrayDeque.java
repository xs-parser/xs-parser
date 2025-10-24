package xs.parser.internal.util;

import java.util.*;

public class DeferredArrayDeque<E> implements Deque<E> {

	private Deque<Deferred<? extends E>> deq;

	public DeferredArrayDeque() {
		this.deq = new ArrayDeque<>();
	}

	public DeferredArrayDeque(final Collection<? extends E> c) {
		this();
		addAll(c);
	}

	public DeferredArrayDeque(final Deferred<? extends Deque<E>> def) {
		this();
		addAllDeferred(def);
	}

	@Override
	public boolean add(final E e) {
		addLast(e);
		return true;
	}

	@Override
	public int size() {
		return deq.size();
	}

	@Override
	public boolean isEmpty() {
		return deq.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		for (final Deferred<? extends E> def : deq) {
			if (Objects.equals(def.get(), o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		final Iterator<Deferred<? extends E>> iter = deq.iterator();
		return new Iterator<>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				return iter.next().get();
			}

			@Override
			public void remove() {
				iter.remove();
			}

		};
	}

	@Override
	public Object[] toArray() {
		final Object[] a = new Object[size()];
		final Iterator<E> iter = iterator();
		int i = 0;
		while (iter.hasNext()) {
			a[i++] = iter.next();
		}
		return a;
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return def.get().toArray(a);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Deferred<? extends E> def : this) {
			if (!c.contains(def.get())) {
				return false;
			}
		}
		return true;
	}

	public void addAll(final Deferred<? extends Collection<? extends E>> d) {
		def = def.map(x -> {
			x.addAll(d.get());
			return x;
		});
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		for (final E e : c) {
			deq.add(e);
		}
		return !c.isEmpty();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		final Iterator<E> iter = iterator();
		boolean changed = false;
		while (iter.hasNext()) {
			if (c.contains(iter.next())) {
				iter.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		final Iterator<E> iter = iterator();
		boolean changed = false;
		while (iter.hasNext()) {
			if (!c.contains(iter.next())) {
				iter.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		deq.clear();
	}

	@Override
	public void addFirst(final E e) {
		deq.addFirst(new DeferredValue<>(e));
	}

	public void addLast(final Deferred<E> def) {
		deq.addLast(def);
	}

	@Override
	public void addLast(final E e) {
		deq.addLast(new DeferredValue<>(e));
	}

	@Override
	public boolean offerFirst(final E e) {
		addFirst(e);
		return true;
	}

	@Override
	public boolean offerLast(final E e) {
		addLast(e);
		return true;
	}

	@Override
	public E removeFirst() {
		final E first = pollFirst();
		if (first == null) {
			throw new NoSuchElementException();
		}
		return first;
	}

	@Override
	public E removeLast() {
		final E last = pollLast();
		if (last == null) {
			throw new NoSuchElementException();
		}
		return last;
	}

	@Override
	public E pollFirst() {
		final Deferred<? extends E> first = deq.pollFirst();
		return first != null ? first.get() : null;
	}

	@Override
	public E pollLast() {
		final Deferred<? extends E> last = deq.pollLast();
		return last != null ? last.get() : null;
	}

	@Override
	public E getFirst() {
		final E first = peekFirst();
		if (first == null) {
			throw new NoSuchElementException();
		}
		return first;
	}

	@Override
	public E getLast() {
		final E last = peekLast();
		if (last == null) {
			throw new NoSuchElementException();
		}
		return last;
	}

	@Override
	public E peekFirst() {
		final Deferred<? extends E> first = deq.peekFirst();
		return first != null ? first.get() : null;
	}

	@Override
	public E peekLast() {
		final Deferred<? extends E> last = deq.peekLast();
		return last != null ? last.get() : null;
	}

	@Override
	public boolean removeFirstOccurrence(final Object o) {
		final Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			if (Objects.equals(iter.next(), o)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeLastOccurrence(final Object o) {
		final Iterator<E> iter = descendingIterator();
		while (iter.hasNext()) {
			if (Objects.equals(iter.next(), o)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean offer(final E e) {
		return offerLast(e);
	}

	@Override
	public E remove() {
		return removeFirst();
	}

	@Override
	public boolean remove(final Object o) {
		return removeFirstOccurrence(o);
	}

	@Override
	public E poll() {
		return pollFirst();
	}

	@Override
	public E element() {
		return getFirst();
	}

	@Override
	public E peek() {
		return peekFirst();
	}

	@Override
	public void push(final E e) {
		addFirst(e);
	}

	@Override
	public E pop() {
		return removeFirst();
	}

	@Override
	public Iterator<E> descendingIterator() {
		return def.get().descendingIterator();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		final Iterator<E> iter = iterator();
		if (iter.hasNext()) {
			builder.append(iter.next());
		}
		while (iter.hasNext()) {
			builder.append(", ");
			builder.append(iter.next());
		}
		return builder.append("]").toString();
	}

}
