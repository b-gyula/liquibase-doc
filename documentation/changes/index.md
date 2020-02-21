---
layout: default
title: Docs | Refactorings
---

# Bundled Liquibase Changes

Liquibase ships with a large number of refactoring/changes that can be applied to your database as well as the ability to write more through the
[extension system](../../extensions/index.html).

#### Note
- Property values are <i>string</i> except noted
- Boolean parameters are defaulted to <i>false</i> unless otherwise noted


| **Entity**| Create / Add | Drop | Change
|====|===|
| Table | [createTable](create_table.html) | [dropTable](drop_table.html) | [renameTable](rename_table.html) [setTableRemarks](set_table_remarks.html)
| Column | [addColumn](add_column.html) |  [dropColumn](drop_column.html) | [renameColumn](rename_column.html) [modityDataType](modify_data_type.html)  [setColumnRemarks](set_column_remarks.html) [addAutoIncrement](add_auto_increment.html)
| Index | [createIndex](create_index.html) | [dropIndex](drop_index.html) |
| View | [createView](create_view.html) |  [dropView](drop_view.html) | [renameView](rename_view.html)
| Procedure | [createProcedure](create_procedure.html) |  [dropProcedure](drop_procedure.html) | 
| Sequence | [createSequence](create_sequence.html) |  [dropSequence](drop_sequence.html) | [renameSequence](rename_sequence.html) [alterSequence](alter_sequence.html)

| **Constraint**| Add | Drop 
|====|===|
| Check | [addCheckConstraint](add_check_constraint.html) | [dropConstraint](drop_check_constraint.html) 
| Default value | [addDefaultValue](add_default_value.html) | [dropDefaultValue](drop_default_value.html)
| Foreign key | [addForeignKeyConstraint](add_foreign_key_constraint.html) | [dropForeignKeyConstraint](drop_foreign_key_constraint.html)
| Not null | [addNotNullConstraint](add_not_null_constraint.html) | [dropNotNullConstraint](drop_not_null_constraint.html) 
| Primary key | [addPrimaryKey](add_primary_key.html) | [dropPrimaryKey](drop_primary_key.html)
| Unique key | [addUniqueKeyConstraint](add_unique_constraint.html) | [dropUniqueKeyConstraint](drop_unique_constraint.html) 

| **Data**|  
|====|===|
| [insert](insert.html) | [update](update.html) | [delete](delete.html) | [loadData](load_data.html) | [loadUpdateData](load_update_data.html) | [mergeColumns](merge_columns.html) | [addLookupTable](add_lookup_table.html)

| **Misc**|  
|====|===|
| [sql](sql.html) | [sqlFile](sql_file.html) | [executeCommand](execute_command.html) | [tagDatabase](tag_database.html) | [stop](stop.html) | [output](output.html) | [customChange](custom_change.html)