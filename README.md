# Description

`xs-parser` is a Java software library that represents the object model described in the W3C XML Schema Definition Language (XSD) 1.1 Part 1 (https://www.w3.org/TR/xmlschema11-1/) and Part 2 (https://www.w3.org/TR/xmlschema11-2/).

![Build](https://github.com/xs-parser/xs-parser/workflows/Build/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=coverage)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=bugs)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=security_rating)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=xs-parser_xs-parser&metric=alert_status)](https://sonarcloud.io/dashboard?id=xs-parser_xs-parser)
[![License](https://img.shields.io/github/license/xs-parser/xs-parser?label=License&logo=github)](https://github.com/xs-parser/xs-parser/blob/main/LICENSE.md)

## Goals

`xs-parser` aims to reduce the mysticism surrounding the complexity of the XSD 1.1 specification by providing users with direct access to the XSD properties.

Due to the complex structure of XML schema documents and the potential variety of the structure of schemas, there is no preference for any particular schema component or structure. Therefore, `xs-parser` only provides the properties described in the XSD 1.1 specification.

Unlike other software libraries that attempt to model XML schema documents, `xs-parser` does not sugarcoat the complexity inherent to the XSD 1.1 specification, but in returns offers a high level of fidelity to the specification. Another added benefit of this approach is that developers who are familiar with the XSD 1.1 specification may quickly adopt this library into their development tool chains.

# Requirements

`xs-parser` requires Java 8 or later and has no third-party dependencies.

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
	implementation 'io.github.xs-parser:xs-parser:2.1'
}
```

-----

`Runner.java`

```java
import java.io.File;
import xs.parser.*;

public class Runner {

	public static void main(final String[] args) throws IOException {
		final Schema schema = new Schema(new File("/path/to/schema.xsd"));
		schema.typeDefinitions().forEach(t ->
			System.out.println((t instanceof ComplexType ? "Complex" : "Simple")
				+ "Type definition: " + t.name()));
		schema.attributeDeclarations().forEach(a ->
			System.out.println("Attribute declaration: " + a.name()));
	}

}
```

## Equals and HashCode

The classes of `xs-parser` do not override `equals()` or `hashCode()`. Use reference equality, `==`, instead. Consider the following code and schema:

```xml
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xs:complexType name="A" />
	<xs:complexType name="B" >
		<xs:sequence>
			<xs:element name="E" />
		</xs:sequence>
	</xs:complexType>
	<xs:element name="E1" type="A" />
	<xs:element name="E2" type="B" />
</xs:schema>
```

where `/path/to/schema.xsd` contains the above schema.

```java
final Schema schema = new Schema(new File("/path/to/schema.xsd"));
final TypeDefinition a = schema.typeDefinitions().getFirst();
System.out.println(a.baseTypeDefinition() == ComplexType.xsAnyType()); // Prints: true
final Element e1 = schema.elementDeclarations().getFirst();
System.out.println(e1.name());                                         // Prints: E1
System.out.println(e1.typeDefinition() == a);                          // Prints: true
final Element e2 = schema.elementDeclarations().getLast();
System.out.println(e2.name());                                         // Prints: E2
final TypeDefinition b = schema.typeDefinitions().getLast();
System.out.println(e2.typeDefinition() == b);                          // Prints: true
```

## Execution Model

Almost all evaluations in `xs-parser` are lazy, accomplished through a deferred execution model. The `Schema` constructor does not do anything more than load the DOM of the file as well as a few sanity checks to ensure that what was provided looks roughly like an actual schema. The definitions and declarations of the schema are not parsed until they are needed.

## XSD Datatypes Mapping

XSD Primitive Datatypes | Java class
-- | --
`xs:string`, `xs:duration`, `xs:dateTime`, `xs:time`, `xs:date`, `xs:gYearMonth`, `xs:gYear`, `xs:gMonthDay`, `xs:gDay`, `xs:gMonth`, `xs:hexBinary`, `xs:base64Binary`, `xs:anyURI` | `java.lang.String`
`xs:boolean` | `java.lang.Boolean`
`xs:decimal`, `xs:float`, `xs:double` | `java.math.BigDecimal`
`xs:QName`, `xs:NOTATION` | `javax.xml.namespace.QName`

XSD Other Built-in Datatypes | Java class
-- | --
`xs:normalizedString`, `xs:token`, `xs:language`, `xs:NMTOKEN`, `xs:NMTOKENS`, `xs:Name`, `xs:NCName`, `xs:ID`, `xs:IDREF`, `xs:IDREFS`, `xs:ENTITY`, `xs:ENTITIES`, `xs:yearMonthDuration`, `xs:dayTimeDuration`, `xs:dateTimeStamp` | `java.lang.String`
`xs:integer`, `xs:nonPositiveInteger`, `xs:negativeInteger`, `xs:long`, `xs:int`, `xs:short`, `xs:byte`, `xs:nonNegativeInteger`, `xs:unsignedLong`, `xs:unsignedInt`, `xs:unsignedShort`, `xs:unsignedByte`, `xs:positiveInteger` | `java.lang.BigInteger`

## XPath & XQuery

```java
final var schema = new xs.parser.Schema(new java.io.File("/path/to/schema.xsd"));
final var root = xs.parser.x.NodeSet.of(schema);

// Performs the XPath evaluation for the root schema and all imported or included schemas
final var allSchemas = root.xpath("fn:collection()/xs:schema");

// Gets all xs:complexType name attributes
// Note: XPath and XQuery usage can be mixed
// However, Saxon-HE must be on the classpath to use XQuery
final var complexTypeNames = allSchemas.xquery("xs:complexType").xpath("@name");

// Only the /path/to/schema.xsd schema file is evaluated
// Will not execute the given XPath for any imported or included schemas
final var rootSchema = root.xpath("/xs:schema");

// Gets all xs:simpleTypes in the /path/to/schema.xsd file
final var simpleTypes = rootSchema.xpath("xs:simpleType");

System.out.println("xs:complexType size: " + complexTypeNames.size());
System.out.println("xs:simpleType size: " + simpleTypes.size());

// The split() method creates a new single-element NodeSet for every element
complexTypeNames.split().forEach(name ->
	System.out.println("Name: " + name.getStringValue()));
// The stream() method iterates over the underlying org.w3c.dom.Node elements
simpleTypes.stream().forEach(node ->
	System.out.println("Name: " + node.getAttributes().getNamedItemNS(null, "name")));
```
