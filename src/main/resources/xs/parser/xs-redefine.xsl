<xsl:transform version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:f="http://www.w3.org/2008/05/XMLSchema-misc"
  exclude-result-prefixes="f">

  <xsl:template name="perform-redefine">
    <xsl:param name="redefineElement" as="element(xs:redefine)"/>
    <xsl:param name="redefinedSchema" as="element(xs:schema)"/>
    <xsl:result-document>
      <xsl:apply-templates select="$redefinedSchema">
        <xsl:with-param name="redefineElement" select="$redefineElement"/>
        <xsl:with-param name="prefixForTargetNamespace" select="if (empty($redefinedSchema/@targetNamespace))
                                                                then ''
                                                                else f:prefix-from-namespace-uri($redefinedSchema/@targetNamespace, $redefinedSchema)"/>
      </xsl:apply-templates>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="xs:schema | xs:redefine"
                priority="5">
    <xsl:param name="redefineElement"/>
    <xsl:param name="prefixForTargetNamespace"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates>
        <xsl:with-param name="redefineElement" select="$redefineElement"/>
        <xsl:with-param name="prefixForTargetNamespace" select="$prefixForTargetNamespace"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xs:import" priority="5">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!--* replace xs:include elements with xs:redefine elements
      *-->
  <xsl:template match="xs:include" priority="5">
    <xsl:param name="redefineElement" as="element(xs:redefine)"/>
    <xsl:element name="xs:redefine">
      <xsl:copy-of select="@schemaLocation, $redefineElement/*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="xs:schema/xs:simpleType
                       | xs:schema/xs:complexType
                       | xs:schema/xs:group
                       | xs:schema/xs:attributeGroup">
    <xsl:param name="redefineElement" as="element(xs:redefine)"/>
    <xsl:param name="prefixForTargetNamespace" as="xs:string"/>
    <xsl:variable name="redefinable" select="."/>
    <xsl:variable name="redefinedBy" select="$redefineElement/*[f:component-name($redefinable) = f:component-name(.)]"/>
    <xsl:variable name="redefinedName" select="$redefinedBy/f:component-name(.)"/>
    <xsl:choose>
      <xsl:when test="count($redefinedBy) > 0">
        <xsl:element name="{node-name($redefinable)}">
          <xsl:attribute name="name">
            <xsl:value-of select="concat('_', @name)"/>
          </xsl:attribute>
          <xsl:copy-of select="node()|@*[name()!='name']"/>
        </xsl:element>
        <xsl:element name="{node-name($redefinedBy)}">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates select="$redefinedBy/*">
            <xsl:with-param name="redefineElement" select="$redefineElement"/>
            <xsl:with-param name="prefixForTargetNamespace" select="$prefixForTargetNamespace"/>
            <xsl:with-param name="redefinedName" select="$redefinedName"/>
            <xsl:with-param name="redefinable" select="$redefinable"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <!-- copy the original redefinable, no changes should be made -->
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*">
    <xsl:param name="redefineElement" as="element(xs:redefine)"/>
    <xsl:param name="prefixForTargetNamespace" as="xs:string"/>
    <xsl:param name="redefinable" as="element()"/>
    <xsl:param name="redefinedName" as="xs:QName"/>
    <xsl:element name="{node-name(.)}">
      <xsl:apply-templates select="@*|node()">
        <xsl:with-param name="redefineElement" select="$redefineElement"/>
        <xsl:with-param name="prefixForTargetNamespace" select="$prefixForTargetNamespace"/>
        <xsl:with-param name="redefinable" select="$redefinable"/>
        <xsl:with-param name="redefinedName" select="$redefinedName"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:param name="redefineElement" as="element(xs:redefine)"/>
    <xsl:param name="prefixForTargetNamespace" as="xs:string"/>
    <xsl:param name="redefinedName" as="xs:QName"/>
    <xsl:choose>
      <xsl:when test="f:matches-name($redefinedName, .)">
        <xsl:attribute name="{node-name(.)}">
          <xsl:value-of select="if ($prefixForTargetNamespace eq '')
                                then concat('_', .)
                                else concat($prefixForTargetNamespace, ':_', local-name-from-QName($redefinedName))"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--* Redefine children of xs:schema, xs:redefine,
      * and xs:override which match children of
      * $redefineElement. Retain others.
      *-->
  <!-- <xsl:template match="xs:schema/*
                       | xs:redefine/*
                       | xs:override/*"
                priority="3">
    <xsl:param name="redefineElement"/>
    <xsl:variable name="original" select="."/>
    <xsl:variable name="replacement"
                  select="$redefineElement/*
                          [node-name(.)
                            = node-name($original)
                          and
                          f:component-name(.)
                            = f:component-name($original)]"/>
    <xsl:copy-of select="($replacement, $original)[1]"/>
  </xsl:template> -->

  <!--* change xs:redefine elements:  children which match
      * children of $redefineElement are replaced, others
      * are kept, and at the end all children of
      * $redefineElement not already inserted are added.
      *-->
  <!-- <xsl:template match="xs:redefine"
                priority="5">
    <xsl:param name="redefineElement"/>
    <xsl:element name="xs:redefine">
      <xsl:attribute name="schemaLocation">
        <xsl:value-of select="@schemaLocation"/>
      </xsl:attribute> -->
      <!-- <xsl:apply-templates>
        <xsl:with-param name="redefineElement" select="$redefineElement"/>
      </xsl:apply-templates> -->
      <!-- <xsl:apply-templates select="$redefineElement/*"
                           mode="copy-unmatched">
        <xsl:with-param name="redefineElement"
                        select="$redefineElement"/>
        <xsl:with-param name="redefinedRedefine"
                        select="."/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template> -->

  <xsl:function name="f:component-name" as="xs:QName">
    <xsl:param name="component" as="element()"/>
    <xsl:sequence select="QName($component/ancestor::xs:schema/@targetNamespace,
                                $component/@name)"/>
  </xsl:function>

  <xsl:function name="f:matches-name" as="xs:boolean">
    <xsl:param name="name" as="xs:QName"/>
    <xsl:param name="attr" as="attribute()"/>
    <xsl:variable name="tns" select="$attr/ancestor::xs:schema/@targetNamespace"/>
    <xsl:variable name="localName" select="if (contains(string($attr), ':')) then substring-after(string($attr), ':') else string($attr)"/>
    <xsl:sequence select="(if (empty($tns)) then '' else $tns) = namespace-uri-from-QName($name)
                          and $localName = local-name-from-QName($name)"/>
  </xsl:function>

  <xsl:function name="f:prefix-from-namespace-uri" as="xs:string">
    <xsl:param name="namespace-uri" as="xs:anyURI"/>
    <xsl:param name="element" as="element()"/>
    <xsl:variable name="prefixes" select="in-scope-prefixes($element)" as="xs:string*"/>
    <xsl:value-of select="(filter($prefixes, function($p) {namespace-uri-for-prefix($p, $element) = $namespace-uri})[1], '')[1]"/>
  </xsl:function>

</xsl:transform>