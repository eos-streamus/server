package com.eos.streamus.models;

interface Entity {

  String tableName();


  String primaryKeyName();


  String creationFunctionName();

  Integer getId();

  String fieldNamesAndValuesString();
}
