package com.eos.streamus.models;

interface Entity {
    String tableName();
    String primaryKeyName();
    Integer getId();
}
