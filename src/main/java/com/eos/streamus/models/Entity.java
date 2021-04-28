package com.eos.streamus.models;

interface Entity {

  /** @return Table name in database. */
  String tableName();

  /** @return Primary key column name in database. */
  String primaryKeyName();

  /** @return Creation function name in database. */
  String creationFunctionName();

  /** @return Id of this Entity. */
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
