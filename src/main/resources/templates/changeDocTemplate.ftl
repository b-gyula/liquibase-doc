<#-- Template for generating documentation for custom changes -->
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            elementFormDefault="qualified">

<#list changes as name, change>
    <#assign changeMetaData = change.metaData>
    <xsd:complexType name="${changeMetaData.name}">
        <xsd:annotation>
            <xsd:documentation>
                ${changeMetaData.description}
            </xsd:documentation>
        </xsd:annotation>
        <#list change.nestedParams as changeNestedParams>
        <choice maxOccurs="unbounded">
             <#if changeNestedParams.requiredForAll() == true>
                 <#assign mO = 1>
            <#else>
                <#assign mO = 0>
            </#if>
            <element name="${changeNestedParams.parameterName}" type="${changeNestedParams.containedType}" minOccurs="${mO}" maxOccurs="unbounded"/>
        </choice>
        </#list>
        <#list change.getParams() as params>
        <#--  Mapping types to XSD types  -->
        <#if params.dataType == "list" || params.dataType == "databaseFunction" || params.dataType ==  "sequenceNextValueFunction"> <#--   list, databaseFunction and sequenceNextValueFunction should not be printed  -->
        <xsd:attribute name="${params.parameterName}">
        <#else>
            <#assign xsdType = "xsd:${params.dataType?lower_case}">
            <#if xsdType == "xsd:biginteger">
                <#assign xsdType = "xsd:integer">  <#--  Correct mapping for biginteger  -->
            <#--  Add other mappings as needed  -->
            </#if>
        <xsd:attribute name="${params.parameterName}" type="${xsdType}">
        </#if>
            <xsd:annotation>
                <xsd:documentation>
                    ${params.description}
                </xsd:documentation>
            </xsd:annotation>
        </xsd:attribute>
        </#list>
    </xsd:complexType>

    <xsd:group name="changeSetChildren">
        <xsd:choice>
            <#list change.getParams() as params>
             <#if params.dataType == "list" || params.dataType == "databaseFunction" || params.dataType ==  "sequenceNextValueFunction">
            <xsd:element name="${params.parameterName}"  maxOccurs="unbounded"/>
             <#else>
                 <#assign xsdType = "xsd:${params.dataType?lower_case}">
                 <#if xsdType == "xsd:biginteger">
                     <#assign xsdType = "xsd:integer">  <#--  Correct mapping for biginteger  -->
                     <#--  Add other mappings as needed  -->
                 </#if>
             <xsd:element name="${params.parameterName}" type="${xsdType}" maxOccurs="unbounded"/>
             </#if>
             </#list>
        </xsd:choice>
    </xsd:group>
</#list>

</xsd:schema>


