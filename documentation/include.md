---
layout: default
title: Docs | Include tag 
---
# Include Tag

The include tag allows you to break up your change-logs into more manageable pieces. To easily include multiple files, use the [includeAll](includeall.html) tag.

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>

<databaseChangeLog
  xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    <include file="com/example/news/news.changelog.xml"/>
    <include file="com/example/directory/directory.changelog.xml"/>
</databaseChangeLog>
{% endhighlight %}

As projects grow, the number of changeSets in a changeLog can grow unwieldy. To help ease this problem, 
and to make management of changes easier, databaseChangeLogs can be included to create a tree of change-logs. 
In the example above, the root change log includes first the changes in `com/example/news/news.changelog.xml` then 
the changes in `com/example/directory/directory.changelog.xml`.

Included change-logs are run in the order they are found so care does need to be taken to make sure that the included 
changelogs are either completely independent, or that any required changelogs are run first.

Any [preconditions](preconditions.html) defined at the changelog level in sub changelog files will be evaluated *before* any changesets are ran.

The reason to use the `<include>` tag rather than using XML's built-in include functionality is that 
with the built-in functionality the parser sees just one big XML document. Liquibase needs to uniquely identify each changeset with 
the id, the author, and the file name. By using the `<include>` and `<includeAll>` tags Liquibase makes it so you only have to 
ensure that the id/author combinations are unique *within each file*, not across all change logs.

## Available Attributes

<table>
<tr><th>Attribute</th><th>Description</th></tr>
<tr><td>file</td><td>Name of the file to import <b>required</b> </td></tr>
<tr><td>relativeToChangelogFile</td><td>Is the file path relative to the changelog file containing the element rather than to the classpath. Defaults to "false" <b>since 1.9</b> </td></tr>
<tr><td>context</td><td>Append context (using AND) to all contained changeSets <b>since 3.5</b> </td></tr>
</table>


## Implementation Notes

Currently there is no checking for looping changelogs or double inclusion of changelogs.

If you include a changelog twice, you shouldn't run into problems because the second time around, Liquibase will know that the changeSets 
have been run and won't run them again (even if there is a runAlways tag).

If you create a changeLog loop (root.changelog.xml includes news.changelog.xml which includes root.changelog.xml) you will get 
an infinite loop. Checks for loops is a feature on our list of enhancements, but is currently not implemented.
