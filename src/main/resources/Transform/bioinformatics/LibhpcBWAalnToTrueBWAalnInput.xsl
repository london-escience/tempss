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
     <xsl:text> </xsl:text><xsl:value-of select="FASTQFile"/>
  </xsl:template>

  <xsl:template match="IndexConfiguration">
    <xsl:apply-templates select="IlluminaFormattedInput"/>
    <xsl:apply-templates select="BamInput"/>
  </xsl:template>
  
  <xsl:template match="IlluminaFormattedInput">
  	<xsl:choose>
      <xsl:when test=".='yes'">
         <xsl:text> -I </xsl:text>
       </xsl:when>
       <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="BamInput">
    <xsl:choose>
      <xsl:when test=".='no'"></xsl:when>
      <xsl:when test=".='BAM format (single-end reads)'">
        <xsl:text> -b0 </xsl:text>
      </xsl:when>
      <xsl:when test=".='BAM format (first read in pair)'">
        <xsl:text> -b1 </xsl:text>
      </xsl:when>
      <xsl:when test=".='BAM format (second read in pair)'">
        <xsl:text> -b2 </xsl:text>
      </xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="AlignmentConfiguration">

  </xsl:template>
  
  <xsl:template match="AlignmentConfiguration/Gaps">

  </xsl:template>
  
  <xsl:template match="RuntimeConfiguration">

  </xsl:template>
  
  <xsl:template match="Outputs">
    <xsl:text disable-output-escaping="yes"> &gt; </xsl:text><xsl:value-of select="OutputIndexFile"/>
  </xsl:template>

  <xsl:template match="/BWA_aln">
    <commandline>
      <xsl:apply-templates select="InputConfiguration"/>
      <xsl:apply-templates select="AlignmentConfiguration"/>
      <xsl:apply-templates select="AlignmentConfiguration/Gaps"/>
      <xsl:apply-templates select="RuntimeConfiguration"/>
      <xsl:apply-templates select="Inputs"/>
      <xsl:apply-templates select="Outputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
