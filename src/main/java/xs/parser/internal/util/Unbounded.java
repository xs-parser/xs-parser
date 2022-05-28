package xs.parser.internal.util;

public class Unbounded extends Number {

	public static final Unbounded INSTANCE = new Unbounded();

	private Unbounded() { }

	@Override
	public int intValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public long longValue() {
		return Long.MAX_VALUE;
	}

	@Override
	public float floatValue() {
		return Float.MAX_VALUE;
	}

	@Override
	public double doubleValue() {
		return Double.MAX_VALUE;
	}

	@Override
	public String toString() {
		return "unbounded";
	}

}
