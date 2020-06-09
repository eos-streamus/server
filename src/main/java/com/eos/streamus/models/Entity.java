package com.eos.streamus.models;

interface Entity {

  String tableName();


  String primaryKeyName();

  String creationFunctionName();

  Integer getId();

  default String defaultToString() {
    return String.format(
        "%s[%s=%d]",
        getClass().getName(),
        primaryKeyName(),
        getId()
    );
  }
}
