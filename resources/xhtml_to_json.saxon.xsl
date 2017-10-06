<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:json_util="http://experian.com/json-util">

	<!-- Transforms arbitrary XML to the JSONML format, see: http://www.jsonml.org/ -->
	
	<xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="utf-8"/>
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="node()">
		<xsl:text>[</xsl:text>
		<xsl:value-of select="json_util:toJsonNoHtmlEscaping(name())"/>
		<xsl:if test="@*">
			<xsl:text>,</xsl:text>
			<xsl:text>{</xsl:text>
			<xsl:for-each select="@*">
				<xsl:apply-templates select="."/>
				<xsl:if test="position() != last()">
					<xsl:text>,</xsl:text>
				</xsl:if>
			</xsl:for-each>
			<xsl:text>}</xsl:text>
		</xsl:if>
		<xsl:if test="node()">
			<xsl:text>,</xsl:text>
			<xsl:for-each select="node()">
				<xsl:apply-templates select="."/>
				<xsl:if test="position() != last()">
					<xsl:text>,</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
		<xsl:text>]</xsl:text>
	</xsl:template>

	<xsl:template match="text()" priority="10">
		<xsl:value-of select="json_util:toJsonNoHtmlEscaping(normalize-space(.))"/>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:value-of select="json_util:toJsonNoHtmlEscaping(name())"/>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="json_util:toJsonNoHtmlEscaping(normalize-space(.))"/>
	</xsl:template>
</xsl:stylesheet>
