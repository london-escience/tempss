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
     <xsl:text> </xsl:text><xsl:value-of select="ZippedInputFile"/>
  </xsl:template>
  
  <xsl:template match="Outputs">
     <xsl:choose>
      <xsl:when test="OutputFile/Default"/>
      <xsl:otherwise>
        <xsl:text disable-output-escaping="yes"> -c &gt; </xsl:text><xsl:value-of select="OutputFile/Specified"/>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/Unzip_Files">
    <commandline>
      <xsl:apply-templates select="Outputs"/>
      <xsl:apply-templates select="Inputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
