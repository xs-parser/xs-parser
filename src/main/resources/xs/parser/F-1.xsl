<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:f="http://www.w3.org/2008/05/XMLSchema-misc"
  version="2.0">

    <xsl:param name="newTargetNamespace" as="xs:anyURI"
      required="yes"/>
    <xsl:param name="prefixForTargetNamespace" as="xs:string"
      select="f:generateUniquePrefix(., 0)"/>

    <xsl:template match="@*|node()">
      <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
    </xsl:template>

    <xsl:template match="xs:schema">
      <xsl:copy>
        <xsl:namespace name="{$prefixForTargetNamespace}"
          select="$newTargetNamespace"/>
        <xsl:apply-templates select="@*"/>
        <xsl:attribute name="targetNamespace"
          select="$newTargetNamespace"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>

    <xsl:template match="xs:*/@ref
      | xs:*/@base
      | xs:*/@type
      | xs:schema/@defaultAttributes
      | xs:keyref/@refer
      | xs:list/@itemType">
      <xsl:choose>
        <xsl:when test="namespace-uri-from-QName(resolve-QName(string(.), ..))=''">
          <xsl:attribute name="{name()}"
            select="concat($prefixForTargetNamespace,
            ':',
            local-name-from-QName(resolve-QName(string(.), ..)))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="@memberTypes | @substitutionGroup">
      <xsl:variable name="context" select=".."/>
      <xsl:variable name="values" as="xs:string+">
        <xsl:for-each select="tokenize(., '\s+')">
          <xsl:variable name="oldValue"
            select="resolve-QName(., $context)"
            as="xs:QName"/>
          <xsl:sequence
            select="if (namespace-uri-from-QName($oldValue) eq '')
            then concat($prefixForTargetNamespace, ':',
            local-name-from-QName($oldValue))
            else string(.)"/>
        </xsl:for-each>
      </xsl:variable>
      <xsl:attribute name="{name()}" select="string-join($values, ' ')"/>
    </xsl:template>

  <xsl:template match="@notQName">
    <xsl:variable name="context" select=".."/>
    <xsl:variable name="values" as="xs:string+">
      <xsl:for-each select="tokenize(., '\s+')">
        <xsl:variable name="oldValue"
          select="if (starts-with(.,'##'))
          then ()
          else resolve-QName(., $context)"
          as="xs:QName?"/>
        <xsl:sequence
          select="if (starts-with(.,'##'))
          then string(.)
          else if ((namespace-uri-from-QName($oldValue) eq '')
                  or
                  empty(namespace-uri-from-QName($oldValue)))
          then concat($prefixForTargetNamespace,
                     ':',
                     local-name-from-QName($oldValue))
          else string(.)"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:attribute name="{name()}" select="string-join($values, ' ')"/>
  </xsl:template>


    <xsl:function name="f:generateUniquePrefix" as="xs:string">
      <xsl:param name="xsd"/>
      <xsl:param name="try" as="xs:integer"/>
      <xsl:variable name="disallowed"
        select="distinct-values($xsd//*/in-scope-prefixes(.))"/>
      <xsl:variable name="candidate"
        select="xs:string(concat('p', $try))"/>
      <xsl:sequence select="if ($candidate = $disallowed) then
        f:generateUniquePrefix($xsd, $try+1)
        else
        $candidate"/>
    </xsl:function>

</xsl:transform>