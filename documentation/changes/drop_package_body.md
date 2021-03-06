---
layout: default
title: Docs | Change 'dropPackageBody'
---

<!-- ====================================================== -->
<!-- GENERATED BY ChangeDocGenerator DO NOT MODIFY MANUALLY -->
<!-- ====================================================== -->

  <script>
  $(function() {
    $( "#changelog-tabs" ).tabs();
  });
</script>

# Change: 'dropPackageBody'

Drops an existing package body

## Available Attributes ##

<table class='attribs'>
<tr><th>Name</th><th>Description</th></tr>
<tr><td class="name">catalogName</td><td class="desc">Name of the catalog<span class="right"><span class="sample">E.g. <span class="val">&#x27;cat&#x27;</span></span></span></td></tr>
<tr><td class="name" required>packageBodyName</td><td class="desc">Name of the package body to drop<span class="right"><span class="sample">E.g. <span class="val">&#x27;A String&#x27;</span></span></span></td></tr>
<tr><td class="name">schemaName</td><td class="desc">Name of the schema<span class="right"><span class="sample">E.g. <span class="val">&#x27;public&#x27;</span></span></span></td></tr>
</table>

<div id='changelog-tabs'>
<ul>
    <li><a href="#tab-xml">XML Sample</a></li>
    <li><a href="#tab-yaml">YAML Sample</a></li>
    <li><a href="#tab-json">JSON Sample</a></li>
  </ul>
<div id='tab-xml'>
{% highlight xml %}
<changeSet author="liquibase-docs" id="dropPackageBody-example">
    <pro:dropPackageBody catalogName="cat"
            packageBodyName="A String"
            schemaName="public"/>
</changeSet>
{% endhighlight %}
</div>
<div id='tab-yaml'>
{% highlight yaml %}
changeSet:
  id: dropPackageBody-example
  author: liquibase-docs
  changes:
  - dropPackageBody:
      catalogName: cat
      packageBodyName: A String
      schemaName: public

{% endhighlight %}
</div>
<div id='tab-json'>
{% highlight json %}
{
  "changeSet": {
    "id": "dropPackageBody-example",
    "author": "liquibase-docs",
    "changes": [
      {
        "dropPackageBody": {
          "catalogName": "cat",
          "packageBodyName": "A String",
          "schemaName": "public"
        }
      }]
    
  }
}

{% endhighlight %}
</div>
</div>


## SQL Generated From Above Sample (MySQL)

{% highlight sql %}
DROP PACKAGE BODY cat.`A String`;


{% endhighlight %}

## Database Support

<table style='border:1;'>
<tr><th>Database</th><th>Notes</th><th>Auto Rollback</th></tr>
<tr><td>DB2/LUW</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>DB2/z</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Derby</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Firebird</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>H2</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>HyperSQL</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>INGRES</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Informix</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>MariaDB</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>MySQL</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Oracle</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>PostgreSQL</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>SQL Server</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>SQLite</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Sybase</td><td><b>Supported</b></td><td>No</td></tr>
<tr><td>Sybase Anywhere</td><td><b>Supported</b></td><td>No</td></tr>
</table>
