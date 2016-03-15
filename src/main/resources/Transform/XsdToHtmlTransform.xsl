<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
           xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:libhpc="http://www.libhpc.imperial.ac.uk/SchemaAnnotation"
           xmlns:xalan="http://xml.apache.org/xslt"
           xmlns:saxon="http://saxon.sf.net/"
           exclude-result-prefixes="xs"
>

  <xsl:output method="html" indent="yes" xalan:indent-amount="2"/>
  <!-- ignore white space. Otherwise you get a great long html output full of new lines -->
  <xsl:strip-space elements="xs:schema xs:element xs:complexType xs:simpleType xs:restriction"/>
  <xsl:strip-space elements="xs:sequence xs:choice xs:minExclusive xs:simpleContent xs:extension"/>
  <xsl:strip-space elements="xs:annotation xs:appinfo libhpc:documentation"/>

  <!--xsl:template match="xs:element[@type]" mode="findChildNodes"-->
  <xsl:template match="xs:element" mode="findChildNodes">
    <xsl:param name="path" />
    <xsl:param name="nodeName" select="@name"/>
    <!-- libhpc:trueName can be used to define true parameter name if it needed
         to be sanitised to produce correct XML in the schema node name -->
    <xsl:param name="trueNodeName">
      <xsl:choose>
        <xsl:when test="@libhpc:trueName">
          <xsl:value-of select="@libhpc:trueName"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$nodeName"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="type" select="@type"/>
    <xsl:param name="this_path" select="concat($path,concat('.',$nodeName))"/>
    <xsl:param name="documentation" select="xs:annotation/xs:appinfo/libhpc:documentation"/>
    <xsl:param name="units" select="xs:annotation/xs:appinfo/libhpc:units"/>
    <xsl:param name="badge_type">
      <xsl:choose>
        <xsl:when test="./xs:complexType/xs:choice or /xs:schema/xs:complexType[@name=$type]/xs:choice">badge badge-warning</xsl:when>
        <xsl:when test=".//xs:element or /xs:schema/xs:complexType[@name=$type]//xs:element">badge badge-success</xsl:when> <!-- if there is an element anywhere below, it's not a leaf-->
        <xsl:otherwise>badge badge-info</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:element name="ul">
      <xsl:attribute name="role">group</xsl:attribute>
      <xsl:if test="../../xs:choice">
        <xsl:attribute name="choice-id">
          <xsl:value-of select="@name" />
        </xsl:attribute>
        <xsl:attribute name="path">
          <xsl:value-of select="$this_path" />
        </xsl:attribute>
        <xsl:attribute name="chosen">false</xsl:attribute>
      </xsl:if>
      <!-- Test if it's a leaf by looking at the badge colour ... -->
      <xsl:if test="$badge_type = 'badge badge-info'">
        <xsl:attribute name="leaf">true</xsl:attribute>
      </xsl:if>
      <xsl:element name="li">
        <xsl:attribute name="class">parent_li</xsl:attribute>
        <xsl:attribute name="role">treeitem</xsl:attribute>
        <xsl:if test="xs:annotation/xs:appinfo/libhpc:documentation">
          <xsl:attribute name="documentation">
            <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:documentation" />
          </xsl:attribute>
        </xsl:if>
        <xsl:element name="span">
          <xsl:attribute name="class">
            <xsl:value-of select="$badge_type"/>
          </xsl:attribute>
          <xsl:attribute name="data-fqname">
            <xsl:value-of select="$nodeName"/>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of select="$documentation"/>
          </xsl:attribute>
          <xsl:if test="xs:annotation/xs:appinfo/libhpc:refersToFile">
            <xsl:attribute name="refersToFileTreePath">
              <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:refersToFile/libhpc:fileTreePath"/>
            </xsl:attribute>
            <xsl:attribute name="refersToFileDataXPath">
              <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:refersToFile/libhpc:dataXPath"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="xs:annotation/xs:appinfo/libhpc:locationInFile">
            <xsl:attribute name="locationInFileXPath">
              <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:locationInFile"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:value-of select="$trueNodeName"/>
        </xsl:element><!-- </span> -->
        <xsl:text> </xsl:text>
        <xsl:if test="substring($type,1,3)='xs:'">
          <xsl:variable name="apos">'</xsl:variable>
          <xsl:variable name="onchange" select="concat('validateEntries($(this), ', $apos, $type, $apos, ');')"/>
          <input type="text" class="data">
            <xsl:attribute name="onchange">
              <xsl:value-of select="$onchange"/>
            </xsl:attribute>
            <xsl:if test="xs:annotation/xs:appinfo/libhpc:editDisabled">
              <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
          </input>
        </xsl:if>
        <xsl:if test="@type">
          <xsl:apply-templates select="/xs:schema/xs:complexType[@name=$type]" mode="findChildNodes">
            <xsl:with-param name="path" select="$this_path"/>
          </xsl:apply-templates>
          <xsl:apply-templates select="/xs:schema/xs:simpleType[@name=$type]" mode="findChildNodes">
            <xsl:with-param name="path" select="$this_path"/>
          </xsl:apply-templates>
        </xsl:if>
        <xsl:apply-templates mode="findChildNodes">
          <xsl:with-param name="path" select="$this_path"/>
        </xsl:apply-templates>
        <xsl:text> </xsl:text><xsl:value-of select="$units" disable-output-escaping="yes"/>
      </xsl:element><!-- </li> -->
    </xsl:element><!-- </ul> -->
  </xsl:template>

  <xsl:template match="xs:extension[@base='xs:string']/xs:attribute[@name='fileType']" mode="findChildNodes">
    <xsl:param name="path" />
    <span>
      <input type="file">
        <xsl:attribute name="onchange">
          <xsl:text>validateEntries($(this), 'xs:file', '{"xs:filetype": ["</xsl:text>
          <xsl:value-of select="@fixed"/>
          <xsl:text>"]}');extractEntriesFromFile(event, '</xsl:text>
          <xsl:value-of select="$path"/>
          <xsl:text>')</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="path">
          <xsl:value-of select="$path"/>
        </xsl:attribute>
      </input>
    </span>
  </xsl:template>

  <xsl:template match="xs:extension[@base='xs:string']/xs:attribute[@name='multipleFileType']" mode="findChildNodes">
    <span>
      <input type="file">
        <xsl:attribute name="onchange">
          <xsl:text>validateEntries($(this), 'xs:file', '{"xs:filetype": [</xsl:text>
          <xsl:call-template name="process-enumeration-restrictions">
            <xsl:with-param name="elements-to-process"
                            select="xs:simpleType/xs:restriction/child::*"/>
          </xsl:call-template>
          <xsl:text>]}');</xsl:text>
        </xsl:attribute>
      </input>
    </span>
  </xsl:template>

  <xsl:template match="xs:extension[@base='xs:string']/xs:attribute[@name='multipleFileTypeOutput']" mode="findChildNodes">
    <input type="text" class="data">
      <xsl:attribute name="onchange">
        <xsl:text>validateEntries($(this), 'xs:string', '{"xs:filetype": [</xsl:text>
        <xsl:call-template name="process-enumeration-restrictions">
          <xsl:with-param name="elements-to-process"
                          select="xs:simpleType/xs:restriction/child::*"/>
        </xsl:call-template>
        <xsl:text>]}');</xsl:text>
      </xsl:attribute>
    </input>
  </xsl:template>

  <!-- Recurse over the restrictions on xs:string to create json for use by javascript-->
  <xsl:template name="process-enumeration-restrictions">
    <xsl:param name="elements-to-process" select="/.."/>
    <xsl:param name="index" select="1"/>
    <xsl:if test="$elements-to-process">
      <xsl:variable name="element" select="$elements-to-process[1]"/>

      <!-- ... Do stuff with $element ...-->
      <xsl:if test="name($element) = 'xs:enumeration'">
        <xsl:if test="$index &gt; 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$element/@value"/>
        <xsl:text>"</xsl:text>
      </xsl:if>

      <!-- Recursively invoke template for remaining elements -->
      <xsl:call-template name="process-enumeration-restrictions">
        <xsl:with-param name="elements-to-process"
                        select="$elements-to-process[position() != 1]"/>
        <xsl:with-param name="index"
                        select="$index+1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xs:restriction[@base='xs:string']" mode="findChildNodes">
    <select class="choice">
      <xsl:attribute name="onchange">
        <xsl:text>validateEntries($(this), '</xsl:text>
        <xsl:value-of select="@base"/>
        <xsl:text>', '{"xs:enumeration": [</xsl:text>
        <xsl:call-template name="process-enumeration-restrictions">
          <xsl:with-param name="elements-to-process"
                          select="child::*"/>
        </xsl:call-template>
        <xsl:text>]}');</xsl:text>
      </xsl:attribute>
      <option value="Select from list">Select from list</option>
      <xsl:for-each select="xs:enumeration">
        <option>
          <xsl:attribute name="value">
            <xsl:value-of select="@value"/>
          </xsl:attribute>
          <xsl:if test="xs:annotation/xs:appinfo/libhpc:documentation">
            <xsl:attribute name="title">
              <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:documentation"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:value-of select="@value"/>
        </option>
      </xsl:for-each>
    </select>
  </xsl:template>


  <!-- Recurse over the restrictions on xs:double to create json for use by javascript-->
  <xsl:template name="process-restrictions">
    <xsl:param name="elements-to-process" select="/.."/>
    <xsl:param name="processed-elements" select="/.."/>
    <xsl:if test="$elements-to-process">
      <xsl:variable name="element" select="$elements-to-process[1]"/>

      <!-- ... Do stuff with $element ...-->
      <xsl:text>, "</xsl:text>
      <xsl:value-of select="name($element)"/>
      <xsl:text>": "</xsl:text>
      <xsl:value-of select="$element/@value"/>
      <xsl:text>"</xsl:text>

      <!-- Recursively invoke template for remaining elements -->
      <xsl:call-template name="process-restrictions">
        <xsl:with-param name="elements-to-process"
                        select="$elements-to-process[position() != 1]"/>
        <xsl:with-param name="processed-elements"
                        select="$processed-elements|$element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="xs:restriction" mode="findChildNodes">
    <input type="text" class="data">
      <xsl:attribute name="onchange">
        <xsl:text>validateEntries($(this), '</xsl:text>
        <xsl:value-of select="@base"/>
        <xsl:text>', '{"json": "restrictions"</xsl:text>
        <xsl:call-template name="process-restrictions">
          <xsl:with-param name="elements-to-process"
                          select="child::*"/>
        </xsl:call-template>
        <xsl:text>}');</xsl:text>
      </xsl:attribute>
    </input>
  </xsl:template>


  <xsl:template match="xs:choice" mode="findChildNodes">
    <xsl:param name="path" />
    <select class="choice" onchange="selectChoiceItem(event);">
      <xsl:attribute name="choice-path">
        <xsl:value-of select="$path" />
      </xsl:attribute>
      <option value="Select from list">Select from list</option>
      <xsl:for-each select="xs:element">
        <option>
          <xsl:attribute name="value">
            <xsl:value-of select="@name"/>
          </xsl:attribute>
          <xsl:if test="xs:annotation/xs:appinfo/libhpc:documentation">
            <xsl:attribute name="title">
              <xsl:value-of select="xs:annotation/xs:appinfo/libhpc:documentation" />
            </xsl:attribute>
          </xsl:if>
          <xsl:value-of select="@name"/>
        </option>
      </xsl:for-each>
    </select>
    <!-- Necessary so hide / show behaviour is correct-->
    <ul role="group" choice-id="Select from list">
      <xsl:attribute name="path">
        <xsl:value-of select="concat($path,'.Select from list')" />
      </xsl:attribute>
      <xsl:attribute name="chosen">true</xsl:attribute>
    </ul>
    <xsl:apply-templates mode="findChildNodes">
      <xsl:with-param name="path" select="$path"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="xs:complexType" mode="findChildNodes">
    <xsl:param name="path" />
    <xsl:apply-templates mode="findChildNodes">
      <xsl:with-param name="path" select="$path"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="xs:sequence" mode="findChildNodes">
    <xsl:param name="path" />
    <xsl:apply-templates mode="findChildNodes">
      <xsl:with-param name="path" select="$path"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="libhpc:documentation" mode="findChildNodes">
  </xsl:template>
  <xsl:template match="libhpc:units" mode="findChildNodes">
  </xsl:template>
  <xsl:template match="libhpc:locationInFile" mode="findChildNodes">
  </xsl:template>
  <xsl:template match="libhpc:refersToFile" mode="findChildNodes">
  </xsl:template>

  <xsl:template match="*">
    <xsl:message terminate="no">
      WARNING: Unmatched element: <xsl:value-of select="name()"/>
    </xsl:message>
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Prevent auto copying of all text. See:
        http://stackoverflow.com/questions/3360017/why-does-xslt-output-all-text-by-default
  -->
  <xsl:template match="text()|@*">
  </xsl:template>

  <xsl:template match="xs:schema/xs:element">
    <xsl:param name="node" select="@name"/>
    <xsl:param name="documentation" select="xs:annotation/xs:appinfo/libhpc:documentation"/>
        <div id="schema-tree">
          <div class="tree">
            <ul role="tree">
              <li class="parent_li" role="treeitem">
                <span>
                  <xsl:attribute name="data-fqname">
                    <xsl:value-of select="$node"/>
                  </xsl:attribute>
                  <xsl:attribute name="title">
                    <xsl:value-of select="$documentation"/>
                  </xsl:attribute>
                  <xsl:value-of select="$node"/>
                </span>
                <xsl:apply-templates mode="findChildNodes">
                  <xsl:with-param name="path" select="$node"/>
                </xsl:apply-templates>
              </li>
            </ul>
          </div>
        </div>
  </xsl:template>
</xsl:stylesheet>
