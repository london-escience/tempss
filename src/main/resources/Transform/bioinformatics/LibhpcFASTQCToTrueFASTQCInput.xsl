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
     <xsl:text> </xsl:text><xsl:value-of select="SequenceFiles"/>
  </xsl:template>

  <xsl:template match="Configuration">

  </xsl:template>
  
  <xsl:template match="Outputs">
     <xsl:choose>
      <xsl:when test="OutputFile/Default"/>
      <xsl:otherwise>
        <xsl:text disable-output-escaping="yes"> --outdir=</xsl:text><xsl:value-of select="OutputDirectory"/>
        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/FASTQC">
    <commandline>
      <xsl:apply-templates select="Configuration"/>
      <xsl:apply-templates select="Outputs"/>
      <xsl:apply-templates select="Inputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
