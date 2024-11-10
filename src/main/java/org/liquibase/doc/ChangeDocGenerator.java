package org.liquibase.doc;

import freemarker.template.*;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import liquibase.change.*;
import liquibase.change.core.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
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
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

public class ChangeDocGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocGenerator.class);

    public static void main(String[] args) throws Exception {
        logger.info("Generating xsd for all changes in included jar");
        Map<String, SortedSet<Class<? extends Change>>> definedChanges = ChangeFactory.getInstance().getRegistry();
        TreeMap<String, SortedSet<Class<? extends Change>>> sortedChanges = new TreeMap<>(definedChanges);
        generateXSDwithFreeMarker(sortedChanges, new MySQLDatabase());
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

        public Class getContainedType() {
            return containedType;
        }

        public boolean requiredForAll() {
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

    /**
     * Wrapper class to store the change metadata, parameters, and nested parameters for the FreeMarker template
     */
    public static class ChangeData {

        public ChangeData() {
            this.nestedParams = new ArrayList<>();
            this.metaData = null;
            this.params = new ArrayList<>();
        }

        public ChangeMetaData getMetaData() {
            return metaData;
        }

        public List<ChangeParamMetaData> getParams() {
            return params;
        }

        public List<ChangeParamMetaData> getNestedParams() {
            return nestedParams;
        }

        /**
         * Nested parameters of a change (children)
         */
        List<ChangeParamMetaData> nestedParams;

        /**
         * Metadata of a change
         */
        ChangeMetaData metaData;

        /**
         * Parameters of a change, wrapped in ChangeParamMetaData
         */
        List<ChangeParamMetaData> params;
    }

    /**
     * Generate XSD file using FreeMarker template
     * @param definedChanges Changes to generate XSD for
     * @param defaultExampleDatabase Default database to use for examples
     * @throws IOException if an I/O error occurs
     * @throws TemplateException if an error occurs while processing the template
     */
    private static void generateXSDwithFreeMarker(Map<String, SortedSet<Class<? extends Change>>> definedChanges, MySQLDatabase defaultExampleDatabase) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(ChangeDocGenerator.class, "/templates");
        Template xsdTemplate = cfg.getTemplate("changeDocTemplate.ftl");

        Map<String, ChangeData> changeDataModel = new HashMap<>(); /// Map for the processed changes
        for (String changeName : definedChanges.keySet()) {
            Change exampleChange = ChangeFactory.getInstance().create(changeName); /// Create an instance of the change
            ChangeData changeData = new ChangeData();
            changeData.metaData = ChangeFactory.getInstance().getChangeMetaData(exampleChange); /// Get the metadata of the change
            changeData.params =  setExamples(defaultExampleDatabase, exampleChange, changeData.metaData); /// Set the examples for the change
            for (ChangeParamMetaData param : changeData.params) {
                if (param.isContainer()) { /// If the parameter is a container, add it to the nested parameters
                    changeData.nestedParams.add(param);
                }
            }
            if (changeData.metaData != null) { /// If the metadata is present, add it to the changeDataModel
                changeDataModel.put(changeData.metaData.getName(), changeData);
            }
        }

        Map<String, Object> xsdDataModel = new HashMap<>(); /// Map for the processed changes and for the template
        xsdDataModel.put("changes", changeDataModel);

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("liquibase_changes.xsd"));
        xsdTemplate.process(xsdDataModel, bufferedWriter);
        bufferedWriter.close();
    }

    final static String ALL = "all";

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

}
