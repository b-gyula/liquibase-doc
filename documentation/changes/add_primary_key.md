---
layout: default
title: Docs | Change 'addPrimaryKey'
---

<!-- ====================================================== -->
<!-- GENERATED BY ChangeDocGenerator DO NOT MODIFY MANUALLY -->
<!-- ====================================================== -->

  <script>
  $(function() {
    $( "#changelog-tabs" ).tabs();
  });
</script>

# Change: 'addPrimaryKey'

Adds a primary key out of an existing column or set of columns.

## Available Attributes ##

<table class='attribs'>
<tr><th>Name</th><th>Description</th></tr>
<tr><td class="name">catalogName</td><td class="desc">Name of the catalog<span class="right"><span class="since">@ v3.0</span><span class="sample">E.g. <span class="val">&#x27;cat&#x27;</span></span></span></td></tr>
<tr><td class="name">clustered</td><td class="desc"><span class="type">boolean</span><span class="right"></span></td></tr>
<tr><td class="name" required>columnNames</td><td class="desc">Name of the column(s) to create the primary key on. Comma separated if multiple<span class="right"><span class="sample">E.g. <span class="val">&#x27;id, name&#x27;</span></span></span></td></tr>
<tr><td class="name">constraintName</td><td class="desc">Name of primary key constraint<span class="right"><span class="sample">E.g. <span class="val">&#x27;pk_person&#x27;</span></span></span></td></tr>
<tr><td class="name">forIndexCatalogName</td><td class="desc"><span class="right"></span></td></tr>
<tr><td class="name">forIndexName</td><td class="desc"><span class="right"><span class="sample">E.g. <span class="val">&#x27;A String&#x27;</span></span></span><span class="right"><b>Supported by: </b>db2, db2z, oracle</span></td></tr>
<tr><td class="name">forIndexSchemaName</td><td class="desc"><span class="right"></span></td></tr>
<tr><td class="name">schemaName</td><td class="desc">Name of the schema<span class="right"><span class="sample">E.g. <span class="val">&#x27;public&#x27;</span></span></span></td></tr>
<tr><td class="name" required>tableName</td><td class="desc">Name of the table to create the primary key on<span class="right"><span class="sample">E.g. <span class="val">&#x27;person&#x27;</span></span></span></td></tr>
<tr><td class="name">tablespace</td><td class="desc"><span class="right"><span class="sample">E.g. <span class="val">&#x27;A String&#x27;</span></span></span></td></tr>
<tr><td class="name">validate</td><td class="desc"><span class="type">boolean</span>This is true if the primary key has 'ENABLE VALIDATE' set, or false if the primary key has 'ENABLE NOVALIDATE' set.<span class="right"></span></td></tr>
</table>

<div id='changelog-tabs'>
<ul>
    <li><a href="#tab-xml">XML Sample</a></li>
    <li><a href="#tab-yaml">YAML Sample</a></li>
    <li><a href="#tab-json">JSON Sample</a></li>
  </ul>
<div id='tab-xml'>
{% highlight xml %}
<changeSet author="liquibase-docs" id="addPrimaryKey-example">
    <addPrimaryKey catalogName="cat"
            clustered="true"
            columnNames="id, name"
            constraintName="pk_person"
            forIndexName="A String"
            schemaName="public"
            tableName="person"
            tablespace="A String"
            validate="true"/>
</changeSet>
{% endhighlight %}
</div>
<div id='tab-yaml'>
{% highlight yaml %}
changeSet:
  id: addPrimaryKey-example
  author: liquibase-docs
  changes:
  - addPrimaryKey:
      catalogName: cat
      clustered: true
      columnNames: id, name
      constraintName: pk_person
      forIndexName: A String
      schemaName: public
      tableName: person
      tablespace: A String
      validate: true

{% endhighlight %}
</div>
<div id='tab-json'>
{% highlight json %}
{
  "changeSet": {
    "id": "addPrimaryKey-example",
    "author": "liquibase-docs",
    "changes": [
      {
        "addPrimaryKey": {
          "catalogName": "cat",
          "clustered": true,
          "columnNames": "id, name",
          "constraintName": "pk_person",
          "forIndexName": "A String",
          "schemaName": "public",
          "tableName": "person",
          "tablespace": "A String",
          "validate": true
        }
      }]
    
  }
}

{% endhighlight %}
</div>
</div>


## SQL Generated From Above Sample (MySQL)

{% highlight sql %}
ALTER TABLE cat.person ADD PRIMARY KEY (id,
 name);


{% endhighlight %}

## Database Support

<table style='border:1;'>
<tr><th>Database</th><th>Notes</th><th>Auto Rollback</th></tr>
<tr><td>DB2/LUW</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>DB2/z</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>Derby</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>Firebird</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>H2</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>HyperSQL</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>INGRES</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>Informix</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>MariaDB</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>MySQL</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>Oracle</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>PostgreSQL</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>SQL Server</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>SQLite</td><td>Not Supported</td><td><b>Yes</b></td></tr>
<tr><td>Sybase</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
<tr><td>Sybase Anywhere</td><td><b>Supported</b></td><td><b>Yes</b></td></tr>
</table>
