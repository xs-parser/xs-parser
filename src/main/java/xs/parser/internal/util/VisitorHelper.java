package xs.parser.internal.util;

import java.util.*;
import java.util.function.*;
import xs.parser.*;
import xs.parser.v.*;

public final class VisitorHelper {

	private static final Map<Class<? extends SchemaComponent>, BiConsumer<? extends SchemaComponent, Visitor>> lookup = new HashMap<>();

	private VisitorHelper() { }

	public static <T extends SchemaComponent> void register(final Class<T> cls, final BiConsumer<T, Visitor> visit) {
		final Object oldValue = lookup.put(cls, visit);
		if (oldValue != null) {
			throw new IllegalArgumentException("Already registered for " + cls);
		}
	}

	@SuppressWarnings("unchecked")
	public static BiConsumer<SchemaComponent, Visitor> lookup(final Class<? extends SchemaComponent> cls) {
		Objects.requireNonNull(cls);
		BiConsumer<? extends SchemaComponent, Visitor> lookupFn = lookup.get(cls);
		if (lookupFn != null) {
			return (BiConsumer<SchemaComponent, Visitor>) lookupFn;
		}
		Class<? extends SchemaComponent> lookupCls = cls;
		do {
			final Class<?> superclass = lookupCls.getSuperclass();
			if (!SchemaComponent.class.isAssignableFrom(superclass)) {
				break; // throw exception below
			}
			lookupCls = superclass.asSubclass(SchemaComponent.class);
			lookupFn = lookup.get(lookupCls);
			if (lookupFn != null) {
				return (BiConsumer<SchemaComponent, Visitor>) lookupFn;
			}
		} while (lookupCls != null);
		throw new IllegalArgumentException("No visit method for " + cls);
	}

}
