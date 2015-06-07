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
    <xsl:choose>
      <xsl:when test="OptionalRefListFile/NotProvided"/>
      <xsl:otherwise>
        <xsl:text> -R </xsl:text><xsl:value-of select="OptionalRefListFile/FileName"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="OptionalOutputReadGroupListFile/NotProvided"/>
      <xsl:otherwise>
        -t <xsl:value-of select="OptionalOutputReadGroupListFile/FileName"/>
      </xsl:otherwise>
    </xsl:choose>
     <xsl:text> </xsl:text><xsl:value-of select="SAMorBAMFile"/>
  </xsl:template>

  <xsl:template match="FormatConfiguration">

  </xsl:template>
  
  <xsl:template match="GeneralConfiguration">

  </xsl:template>

  <xsl:template match="Outputs">
    <xsl:text> -o </xsl:text><xsl:value-of select="OutputFile"/>
  </xsl:template>

  <xsl:template match="/SAMTOOLS_view">
    <commandline>
      <xsl:apply-templates select="FormatConfiguration"/>
      <xsl:apply-templates select="GeneralConfiguration"/>
      <xsl:apply-templates select="Outputs"/>
      <xsl:apply-templates select="Inputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
