<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:strip-space elements="*"/>
  
  <!-- These two templates allow us to copy a region of  but convert
        the node names to upper case. 
        NB: when two templates match, the last one gets triggered first.
        Hence, the node name gets converted to upper case. 
        Then the contents are copied.-->
  <xsl:template match="node()|@*" mode="NodesToUpperCase">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="*" mode="NodesToUpperCase">
    <xsl:element name="{
            translate(name(.),
            'abcdefghijklmnopqrstuvwxyz',
            'ABCDEFGHIJKLMNOPQRSTUVWXYZ')}">
      <xsl:apply-templates select="node()|@*" mode="NodesToUpperCase"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="Variable" mode ="InitialConditionVars">
    <xsl:if test="VariableName">
        <xsl:choose>
          <xsl:when test="Type/Expression">
            <E> 
              <xsl:attribute name="VAR">
                <xsl:value-of select="VariableName"/>
              </xsl:attribute>
              <xsl:attribute name="VALUE">
                <xsl:value-of select="Type/Expression"/>
              </xsl:attribute>
            </E>
          </xsl:when>
          <xsl:when test="Type/File">
            <F>
              <xsl:attribute name="FILE">
                <xsl:value-of select ="Type/File"/>
              </xsl:attribute>
            </F>
          </xsl:when>
        </xsl:choose>
    </xsl:if>    
  </xsl:template>
  
  <xsl:template match="InitialConditions" mode ="HandleConditions">
    <xsl:apply-templates select="Variable" mode ="InitialConditionVars"/>
  </xsl:template>

  <!-- Incompressible Navier-Stokes Test transform -->
  <xsl:template match="/IncompressibleNavierStokes">
    <NEKTAR>
      <xsl:apply-templates select="InitialConditions" mode ="HandleConditions"/>  
    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>
