# Description

# Requirements

`xs-parser` requires Java 8 or later. `xs-parser` has no third-party dependencies.

# Usage

`build.gradle`

```groovy
plugins {
	id 'java'
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'com.github.xs-parser:xs-parser:1.0.0'
}
```

-----

`Runner.java`

```java
import xs.parser.*;

public class Runner {

	public static void main(final String[] args) throws IOException {
		final Schema schema = new Schema(new File("/path/to/schema/file.xsd"));
		schema.typeDefinitions().forEach(t -> System.out.println((ComplexType.class.isInstance(t) ? "Complex" : "Simple") + "Type definition: " + t.name()));
		schema.attributeDeclarations().forEach(a -> System.out.println("Attribute declaration: " + a.name()));
	}

}
```

# Design Goals

Due to the complex structure of XML schema documents and the potential variety of usages, there is no preference for any particular schema component. Therefore, `xs-parser` aims to provide only the methods described in the XSD 1.1 specification with no added frills.

This may inhibit clean and concise code but provides users the ability to reference the XSD specification directly and utilize `xs-parser` with regards to other specifications that are built upon XSD.