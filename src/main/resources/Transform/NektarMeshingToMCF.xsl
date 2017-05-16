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

  

  <xsl:template match="MeshConfiguration" mode="MeshingInformation">
    <xsl:variable name="meshtype" select="name(MeshType/*[1])"/>
    <I PROPERTY="CADFile">
      <xsl:attribute name="VALUE">
        <xsl:choose>
          <xsl:when test="CADSource/CADFile">
            <xsl:value-of select="CADSource/CADFile" />
          </xsl:when>
          <xsl:when test="CADSource/NACAAerofoil">
            <xsl:value-of select="CADSource/NACAAerofoil/NACACode" />
          </xsl:when>
        </xsl:choose>  
      </xsl:attribute>
    </I>
    
    <I PROPERTY="MeshType">
      <xsl:attribute name="VALUE">
        
        <xsl:choose>
          <xsl:when test="$meshtype = 'Mesh2D'">2D</xsl:when>
          <xsl:when test="$meshtype = 'Mesh3D'">3D</xsl:when>
          <xsl:when test="$meshtype = 'BoundaryLayerMesh2D'">2DBndLayer</xsl:when>
          <xsl:when test="$meshtype = 'BoundaryLayerMesh3D'">3DBndLayer</xsl:when>
        </xsl:choose>
      </xsl:attribute>
    </I>
    
  </xsl:template>

  <xsl:template match="MeshConfiguration" mode="NACAParameters">
    <xsl:if test="CADSource/NACAAerofoil">
      <xsl:text>
      
      </xsl:text>
      <P PARAM="Xmin">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CADSource/NACAAerofoil/NACAParameters/Xmin"/>
        </xsl:attribute>
      </P>
      <P PARAM="Ymin">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CADSource/NACAAerofoil/NACAParameters/Ymin"/>
        </xsl:attribute>
      </P>
      <P PARAM="Xmax">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CADSource/NACAAerofoil/NACAParameters/Xmax"/>
        </xsl:attribute>
      </P>
      <P PARAM="Ymax">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CADSource/NACAAerofoil/NACAParameters/Ymax"/>
        </xsl:attribute>
      </P>
      <P PARAM="AOA">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="CADSource/NACAAerofoil/NACAParameters/AOA"/>
        </xsl:attribute>
      </P>  
    </xsl:if>
  </xsl:template>

  <xsl:template match="MeshParameters" mode="MeshingParameters">
    <xsl:variable name="meshtype" select="name(/NektarMeshing/MeshConfiguration/MeshType/*[1])"/>
    
    <P PARAM="MinDelta">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="MinimumDelta"/>
      </xsl:attribute>
    </P>
    
    <P PARAM="MaxDelta">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="MaximumDelta"/>
      </xsl:attribute>
    </P>
    
    <P PARAM="EPS">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="CurvatureSensitivity"/>
      </xsl:attribute>
    </P>
    
    <P PARAM="Order">
      <xsl:attribute name="VALUE">
        <xsl:value-of select="Order"/>
      </xsl:attribute>
    </P>
    
    <xsl:if test="($meshtype = 'BoundaryLayerMesh2D') or ($meshtype = 'BoundaryLayerMesh3D')">
      <xsl:comment> Boundary layer </xsl:comment>
      <xsl:if test="$meshtype = 'BoundaryLayerMesh2D'">
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerSurfaces) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerSurfaces != ''))">
          <P PARAM="BndLayerSurfaces">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerSurfaces" />
            </xsl:attribute>
          </P>
        </xsl:if>
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness != ''))">
          <P PARAM="BndLayerThickness">
            <xsl:attribute name="VALUE">
              <xsl:if test="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness/Constant">
                <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness/Constant"/>
              </xsl:if>
              <xsl:if test="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness/Expression">
                <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerThickness/Expression"/>
              </xsl:if>
            </xsl:attribute>
          </P>
        </xsl:if>
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes/BoundaryLayerLayers))">
          <P PARAM="BndLayerLayers">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes/BoundaryLayerLayers" />
            </xsl:attribute>
          </P>
        </xsl:if>
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes/BoundaryLayerProgression))">
          <P PARAM="BndLayerProgression">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/SplitBoundaryLayer/Yes/BoundaryLayerProgression"/>
            </xsl:attribute>
          </P>
        </xsl:if>        
      </xsl:if>
      <xsl:if test="$meshtype = 'BoundaryLayerMesh3D'">
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerSurfaces) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerSurfaces != ''))">
          <P PARAM="BndLayerSurfaces">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerSurfaces" />
            </xsl:attribute>
          </P>
        </xsl:if>
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness != ''))">
          <P PARAM="BndLayerThickness">
            <xsl:attribute name="VALUE">
              <xsl:if test="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness/Constant">
                <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness/Constant"/>
              </xsl:if>
              <xsl:if test="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness/Expression">
                <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerThickness/Expression"/>
              </xsl:if>
            </xsl:attribute>
          </P>
        </xsl:if>

        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes/BoundaryLayerLayers))">
          <P PARAM="BndLayerLayers">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes/BoundaryLayerLayers" />
            </xsl:attribute>
          </P>
        </xsl:if>
        
        <xsl:if test="((/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes) and (/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes/BoundaryLayerProgression))">
          <P PARAM="BndLayerProgression">
            <xsl:attribute name="VALUE">
              <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/SplitBoundaryLayer/Yes/BoundaryLayerProgression"/>
            </xsl:attribute>
          </P>
        </xsl:if>
      </xsl:if>
    </xsl:if>
                    
  </xsl:template>
  
  <xsl:template match="AdditionalParameters" mode="BoolParameters">
    <xsl:if test="SurfaceOptimiser">
      <P PARAM="SurfaceOptimiser" />
    </xsl:if>
    <xsl:if test="VariationalOptimiser">
      <P PARAM="VariationalOptimiser" />
    </xsl:if>
    <xsl:if test="WriteOctree">
      <P PARAM="WriteOctree" />
    </xsl:if>
  </xsl:template>

  <xsl:template match="LineRefinements" mode="LineRefinementParameters">
    <REFINEMENT>
      <LINE>
        <xsl:if test="X1">
          <X1><xsl:value-of select="X1"/></X1>
        </xsl:if>
        <xsl:if test="Y1">
          <Y1><xsl:value-of select="Y1"/></Y1>
        </xsl:if>
        <xsl:if test="Z1">
          <Z1><xsl:value-of select="Z1"/></Z1>
        </xsl:if>
        <xsl:if test="X2">
          <X2><xsl:value-of select="X2"/></X2>
        </xsl:if>
        <xsl:if test="Y2">
          <Y2><xsl:value-of select="Y2"/></Y2>
        </xsl:if>
        <xsl:if test="Z2">
          <Z2><xsl:value-of select="Z2"/></Z2>
        </xsl:if>
        <xsl:if test="R">
          <R><xsl:value-of select="R"/></R>
        </xsl:if>
        <xsl:if test="D">
          <D><xsl:value-of select="D"/></D>
        </xsl:if>
      </LINE>
    </REFINEMENT>  
  </xsl:template>

  <!-- NekMesh Mesh Configuration File transform -->
  <xsl:template match="/NektarMeshing">
    <NEKTAR>
      <MESHING>

        <INFORMATION>
          <xsl:apply-templates select="MeshConfiguration" mode ="MeshingInformation"/>
        </INFORMATION>
        
        <PARAMETERS>
          <xsl:apply-templates select="MeshParameters" mode ="MeshingParameters"/>
          <xsl:apply-templates select="MeshConfiguration" mode ="NACAParameters"/>
        </PARAMETERS>
        
        <BOOLPARAMETERS>
          <xsl:apply-templates select="AdditionalParameters" mode ="BoolParameters"/>
        </BOOLPARAMETERS>
        
        <xsl:apply-templates select="LineRefinements" mode ="LineRefinementParameters"/>
      
      </MESHING>

    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>