<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            elementFormDefault="qualified">

<#list changes as name, change>
    <xsd:complexType name="${name}">
        <xsd:annotation>
            <xsd:documentation><![CDATA[${change.metaData.description}]]></xsd:documentation>
        </xsd:annotation>
        <xsd:choice maxOccurs="unbounded">
        <#list change.nestedParams as nP>
            <#if nP.shouldTypeBePrintedFlag>
            <xsd:element name="${nP.paramData.parameterName}" type="${nP.dataType}" minOccurs="${nP.isRequiredForAll()}"/>
            <#else >
            <xsd:element name="${nP.paramData.parameterName}" minOccurs="${nP.isRequiredForAll()}"/>
            </#if>
        </#list>
        </xsd:choice>
        <#list change.params as param>
            <#if param.shouldTypeBePrintedFlag>
                <#if param.isRequiredForAll() == 1>
            <xsd:attribute name="${param.paramData.parameterName}" type="${param.dataType}" use="required"/>
                <#else >
            <xsd:attribute name="${param.paramData.parameterName}" type="${param.dataType}"/>
                </#if>
            <#else >
                <#if param.isRequiredForAll() == 1>
            <xsd:attribute name="${param.paramData.parameterName}" use="required"/>
                <#else >
            <xsd:attribute name="${param.paramData.parameterName}"/>
                </#if>
            </#if>
            <xsd:annotation>
                <xsd:documentation><![CDATA[${change.metaData.description}]]></xsd:documentation>
            </xsd:annotation>
        </#list>
    </xsd:complexType>
</#list>
    <xsd:group name="changeSetChildren">
        <xsd:choice>
        <#list changes as name, change>
            <xsd:element name="${name}" type="${name}"/>
        </#list>
        </xsd:choice>
    </xsd:group>
</xsd:schema>