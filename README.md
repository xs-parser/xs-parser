# Description

`xs-parser` is a Java software library that represents the object model described in the W3C XML Schema Definition Language (XSD) 1.1 Part 1 (https://www.w3.org/TR/xmlschema11-1/) and Part 2 (https://www.w3.org/TR/xmlschema11-2/).

# Requirements

`xs-parser` requires Java 8 or later. `xs-parser` has no third-party dependencies.

If `Saxon-HE` version 10 is detected at runtime on the classpath, then it is used as the XPath and XQuery evaluation engine. When not detected, XQuery evaluation is disabled and the XPath engine defaults to the JAXP XPath 1.0 implementation. `Saxon-HE` version 11+ is not supported at this time.

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
	implementation 'io.github.xs-parser:xs-parser:1.0'
}
```

-----

`Runner.java`

```java
import xs.parser.*;

public class Runner {

	public static void main(final String[] args) throws IOException {
		final Schema schema = new Schema(new File("/path/to/schema.xsd"));
		schema.typeDefinitions().forEach(t -> System.out.println((t instanceof ComplexType ? "Complex" : "Simple") + "Type definition: " + t.name()));
		schema.attributeDeclarations().forEach(a -> System.out.println("Attribute declaration: " + a.name()));
	}

}
```

## XPath & XQuery

```java
final var schema = new xs.parser.Schema(new java.io.File("/path/to/schema.xsd"));
var ns = xs.parser.x.NodeSet.of(schema);
// Performs the XPath evaluation for every imported/included/redefined/overridden schema file
ns = ns.xpath("fn:collection()/xs:schema");
/* If instead of the above line you had used:
ns = ns.xpath("/xs:schema");
then only the /path/to/schema.xsd file would have been evaluated */
ns = ns.xquery("xs:complexType"); // Gets all xs:complexTypes, note: XPath and XQuery usage can be mixed, however, Saxon-HE must be on the classpath to the use XQuery
System.out.println(ns.size());
```

# Design Goals

Due to the complex structure of XML schema documents and the potential variety of usages, there is no preference for any particular schema component. Therefore, `xs-parser` aims to provide only the methods described in the XSD 1.1 specification with no added frills.

This may inhibit clean and concise code but provides users the ability to reference the XSD specification directly and utilize `xs-parser` with regards to other specifications that are built upon XSD.
