package com.eos.streamus.models;

interface Entity {
  String getTableName();

  String getPrimaryKeyName();

  String getCreationFunctionName();

  Integer getId();
}
