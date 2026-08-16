Feature: UserRepository operations

  Scenario: Save and find a User by keycloakId
    Given a user with keycloakId "kc-cuc-1", name "Cucumber User", email "cuc@example.com", firstName "Cuc", lastName "Umber"
    When the user is saved to the user repository
    Then the user can be found in the user repository by keycloakId "kc-cuc-1"
    And the user's name should be "Cucumber User"

  Scenario: Count users in repository
    Given a user with keycloakId "kc-cuc-2", name "Second User", email "second@example.com", firstName "Second", lastName "User"
    When the user is saved to the user repository
    Then the user repository count should be at least 1
