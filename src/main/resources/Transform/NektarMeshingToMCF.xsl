<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            
>
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

  <xsl:template match="ProblemSpecification" mode ="Expansion">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="NUMMODES"><xsl:value-of select="Expansion/PolynomialOrder + 1"/></xsl:attribute>
    <xsl:attribute name="TYPE"><xsl:value-of select="Expansion/BasisType"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="CompositeCardiac">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="COMPOSITE"><xsl:value-of select="GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY/DOMAIN"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="ProblemSpecification" mode ="CompositeCompressible">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="COMPOSITE"><xsl:value-of select="GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY/DOMAIN"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="ProblemSpecification" mode ="CompositeNavierStokes">
    <!-- We assume the composites required for the expansion match the domain -->
    <xsl:attribute name="COMPOSITE"><xsl:value-of select="GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY/DOMAIN"/></xsl:attribute>
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
      <P>IterativeSolverTolerance = <xsl:value-of select="MatrixInversion/Iterative/IterativeSolverTolerance"/></P>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Admin" mode ="Parameters">
    <P>IO_CheckSteps = <xsl:value-of select="IO_CheckSteps"/></P>
    <P>IO_InfoSteps = <xsl:value-of select="IO_InfoSteps"/></P>
  </xsl:template>

  <xsl:template match="Physics" mode ="CardiacParameters">
    <P>Substeps = <xsl:value-of select="CellModel/SubSteps"/></P>
    <xsl:if test="Model/Monodomain">
      <P>Chi = <xsl:value-of select="Model/Monodomain/Chi"/></P>
      <P>Cm = <xsl:value-of select="Model/Monodomain/Cm"/></P>
      <xsl:if test="Model/Monodomain/IsotropicConductivity/Intensity">
        <P>d_min = <xsl:value-of select="Model/Monodomain/IsotropicConductivity/Intensity/d_min"/></P>
        <P>d_max = <xsl:value-of select="Model/Monodomain/IsotropicConductivity/Intensity/d_max"/></P>
      </xsl:if>
    </xsl:if>
    <xsl:if test="Model/Bidomain">
      <P>Sigmai = <xsl:value-of select="Model/Monodomain/Sigmai"/></P>
      <P>Sigmaix = <xsl:value-of select="Model/Monodomain/Sigmaix"/></P>
      <P>Sigmaiy = <xsl:value-of select="Model/Monodomain/Sigmaiy"/></P>
      <P>Sigmaiz = <xsl:value-of select="Model/Monodomain/Sigmaiz"/></P>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Physics" mode ="CompressibleParameters">
      <P> Gamma       = <xsl:value-of select="Gamma"/> </P>
      <P> GasConstant = <xsl:value-of select="GasConstant"/> </P>
      <P> pInf        = <xsl:value-of select="pInf"/> </P>
      <P> rhoInf      = <xsl:value-of select="rhoInf"/> </P>
      <P> TInf        = pInf/(GasConstant*rhoInf) </P>
      <P> Mach        = <xsl:value-of select="MachInf"/> </P>
      <P> cInf        = sqrt(Gamma*GasConstant*TInf) </P>
      <P> alpha       = <xsl:value-of select="AngleOfAttack"/> </P>
      <P> uInf        = Mach*cInf*cos(alpha*PI/180) </P>
      <P> vInf        = Mach*cInf*sin(alpha*PI/180) </P>
      <P> Twall       = TInf </P>
      <xsl:if test="EquationSystem/NavierStokesCFE">
      <P> Re          = <xsl:value-of select="EquationSystem/NavierStokesCFE/Re"/> </P>
      <P> L           = <xsl:value-of select="EquationSystem/NavierStokesCFE/L"/> </P>
      <P> Pr          = <xsl:value-of select="EquationSystem/NavierStokesCFE/Pr"/> </P>
      <P> mu          = rhoInf * L * uInf / Re </P>
      <P> Cp          = (Gamma / (Gamma - 1))*GasConstant</P>
      <P> thermalConductivity = Cp * mu / Pr </P>
      </xsl:if>
  </xsl:template>

  <xsl:template match="Physics" mode ="NavierStokesParameters">
    <P>Kinvis = <xsl:value-of select="KinematicViscosity"/></P>
  </xsl:template>

  <xsl:template match="Physics" mode ="CardiacSolverInfo">
    <I PROPERTY="EQTYPE">
      <!-- Should be Monodomain od BiDomain-->
      <xsl:attribute name="VALUE">
        <xsl:value-of select="name(Model/*[1])" />
      </xsl:attribute>
    </I>
    <I PROPERTY="CellModel">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="name(CellModel/CellModelType/*[1])" />
      </xsl:attribute>
    </I>
    <xsl:if test="CellModel/CellModelType/*/CellModelVariant">
      <I PROPERTY="CellModelVariant">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CellModel/CellModelType/*/CellModelVariant" />
        </xsl:attribute>
      </I>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Physics" mode ="CompressibleSolverInfo">
      <I PROPERTY="EQTYPE">
          <xsl:attribute name="VALUE">
              <xsl:value-of select="name(EquationSystem/*[1])" />
          </xsl:attribute>
      </I>
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
    <I PROPERTY="GlobalSysSoln">
      <xsl:attribute name="VALUE">
        <xsl:choose>
          <xsl:when test="MatrixInversion/Iterative/SubStructuring = 'StaticCondensation'">IterativeStaticCond</xsl:when>
          <xsl:when test="MatrixInversion/Iterative/SubStructuring = 'Full'">IterativeFull</xsl:when>
          <xsl:when test="MatrixInversion/Direct/SubStructuring = 'StaticCondensation'">DirectStaticCond</xsl:when>
          <xsl:when test="MatrixInversion/Direct/SubStructuring = 'Full'">DirectFull</xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">
              Error: unhandled matrix inversion approach -> cannot set GlobalSysSoln
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </I>
  </xsl:template>

  <xsl:template match="Physics" mode ="CardiacFunctions">
    <xsl:if test="Model/Monodomain">
      <xsl:choose>
        <xsl:when test="Model/Monodomain/IsotropicConductivity/Intensity">
          <FUNCTION NAME="IsotropicConductivity">
            <E VAR="intensity">
              <xsl:attribute name="VALUE">
                <xsl:value-of select="Model/Monodomain/IsotropicConductivity/Intensity/Intensity"/>
              </xsl:attribute>
            </E>
          </FUNCTION>
        </xsl:when>
        <xsl:when test="Model/Monodomain/IsotropicConductivity/Conductivity">
          <FUNCTION NAME="IsotropicConductivity">
            <E VAR="intensity">
              <xsl:attribute name="VALUE">
                <xsl:value-of select="Model/Monodomain/IsotropicConductivity/Conductivity"/>
              </xsl:attribute>
            </E>
          </FUNCTION>
        </xsl:when>
        <xsl:when test="Model/Monodomain/IsotropicConductivity/Function">
          <xsl:message terminate="yes">
            Transform error: Isotropic conductivity: functions not handled yet
          </xsl:message>
        </xsl:when>
        <xsl:when test="Model/Monodomain/IsotropicConductivity/NotProvided">
          <!-- Do nothing-->
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            Transform error: Unhandled IsotropicConductivity type
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Physics" mode ="CompressibleFunctions">
      <FUNCTION NAME="InitialConditions">
          <E VAR="rho"    VALUE="rhoInf"/>
          <E VAR="rhou"   VALUE="rhoInf * uInf"   />
          <E VAR="rhov"   VALUE="rhoInf * vInf"   />
          <E VAR="E"      VALUE="pInf / (Gamma - 1) + 0.5 * rhoInf * (uInf * uInf + vInf * vInf)"  />
      </FUNCTION>
  </xsl:template>
  
  <xsl:template match="ProblemSpecification" mode ="CardiacFunctions">
    <FUNCTION NAME="InitialConditions">
      <E VAR="u" >
        <xsl:attribute name="VALUE">
          <xsl:choose>
            <xsl:when test="InitialConditions/Constant">
              <xsl:value-of select="InitialConditions/Constant"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:message terminate="yes">
                Transform error: only constant initial conditions supported at present.
              </xsl:message>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </E>
    </FUNCTION>
  </xsl:template>

  <xsl:template match="ProblemSpecification" mode ="CardiacStimuli">
    <!-- Currently only one stimulus supported.
            To do: support multiple stimuli-->
    <STIMULI>
      <STIMULUS ID="0">
        <xsl:attribute name="TYPE">
          <xsl:choose>
            <xsl:when test="Stimuli/Stimulus/StimulusType/StimulusCircle">StimulusCirc</xsl:when>
            <xsl:when test="Stimuli/Stimulus/StimulusType/StimulusRectangle">StimulusRect</xsl:when>
            <xsl:when test="Stimuli/Stimulus/StimulusType/StimulusPoint">StimulusPoint</xsl:when>
            <xsl:otherwise>
              <xsl:message terminate="yes">
                Transform error: unhandled stimulus type.
              </xsl:message>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Copy the stumulus details -->
        <xsl:copy-of select="Stimuli/Stimulus/StimulusType/*[1]/node()"/>

        <PROTOCOL>
          <xsl:attribute name="TYPE">
            <xsl:value-of select="name(Stimuli/Stimulus/Protocol/*[1])"/>
          </xsl:attribute>
          <!-- Copy the protocol details, converting node names to upper case -->
          <xsl:apply-templates select="Stimuli/Stimulus/Protocol/*[1]/*" mode="NodesToUpperCase"/>
        </PROTOCOL>

      </STIMULUS>
    </STIMULI>
  </xsl:template>

  <!-- *** 
       Note: We match up the regions for boundary conditions between the geometry file and the inputs by matching the comments.
       so if the comments don't match, we're in trouble!!! ...
       *** -->
  <xsl:template match="REGION" mode ="GetIncNSBoundaryConditions">
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

  <xsl:template match="REGION" mode ="GetCardiacBoundaryConditions">
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
    </REGION>
  </xsl:template>

  <xsl:template match="REGION" mode ="GetCompressibleBoundaryConditions">
    <xsl:variable name="comment" select="@COMMENT" />
    <REGION>
      <xsl:attribute name="REF"><xsl:value-of select="@REF"/></xsl:attribute>
      <xsl:attribute name="COMMENT"><xsl:value-of select="@COMMENT"/></xsl:attribute>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/rho/Type}">
        <xsl:attribute name="VAR">rho</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/rho/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rho/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rho/Value"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:attribute name="REF"><xsl:value-of select="@REF"/></xsl:attribute>
      <xsl:attribute name="COMMENT"><xsl:value-of select="@COMMENT"/></xsl:attribute>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/rhou/Type}">
        <xsl:attribute name="VAR">rhou</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/rhou/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rhou/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rhou/Value"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:attribute name="REF"><xsl:value-of select="@REF"/></xsl:attribute>
      <xsl:attribute name="COMMENT"><xsl:value-of select="@COMMENT"/></xsl:attribute>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/rhov/Type}">
        <xsl:attribute name="VAR">rhov</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/rhov/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rhov/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/rhov/Value"/>
        </xsl:attribute>
      </xsl:element>
      <xsl:attribute name="REF"><xsl:value-of select="@REF"/></xsl:attribute>
      <xsl:attribute name="COMMENT"><xsl:value-of select="@COMMENT"/></xsl:attribute>
      <xsl:element name="{../../../../BoundaryCondition[Comment=$comment]/E/Type}">
        <xsl:attribute name="VAR">E</xsl:attribute>
        <xsl:if test="../../../../BoundaryCondition[Comment=$comment]/E/UserDefinedType!='NotProvided'">
          <xsl:attribute name="USERDEFINEDTYPE">
            <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/E/UserDefinedType"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:attribute name="VALUE">
          <xsl:value-of select="../../../../BoundaryCondition[Comment=$comment]/E/Value"/>
        </xsl:attribute>
      </xsl:element>
    </REGION>
  </xsl:template>

  
  <!-- CompressibleFlowSolver transform -->
  <xsl:template match="/CompressibleFlowSolver">
      <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry" mode ="ErrorChecks"/>
      <NEKTAR>
          <EXPANSIONS>
              <E>
                  <xsl:apply-templates select="ProblemSpecification" mode ="CompositeCompressible"/>
                  <xsl:apply-templates select="ProblemSpecification" mode ="Expansion"/>
                  <xsl:attribute name="FIELDS">rho,rhou,rhov,E</xsl:attribute>
              </E>
          </EXPANSIONS>
          <CONDITIONS>
              <PARAMETERS>
                  <xsl:apply-templates select="NumericalAlgorithm" mode ="Parameters"/>
                  <xsl:apply-templates select="ProblemSpecification" mode ="Parameters"/>
                  <xsl:apply-templates select="Admin" mode ="Parameters"/>
                  <xsl:apply-templates select="Physics" mode ="CompressibleParameters"/>
              </PARAMETERS>
              <VARIABLES>
                  <V ID="0">rho</V>
                  <V ID="1">rhou</V>
                  <V ID="2">rhov</V>
                  <V ID="3">E</V>
              </VARIABLES>
              <SOLVERINFO>
                  <xsl:apply-templates select="Physics" mode ="CompressibleSolverInfo"/>
                  <xsl:apply-templates select="NumericalAlgorithm" mode ="SolverInfo"/>
                  <I PROPERTY="ProblemType" VALUE="General" />
                  <I PROPERTY="UpwindType">
                      <xsl:attribute name="VALUE">
                          <xsl:value-of select="ProblemSpecification/RiemannSolver" />
                      </xsl:attribute>
                  </I>
              </SOLVERINFO>
              <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYREGIONS"/>
              <BOUNDARYCONDITIONS>
                  <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYCONDITIONS/REGION" mode ="GetCompressibleBoundaryConditions"/>
              </BOUNDARYCONDITIONS>
              <FUNCTION NAME="InitialConditions">
                  <E VAR="rho"    VALUE="rhoInf"/>
                  <E VAR="rhou"   VALUE="rhoInf * uInf"   />
                  <E VAR="rhov"   VALUE="rhoInf * vInf"   />
                  <E VAR="E"      VALUE="pInf / (Gamma - 1) + 0.5 * rhoInf * (uInf * uInf + vInf * vInf)"  />
              </FUNCTION>
          </CONDITIONS>
          <GLOBALOPTIMIZATIONPARAMETERS>
              <xsl:apply-templates select="NumericalAlgorithm/GlobalOptimizationParameters/*" mode ="GlobalMatrixOpt"/>
          </GLOBALOPTIMIZATIONPARAMETERS>
          <!-- Copy in the geometry -->
          <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY"/>
      </NEKTAR>
  </xsl:template>

  <!-- Cardiac transform -->
  <xsl:template match="/CardiacElectrophysiology">
    <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry" mode ="ErrorChecks"/>
    <NEKTAR>
      <EXPANSIONS>
        <E>
          <xsl:apply-templates select="ProblemSpecification" mode ="CompositeCardiac"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="Expansion"/>
          <xsl:attribute name="FIELDS">u</xsl:attribute>
        </E>
      </EXPANSIONS>
      <CONDITIONS>
          <PARAMETERS>
            <xsl:apply-templates select="NumericalAlgorithm" mode ="Parameters"/>
            <xsl:apply-templates select="ProblemSpecification" mode ="Parameters"/>
            <xsl:apply-templates select="Admin" mode ="Parameters"/>
            <xsl:apply-templates select="Physics" mode ="CardiacParameters"/>
          </PARAMETERS>
          <VARIABLES>
            <V ID="0">u</V>
          </VARIABLES>
          <SOLVERINFO>
            <xsl:apply-templates select="Physics" mode ="CardiacSolverInfo"/>
            <xsl:apply-templates select="NumericalAlgorithm" mode ="SolverInfo"/>
          </SOLVERINFO>

          <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYREGIONS"/>
          <BOUNDARYCONDITIONS>
              <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYCONDITIONS/REGION" mode ="GetCardiacBoundaryConditions"/>
          </BOUNDARYCONDITIONS>
          <xsl:apply-templates select="Physics" mode ="CardiacFunctions"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="CardiacFunctions"/>
      </CONDITIONS>        
      <xsl:apply-templates select="ProblemSpecification" mode ="CardiacStimuli"/>
      <GLOBALOPTIMIZATIONPARAMETERS>
        <xsl:apply-templates select="NumericalAlgorithm/GlobalOptimizationParameters/*" mode ="GlobalMatrixOpt"/>
      </GLOBALOPTIMIZATIONPARAMETERS>
      <!-- Copy in the geometry -->
      <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY"/>
    </NEKTAR>
  </xsl:template>

  <!-- Incompressible Navier-Stokes transform -->
  <xsl:template match="/IncompressibleNavierStokes">
    <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry" mode ="ErrorChecks"/>
    <NEKTAR>
      <EXPANSIONS>
        <E>
          <xsl:apply-templates select="ProblemSpecification" mode ="CompositeNavierStokes"/>
          <xsl:apply-templates select="ProblemSpecification" mode ="Expansion"/>
          <xsl:attribute name="FIELDS">u,v,p</xsl:attribute>
        </E>
      </EXPANSIONS>
      <CONDITIONS>
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
        <SOLVERINFO>
          <xsl:apply-templates select="ProblemSpecification" mode ="NavierStokesSolverInfo"/>
          <xsl:apply-templates select="NumericalAlgorithm" mode ="SolverInfo"/>
        </SOLVERINFO>
        <!-- Copy in the boundaries -->
        <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYREGIONS"/>

        <BOUNDARYCONDITIONS>
          <xsl:apply-templates select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/BOUNDARYCONDITIONS/REGION" mode ="GetIncNSBoundaryConditions"/>
        </BOUNDARYCONDITIONS>

        <FUNCTION NAME="InitialConditions">
          <E VAR="u">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="ProblemSpecification/InitialConditions/u"/>
            </xsl:attribute>
          </E>
          <E VAR="v">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="ProblemSpecification/InitialConditions/v"/>
            </xsl:attribute>
          </E>
          <E VAR="p">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="ProblemSpecification/InitialConditions/p"/>
            </xsl:attribute>
          </E>
        </FUNCTION>

      </CONDITIONS>

      <GLOBALOPTIMIZATIONPARAMETERS>
        <xsl:apply-templates select="NumericalAlgorithm/GlobalOptimizationParameters/*" mode ="GlobalMatrixOpt"/>
      </GLOBALOPTIMIZATIONPARAMETERS>
      <!-- Copy in the geometry -->
      <xsl:copy-of select="ProblemSpecification/GeometryAndBoundaryConditions/Geometry/GeometryAndBoundaryConditions/GEOMETRY"/>
    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>
