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
        <xsl:value-of select="CADFile" />
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
        <P PARAM="BndLayerSurfaces">
          <xsl:attribute name="VALUE">
            <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh2D/BoundaryLayerSurfaces" />
          </xsl:attribute>
        </P>
      </xsl:if>
      <xsl:if test="$meshtype = 'BoundaryLayerMesh3D'">
        <P PARAM="BndLayerSurfaces">
          <xsl:attribute name="VALUE">
            <xsl:value-of select="/NektarMeshing/MeshConfiguration/MeshType/BoundaryLayerMesh3D/BoundaryLayerSurfaces" />
          </xsl:attribute>
        </P>
      </xsl:if>
    </xsl:if>
        
    <xsl:if test="$meshtype = 'BoundaryLayer'">
      <P PARAM="BndLayerThickness">
        <xsl:attribute name="VALUE">
          <xsl:value-of select="BoundaryLayerThickness"/>
        </xsl:attribute>
      </P>
    </xsl:if>
                
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
        </PARAMETERS>
      
      </MESHING>

    </NEKTAR>
  </xsl:template>

</xsl:stylesheet>