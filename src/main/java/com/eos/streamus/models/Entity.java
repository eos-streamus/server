package com.eos.streamus.models;

interface Entity {

  /** @return Table name in database. */
  String tableName();

  /** @return primary key column name. */
  String primaryKeyName();

  /** @return creation function name in database. */
  String creationFunctionName();

  /** @return Id of entity in database. */
  Integer getId();

  /** @return default string representation of Entity. */
  default String defaultToString() {
    return String.format(
        "%s[%s=%d]",
        getClass().getName(),
        primaryKeyName(),
        getId()
    );
  }

}
