package xs.parser.internal.util;

import java.util.*;

public class DeferredArrayDeque<E> implements Deque<E> {

	private final int capacity;
	private final ArrayDeque<Object> arr;

	public DeferredArrayDeque(final int capacity) {
		this.capacity = capacity;
		this.arr = new ArrayDeque<>(capacity);
	}

	public DeferredArrayDeque(final int additionalCapacity, final Collection<E> c) {
		this(additionalCapacity + c.size());
		addAll(c);
	}

	private boolean inBounds(final int index, final int increment) {
		return index < 0 || index + increment > capacity || index + increment < 0 || capacity < 0;
	}

	private void checkBounds(final int index, final int increment) {
		if (inBounds(index, increment)) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
	}

	@SuppressWarnings("unchecked")
	private E resolve(final Object o) {
		if (o instanceof Deferred) {
			// TODO Replace Deferred with E instance in deque
			return (E) ((Deferred<?>) o).get();
			// final Deferred<?> d = (Deferred<?>) o;
			// final E e = (E) d.get();
		} else {
			return (E) o;
		}
	}

	public static <T> DeferredArrayDeque<T> of(final Collection<Deferred<T>> c) {
		final DeferredArrayDeque<T> ls = new DeferredArrayDeque<>(c.size());
		for (final Deferred<T> d : c) {
			ls.add(d);
		}
		return ls;
	}

	public void add(final Deferred<E> d) {
		checkBounds(arr.size(), 1);
		arr.add(d);
	}

	@Override
	public boolean add(final E e) {
		checkBounds(arr.size(), 1);
		return arr.add(e);
	}

	public Iterator<Object> rawIterator() {
		return arr.iterator();
	}

	@Override
	public int size() {
		return arr.size();
	}

	@Override
	public boolean isEmpty() {
		return arr.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		final Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			if (Objects.equals(o, iter.next())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		final Iterator<Object> iter = arr.iterator();
		return new Iterator<E>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				return resolve(iter.next());
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		// TODO special case for DeferredArrayList
		for (final Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		checkBounds(arr.size(), c.size());
		if (c instanceof DeferredArrayDeque) {
			final DeferredArrayDeque<?> d = (DeferredArrayDeque<?>) c;
			final Iterator<?> iter = d.iterator();
			while (iter.hasNext()) {
				arr.offer(iter.next());
			}
			return !c.isEmpty();
		} else {
			return arr.addAll(c);
		}
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		// TODO special case for DeferredArrayDeque
		boolean changed = false;
		for (final Object o : c) {
			changed |= remove(o);
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		// TODO special case for DeferredArrayDeque
		boolean changed = false;
		final Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			final E e = iter.next();
			if (!c.contains(e)) {
				iter.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		arr.clear();
	}

	@Override
	public void addFirst(final E e) {
		checkBounds(arr.size(), 1);
		arr.addFirst(e);
	}

	@Override
	public void addLast(final E e) {
		checkBounds(arr.size(), 1);
		arr.addLast(e);
	}

	@Override
	public boolean offerFirst(final E e) {
		if (inBounds(arr.size(), 1)) {
			return arr.offerFirst(e);
		}
		return false;
	}

	@Override
	public boolean offerLast(final E e) {
		if (inBounds(arr.size(), 1)) {
			return arr.offerLast(e);
		}
		return false;
	}

	@Override
	public E removeFirst() {
		return resolve(arr.removeFirst());
	}

	@Override
	public E removeLast() {
		return resolve(arr.removeLast());
	}

	@Override
	public E pollFirst() {
		return resolve(arr.pollFirst());
	}

	@Override
	public E pollLast() {
		return resolve(arr.pollLast());
	}

	@Override
	public E getFirst() {
		return resolve(arr.getFirst());
	}

	@Override
	public E getLast() {
		return resolve(arr.getLast());
	}

	@Override
	public E peekFirst() {
		return resolve(arr.peekFirst());
	}

	@Override
	public E peekLast() {
		return resolve(arr.peekLast());
	}

	@Override
	public boolean removeFirstOccurrence(final Object o) {
		return arr.removeFirstOccurrence(o);
	}

	@Override
	public boolean removeLastOccurrence(final Object o) {
		return arr.removeLastOccurrence(o);
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
		final Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			final E e = iter.next();
			if (Objects.equals(e, o)) {
				iter.remove();
				return true;
			}
		}
		return false;
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
		final Iterator<Object> iter = arr.descendingIterator();
		return new Iterator<E>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				return resolve(iter.next());
			}

			@Override
			public void remove() {
				iter.remove();
			}

		};
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
