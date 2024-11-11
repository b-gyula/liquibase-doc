package org.liquibase.doc;

import freemarker.template.*;
import liquibase.change.*;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.serializer.LiquibaseSerializable.SerializationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ChangeDocGenerator {

    /**
     * [change name] -> [columnConfig] map
     */
    static final Map<String, String> specialColumnConfig = new HashMap<>();
    static final String ALL = "all";
    /**
     * List types should not be displayed
     */
    static final List<String> skipTypeForType = Arrays.asList("list", "databaseFunction", "sequenceNextValueFunction");
    /**
     * Display names for types
     */
    static final Map<String, String> typeDisplayName = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ChangeDocGenerator.class);

    static {
        specialColumnConfig.put("dropColumn", "DropColumnConfig");
        specialColumnConfig.put("createIndex", "IndexColumnConfig");
        specialColumnConfig.put("insert", "insertUpdateColumnConfig");
        specialColumnConfig.put("update", "insertUpdateColumnConfig");
    }

    static {
        typeDisplayName.put("bigInteger", "integer");
    }

    public static void main(String[] args) throws Exception {
        logger.info("Generating xsd for all changes in included jar");
        TreeMap<String, SortedSet<Class<? extends Change>>> sortedChanges = new TreeMap<>(ChangeFactory.getInstance().getRegistry());
        generateXSDwithFreeMarker(ChangeFactory.getInstance().getRegistry(), new MySQLDatabase());
    }

    /**
     * Generate XSD file using FreeMarker template
     *
     * @param definedChanges         Changes to generate XSD for
     * @param defaultExampleDatabase Default database to use for examples
     * @throws IOException       if an I/O error occurs
     * @throws TemplateException if an error occurs while processing the template
     */
    private static void generateXSDwithFreeMarker(Map<String, SortedSet<Class<? extends Change>>> definedChanges, MySQLDatabase defaultExampleDatabase) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(ChangeDocGenerator.class, "/templates");

        /// Set the template encoding and exception handling for the avoidance of xsd errors in documentation tags
        cfg.setOutputEncoding("UTF-8");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_31).build());
        cfg.setSharedVariable("esc", new freemarker.template.utility.XmlEscape());
        Template xsdTemplate = cfg.getTemplate("changeDocTemplate.ftl");

        Map<String, ChangeData> changeDataModel = new HashMap<>(); /// Map for the processed changes
        for (String changeName : definedChanges.keySet()) {
            Change change = ChangeFactory.getInstance().create(changeName);
            ChangeData changeData = new ChangeData();
            changeData.metaData = ChangeFactory.getInstance().getChangeMetaData(changeName);
            List<ChangeParamMetaData> params = setExamples(defaultExampleDatabase, change, changeData.metaData);
            params.forEach(param -> {
                boolean shouldBePrinted = !skipTypeForType.contains(param.getDataType()); /// Determines if the type should be printed
                String dataType = null;
                if (shouldBePrinted)
                    dataType = convertDataTypeToXsdFormat(param);
                ChangeData.ParamWithTypeFlag paramWithTypeFlag = new ChangeData.ParamWithTypeFlag(param, shouldBePrinted, dataType);
                if (param.isNested()) { /// If the parameter is a container, add it to the nested parameters
                    changeData.nestedParams.add(paramWithTypeFlag);
                } else {
                    changeData.params.add(paramWithTypeFlag);
                }
            });
            changeDataModel.put(changeName, changeData);
        }

        Map<String, Object> xsdDataModel = new HashMap<>(); /// Map for the processed changes and for the template
        xsdDataModel.put("changes", changeDataModel);

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("liquibase_changes.xsd"));
        xsdTemplate.process(xsdDataModel, bufferedWriter);
        bufferedWriter.close();
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
                            exampleValue = Collections.singletonList(columnConfig);
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
                        exampleValue = Collections.singletonList(columnConfig);
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
     * Convert the data type to the XSD format
     *
     * @param param Change parameter
     * @return XSD format of the data type
     */
    static String convertDataTypeToXsdFormat(ChangeParamMetaData param) {
        switch (param.getDataType()) {
            case "bigInteger":
                return "integerExp";
            case "boolean":
                return "booleanExp";
            case "string":
                if (param.requiredForAll())
                    return "nonEmptyString";
                else
                    return "xsd:string";
            default:
                return "xsd:" + param.getDataType();
        }
    }

    /**
     * Wrapper class to store the default values along with the parameters
     */
    public static class ChangeParamMetaData extends ChangeParameterMetaData {
        final Change change;
        String defaultValue = "";
        Class containedType = null;

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
                    if (!isBool || (null != o && (boolean) o)) {
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

        boolean isNested() {
            return getSerializationType() == SerializationType.NESTED_OBJECT;
        }

        boolean isDirectValue() {
            return getSerializationType() == SerializationType.DIRECT_VALUE;
        }
    }

    /**
     * Wrapper class to store the change metadata, parameters, and nested parameters for the FreeMarker template
     */
    public static class ChangeData {

        /**
         * Nested parameters of a change (children)
         */
        final List<ParamWithTypeFlag> nestedParams;
        /**
         * Parameters of a change, wrapped in ChangeParamMetaData
         */
        final List<ParamWithTypeFlag> params;
        /**
         * Metadata of a change
         */
        ChangeMetaData metaData;

        public ChangeData() {
            this.nestedParams = new ArrayList<>();
            this.metaData = null;
            this.params = new ArrayList<>();
        }

        public ChangeMetaData getMetaData() {
            return metaData;
        }

        public List<ParamWithTypeFlag> getParams() {
            return params;
        }

        public List<ParamWithTypeFlag> getNestedParams() {
            return nestedParams;
        }

        /**
         * Wrapper class to store the parameter data along with the type flag, for the purpose of FreeMarker template type tag writing
         */
        public static class ParamWithTypeFlag {

            final ChangeParamMetaData paramData;
            final boolean shouldTypeBePrintedFlag; /// Flag to determine if the type should be printed in the XSD
            final String dataType;

            public ParamWithTypeFlag(ChangeParamMetaData paramData, boolean shouldTypeBePrintedFlag, String dataType) {
                this.paramData = paramData;
                this.shouldTypeBePrintedFlag = shouldTypeBePrintedFlag;
                this.dataType = dataType;
            }

            public boolean getShouldTypeBePrintedFlag() {
                return shouldTypeBePrintedFlag;
            }

            public ChangeParamMetaData getParamData() {
                return paramData;
            }

            /**
             * Check if the parameter is required for all databases, in the format of xsd minOccours
             *
             * @return 1 if required for all, 0 otherwise
             */
            public int isRequiredForAll() {
                if (paramData.requiredForAll()) return 1;
                else return 0;
            }

            public String getDataType() {
                return dataType;
            }
        }
    }

}
