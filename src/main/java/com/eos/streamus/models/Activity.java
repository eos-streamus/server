package com.eos.streamus.models;

public abstract class Activity implements Entity {
  //#region Static Attributes
  private static final String TABLE_NAME = "Activity";
  private static final String PRIMARY_KEY_NAME = "id";
  //#endregion Static Attributes

  //#region Instance Attributes
  private Integer id;
  //#endregion Instance Attributes

  //#region Constructors
  protected Activity() {
  }

  protected Activity(Integer id) {
    this.id = id;
  }
  //#endregion Constructors

  //#region Getters and Setters

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public Integer getId() {
    return id;
  }
  //#endregion Getters and Setters

  //#region String representations
  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return String.format("id: %d", id);
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    Activity activity = (Activity) obj;
    return activity.id.equals(id);
  }
  //#endregion Equals
}
