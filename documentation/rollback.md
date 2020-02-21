---
layout: default
title: Docs | Rollback 
---

# Rolling Back ChangeSets #

Liquibase allows you to undo changes you have made to your database, either automatically or via custom rollback SQL. Rollback support is available in [command_line](command_line.html), [Ant](ant/index.html), and [Maven](maven/index.html).


## How Rollback SQL is controlled ##

Many refactorings such as "create table", "rename column", and "add column" can automatically create rollback statements. If your change log contains only statements that fit into this category, your rollback commands will be generated automatically.

Other refactorings such as "drop table" and "insert data" have no corresponding rollback commands that can be automatically generated. In these cases, and cases where you want to override the default generated rollback commands, you can specify the rollback commands via the <rollback/> tag within the changeSet tag.  If you do not want anything done to undo a change in rollback mode, use an empty <rollback/> tag.


## Rollback Tag Available Attributes ##

<table>
<tr><th>Attribute</th><th>Description</th></tr>
<tr><td>nested tags</td><td>Standard Liquibase change tags to generate rollback statements  </td></tr>
<tr><td>tag text</td><td>SQL to run to rollback this change. The text in the element is treated as a sql change with `stripComments` set to `true`, 
`splitStatements` set to `true` and `endDelimiter` set to `;`.</td></tr>
<tr><td>changeSetId</td><td>Id of changeset to rerun in order to rollback this change.  Example: for rolling back a dropTable change, reference the changeSet that created the table.  </td></tr>
<tr><td>changeSetAuthor</td><td>Author of changeset to rerun in order to rollback this change  </td></tr>
<tr><td>changeSetPath</td><td>Normalized name of the file containing the changeset to rerun in order to rollback this
 changeSet. If not defined, the actual file is used.</td></tr>
</table>

## Samples ##

Many change tags do not need a rollback tag.  They can be generated from the update statement.
{% highlight xml %}
    <changeSet id="changeRollback2-create" author="nvoxland">
        <createTable tableName="changeRollback2">
            <column name="id" type="int"/>
        </createTable>
    </changeSet>
{% endhighlight %}

Standard change tags can be used in a `<rollback>` tag
{% highlight xml %}
    <changeSet id="changeRollback" author="nvoxland">
        <createTable tableName="changeRollback1">
            <column name="id" type="int"/>
        </createTable>
        <rollback>
            <dropTable tableName="changeRollback1"/>
        </rollback>
    </changeSet>
{% endhighlight %}

Multiple statements can be included in one `<rollback>` tag.  Multiple rollback tags can be specified in one changeSet.
{% highlight xml %}
<changeSet id="multiRollbackTest" author="rs">
        <createTable tableName="multiRollback1">
            <column name="id" type="int"/>
        </createTable>
        <createTable tableName="multiRollback2">
            <column name="id" type="int"/>
        </createTable>
        <createTable tableName="multiRollback3">
            <column name="id" type="int"/>
        </createTable>
        <rollback>
            drop table multiRollback1;
            drop table multiRollback2;
        </rollback>
        <rollback>drop table multiRollback3</rollback>
    </changeSet>
{% endhighlight %}

A rollback tag can reference the changeSet that originally created a statement.
{% highlight xml %}
    <changeSet id="changeRollback2-drop" author="nvoxland">
        <dropTable tableName="changeRollback2"/>
        <rollback changeSetId="changeRollback2-create" changeSetAuthor="nvoxland"/>
    </changeSet>
{% endhighlight %}

Rollback tags can be empty if no rollback is possible/needed
{% highlight xml %}
    <changeSet id="noRollback" author="nvoxland">
        <sql>insert into multiRollback3 (id) values (1)</sql>
        <rollback/>
    </changeSet>
{% endhighlight %}

## "Roll Back To" Modes ##

You can specify what changes to rollback in three ways:



### Tag ###

Specifying a tag to rollback to will roll back all change-sets that were executed against the target database after the given tag was applied. See the [command line](command_line.html) documentation for how to tag your database.

### Number of Change Sets ###

You can specify the number of change-sets to rollback.

### Date ###

You can specify the date to roll back to.

## Roll back Execution Modes ##

Liquibase has three modes for managing rollbacks:

### Execute Rollback Directly ###

The rollback commands can be executed directly against the target database. If any changes cannot be rolled back, you will be notified and none of the changes will roll back.

### Generating a Rollback Script ###

Rather than actually updating the database, the SQL required to roll back the database can be generated. This is useful if you want to preview what rollback commands will be executed before they are actually run.

### Generating a "Future Rollback" Script ###

This mode is designed to allow you to generate a rollback script at the same time you generate your migration script. It allows you to take an updated application and generate both the SQL to update the database to the new version as well as the SQL to bring that new version back to the current version if needed. This functionality is very useful when a DBA wants to control SQL going into the database, as well as for applications that require rollback documentation for internal and/or ["SOX-compliant"](http://en.wikipedia.org/wiki/Sarbanes-Oxley_Act) processes. You do not need to specify a rollback date, tag, or count in this mode.
