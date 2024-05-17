Feature: Address CRUD operations
  The CrudController and CrudService should be able to handle all the basic requests made to the test API

  Scenario: Caller wants to create a new Address
    Given the given User exists:
      | name     |
      | testUser |
    And the caller has the given Address:
      | address     |
      | testAddress |
    When the POST "/address" endpoint is called
    Then the service returns HTTP 201
    And the new Address was created

  Scenario: Caller wants to get an already existing Address by ID
    Given the given User exists:
      | name     |
      | testUser |
    Given the given Address exists:
      | address     |
      | testAddress |
    When the GET "/address/{addressID}" endpoint is called
    Then the service returns HTTP 200
    And the service returns the Address

  Scenario: Caller wants to update an already existing Address by ID
    Given the given User exists:
      | name     |
      | testUser |
    And the given Address exists:
      | address     |
      | testAddress |
    And the caller has the given Address:
      | address       |
      | otherTestName |
    When the PUT "/address/{addressID}" endpoint is called
    Then the service returns HTTP 204
    And the updated Address is found in the database

  Scenario: Caller wants to delete a Address by ID
    Given the given User exists:
      | name     |
      | testUser |
    Given the given Address exists:
      | address     |
      | testAddress |
    When the DELETE "/address/{addressID}" endpoint is called
    Then the service returns HTTP 204
    And the Address was removed from the database

  Scenario: Caller wants to get all Address by User ID
    Given the given User exists:
      | name     |
      | testUser |
    Given the given Address exists:
      | address     |
      | testAddress |
    When the GET "/address/findAllBy/userID" endpoint is called with request params:
      | userID | {userID} |
    And the service returns HTTP 200
    And the service returns a page with the matching Addresses
