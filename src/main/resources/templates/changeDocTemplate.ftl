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
        <#if change.nestedParams?size != 0>
        <xsd:choice maxOccurs="unbounded">
        <#list change.nestedParams as nP>
            <#if nP.shouldTypeBePrintedFlag>
            <xsd:element name="${nP.paramData.parameterName}" type="${nP.dataType}" minOccurs="${nP.isRequiredForAll()}"/>
            <#else >
            <xsd:element name="${nP.paramData.parameterName}" minOccurs="${nP.isRequiredForAll()}"/>
            </#if>
        </#list>
        </xsd:choice>
        </#if>
        <#list change.params as param>

            <#if param.shouldTypeBePrintedFlag>
                <#if param.isRequiredForAll() == 1>
            <xsd:attribute name="${param.paramData.parameterName}" type="${param.dataType}" use="required">
                <#else >
            <xsd:attribute name="${param.paramData.parameterName}" type="${param.dataType}">
                </#if>
            <#else >
                <#if param.isRequiredForAll() == 1>
            <xsd:attribute name="${param.paramData.parameterName}" use="required">
                <#else >
            <xsd:attribute name="${param.paramData.parameterName}">
                </#if>
            </#if>
                <xsd:annotation>
                    <xsd:documentation><![CDATA[${param.paramData.description}]]></xsd:documentation>
                </xsd:annotation>
            </xsd:attribute>
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

    <xsd:simpleType name="propertyExpression" id="propertyExpression">
        <xsd:restriction base="xsd:string">
            <xsd:pattern value="\$\{[\w\.\-\+_]+\}"/>
        </xsd:restriction>
    </xsd:simpleType>

    <xsd:simpleType name="booleanExp" id="booleanExp">
        <xsd:annotation>
            <xsd:documentation>Extension to standard XSD boolean type to allow ${r"${}"} parameters</xsd:documentation>
        </xsd:annotation>

        <xsd:union>
            <xsd:simpleType>
                <xsd:restriction base="xsd:boolean"/>
            </xsd:simpleType>
            <xsd:simpleType>
                <xsd:restriction base="propertyExpression"/>
            </xsd:simpleType>
        </xsd:union>
    </xsd:simpleType>

    <xsd:simpleType name="integerExp" id="integerExp">
        <xsd:annotation>
            <xsd:documentation>Extension to standard XSD integer type to allow ${r"${}"} parameter placeholders</xsd:documentation>
        </xsd:annotation>
        <xsd:union>
            <xsd:simpleType>
                <xsd:restriction base="xsd:int">
                    <xsd:minInclusive value="0"/>
                </xsd:restriction>
            </xsd:simpleType>
            <xsd:simpleType>
                <xsd:restriction base="propertyExpression"/>
            </xsd:simpleType>
        </xsd:union>
    </xsd:simpleType>

    <xsd:simpleType name="nonEmptyString">
        <xsd:annotation>
            <xsd:documentation>String containing at least 1 non whitespace character</xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern value="[\S\t].*"/>
        </xsd:restriction>
    </xsd:simpleType>
</xsd:schema>