<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no"/>
  <xsl:strip-space elements="*"/>
<!--
  <xsl:output method="text" indent="yes"/>
  <xsl:strip-space elements="*"/>
-->

  <xsl:template match="Outputs/MandatoryOutputs">
    <xsl:choose>
      <xsl:when test="IndexFile/NotProvided"></xsl:when>
      <xsl:otherwise> -n <xsl:value-of select="IndexFile/FileName"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/GROMACS_pdb2gmx">
<commandline>
-f <xsl:value-of select="Inputs/MandatoryInputs/InputStructureFile"/>
-o <xsl:value-of select="Outputs/MandatoryOutputs/OutputStructureFile"/>
-p <xsl:value-of select="Outputs/MandatoryOutputs/OutputTopologyFile"/>
-water <xsl:value-of select="ProblemSetup/WaterModel"/>
-ff <xsl:value-of select="ProblemSetup/ForceField"/>
<xsl:apply-templates select="Outputs/OptionalOutputs"/>
</commandline>
  </xsl:template>
</xsl:stylesheet>
