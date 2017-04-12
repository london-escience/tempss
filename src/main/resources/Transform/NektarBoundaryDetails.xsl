<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">

  <!-- 
    -  Templates for generating Nektar++ boundary conditions and boundary 
    -  regions. This file is imported by other Nektar++ transforms.
   -->
  <xsl:template match="*" mode="BoundaryRegionsAndConditions">
    <xsl:message>Processing boundary conditions and regions.</xsl:message>
    
    <xsl:apply-templates select="." mode="BoundaryRegions" />
      
  </xsl:template>
   
  <xsl:template match="BoundaryDetails" mode="BoundaryRegions">
	<xsl:variable name="br">
    <REGIONSANDMAPPINGS>
      <xsl:for-each select="BoundaryCondition">
        <xsl:variable name="jcount"><xsl:number/></xsl:variable>
        <xsl:variable name="bcname" select="BoundaryConditionName"/>
          <BOUNDARYREGION>
            <!-- 
                 Now we need to look up all the BoundaryRegions within 
                 BoundaryDetails that have a BoundaryCondition value of $bcname...
                 To handle cases where there may be multiple composites involved
                 in a boundary region, we sort the BoundaryRegions associated with  
                 a given BoundaryCondition in order to build the composite 
                 expression. 
            -->
            <xsl:variable name="composites">
              <xsl:apply-templates select="../BoundaryRegion[BoundaryCondition=$bcname]" mode ="BuildBoundaryRegion">
                <xsl:with-param name="bcname" select="$bcname"/>
                <xsl:sort select="CompositeID" data-type="number" />
              </xsl:apply-templates>
            </xsl:variable>
            <xsl:message>Value of bcname is <xsl:value-of select="$bcname"/>. Composites are <xsl:value-of select="$composites"/></xsl:message>
          
            <B> 
              <xsl:attribute name="ID"><xsl:value-of select="$jcount - 1"/></xsl:attribute>
          
              <xsl:text>C[</xsl:text><xsl:value-of select="$composites"/><xsl:text>]</xsl:text>
          
            </B>
          </BOUNDARYREGION>
          <MAPPING>
            <xsl:attribute name="ID"><xsl:value-of select="$jcount - 1"/></xsl:attribute>
            <xsl:attribute name="BCNAME"><xsl:value-of select="$bcname"/></xsl:attribute>
          </MAPPING>
        </xsl:for-each>
      </REGIONSANDMAPPINGS>
    </xsl:variable>
    
    <BOUNDARYREGIONS>
      <xsl:copy-of select="exslt:node-set($br)/REGIONSANDMAPPINGS/BOUNDARYREGION/*"/>
    </BOUNDARYREGIONS>

    <BOUNDARYCONDITIONS>
      <xsl:apply-templates select="BoundaryCondition" mode="BoundaryConditions">
        <xsl:with-param name="mappings" select="exslt:node-set($br)/REGIONSANDMAPPINGS"/>
      </xsl:apply-templates>
    </BOUNDARYCONDITIONS>    
    
  </xsl:template>
  
   
  <xsl:template match="BoundaryCondition" mode="BoundaryConditions">
    <xsl:param name="mappings"/>
    
    <xsl:variable name="bcname"><xsl:value-of select="BoundaryConditionName"/></xsl:variable>
    
    <xsl:variable name="icount"><xsl:number/></xsl:variable>
    
    <REGION>
      <xsl:attribute name="REF"><xsl:value-of select="exslt:node-set($mappings)/MAPPING[@BCNAME=$bcname]/@ID"/></xsl:attribute>
        <xsl:apply-templates select="Variable" mode ="BCVariable"/>
    </REGION>
    
  </xsl:template>
  <!-- Ignore BoundaryRegion elements when processing boundary conditions -->
  <xsl:template match="BoundaryRegion" mode="BoundaryConditions" />
  
  <xsl:template match="Variable" mode ="BCVariable">
    <xsl:choose>
      <xsl:when test="ConditionType = 'Dirichlet (D)'">
        <D>
          <xsl:attribute name="VAR"><xsl:value-of select="VariableName"/></xsl:attribute>
          <xsl:choose>
            <xsl:when test="UserDefinedType">
              <xsl:if test="UserDefinedType = 'HighOrderPressure - INS Only'">
                <xsl:attribute name="USERDEFINEDTYPE">H</xsl:attribute>
              </xsl:if>
              <xsl:if test="UserDefinedType = 'TimeDependent'">
                <xsl:attribute name="USERDEFINEDTYPE">T</xsl:attribute>
              </xsl:if>
            </xsl:when>
          </xsl:choose>
          <xsl:choose>
	        <xsl:when test="ConditionValue/Expression">
	          <xsl:attribute name="VALUE">
	            <xsl:value-of select="ConditionValue/Expression"/>
	          </xsl:attribute>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:message>Unable to set the value for this boundary condition variable, it uses a value type that is currently unsupported.</xsl:message>
	        </xsl:otherwise>
	      </xsl:choose>
        </D>
      </xsl:when>
      <xsl:when test="ConditionType = 'Neumann (N)'">
        <N>
          <xsl:attribute name="VAR"><xsl:value-of select="VariableName"/></xsl:attribute>
          <xsl:choose>
            <xsl:when test="UserDefinedType">
              <xsl:if test="UserDefinedType = 'HighOrderPressure - INS Only'">
                <xsl:attribute name="USERDEFINEDTYPE">H</xsl:attribute>
              </xsl:if>
              <xsl:if test="UserDefinedType = 'TimeDependent'">
                <xsl:attribute name="USERDEFINEDTYPE">T</xsl:attribute>
              </xsl:if>
            </xsl:when>
          </xsl:choose>
          <xsl:choose>
	        <xsl:when test="ConditionValue/Expression">
	          <xsl:attribute name="VALUE">
	            <xsl:value-of select="ConditionValue/Expression"/>
	          </xsl:attribute>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:message>Unable to set the value for this boundary condition variable, it uses a value type that is currently unsupported.</xsl:message>
	        </xsl:otherwise>
	      </xsl:choose>
        </N>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="BoundaryRegion" mode="BuildBoundaryRegion">
    <xsl:param name="bcname"/>
    <xsl:if test="BoundaryCondition = $bcname">
      <xsl:choose>
        <!-- Updated this to generate composite data as a series of 
             comma-separated values rather than using a hyphen for a group of 
             contiguous boundary condition IDs 
        -->
      	<!--
        <xsl:when test="(position() = 1) and (position != last())">
          <xsl:text></xsl:text><xsl:value-of select="CompositeID"/>
        </xsl:when>
        <xsl:when test="position() = 1">
          <xsl:text></xsl:text><xsl:value-of select="CompositeID"/>
        </xsl:when>
        <xsl:when test="position() = last()">
          <xsl:text>-</xsl:text><xsl:value-of select="CompositeID"/>
        </xsl:when>
        -->
        <xsl:when test="position() = 1">
          <xsl:text></xsl:text><xsl:value-of select="CompositeID"/>
        </xsl:when>
        <xsl:when test="position() > 1">
          <xsl:text>,</xsl:text><xsl:value-of select="CompositeID"/>
        </xsl:when>
    </xsl:choose>
    </xsl:if>
  </xsl:template>

                
</xsl:stylesheet>