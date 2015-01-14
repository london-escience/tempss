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
    -f <xsl:value-of select="InputStructureFile"/>
  </xsl:template>

  <xsl:template match="Outputs/MandatoryOutputs">
    -o <xsl:value-of select="OutputStructureFile"/>
  </xsl:template>

  <xsl:template match="ProblemSetup">
    <xsl:choose>
      <xsl:when test="DistanceBetweenSoluteAndBox/Default"></xsl:when>
      <xsl:otherwise> -d <xsl:value-of select="DistanceBetweenSoluteAndBox/Specified"/></xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="CentreMoleculeInBox = 'yes'"> -c</xsl:when>
      <xsl:when test="CentreMoleculeInBox = 'no'"> -noc</xsl:when>
    </xsl:choose>
    -bt <xsl:value-of select="BoxType"/>
  </xsl:template>

  <xsl:template match="/GROMACS_editconf">
    <commandline>
      <xsl:apply-templates select="Inputs/MandatoryInputs"/>
      <xsl:apply-templates select="Outputs/MandatoryOutputs"/>
      <xsl:apply-templates select="ProblemSetup"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
