Feature: Complete an item


  Scenario: Aaron completes an item

    Given Aaron has an item to do
    When he completes the item
    Then he sees the item striked out
