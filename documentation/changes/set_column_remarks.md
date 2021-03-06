---
layout: default
title: Docs | Change 'setColumnRemarks'
---

<!-- ====================================================== -->
<!-- GENERATED BY ChangeDocGenerator DO NOT MODIFY MANUALLY -->
<!-- ====================================================== -->

  <script>
  $(function() {
    $( "#changelog-tabs" ).tabs();
  });
</script>

# Change: 'setColumnRemarks'

Set remarks on a column

## Available Attributes ##

<table class='attribs'>
<tr><th>Name</th><th>Description</th></tr>
<tr><td class="name">catalogName</td><td class="desc">Name of the catalog<span class="support"><b>Supported by: </b>asany, db2, db2z, h2, mariadb, mysql, oracle, postgresql</span><span class="right"><span class="sample">E.g. <span class="val">&#x27;cat&#x27;</span></span></span></td></tr>
<tr><td class="name" required>columnName</td><td class="desc">Name of the column<span class="right"><span class="sample">E.g. <span class="val">&#x27;id&#x27;</span></span></span></td></tr>
<tr><td class="name" required>remarks</td><td class="desc">Comment to set on the column<span class="right"><span class="sample">E.g. <span class="val">&#x27;A String&#x27;</span></span></span></td></tr>
<tr><td class="name">schemaName</td><td class="desc">Name of the schema<span class="right"><span class="sample">E.g. <span class="val">&#x27;public&#x27;</span></span></span></td></tr>
<tr><td class="name" required>tableName</td><td class="desc">Name of the table<span class="right"><span class="sample">E.g. <span class="val">&#x27;person&#x27;</span></span></span></td></tr>
</table>

<div id='changelog-tabs'>
<ul>
    <li><a href="#tab-xml">XML Sample</a></li>
    <li><a href="#tab-yaml">YAML Sample</a></li>
    <li><a href="#tab-json">JSON Sample</a></li>
  </ul>
<div id='tab-xml'>
{% highlight xml %}
<changeSet author="liquibase-docs" id="setColumnRemarks-example">
    <setColumnRemarks catalogName="cat"
            columnName="id"
            remarks="A String"
            schemaName="public"
            tableName="person"/>
</changeSet>
{% endhighlight %}
</div>
<div id='tab-yaml'>
{% highlight yaml %}
changeSet:
  id: setColumnRemarks-example
  author: liquibase-docs
  changes:
  - setColumnRemarks:
      catalogName: cat
      columnName: id
      remarks: A String
      schemaName: public
      tableName: person

{% endhighlight %}
</div>
<div id='tab-json'>
{% highlight json %}
{
  "changeSet": {
    "id": "setColumnRemarks-example",
    "author": "liquibase-docs",
    "changes": [
      {
        "setColumnRemarks": {
          "catalogName": "cat",
          "columnName": "id",
          "remarks": "A String",
          "schemaName": "public",
          "tableName": "person"
        }
      }]
    
  }
}

{% endhighlight %}
</div>
</div>


## SQL Generated From Above Sample (MySQL)

{% highlight sql %}
ALTER TABLE cat.person COMMENT = 'A String';


{% endhighlight %}

## Database Support

<table style='border:1;'>
<tr><th>Database</th><th>Notes</th><th>Auto Rollback</th></tr>
<tr><td>DB2/LUW</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>DB2/z</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Derby</td><td>Not Supported</td><td>No</td></tr>
<tr><td>Firebird</td><td>Not Supported</td><td>No</td></tr>
<tr><td>H2</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>HyperSQL</td><td>Not Supported</td><td>No</td></tr>
<tr><td>INGRES</td><td>Not Supported</td><td>No</td></tr>
<tr><td>Informix</td><td>Not Supported</td><td>No</td></tr>
<tr><td>MariaDB</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>MySQL</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Oracle</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>PostgreSQL</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>SQL Server</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>SQLite</td><td>Not Supported</td><td>No</td></tr>
<tr><td>Sybase</td><td>Not Supported</td><td>No</td></tr>
<tr><td>Sybase Anywhere</td><td><b>Supported</b></td><td>No</td></tr>
</table>
