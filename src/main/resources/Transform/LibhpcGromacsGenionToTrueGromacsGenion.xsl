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
    -s <xsl:value-of select="RunInputFile"/>
  </xsl:template>

  <xsl:template match="Inputs/OptionalInputs">
    <xsl:choose>
      <xsl:when test="TopologyFile/NotSpecified"/>
      <xsl:otherwise>
        -p <xsl:value-of select="TopologyFile/FileName"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="Outputs/MandatoryOutputs">
    -o <xsl:value-of select="OutputStructureFile"/>
  </xsl:template>

  <xsl:template match="ProblemSetup">
    <xsl:choose>
      <xsl:when test="PositiveIon/Default"/>
      <xsl:otherwise>
        -pname <xsl:value-of select="PositiveIon/Specified"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="NegativeIon/Default"/>
      <xsl:otherwise>
        -nname <xsl:value-of select="NegativeIon/Specified"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="NumberOfPositiveIons/Default"/>
      <xsl:otherwise>
        -np <xsl:value-of select="NumberOfPositiveIons/Specified"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="NumberOfNegativeIons/Default"/>
      <xsl:otherwise>
        -nn <xsl:value-of select="NumberOfNegativeIons/Specified"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/GROMACS_genion">
    <commandline>
      <xsl:apply-templates select="Inputs/MandatoryInputs"/>
      <xsl:apply-templates select="Inputs/OptionalInputs"/>
      <xsl:apply-templates select="Outputs/MandatoryOutputs"/>
      <xsl:apply-templates select="ProblemSetup"/>
    </commandline>
  </xsl:template>
</xsl:stylesheet>
