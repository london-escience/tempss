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
     <xsl:text> </xsl:text><xsl:value-of select="BAMFile"/>
  </xsl:template>

  <xsl:template match="/SAMTOOLS_index">
    <commandline>
      <xsl:apply-templates select="Inputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
