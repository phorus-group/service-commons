Feature: User CRUD operations
  The CrudController and CrudService should be able to handle all the basic requests made to the test API

  Scenario: Caller wants to create a new User
    Given the caller has the given User:
      | name     |
      | testUser |
    When the POST "/user" endpoint is called
    Then the service returns HTTP 201
    And the new User was created

  Scenario: Caller wants to create a new User, but has a non-blank field as null
    Given the caller has the given User:
      | name     |
      |          |
    When the POST "/user" endpoint is called
    Then the service returns HTTP 400
    And the service returns a message with the validation errors
      | obj     | field | rejectedValue | message         |
      | userDTO | name  | null          | Cannot be blank |

  Scenario: Caller wants to get an already existing User by ID
    Given the given User exists:
      | name     |
      | testUser |
    When the GET "/user/{userId}" endpoint is called
    Then the service returns HTTP 200
    And the service returns the User

  Scenario: Caller wants to update an already existing User by ID
    Given the given User exists:
      | name     |
      | testUser |
    And the caller has the given User:
      | name          |
      | otherTestName |
    When the PUT "/user/{userId}" endpoint is called
    Then the service returns HTTP 204
    And the updated User is found in the database

  Scenario: Caller wants to delete a User by ID
    Given the given User exists:
      | name     |
      | testUser |
    When the DELETE "/user/{userId}" endpoint is called
    Then the service returns HTTP 204
    And the User was removed from the database
