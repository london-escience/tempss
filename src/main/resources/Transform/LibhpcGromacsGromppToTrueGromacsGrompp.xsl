<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no"/>
  <xsl:strip-space elements="*"/>
<!--
  <xsl:output method="text" indent="yes"/>
  <xsl:strip-space elements="*"/>
-->

  <xsl:template match="Inputs/MandatoryInputs">
    -f <xsl:value-of select="GromppInputFile"/>
    -c <xsl:value-of select="InputStructureFile"/>
    -p <xsl:value-of select="InputTopologyFile"/>
  </xsl:template>
<!--
  <xsl:template match="Inputs/OptionalInputs">
    <xsl:choose>
      <xsl:when test="SoluteStructureFile/NotProvided"></xsl:when>
      <xsl:otherwise> -cp <xsl:value-of select="SoluteStructureFile/FileName"/></xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="TopologyFile/NotProvided"></xsl:when>
      <xsl:otherwise> -p <xsl:value-of select="TopologyFile/FileName"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
-->
  <xsl:template match="Outputs/MandatoryOutputs">
    -o <xsl:value-of select="RunInputFile"/>
  </xsl:template>

  <xsl:template match="/GROMACS_grompp">
    <commandline>
      <xsl:apply-templates select="Inputs/MandatoryInputs"/>
<!--      <xsl:apply-templates select="Inputs/OptionalInputs"/>-->
      <xsl:apply-templates select="Outputs/MandatoryOutputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
