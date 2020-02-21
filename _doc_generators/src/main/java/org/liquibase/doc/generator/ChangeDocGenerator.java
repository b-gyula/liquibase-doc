package org.liquibase.doc.generator;

import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import liquibase.change.*;
import liquibase.change.core.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.*;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializable.SerializationType;
import liquibase.serializer.core.json.JsonChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

public class ChangeDocGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocGenerator.class);

    public static void main(String[] args) throws Exception {
        logger.info("Generating documentation for all changes in included jar");
        Map<String, SortedSet<Class<? extends Change>>> definedChanges = ChangeFactory.getInstance().getRegistry();
        TreeMap<String, SortedSet<Class<? extends Change>>> sortedChanges = new TreeMap<>(definedChanges);

        List <Database> databases = DatabaseFactory.getInstance().getImplementedDatabases();
        Collections.sort(databases, new Comparator<Database>() {
            @Override
            public int compare(Database o1, Database o2) {
                return o1.getDatabaseProductName().compareTo(o2.getDatabaseProductName());
            }
        });

        writeChangeNav(sortedChanges);
        writeChangePages(sortedChanges, databases);
    }

    private static void writeChangeNav(Map<String, SortedSet<Class<? extends Change>>> definedChanges) throws Exception {
        // Ensure the directory exists so we know we are running in the right place.
        File includesDir = new File("_includes");
        if (! includesDir.exists() || ! includesDir.isDirectory()) {
            logger.error("The directory '" + includesDir.getAbsolutePath() + "' was not found. Copy the jar file to the root directory of the project and re-run.");
            System.exit(1);
        }

        String content = "";
        content = addGeneratedHeader(content);
        content += "<li><a href=\"/documentation/index.html\"><span>Documentation Home</span></a></li>\n\n<hr>\n";

        // Write out the Pro changes
        content += "<h3 style='color: #fd8c00'>Liquibase Pro Changes</h3>\n";
        content += addChangesToContent(definedChanges, true);

        // Write out the Core changes
        content += "\n<hr>\n";
        content += "<h3 style='color: #747373'>Liquibase Changes</h3>\n\n";
        content += addChangesToContent(definedChanges, false);

        // Write out the file
        File file = new File("_includes/subnav_documentation_changes.md");
        new FileOutputStream(file).write(content.getBytes());
    }

    private static String addChangesToContent(Map<String, SortedSet<Class<? extends Change>>> definedChanges, boolean onlyProClasses) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry mapEntry : definedChanges.entrySet()) {
            String changeName = (String)mapEntry.getKey();
            SortedSet<Class<? extends Change>> changeClasses = (SortedSet<Class<? extends Change>>) mapEntry.getValue();
            for (Class change:changeClasses) {
                String packageName = change.getName();
                String navEntry = "<li><a href='" + getChangeDocFileName(changeName) + ".html'><span>" + changeName.replaceAll("([A-Z])", " $1") + "</span></a></li>\n";
                if (onlyProClasses) {
                    if (isProClass(packageName)) {
                        logger.info ("adding Pro subnav entry for {}",change.getSimpleName());
                        content.append (navEntry);
                    }
                } else {
                    if (! isProClass(packageName)) {
                        logger.info ("adding Core subnav entry for {}",change.getSimpleName());
                        content.append(navEntry);
                    }
                }
            }
        }
        return content.toString();
    }

    // return true if the change is in one of the "pro" packages, false if part of core.
    private static boolean isProClass(String packageName) {
        return packageName.startsWith("com.datical");
    }

    /**
     * Wrapper class to store the default values along with the parameters
     */
    public static class ChangeParamMetaData extends ChangeParameterMetaData {
        String defaultValue = "";
        Class containedType = null;
        final Change change;

        ChangeParamMetaData(Database db, ChangeParameterMetaData orig, Change change) {
            super(change, orig.getParameterName(),
                    orig.getDisplayName(), orig.getDescription(),
                    new HashMap<String, Object>() {{
                        put(db.getShortName(), orig.getExampleValue(db));
                    }}, orig.getSince(), orig.getDataTypeClass()
                    , orig.getRequiredForDatabase().toArray(new String[0])
                    , orig.getSupportedDatabases().toArray(new String[0])
                    , orig.getMustEqualExisting(), orig.getSerializationType());
            if (!orig.getDataType().contains(" of ")) {
                try {
                    Object o = orig.getCurrentValue(change);
                    boolean isBool = "boolean".equals(orig.getDataType());
                    if ((isBool && null != o && (boolean) o) || !isBool) {
                        this.defaultValue = o == null ? "" : o.toString();
                    }
                } catch (Exception e) {
                    logger.error("Error getting current value for: " + change.getSerializedObjectName()
                            + " / " + orig.getParameterName(), e);
                }
            } else {
                containedType = (Class) orig.getDataTypeClassParameters()[0];
            }
            this.change = change;
        }

        boolean isContainer() {
            return null != containedType;
        }

        String getDefaultValue() {
            return this.defaultValue;
        }

        Class getContainedType() {
            return containedType;
        }

        boolean requiredForAll() {
            return getRequiredForDatabase().contains(ALL);
        }

        boolean isNested() {return getSerializationType() == SerializationType.NESTED_OBJECT;}
        boolean isDirectValue() {return getSerializationType() == SerializationType.DIRECT_VALUE;}
    }

    /**
     * [change name] -> [columnConfig] map
     */
    static final Map<String, String> specialColumnConfig = new HashMap<>();

    static {
        specialColumnConfig.put("dropColumn", "DropColumnConfig");
        specialColumnConfig.put("createIndex", "IndexColumnConfig");
        specialColumnConfig.put("insert", "insertUpdateColumnConfig");
        specialColumnConfig.put("update", "insertUpdateColumnConfig");
    }

    private static List<ChangeParamMetaData> setExamples(MySQLDatabase defaultExampleDatabase, Change exampleChange, ChangeMetaData changeMetaData) {
        List<ChangeParamMetaData> params = new ArrayList<>();
            for (ChangeParameterMetaData param : changeMetaData.getParameters().values()) {
                if (param.getParameterName().equals("encoding")) {
                param.setValue(exampleChange, "utf-8"); // Default value for encoding
            }
            params.add(new ChangeParamMetaData(defaultExampleDatabase, param, exampleChange));
            if (param.getParameterName().equals("replaceIfExists")) {
                    param.setValue(exampleChange, false);
                } else if (param.getParameterName().equals("defaultOnNull")) {
                    param.setValue(exampleChange, false);
            } else if (Collection.class.isAssignableFrom(param.getDataTypeClass())) {
                Collection exampleValue = (Collection) param.getExampleValue(defaultExampleDatabase);
                ConstraintsConfig constrNonNull = new ConstraintsConfig().setNullable(false);
                    if (param.getDataType().endsWith(" of columnConfig")) {
                    ColumnConfig columnConfig = new ColumnConfig().setName("address");
                    switch (changeMetaData.getName()) {
                        case "insert":
                        case "update":
                            columnConfig.setValue("address value");
                        case "dropColumn":
                            exampleValue = Arrays.asList(columnConfig);
                            break;
                        case "createTable":
                            ArrayList<ColumnConfig> columns =
                                    new ArrayList<>((Collection<ColumnConfig>) exampleValue);
                            columns.get(0).setConstraints(constrNonNull);
                            columns.add(columnConfig.setType("varchar(50)"));
                            exampleValue = columns;
                    }
                    } else if (param.getDataType().endsWith(" of addColumnConfig")) {
                        AddColumnConfig columnConfig = new AddColumnConfig();
                        columnConfig.setName("address");
                    if (exampleChange instanceof CreateIndexChange) {
                        columnConfig.setDescending(true);
                        exampleValue = Arrays.asList(columnConfig);
                    } else {
                        columnConfig.setType("varchar(255)");
                        columnConfig.setPosition(2);
                        AddColumnConfig cfg2 = new AddColumnConfig();
                        cfg2.setName("name");
                        cfg2.setType("varchar(50)");
                        cfg2.setAfterColumn("id");
                        exampleValue = Arrays.asList(columnConfig, cfg2.setConstraints(constrNonNull));
                    }
                }

                if (null == exampleValue) {
                    logger.warn("No example values for: " + param.getParameterName() + ": " + param.getDataType());
                    } else {
                    ((Collection) param.getCurrentValue(exampleChange)).addAll(exampleValue);
                    }
                } else {
                    Object exampleValue = param.getExampleValue(defaultExampleDatabase);
                    if (exampleValue != null && exampleValue.equals("A String")) {
                        if (param.getParameterName().toLowerCase().contains("schema") || param.getParameterName().toLowerCase().contains("catalog")) {
                            exampleValue = null;
                        }
                    }
                        param.setValue(exampleChange, exampleValue);
            }
                    }

        if (CustomChangeWrapper.class.isAssignableFrom(exampleChange.getClass())) {
            try {
                CustomChangeWrapper custom = (CustomChangeWrapper) exampleChange;
                custom.setClassLoader(exampleChange.getClass().getClassLoader());
                custom.setClass("com.example.CustomChange");
            } catch (Exception e) { // Expected
                }
            }
        return params;
    }

    private static void writeChangePages(Map<String, SortedSet<Class<? extends Change>>> definedChanges, List<Database> databases) throws Exception {
        List<Database> exampleDatabases = new ArrayList<Database>(databases);
        exampleDatabases.add(0, new HsqlDatabase());
        exampleDatabases.add(0, new OracleDatabase());
        exampleDatabases.add(0, new MSSQLDatabase());
        MySQLDatabase defaultExampleDatabase = new MySQLDatabase();
        exampleDatabases.add(0, defaultExampleDatabase);

        for (String changeName : definedChanges.keySet()) {
            Change exampleChange = ChangeFactory.getInstance().create(changeName);
            ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(exampleChange);
            List<ChangeParamMetaData> params = setExamples(defaultExampleDatabase, exampleChange, changeMetaData);

            String content = "---\n" +
                    "layout: default\n" +
                    "title: Docs | Change '" + changeMetaData.getName() + "'\n" +
                    "---\n\n";

            content = addGeneratedHeader(content);

            content += "<script>\n" +
                    "  $(function() {\n" +
                    "    $( \"#changelog-tabs\" ).tabs();\n" +
                    "  });\n" +
                    "</script>\n\n";

            ChangeSet exampleChangeSet = new ChangeSet(exampleChange.getSerializedObjectName() + "-example", "liquibase-docs", false, false,
                    "changelog.xml", null, null, null, null);
            exampleChangeSet.addChange(exampleChange);

            content += "# Change: '" + changeMetaData.getName() + "'\n\n";
            content += changeMetaData.getDescription().replace("<", "&lt;").replace(">", "&gt;").replace("\\", "\\\\").replace("*", "\\*") + "\n\n";

            content += "## Available Attributes ##\n\n";
            content += "<table class='attribs'>\n";
            content += tr(th("Name"), th("Description")).render() + "\n";
            Collections.sort(params, new Comparator<ChangeParameterMetaData>() {
                @Override
                public int compare(ChangeParameterMetaData o1, ChangeParameterMetaData o2) {
                    return o1.getParameterName().compareTo(o2.getParameterName());
                }
            });

            List<ChangeParamMetaData> nestedParams = new ArrayList<>();
            for (ChangeParamMetaData param : params) {
                if (param.isContainer() || param.isNested()) {
                    nestedParams.add(param);
                    continue;
                }

                content += tr(
                        td(attrs(".name"),
                                (param.isDirectValue() ? "[XML: text content] / " : "") + param.getParameterName())
                                .withCondRequired(param.requiredForAll()),
                        td(attrs(".desc"), rawHtml(getParamDescription(changeMetaData, param, exampleChange)))
                                .with(commonAttribs(param))
                                .condWith(param.isDirectValue(), note("", "the content of the tag in XML"))
                ).render() + "\n";
            }
            content += "</table>\n\n";

            if (nestedParams.size() > 0) {
                content += "## Nested Properties ##\n\n";
                ContainerTag table = table(attrs("#nestedProps.attribs")).withText("\n");
                table.with(tr(th("Name"), th("Description")));
                for (ChangeParamMetaData param : nestedParams) {
                    ContainerTag description = td(attrs(".desc"),
                            rawHtml(getParamDescription(changeMetaData, param, exampleChange)));
                    if (!param.isNested()) {
                        description.with(note(param.getParameterName(), "tag not required in XML"));
                    }
                    ContainerTag name = td(attrs(".name"), param.getParameterName());
                    if (param.isContainer()) {
                        Class collectionType = param.getContainedType();
                        LiquibaseSerializable child = (LiquibaseSerializable) collectionType.getConstructor().newInstance();
                        String childName = child.getSerializedObjectName();
                        String sNestedConfig = (collectionType.getSimpleName().equals("ColumnConfig")
                                || collectionType.getSimpleName().equals("AddColumnConfig")) ?
                                specialColumnConfig.get(changeName) : collectionType.getSimpleName();
                        if (null == sNestedConfig) {
                            description.with(br(), br(),
                                    join("See the", a("column tag").withHref("../column.html"),
                                            "documentation for more information"));
                        } else if (LiquibaseSerializable.class.isAssignableFrom(collectionType)) {
                            // Not nested element -> inner children are described only
                            description.with(div(attrs(".header"), "Attributes"),
                                    table(attrs("#nestedAttrs"), "{%include " + sNestedConfig + ".md%}"));
                    }
                        name.with(join("&nbsp;/",
                                span(attrs(".right"), text(childName), rawHtml("&nbsp;"),
                                        sup((param.requiredForAll() ? "[1" : "[0") + "..N]"))
                                        .withCondRequired(param.requiredForAll())));
                    } else {
                        description.with(commonAttribs(param));
                }
                    table.with(tr(name, description)).withText("\n");
                }
                content += table.render();
            }

            content += "<div id='changelog-tabs'>\n" +
                    "<ul>\n" +
                    "    <li><a href=\"#tab-xml\">XML Sample</a></li>\n" +
                    "    <li><a href=\"#tab-yaml\">YAML Sample</a></li>\n" +
                    "    <li><a href=\"#tab-json\">JSON Sample</a></li>\n" +
                    "  </ul>\n";
            content += "<div id='tab-xml'>\n";
            content += "{% highlight xml %}\n";
            content += new XMLChangeLogSerializer().serialize(exampleChangeSet, true);
            content += "\n{% endhighlight %}\n";
            content += "</div>\n";

            content += "<div id='tab-yaml'>\n";
            content += "{% highlight yaml %}\n";
            content += new YamlChangeLogSerializer().serialize(exampleChangeSet, true);
            content += "\n{% endhighlight %}\n";
            content += "</div>\n";

            content += "<div id='tab-json'>\n";
            content += "{% highlight json %}\n";
            content += new JsonChangeLogSerializer().serialize(exampleChangeSet, true);
            content += "\n{% endhighlight %}\n";
            content += "</div>\n";

            content += "</div>\n\n\n";

            exampleChange.setResourceAccessor(new CompositeResourceAccessor(new ClassLoaderResourceAccessor(ChangeDocGenerator.class.getClassLoader()), new FakeDataResourceAccessor()));
            if (exampleChange instanceof CreateProcedureChange) {
                ((CreateProcedureChange) exampleChange).setPath(null); // use procedureText.
            } else if (exampleChange instanceof CreateViewChange) {
                ((CreateViewChange) exampleChange).setPath(null); // use selectQuery
                ((CreateViewChange) exampleChange).setFullDefinition(false); // fix to match example selectQuery
            } else if (exampleChange instanceof LoadDataChange) {
                ((LoadDataChange) exampleChange).setFile("org/liquibase/doc/generator/example.csv");
            } else if (exampleChange instanceof SQLFileChange) {
                ((SQLFileChange) exampleChange).setPath("org/liquibase/doc/generator/sqlfile.sql");
            }

            Database exampleDatabase = null;
            for (Database db : exampleDatabases) {
                if (exampleChange.supports(db)) {
                    exampleDatabase = db;
                    break;
                }
            }

            if (exampleChange instanceof LoadDataChange) {
                ((LoadDataChange) exampleChange).setFile("org/liquibase/doc/generator/example.csv");
            }

            if (canGeneratePage(exampleChange, exampleDatabase)) {
                if (exampleDatabase != null) {
                    String sql = "";
                    try {
                        for (SqlStatement statement : exampleChange.generateStatements(exampleDatabase)) {
                            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, exampleDatabase);
                            if (sqls != null) {
                                for (Sql out : sqls) {
                                    sql += out.toSql() + out.getEndDelimiter() + "\n\n";
                                }
                            }
                        }
                    } catch (Throwable e) {
                        throw e;
                    }
                    sql = sql.replace(",", ",\n");
                    content += "## SQL Generated From Above Sample (" + exampleDatabase.getDatabaseProductName() + ")\n\n";
                    content += "{% highlight sql %}\n";
                    content += sql;
                    content += "\n{% endhighlight %}\n\n";
                }
            }

            content += "## Database Support\n\n";
            content += "<table style='border:1;'>\n";
            content += "<tr><th>Database</th><th>Notes</th><th>Auto Rollback</th></tr>\n";
            for (Database database : databases) {
                if (database.getShortName().equals("unsupported")) {
                    continue;
                }

                String supported;
                if (exampleChange.supports(database)) {
                    supported = "<b>Supported</b>";
                } else {
                    supported = "Not Supported";
                }
                String notes = StringUtils.trimToNull(changeMetaData.getNotes(database.getShortName()));
                if (notes != null) {
                    supported += ": " + notes;
                }

                String rollback;
                if (exampleChange.supportsRollback(database)) {
                    rollback = "<b>Yes</b>";
                } else {
                    rollback = "No";
                }

                content += "<tr><td>" + database.getDatabaseProductName() + "</td><td>" + supported + "</td><td>" + rollback + "</td></tr>\n";
            }
            content += "</table>\n";

            if (exampleChange instanceof CreateProcedureChange) {
                // Fix deprecated attribute name
                content = content.replaceAll("procedureBody", "procedureText");
            }

            String changename = changeMetaData.getName();

            File documentationDir = new File("documentation");
            if (! documentationDir.exists() || ! documentationDir.isDirectory()) {
                logger.error("The directory '" + documentationDir.getAbsolutePath() + "' was not found. Copy the jar file to the root directory of the project and re-run.");
                System.exit(1);
            }

            String pathname = "documentation/changes/" + getChangeDocFileName(changename) + ".md";
            File file = new File(pathname);
            logger.info("Writing content to '" + pathname + "'");
            new FileOutputStream(file).write(content.getBytes());
        }
    }

    private static ContainerTag note(String paramName, String text) {
        return span(attrs(".right"), join(b("Note:"),
                i(paramName), text));
    }

    final static String ALL = "all";

    final static List<String> exampleValueForType = Arrays.asList("string", "bigInteger");
    /**
     * List types should not be displayed
     */
    final static List<String> skipTypeForType = Arrays.asList("string", "list", "databaseFunction", "sequenceNextValueFunction");
    /**
     * Display names for types
     */
    final static Map<String, String> typeDisplayName = new HashMap<>();

    static {
        typeDisplayName.put("bigInteger", "integer");
    }

    /**
     * Generate common attributes section in this order: required / supports / since + default + sample
     * required and supported is right aligned only if they are shorter, tha the half width
     * since + default + sample is in a separate section right aligned together
     * Their position is depending required and supports right alignment, because right aligned
     * elements has to be in the reverse order
     */
    private static Tag[] commonAttribs(ChangeParamMetaData param) {
        List<Tag> attribs = new ArrayList<>();
        TreeSet<String> requiredForDatabase = new TreeSet<>(param.getRequiredForDatabase());
        TreeSet<String> supportsDatabase = new TreeSet<>(param.getSupportedDatabases());
        int iRAttribPos = 0;
        String supports = StringUtils.trimToEmpty(StringUtils.join(supportsDatabase, ", "));
        if (!requiredForDatabase.isEmpty() && !param.requiredForAll()) {
            String required = StringUtils.trimToEmpty(StringUtils.join(requiredForDatabase, ", "));
            Tag tReqs = span(attrs(".required"), b("Required for: "), text(required));
            attribs.add(tReqs);
            if (required.length() < 50 && supports.length() < 50) {
                tReqs.withClass("right");
            } else {
                iRAttribPos++;
            }
        }

        if (!supportsDatabase.isEmpty() && !supportsDatabase.contains(ALL)) {
            Tag tSup = span(attrs(".support"), b("Supported by: "), text(supports));
            attribs.add(tSup);
            if (supports.length() < 50) {
                tSup.withClass("right");
            } else {
                iRAttribPos++;
            }
        }
        String sample = getSampleValue(param);
        ContainerTag rAttribs = span(attrs(".right"), "");

        String since = StringUtils.trimToEmpty(param.getSince());
        if (!since.isEmpty()) {
            rAttribs.with(span(attrs(".since"), "@ v" + since));
        }

        if (!param.requiredForAll() && !param.getDefaultValue().isEmpty()) {
            rAttribs.with(span(attrs(".default"), text("Default: "),
                    span(attrs(".val"), param.getDataType().equals("string") ? "'" + param.getDefaultValue() + "'" :
                            param.getDefaultValue())));
        } else if (!sample.isEmpty()) {
            rAttribs.with(span(attrs(".sample"), text("E.g. "),
                    span(attrs(".val"), "'" + sample + "'")));
        }
        if (rAttribs.getNumChildren() > 0) {
            attribs.add(iRAttribPos, rAttribs);
        }
        return attribs.toArray(new Tag[0]);
    }

    private static String getSampleValue(ChangeParamMetaData param) {
        Object sampleValue = param.getCurrentValue(param.change);
        String sample = "";
        if (exampleValueForType.contains(param.getDataType()) && sampleValue != null) {
            sample = StringUtils.trimToEmpty(sampleValue.toString());
        }
        return sample;
    }

    /**
     * Get param description. Return generic if not set
     */
    private static String getParamDescription(ChangeMetaData changeMetaData, ChangeParamMetaData param
            , Change change) {
        String description = param.getDescription();
        if (StringUtils.isEmpty(description)) {
            Optional<String> fileOrPath = Stream.of("file", "path")
                    .filter((String p) -> changeMetaData.getParameters().get(p) != null).findFirst();
            switch (param.getParameterName()) {
                case "encoding":
                    if (fileOrPath.isPresent()) {
                        description = "Name of the encoding (as specified in " +
                                "<a href=\"http://docs.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html\">java.nio.Charset javadoc</a>)" +
                                " used in the file defined in the `" + fileOrPath.get() + "` attribute";
                    }
                    break;
                case "relativeToChangelogFile":
                    description = "Whether the file path is relative to the root changelog file rather than to the " +
                            "classpath.";
                    break;
                case "dbms":
                    description = "Logical expression of database type(s) on which the change must be applied. " +
                            "Valid database type names are listed on the <a href='../../databases.html'>supported " +
                            "databases page</a>\n" +
                            "It can be a comma separated list of multiple databases.\n" +
                            "Or You can also specify that a change is <b>NOT</b> applicable to a " +
                            "particular database type by prefixing with <code>!</code>. " +
                            "The keywords <code>all</code> and <code>none</code> are also available.";
                    break;
            }
        }
        if (!skipTypeForType.contains(param.getDataType())) {
            String type = typeDisplayName.get(param.getDataType());
            description = span(attrs(".type"), (null == type ? param.getDataType() : type)).render()
                    + description;
        }
        return description;
    }

    private static boolean canGeneratePage(Change exampleChange, Database exampleDatabase) {
        return !(exampleChange instanceof CreateProcedureChange)
                && !(exampleChange instanceof SQLFileChange)
                && !exampleChange.generateStatementsVolatile(exampleDatabase);
    }

    private static String addGeneratedHeader(String content) {
        content += "<!-- ====================================================== -->\n";
        content += "<!-- GENERATED BY ChangeDocGenerator DO NOT MODIFY MANUALLY -->\n";
        content += "<!-- ====================================================== -->\n\n";
        return content;
    }

    private static String getChangeDocFileName(String changename) {
        return changename.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    private static class FakeDataResourceAccessor extends AbstractResourceAccessor {
        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
//            if (path.endsWith("sql")) {
            return Collections.singleton(new ByteArrayInputStream("RANDOM SQL HERE".getBytes()));
//            }
//            throw new RuntimeException("Unknown path "+path);
        }

        @Override
        public Set<String> list(String s, String s1, boolean b, boolean b1, boolean b2) throws IOException {
            return Collections.emptySet();
        }

        @Override
        public ClassLoader toClassLoader() {
            return null;
        }
    }
}
