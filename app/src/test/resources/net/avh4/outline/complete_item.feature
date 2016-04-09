Feature: Complete an item


  Scenario: Aaron completes an item

    Given Aaron has an item to do
    When he completes the item
    Then he sees the item striked out

  Scenario: Completed items are hidden

    Given Aaron completes an item
    When one day passes
    Then he no longer sees the completed item