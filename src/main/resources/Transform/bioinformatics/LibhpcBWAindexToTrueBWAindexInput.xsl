<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no"/>
  <xsl:strip-space elements="*"/>
<!--
  <xsl:output method="text" indent="yes"/>
  <xsl:strip-space elements="*"/>
-->

  <xsl:template match="Inputs">
     <xsl:text> </xsl:text><xsl:value-of select="FASTAFile"/>
  </xsl:template>

  <xsl:template match="IndexConfiguration">
    <xsl:choose>
      <xsl:when test="BWTAlgorithm!='auto'">
         -a <xsl:value-of select="BWTAlgorithm"/>
       </xsl:when>
       <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="OutputFilePrefix/Default"></xsl:when>
      <xsl:otherwise> -p <xsl:value-of select="OutputFilePrefix/Specified"/></xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="FileName64='yes'"> -6 </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/BWA_index">
    <commandline>
      <xsl:apply-templates select="IndexConfiguration"/>
      <xsl:apply-templates select="Inputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
