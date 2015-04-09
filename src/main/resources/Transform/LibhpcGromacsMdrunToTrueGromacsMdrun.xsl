<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="no"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="Inputs/MandatoryInputs">
    -s <xsl:value-of select="SimulationInput"/>
  </xsl:template>
  
  <xsl:template match="Outputs/MandatoryOutputs">
    -e <xsl:value-of select="EnergyFile"/>
  </xsl:template>
  
  <xsl:template match="/GROMACS_mdrun">
    <commandline>
      <xsl:apply-templates select="Inputs/MandatoryInputs"/>
      <xsl:apply-templates select="Outputs/MandatoryOutputs"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
