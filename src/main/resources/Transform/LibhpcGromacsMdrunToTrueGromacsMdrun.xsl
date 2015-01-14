<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/GROMACS_mdrun">
<!-- 
    <xsl:if test="not(ProblemSpecification/Geometry/GeometryAndBoundaryConditions/GEOMETRY)">
      <Error>
        Transform error: Geometry has not been supplied or is not in correct format.
      </Error>
      <xsl:message terminate="yes">
        Transform error: Geometry has not been supplied or is not in correct format.
      </xsl:message>
    </xsl:if>
-->
-s <xsl:value-of select="Inputs/MandatoryInputs/SimulationInput"/>
-e <xsl:value-of select="Outputs/MandatoryOutputs/EnergyFile"/>
  </xsl:template>
</xsl:stylesheet>
