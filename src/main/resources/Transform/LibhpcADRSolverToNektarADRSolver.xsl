<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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

<!-- Template for global matrix optimization parameters -->
  <xsl:template match="GlobalMatrixEvaluation">
    <DO_GLOBAL_MAT_OP VALUE="1" />
  </xsl:template>
  <xsl:template match="ElementalEvaluation/*">
    <xsl:param name="attrname">
      <xsl:choose>
        <xsl:when test="name() = 'Triangles'">TRI</xsl:when>
        <xsl:when test="name() = 'Quadrilaterals'">QUAD</xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            Transform error: unhandled element type (only Triangles and Quadrilaterals supported at present).
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:attribute name="{$attrname}">
      <xsl:choose>
        <xsl:when test=". = 'LocalMatrix'">0</xsl:when>
        <xsl:when test=". = 'SumFactorization'">1</xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            Transform error: unhandled elemental evaluation method.
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute> 
  </xsl:template>
  <xsl:template match="ElementalEvaluation">
    <DO_GLOBAL_MAT_OP VALUE="0" />
    <DO_BLOCK_MAT_OP>
      <xsl:apply-templates select="./*" />
      <!--xsl:if test="Triangles">
        <xsl:attribute name="TRI">
          <xsl:apply-templates select="." />
        </xsl:attribute>
      </xsl:if-->
    </DO_BLOCK_MAT_OP>
  </xsl:template>
  <xsl:template match="*" mode="GlobalMatrixOpt">
    <Error>
      Error: unhandled GlobalOptimizationParameters
    </Error>
    <xsl:message terminate="yes">
      Transform error: unhandled GlobalOptimizationParameters
    </xsl:message>
  </xsl:template>
  <xsl:template match="BackwardTransform" mode="GlobalMatrixOpt">
    <BwdTrans>
      <xsl:apply-templates select="ElementalEvaluation" />
    </BwdTrans>
  </xsl:template>
  <xsl:template match="InnerProductWRTBase" mode="GlobalMatrixOpt">
    <IProductWRTBase>
      <xsl:apply-templates select="ElementalEvaluation" />
    </IProductWRTBase>
  </xsl:template>
  <xsl:template match="MassMatrixOp" mode="GlobalMatrixOpt">
    <MassMatrixOp>
      <xsl:apply-templates select="ElementalEvaluation" />
    </MassMatrixOp>
  </xsl:template>
  <xsl:template match="HelmholtzMatrixOp" mode="GlobalMatrixOpt">
    <HelmholtzMatrixOp>
      <xsl:apply-templates select="ElementalEvaluation" />
    </HelmholtzMatrixOp>
  </xsl:template>

  <xsl:template match ="Geometry" mode="ErrorChecks">
     <xsl:if test="not(GeometryAndBoundaryConditions/GEOMETRY)">
      <Error>
        Transform error: Geometry has not been supplied or is not in correct format.
      </Error>
      <xsl:message terminate="yes">
        Transform error: Geometry has not been supplied or is not in correct format.
      </xsl:message>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="CompositeADR">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="COMPOSITE"><xsl:value-of select="GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY/DOMAIN"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="Expansion">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="NUMMODES"><xsl:value-of select="Expansion/PolynomialOrder + 1"/></xsl:attribute>
    <xsl:attribute name="TYPE"><xsl:value-of select="Expansion/BasisType"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="ProblemSpecification" mode ="Parameters">
    <!-- <xsl:message>Handling problem spec parameters...</xsl:message>
    <xsl:message>Eqtype: <xsl:value-of select="name(EquationType/*[1])"/></xsl:message>  -->
    <xsl:if test="EquationType/*[1]/SimulationTimeParams">
      <P>FinTime    = <xsl:value-of select="EquationType/*[1]/SimulationTimeParams/FinalTime"/><xsl:text>  </xsl:text></P>
      <P>TimeStep   = <xsl:value-of select="EquationType/*[1]/SimulationTimeParams/TimeStep"/><xsl:text>  </xsl:text></P>
      <P>NumSteps   = FinTime/TimeStep  </P>
    </xsl:if>
    <xsl:if test="EquationType/*[1]/Velocity">
      <xsl:variable name="advx" select="EquationType/*[1]/Velocity/VelocityX"/>
      <xsl:variable name="advy" select="EquationType/*[1]/Velocity/VelocityY"/>
      <xsl:variable name="advz" select="EquationType/*[1]/Velocity/VelocityZ"/>
      
      <xsl:if test="$advx != '-'">
        <P>advx    = <xsl:value-of select="$advx"/><xsl:text>  </xsl:text></P>
      </xsl:if>
      <xsl:if test="$advy != '-'">
        <P>advy    = <xsl:value-of select="$advy"/><xsl:text>  </xsl:text></P>
      </xsl:if>
      <xsl:if test="$advz != '-'">
        <P>advz    = <xsl:value-of select="$advz"/><xsl:text>  </xsl:text></P>
      </xsl:if>
    </xsl:if>
    
    <xsl:if test="EquationType/*[1]/Epsilon">
      <xsl:variable name="epsilon" select="EquationType/*[1]/Epsilon"/>
      
      <xsl:if test="$epsilon != '-'">
        <P>epsilon    = <xsl:value-of select="$epsilon"/><xsl:text>  </xsl:text></P>
      </xsl:if>
    </xsl:if>
    
    <xsl:if test="EquationType/*[1]/Lambda">
      <xsl:variable name="lambda" select="EquationType/*[1]/Lambda"/>
      
      <xsl:if test="$lambda != '-'">
        <P>lambda    = <xsl:value-of select="$lambda"/><xsl:text>  </xsl:text></P>
      </xsl:if>
    </xsl:if>
    
    <xsl:if test="EquationType/*[1]/Variables">
    	<xsl:if test="EquationType/*[1]/Variables/k">
    	  <xsl:variable name="k" select="EquationType/*[1]/Variables/k"/>
    	  <xsl:if test="$k != '-'">
            <P>k    = <xsl:value-of select="$k"/><xsl:text>  </xsl:text></P>
      </xsl:if>
    	</xsl:if>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="ProblemSpecification" mode="ADRAdvectionVelocity">
    <xsl:if test="EquationType/*[1]/Velocity">
      <xsl:variable name="advx" select="EquationType/*[1]/Velocity/VelocityX"/>
      <xsl:variable name="advy" select="EquationType/*[1]/Velocity/VelocityY"/>
      <xsl:variable name="advz" select="EquationType/*[1]/Velocity/VelocityZ"/>
  
      <FUNCTION NAME="AdvectionVelocity">
        <xsl:if test="$advx != '-'">
          <E VAR="Vx" VALUE="advx" />
        </xsl:if>
        <xsl:if test="$advy != '-'">
          <E VAR="Vy" VALUE="advy" />
        </xsl:if>
        <xsl:if test="$advz != '-'">
          <E VAR="Vz" VALUE="advz" />
        </xsl:if>
      </FUNCTION>
    </xsl:if>
  </xsl:template>

  <xsl:template match="NumericalAlgorithm" mode ="Parameters">
    
  </xsl:template>

  <xsl:template match="Admin" mode ="Parameters">
    <P>IO_CheckSteps = <xsl:value-of select="IO_CheckSteps"/></P>
    <P>IO_InfoSteps = <xsl:value-of select="IO_InfoSteps"/></P>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="ADRSolverInfo">
    <I PROPERTY="EQTYPE">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="name(EquationType/*[1])"/>
      </xsl:attribute>
    </I>
  </xsl:template>

  <xsl:template match="NumericalAlgorithm" mode ="SolverInfo">
    <xsl:variable name="projection" select="name(Projection/*[1])"/>
    <I PROPERTY="Projection">
      <xsl:attribute name="VALUE">
        <xsl:choose>
          <xsl:when test="$projection = 'ContinuousGalerkin'">Continuous</xsl:when>
          <xsl:when test="$projection = 'DiscontinuousGalerkin'">DisContinuous</xsl:when>
        </xsl:choose>
      </xsl:attribute>
    </I>
    
    <xsl:if test="Projection/*[1]/DiffusionType">
      <xsl:variable name="diftype" select="Projection/*[1]/DiffusionType"/>
      <xsl:if test="$diftype != 'Not Provided'">
        <I PROPERTY="DiffusionType">
          <xsl:attribute name="VALUE">
            <xsl:value-of select="Projection/*[1]/DiffusionType" />
          </xsl:attribute>
        </I>
      </xsl:if>
    </xsl:if>
    
    <I PROPERTY="AdvectionType">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="AdvectionType" />
      </xsl:attribute>
    </I>
    
    <xsl:variable name="upwindtype" select="UpwindType"/>
    <xsl:if test="$upwindtype != 'Not Provided'">
      <I PROPERTY="UpwindType">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="UpwindType" />
        </xsl:attribute>
      </I>
    </xsl:if>
    
    <I PROPERTY="TimeIntegrationMethod">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="TimeIntegration/TimeIntegrationMethod" />
      </xsl:attribute>
    </I>
    
    <xsl:variable name="difadv" select="TimeIntegration/DiffusionAdvancement"/>
    <xsl:if test="$difadv != 'Not Provided'">
      <I PROPERTY="DiffusionAdvancement">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="TimeIntegration/DiffusionAdvancement" />
        </xsl:attribute>
      </I>
    </xsl:if>
  </xsl:template>

  <!-- *** 
       Note: We match up the regions for boundary conditions between the geometry file and the inputs by matching the comments.
       so if the comments don't match, we're in trouble!!! ...
       *** -->
  <xsl:template match="REGION" mode ="GetADRBoundaryConditions">
    <xsl:variable name="comment" select="@COMMENT" />
    <REGION>
      <xsl:attribute name="REF"><xsl:value-of select="@REF"/></xsl:attribute>
      <xsl:attribute name="COMMENT"><xsl:value-of select="@COMMENT"/></xsl:attribute>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/u/Type}">
        <xsl:attribute name="VAR">u</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/u/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/u/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/u/Value"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/v/Type}">
        <xsl:attribute name="VAR">v</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/v/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/v/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/v/Value"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/p/Type}">
        <xsl:attribute name="VAR">p</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/p/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/p/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/p/Value"/>
        </xsl:attribute>
      </xsl:element>
    </REGION>
  </xsl:template>

  <!-- Advection-Diffusion-Reaction solver transform -->
  <xsl:template match="/AdvectionDiffusion">
    <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry" mode ="ErrorChecks"/>
    <NEKTAR>
      <EXPANSIONS>
        <E>
          <xsl:apply-templates select="ProblemSpecification" mode ="CompositeADR"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="Expansion"/>
          <xsl:attribute name="FIELDS">u</xsl:attribute>
        </E>
      </EXPANSIONS>
      
      <CONDITIONS>
        <PARAMETERS>
          <xsl:apply-templates select="ProblemSpecification" mode ="Parameters"/>
          <xsl:apply-templates select="NumericalAlgorithm" mode ="Parameters"/>
          <xsl:apply-templates select="Admin" mode ="Parameters"/>
          <xsl:apply-templates select="Physics" mode ="NavierStokesParameters"/>
        </PARAMETERS>
        
        <SOLVERINFO>
          <xsl:apply-templates select="ProblemSpecification" mode ="ADRSolverInfo"/>
          <xsl:apply-templates select="NumericalAlgorithm" mode ="SolverInfo"/>
        </SOLVERINFO>
        
        <VARIABLES>
          <V ID="0">u</V>
        </VARIABLES>
        
        <!-- Copy in the boundaries -->
        <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYREGIONS"/>

        <!--
        <BOUNDARYCONDITIONS>
          <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYCONDITIONS/REGION" mode ="GetADRBoundaryConditions"/>
        </BOUNDARYCONDITIONS>
        -->
        <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYCONDITIONS"/>
        
        <xsl:apply-templates select="ProblemSpecification" mode ="ADRAdvectionVelocity"/>

        <FUNCTION NAME="InitialConditions">
          <E VAR="u">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="ProblemSpecification/EquationType/*[1]/InitialConditions"/>
            </xsl:attribute>
          </E>
        </FUNCTION>

        <FUNCTION NAME="ExactSolution">
          <E VAR="u">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="ProblemSpecification/EquationType/*[1]/ForcingFunction"/>
            </xsl:attribute>
          </E>
        </FUNCTION>

      </CONDITIONS>

      <!-- Copy in the geometry -->
      <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY"/>
    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>
