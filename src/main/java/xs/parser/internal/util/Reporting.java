package xs.parser.internal.util;

public final class Reporting {

	private static final int SILENT = 0;
	private static final int DEFAULT = 1;
	private static final int STACKTRACE = 2;
	private static final String SILENT_PROPERTY = "XS_PARSER_SILENT";
	private static final String VERBOSE_PROPERTY = "XS_PARSER_VERBOSE";
	private static final int REPORTING_LEVEL;

	static {
		final boolean isSilent = System.getProperty(SILENT_PROPERTY) != null && Boolean.parseBoolean(System.getProperty(SILENT_PROPERTY));
		final boolean isVerbose = System.getProperty(VERBOSE_PROPERTY) != null && Boolean.parseBoolean(System.getProperty(VERBOSE_PROPERTY));
		if (isSilent && isVerbose) {
			throw new ExceptionInInitializerError("Cannot specify both " + SILENT_PROPERTY + " and " + VERBOSE_PROPERTY + " system properties");
		} else {
			REPORTING_LEVEL = isSilent ? SILENT : isVerbose ? STACKTRACE : DEFAULT;
		}
	}

	private Reporting() { }

	public static void report(final String message, final Exception e) {
		if (REPORTING_LEVEL >= DEFAULT) {
			System.err.println(message);
			if (REPORTING_LEVEL >= STACKTRACE) {
				e.printStackTrace(System.err);
			}
		}
	}

}
