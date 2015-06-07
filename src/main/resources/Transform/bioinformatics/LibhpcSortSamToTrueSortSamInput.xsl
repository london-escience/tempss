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
     <xsl:text> I=</xsl:text><xsl:value-of select="SamOrBamInputFile"/>
  </xsl:template>

  <xsl:template match="Configuration">
    <xsl:apply-templates select="SortOrder"/>
  </xsl:template>
  
  <xsl:template match="SortOrder">
  	<xsl:choose>
      <xsl:when test=".='coordinate (default)'">
         <xsl:text> SORT_ORDER=coordinate</xsl:text>
       </xsl:when>
       <xsl:otherwise>
         <xsl:text> SORT_ORDER=</xsl:text><xsl:value-of select="SortOrder"/>
       </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <xsl:template match="Outputs">
    <xsl:text disable-output-escaping="yes"> O=</xsl:text><xsl:value-of select="SortedOutputFile"/>
  </xsl:template>

  <xsl:template match="/PICARD_SortSam">
    <commandline>
      <xsl:apply-templates select="Inputs"/>
      <xsl:apply-templates select="Outputs"/>
      <xsl:apply-templates select="Configuration"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
