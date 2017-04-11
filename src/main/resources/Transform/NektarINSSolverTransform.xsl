<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <!-- These two templates allow us to copy a region of xml but convert
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

  <xsl:template match ="Geometry" mode="ErrorChecks">
     <xsl:if test="not(NEKTAR/GEOMETRY)">
      <Error>
        Transform error: Geometry has not been supplied or is not in correct format.
      </Error>
      <xsl:message terminate="yes">
        Transform error: Geometry has not been supplied or is not in correct format.
      </xsl:message>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="Expansion">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="NUMMODES"><xsl:value-of select="Expansion/PolynomialOrder + 1"/></xsl:attribute>
    <xsl:attribute name="TYPE"><xsl:value-of select="Expansion/BasisType"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="CompositeNavierStokes">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="COMPOSITE"><xsl:value-of select="normalize-space(Geometry/NEKTAR/GEOMETRY/DOMAIN)"/></xsl:attribute>
  </xsl:template>

    <xsl:template match="ProblemSpecification" mode ="Parameters">
    <P>TimeStep = <xsl:value-of select="TimeStep"/></P>
    <xsl:if test="TimeStep='0'">    
    <P>CFL = <xsl:value-of select="CFL"/></P>
    </xsl:if>
    <P>FinTime = <xsl:value-of select="FinalTime"/></P>
  </xsl:template>

  <xsl:template match="NumericalAlgorithm" mode ="Parameters">
    <xsl:if test="TimeIntegration/DiffusionAdvancement!='Explicit'">
      <xsl:choose>
        <xsl:when test="MatrixInversion/Iterative/IterativeSolverTolerance">
          <P>IterativeSolverTolerance = <xsl:value-of select="MatrixInversion/Iterative/IterativeSolverTolerance"/></P>
        </xsl:when>
        <xsl:when test="GlobalSysSolution/MatrixInversion/InversionType/Iterative/IterativeSolverTolerance">
          <P>IterativeSolverTolerance = <xsl:value-of select="GlobalSysSolution/MatrixInversion/InversionType/Iterative/IterativeSolverTolerance"/></P>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Admin" mode ="Parameters">
    <P>IO_CheckSteps = <xsl:value-of select="IO_CheckSteps"/></P>
    <P>IO_InfoSteps = <xsl:value-of select="IO_InfoSteps"/></P>
  </xsl:template>

  <xsl:template match="Physics" mode ="NavierStokesParameters">
    <P>Kinvis = <xsl:value-of select="KinematicViscosity"/></P>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="NavierStokesSolverInfo">
    <I PROPERTY="SolverType">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="SolutionMethod" />
      </xsl:attribute>
    </I>
    <I PROPERTY="EQTYPE">
      <xsl:attribute name="VALUE">UnsteadyNavierStokes</xsl:attribute>
    </I>
    <I PROPERTY="AdvectionForm">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="AdvectionForm" />
      </xsl:attribute>
    </I>
  </xsl:template>

  <xsl:template match="NumericalAlgorithm" mode ="SolverInfo">
    <I PROPERTY="Projection">
      <xsl:attribute name="VALUE">
        <xsl:choose>
          <xsl:when test="Projection = 'ContinuousGalerkin'">Continuous</xsl:when>
          <xsl:when test="Projection = 'DiscontinuousGalerkin'">DisContinuous</xsl:when>
        </xsl:choose>
      </xsl:attribute>
    </I>
    <I PROPERTY="DiffusionAdvancement">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="TimeIntegration/DiffusionAdvancement" />
      </xsl:attribute>
    </I>
    <I PROPERTY="TimeIntegrationMethod">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="TimeIntegration/TimeIntegrationMethod" />
      </xsl:attribute>
    </I>
    <!-- Possible inputs to nektar++ are:
            DirectFull, DirectStaticCond, DirectMultiLevelStaticCond, IterativeFull, 
            IterativeStaticCond, XxtFull, XxtStaticCond -->
    <xsl:if test="GlobalSysSolution">
      <I PROPERTY="GlobalSysSoln">
        <xsl:attribute name="VALUE">
          <xsl:choose>
            <xsl:when test="MatrixInversion/Iterative/SubStructuring = 'StaticCondensation'">IterativeStaticCond</xsl:when>
            <xsl:when test="MatrixInversion/Iterative/SubStructuring = 'Full'">IterativeFull</xsl:when>
            <xsl:when test="MatrixInversion/Direct/SubStructuring = 'StaticCondensation'">DirectStaticCond</xsl:when>
            <xsl:when test="MatrixInversion/Direct/SubStructuring = 'Full'">DirectFull</xsl:when>
            <!-- Options for revised GlobalSysSolution with separate InversionType block -->
            <xsl:when test="GlobalSysSolution/MatrixInversion/InversionType/Iterative/SubStructuring = 'StaticCondensation'">IterativeStaticCond</xsl:when>
            <xsl:when test="GlobalSysSolution/MatrixInversion/InversionType/Iterative/SubStructuring = 'Full'">IterativeFull</xsl:when>
            <xsl:when test="GlobalSysSolution/MatrixInversion/InversionType/Direct/SubStructuring = 'StaticCondensation'">DirectStaticCond</xsl:when>
            <xsl:when test="GlobalSysSolution/MatrixInversion/InversionType/Direct/SubStructuring = 'Full'">DirectFull</xsl:when>

            <xsl:otherwise>
              <xsl:message terminate="yes">
                Error: unhandled matrix inversion approach -> cannot set GlobalSysSoln
              </xsl:message>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </I>
    </xsl:if>

    <xsl:if test="SpectralhpDealiasing">
      <I PROPERTY="SPECTRALHPDEALIASING" VALUE="True" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="Variable" mode ="InitialConditionVars">
    <xsl:if test="VariableName">
      <xsl:message>Processing initial condition variables!</xsl:message>
      <E> 
        <xsl:attribute name="VAR">
          <xsl:value-of select="VariableName"/>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="Type/Expression">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="Type/Expression"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>Unable to set the value for this variable, it uses an unsupported type.</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </E>
    </xsl:if>    
  </xsl:template>
  
  
  <!-- 
    -  Generate boundary conditions and boundary regions.  
    -
   -->
  <xsl:template match="*" mode="BoundaryRegionsAndConditions">
    <xsl:message>Processing boundary conditions and regions.</xsl:message>
    
    <xsl:apply-templates select="." mode="NewBoundaryRegions" />
    
    <!--
    <xsl:variable name="BoundaryData">
      <xsl:call-template name="GenBC"/>
    </xsl:variable>
    <xsl:for-each select="exslt:node-set($BoundaryData)/*">
      <xsl:copy/>
    </xsl:for-each>
     -->
     
    <!--  <xsl:apply-templates select="." mode="NewBoundaryRegions" /> -->
      
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
  
  <xsl:variable name="j" select="0" />
  <xsl:template match="BoundaryRegion" mode="BoundaryRegions">
    <xsl:variable name="jcount"><xsl:number/></xsl:variable> 
    <xsl:if test="BoundaryCondition">
      <xsl:comment>
        <xsl:text> </xsl:text><xsl:value-of select="BoundaryCondition"/><xsl:text> </xsl:text>
      </xsl:comment>
    </xsl:if>
    <B>
      <xsl:attribute name="ID"><xsl:value-of select="$jcount - 1"/></xsl:attribute>
      <xsl:text>C[</xsl:text><xsl:value-of select="CompositeID"/><xsl:text>]</xsl:text>
    </B>
  </xsl:template>
  <!-- Ignore BoundaryCondition elements when processing boundary regions -->
  <xsl:template match="BoundaryCondition" mode="BoundaryRegions" />

  <xsl:template match="BoundaryDetails" mode ="NewBoundaryRegions">
	<xsl:variable name="br">
    <REGIONSANDMAPPINGS>
      <xsl:for-each select="BoundaryCondition">
      <xsl:variable name="jcount"><xsl:number/></xsl:variable>
      <xsl:variable name="bcname" select="BoundaryConditionName"/>
      <BOUNDARYREGION>
        <!-- Get the name for this boundary condition -->
        
        
 		
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
    <!-- 
    <xsl:message>Test <xsl:value-of select="$br"/></xsl:message>
     -->
    <BOUNDARYCONDITIONS>
      <xsl:apply-templates select="BoundaryCondition" mode="BoundaryConditions">
        <xsl:with-param name="mappings" select="exslt:node-set($br)/REGIONSANDMAPPINGS"/>
      </xsl:apply-templates>
      <!-- 
      <xsl:for-each select="exslt:node-set($bc)/*">
        <xsl:copy/>
      </xsl:for-each>
       -->
    </BOUNDARYCONDITIONS>
    
    
    <xsl:variable name="somevar">
      <a value="1"></a>
      <b value="2"></b>
      <c value="3"></c>
    </xsl:variable>
    <xsl:value-of select="$somevar"/>
    
  </xsl:template>
  

  
  <xsl:template name="GenBCTest">
    <elemento/>
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

  <!-- Incompressible Navier-Stokes transform -->
  <xsl:template match="/IncompressibleNavierStokes">
    <xsl:apply-templates select="ProblemSpecification/Geometry" mode ="ErrorChecks"/>
    <NEKTAR>
      <!-- Copy in the geometry -->
      <xsl:copy-of select="ProblemSpecification/Geometry/NEKTAR/GEOMETRY"/>
      
      <EXPANSIONS>
        <E>
          <xsl:apply-templates select="ProblemSpecification" mode ="CompositeNavierStokes"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="Expansion"/>
          <xsl:attribute name="FIELDS">u,v,p</xsl:attribute>
        </E>
      </EXPANSIONS>
      
      <CONDITIONS>
        <SOLVERINFO>
          <xsl:apply-templates select="ProblemSpecification" mode ="NavierStokesSolverInfo"/>
          <xsl:apply-templates select="NumericalAlgorithm" mode ="SolverInfo"/>
        </SOLVERINFO>
        
        <PARAMETERS>
          <xsl:apply-templates select="NumericalAlgorithm" mode ="Parameters"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="Parameters"/>
          <xsl:apply-templates select="Admin" mode ="Parameters"/>
          <xsl:apply-templates select="Physics" mode ="NavierStokesParameters"/>
        </PARAMETERS>
      
        <VARIABLES>
          <V ID="0">u</V>
          <V ID="1">v</V>
          <V ID="2">p</V>
        </VARIABLES>
        
        <xsl:apply-templates select="ProblemSpecification/BoundaryDetails" mode="BoundaryRegionsAndConditions"/>
                
        <FUNCTION NAME="InitialConditions">
          <xsl:apply-templates select="ProblemSpecification/InitialConditions" mode ="InitialConditionVars"/>
        </FUNCTION>

      </CONDITIONS>

    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>
