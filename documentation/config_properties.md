---
layout: default
title: Docs | Creating and Configuring liquibase.properties File 
---
# Creating and Configuring a liquibase.properties File
<div align="center">
<iframe width="560" height="315" src="https://www.youtube.com/embed/VokEe4hTFUI" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe></div>

Creating a *liquibase.properties* file allows you to store properties that don't change often in a *liquibase.properties* file. You can also use parameters to specify your database connection information so Liquibase can access your database and save you the trouble of passing along command line arguments.

#### Creating the File
To create a *liquibase.properties* file, generate a new text file in your project Liquibase directory and name it *liquibase.properties*.

> Note: The file can also be named something other than liquibase.properties and/or be in a completely different directory by using the --defaultsFile parameter during runtime.
To learn more please see [Liquibase Command Line](command_line.html).

## Parameters
The most common properties that you might set in the *liquibase.properties* are listed below. Any parameter that could be specified on
the command line can also be specified in the properties file. If a parameter is specified in both the properties file and the command line,
the command line value will override the value in the file.

| Parameter | Definition |
| --------- | ---------- |
| changeLogFile | The path for your changelog file|
| driver | The driver class name for your source database|
| url | The primary database when doing an update, or the source database for performing comparisons. |
| username | The username for your source database.|
| password | The password for your source database.|
| referenceDriver | The driver class name for your target database. |
| referenceUrl | The database you want to use to compare to your source database. Also known as your target. |
| referenceUsername | The username for your target database. |
| referencePassword | The password for your target database. |
| liquibaseProLicenseKey | Your Liquibase Pro license key (If you have one). |
| classpath | The path for your database driver |
| <code>parameter.</code>[parameter name] | Define global [change log parameter](changelog_parameters.html). E.g. parameter.table.name: tableA

Different commands require different parameter information to work. For more information on parameter requirements, search the Liquibase Command topics in the knowledge base.

## liquibase.properties File Example
{% highlight text %}
changeLogFile: ../path/to/file/dbchangelog.xml
driver: oracle.jdbc.OracleDriver
url: jdbc:oracle:thin:@192.168.0.22:1521/orcl
username: PRO
password: password
referenceDriver: oracle.jdbc.OracleDriver
referenceUrl: jdbc:oracle:thin:@192.168.0.22:1521/orcl
referencePassword: password
liquibaseProLicenseKey: aeioufakekey32aeioufakekey785463214
classpath: ../path/to/file/ojdbc6-11.2.0.3.0.jar
{% endhighlight %}
<p></p>

### Advanced configuration options

| Parameter | Definition |
| --------- | ---------- |
| includeCatalogInSpecification | <i>boolean</i> Should Liquibase include the catalog name when determining equality? |
| convertDataTypes | <i>boolean</i> Should Liquibase convert to/from STANDARD data types. Applies to both <code>snapshot</code> and <code>update</code> commands. Default= true|

### See Also ###
* [Using a liquibase.properties file in the command  line](command_line.html#using-a-liquibaseproperties-file)
* [Using a liquibase.properties file with the maven plugin](maven/index.html#using-configuration-property-files)
