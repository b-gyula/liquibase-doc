---
layout: default
title: Docs | Changelog parameters 
---
<script>
  $(function() {
    $( "#changelog-tabs" ).tabs();
  });
</script>

# Change Log Parameters #

**Since Liquibase 1.7**

Liquibase allows dynamic substitution of parameters in a changelog.  The parameters to replace are described using the <b>${parameter-name}</b> syntax.

## Configuring parameter values ##

Parameter values are looked up in the following order:

  1. Passed as a parameter to your Liquibase runner (see [Ant](ant/index.html), [Maven](maven/index.html), [Servlet listener](servlet_listener.html)) etc. documentation for how to pass them)
  1. As a JVM system property
  1. As an environment variable
  1. [command_line](command_line.html) parameter if executed from the command line
  1. [properties file](config_properties.html) if used or executed from the command line
  1. In the parameters block (<code>property</code> element) of the [DatabaseChangeLog](/documentation/databasechangelog.html) file itself

Once a parameter its value cannot be changed, only the first definition is used, other are skipped. 

<div id='changelog-tabs'>
<ul>
    <li><a href="#tab-xml">XML Sample</a></li>
    <li><a href="#tab-yaml">YAML Sample</a></li>
</ul>
<div id='tab-xml'>
{% highlight xml %}
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">

    <property name="clob.type" value="clob" dbms="oracle,postgresql"/>
    <property name="clob.type" value="longtext" dbms="mysql"/>
    <property name="table.name" value="tableA"/>

    <changeSet id="1" author="joe">
         <createTable tableName="${table.name}">
             <column name="id" type="int"/>
             <column name="${column1.name}" type="${clob.type}"/>
         </createTable>
    </changeSet>
</databaseChangeLog>
{% endhighlight %}
</div>
<div id='tab-yaml'>
{% highlight yaml %}
databaseChangeLog:
  - property:
      dbms: oracle,postgresql
      name: clob.type
      value: clob
  - property:
      dbms: mysql
      name: clob.type
      value: longtext
  - property:
      name: table.name
      value: tableA
  - changeSet:
      id: 1
      author: joe
      changes:
      - createTable:
          tableName: ${table.name}
          columns:
          - column:
              name: id
              type: int
          - column:
              name: ${column1.name}
              type: "${clob.type}
              defaultValue: a string with an ${undefined.param} param against ${dbNote}
{% endhighlight %}
</div></div>
<p></p>

### &lt;property&gt; ###

Defines a parameter for the changelog. Given a list of contexts and/or databases, the parameter will be only used in those contexts and/or databases.

#### Available Attributes ####

<table>
<tr><th>Attribute</th><th>Description</th></tr>
<tr><td>name</td><td>Name of the parameter. <b>Required if <code>file</code> is not set</b></td></tr>
<tr><td>value</td><td>Value of the of the property. <b>required if <code>file</code> is not set</b>  </td></tr>
<tr><td>file</td><td>Name of the file the properties shall be loaded from. It will create a property for all properties in the file. 
The content of the file has to follow the java properties file format</td></tr>
<tr><td>context</td><td>Contexts the property is valid in. Expected as comma separated list.  </td></tr>
<tr><td>dbms</td><td>The type of a database which that property is to be used. When the migration step is running, it checks the database type against this 
  attribute. Valid database type names are listed on the <a href="../databases.html">supported databases page</a>. It is possible to list multiple databases separated by commas. 
  You can also specify that a changeset is <b>NOT</b> applicable to a particular database type by prefixing with <code>!</code>. The keywords <code>all</code> and <code>none</code> are 
  also available.</td></tr>
<tr><td>global</td><td>boolean Defines whether the property is global or limited to the actual databaseChangeLog. Given as "true" or "false".  </td></tr>
</table>

Examples:

{% highlight xml %}
    <property name="simpleproperty" value="somevalue"/>
    <property name="clob.type" value="clob" dbms="oracle,h2"/>
    <property name="clob.type" value="longtext" dbms="mysql"/>
    <property name="myproperty" value="yes" context="common,test"/>
    <property name="localproperty" value="foo" global="false"/>
{% endhighlight %}
