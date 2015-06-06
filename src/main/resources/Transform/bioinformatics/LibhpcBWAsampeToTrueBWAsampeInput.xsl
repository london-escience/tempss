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
     <xsl:text> </xsl:text><xsl:value-of select="FASTQAlignmentIndex1"/>
     <xsl:text> </xsl:text><xsl:value-of select="FASTQAlignmentIndex2"/>
     <xsl:text> </xsl:text><xsl:value-of select="FASTQFile1"/>
     <xsl:text> </xsl:text><xsl:value-of select="FASTQFile2"/>
  </xsl:template>
  
  <xsl:template match="Configuration">

  </xsl:template>
    
  <xsl:template match="Outputs">
    <xsl:text disable-output-escaping="yes"> &gt; </xsl:text><xsl:value-of select="OutputSamFile"/>
  </xsl:template>

  <xsl:template match="/BWA_sampe">
    <commandline>
      <xsl:apply-templates select="Configuration"/>
      <xsl:apply-templates select="Inputs"/>
      <xsl:apply-templates select="Outputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
