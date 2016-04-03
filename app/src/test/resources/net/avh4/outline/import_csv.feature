Feature: Import from CSV


Scenario: Aaron imports his existing outline into the app

    Given Aaron has a CSV file for his existing outline
    When he imports the CSV file
    Then he sees the imported outline as a new node
