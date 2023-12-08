<xsl:transform version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:f="http://www.w3.org/2008/05/XMLSchema-misc"
  exclude-result-prefixes="f">

  <xsl:template name="perform-override">
    <xsl:param name="overrideElement" as="element(xs:override)"/>
    <xsl:param name="overriddenSchema" as="element(xs:schema)"/>
    <xsl:result-document>
      <xsl:apply-templates select="$overriddenSchema">
        <xsl:with-param name="overrideElement" select="$overrideElement"/>
      </xsl:apply-templates>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="xs:schema | xs:redefine"
                priority="5">
    <xsl:param name="overrideElement"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates>
        <xsl:with-param name="overrideElement" select="$overrideElement"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xs:import" priority="5">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!--* Replace children of xs:schema, xs:redefine,
      * and xs:override which match children of
      * $overrideElement. Retain others.
      *-->
  <xsl:template match="xs:schema/*
                       | xs:redefine/*
                       | xs:override/*"
                       priority="3">
    <xsl:param name="overrideElement"/>
    <xsl:variable name="original" select="."/>
    <xsl:variable name="replacement"
                  select="$overrideElement/*
                          [node-name(.)
                            = node-name($original)
                          and
                          f:componentName(.)
                            = f:componentName($original)]"/>
    <xsl:copy-of select="($replacement, $original)[1]"/>
  </xsl:template>

  <!--* replace xs:include elements with overrides
      *-->
  <xsl:template match="xs:include" priority="5">
    <xsl:param name="overrideElement"/>
    <xsl:element name="xs:override">
      <xsl:copy-of
          select="@schemaLocation, $overrideElement/*"/>
    </xsl:element>
  </xsl:template>

  <!--* change xs:override elements:  children which match
      * children of $overrideElement are replaced, others
      * are kept, and at the end all children of
      * $overrideElement not already inserted are added.
      *-->
  <xsl:template match="xs:override"
    priority="5">
    <xsl:param name="overrideElement"/>
    <xsl:element name="xs:override">
      <xsl:attribute name="schemaLocation">
        <xsl:value-of select="@schemaLocation"/>
      </xsl:attribute>
      <xsl:apply-templates>
        <xsl:with-param name="overrideElement" select="$overrideElement"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="$overrideElement/*"
        mode="copy-unmatched">
        <xsl:with-param name="overrideElement"
                        select="$overrideElement"/>
        <xsl:with-param name="overriddenOverride"
                                          select="."/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*" mode="copy-unmatched">
    <xsl:param name="overriddenOverride"/>
    <xsl:variable name="overriding" select="."/>
    <xsl:variable name="overridden"
                  select="$overriddenOverride/*[
                          node-name(.)
                            = node-name($overriding)
                          and
                          f:componentName(.)
                            = f:componentName($overriding)
                          ]"/>
    <xsl:choose>
      <xsl:when test="count($overridden) > 0">
        <!--* do nothing; this element already copied *-->
      </xsl:when>
      <xsl:when test="count($overridden) = 0">
        <!--* copy this element, it isn't already there *-->
        <xsl:copy-of select="."/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:function name="f:componentName" as="xs:QName">
    <xsl:param name="component" as="element()"/>
    <xsl:sequence select="
      QName($component/ancestor::xs:schema/@targetNamespace,
            $component/@name)"/>
  </xsl:function>

</xsl:transform>