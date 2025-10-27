package xs.parser.internal.util;

import java.util.*;
import java.util.concurrent.*;

public class DeferredDeque<E> implements Deque<E> {

	private Deferred<? extends Deque<E>> def;

	public DeferredDeque() {
		this.def = new DeferredValue<>(new ConcurrentLinkedDeque<>());
	}

	public DeferredDeque(final Collection<? extends E> c) {
		this();
		addAll(c);
	}

	public DeferredDeque(final Deferred<? extends Deque<E>> def) {
		this.def = def.map(ConcurrentLinkedDeque::new);
	}

	@Override
	public boolean add(final E e) {
		addLast(e);
		return true;
	}

	@Override
	public int size() {
		return def.get().size();
	}

	@Override
	public boolean isEmpty() {
		return def.get().isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return def.get().contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return def.get().iterator();
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
		return def.get().containsAll(c);
	}

	public void addAll(final Deferred<? extends Collection<? extends E>> d) {
		def = def.map(x -> {
			x.addAll(d.get());
			return x;
		});
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		def = def.map(x -> {
			x.addAll(c);
			return x;
		});
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return def.get().removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return def.get().retainAll(c);
	}

	@Override
	public void clear() {
		def = new DeferredValue<>(new ConcurrentLinkedDeque<>());
	}

	@Override
	public void addFirst(final E e) {
		def = def.map(x -> {
			x.addFirst(e);
			return x;
		});
	}

	public void addLast(final Deferred<E> d) {
		def = def.map(x -> {
			x.addLast(d.get());
			return x;
		});
	}

	@Override
	public void addLast(final E e) {
		def = def.map(x -> {
			x.addLast(e);
			return x;
		});
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
		return def.get().pollFirst();
	}

	@Override
	public E pollLast() {
		return def.get().pollLast();
	}

	@Override
	public E getFirst() {
		return def.get().getFirst();
	}

	@Override
	public E getLast() {
		return def.get().getLast();
	}

	@Override
	public E peekFirst() {
		return def.get().peekFirst();
	}

	@Override
	public E peekLast() {
		return def.get().peekLast();
	}

	@Override
	public boolean removeFirstOccurrence(final Object o) {
		return def.get().removeFirstOccurrence(o);
	}

	@Override
	public boolean removeLastOccurrence(final Object o) {
		return def.get().removeLastOccurrence(o);
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
