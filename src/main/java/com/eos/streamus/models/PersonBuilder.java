package com.eos.streamus.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

public class PersonBuilder implements Builder<Person> {
  /**
   * First Name.
   */
  private final String firstName;
  /**
   * Last Name.
   */
  private final String lastName;
  /**
   * Date of birth.
   */
  private final Date dateOfBirth;
  /**
   * Id.
   */
  private Integer id;
  /**
   * Created at.
   */
  private Timestamp createdAt;
  /**
   * Updated at.
   */
  private Timestamp updatedAt;
  /**
   * IsUser.
   */
  private boolean isUser;
  /**
   * Email.
   */
  private String email;
  /**
   * Username.
   */
  private String username;
  /**
   * Is Admin.
   */
  private boolean isAdmin;

  public PersonBuilder(final String firstName, final String lastName, final Date dateOfBirth) {
    Objects.requireNonNull(firstName);
    Objects.requireNonNull(lastName);
    Objects.requireNonNull(dateOfBirth);
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
  }

  /**
   * Set person id.
   *
   * @param id Id to set.
   * @return Builder.
   */
  public PersonBuilder withId(final Integer id) {
    this.id = id;
    return this;
  }

  /**
   * Set database creation and update timestamps.
   *
   * @param createdAt Created at.
   * @param updatedAt Updated at.
   * @return This builder.
   */
  public PersonBuilder withTimestamps(final Timestamp createdAt, final Timestamp updatedAt) {
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Set the builder to build an User.
   *
   * @param email    Email of user.
   * @param username Username of user.
   * @return This builder.
   */
  public PersonBuilder asUser(final String email, final String username) {
    Objects.requireNonNull(email);
    Objects.requireNonNull(username);
    this.isUser = true;
    this.email = email;
    this.username = username;
    return this;
  }

  /**
   * Set builder to create an Admin. Must already be a User.
   *
   * @return This builder.
   */
  public PersonBuilder asAdmin() {
    if (!this.isUser) {
      throw new IllegalStateException("Builder must be set to create a User before setting it as admin");
    }
    this.isAdmin = true;
    return this;
  }

  /**
   * Set builder to create an Admin.
   *
   * @param email    Email of Admin.
   * @param username Username of Admin.
   * @return This builder.
   */
  public PersonBuilder asAdmin(final String email, final String username) {
    return this.asUser(email, username).asAdmin();
  }

  /**
   * @return Whether the builder has timestamps set.
   */
  public boolean hasTimestamps() {
    return createdAt != null && updatedAt != null;
  }

  /** {@inheritDoc} */
  @Override
  public Person build() {
    if (this.isAdmin) {
      return new Admin(this);
    }
    if (this.isUser) {
      return new User(this);
    }
    return new Person(this);
  }

  /**
   * @return FirstName.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @return LastName.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @return DateOfBirth.
   */
  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * @return Id.
   */
  public Integer getId() {
    return id;
  }

  /**
   * @return CreatedAt.
   */
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  /**
   * @return UpdatedAt.
   */
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  /**
   * @return If Person to build is a User.
   */
  public boolean isUser() {
    return isUser;
  }

  /**
   * @return Email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return Username.
   */
  public String getUsername() {
    return username;
  }
}
