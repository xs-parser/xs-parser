package xs.parser.internal.util;

import java.util.*;

/**
 * Utility class for {@link java.util.Deque}
 */
public final class Deques {

	private static class UnmodifiableDeque<E> implements Deque<E> {

		private final Deque<? extends E> d;

		UnmodifiableDeque(final Deque<? extends E> d) {
			this.d = Objects.requireNonNull(d);
		}

		@Override
		public boolean isEmpty() {
			return d.isEmpty();
		}

		@Override
		public Object[] toArray() {
			return d.toArray();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			return d.toArray(a);
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			return d.containsAll(c);
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addFirst(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addLast(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean offerFirst(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean offerLast(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E removeFirst() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E removeLast() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E pollFirst() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E pollLast() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E getFirst() {
			return d.getFirst();
		}

		@Override
		public E getLast() {
			return d.getLast();
		}

		@Override
		public E peekFirst() {
			return d.peekFirst();
		}

		@Override
		public E peekLast() {
			return d.peekLast();
		}

		@Override
		public boolean removeFirstOccurrence(final Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeLastOccurrence(final Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean add(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean offer(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(final Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E poll() {
			throw new UnsupportedOperationException();
		}

		@Override
		public E element() {
			return d.element();
		}

		@Override
		public E peek() {
			return d.peek();
		}

		@Override
		public boolean addAll(final Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void push(final E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public E pop() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(final Object o) {
			return d.contains(o);
		}

		@Override
		public int size() {
			return d.size();
		}

		@Override
		public Iterator<E> iterator() {
			final Iterator<? extends E> iter = d.iterator();
			return new Iterator<E>() {

				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public E next() {
					return iter.next();
				}

			};
		}

		@Override
		public Iterator<E> descendingIterator() {
			final Iterator<? extends E> iter = d.descendingIterator();
			return new Iterator<E>() {

				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public E next() {
					return iter.next();
				}

			};
		}

		@Override
		public String toString() {
			return d.toString();
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof Deque) {
				final Deque<?> d2 = (Deque<?>) other;
				if (d2.size() != d.size()) {
					return false;
				}
				final Iterator<?> iter = d.iterator();
				final Iterator<?> iter2 = d2.iterator();
				while (iter.hasNext()) {
					if (!iter2.hasNext() || !Objects.equals(iter.next(), iter2.next())) {
						return false;
					}
				}
				return !iter2.hasNext();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return d.hashCode();
		}

	}

	private static final Deque<?> EMPTY = new UnmodifiableDeque<>(new ArrayDeque<>(0));

	private Deques() { }

	/**
	 * @param <E> The item type
	 * @return An empty deque
	 */
	@SuppressWarnings("unchecked")
	public static <E> Deque<E> emptyDeque() {
		return (Deque<E>) EMPTY;
	}

	/**
	 * @param <E> The item type
	 * @param e The items
	 * @return A deque containing the values of {@code e}
	 */
	@SafeVarargs
	public static <E> Deque<E> asDeque(final E... e) {
		return new UnmodifiableDeque<>(new ArrayDeque<>(Arrays.asList(e)));
	}

	/**
	 * @param <E> The item type
	 * @param e The item
	 * @return A deque with a single item
	 */
	public static <E> Deque<E> singletonDeque(final E e) {
		final Deque<E> d = new ArrayDeque<>(1);
		d.add(e);
		return new UnmodifiableDeque<>(d);
	}

	/**
	 * @param <E> The item type
	 * @param d The deque
	 * @return An unmodifiable deque
	 */
	public static <E> Deque<E> unmodifiableDeque(final Deque<? extends E> d) {
		return new UnmodifiableDeque<>(d);
	}

}
