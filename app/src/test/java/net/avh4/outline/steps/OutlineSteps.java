package net.avh4.outline.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;
import net.avh4.outline.OutlineNode;
import net.avh4.outline.domain.FeatureTestPerson;

import static org.assertj.core.api.Java6Assertions.assertThat;

@StepDefAnnotation
public class OutlineSteps {
    private final FeatureTestPerson aaron;

    public OutlineSteps(FeatureTestPerson aaron) {
        this.aaron = aaron;
    }

    @Given("^Aaron has an item to do$")
    public void aaron_has_an_item_to_do() throws Throwable {
        aaron.app.addItem("Walk the dog");
    }

    @When("^he completes the item$")
    public void he_completes_the_item() throws Throwable {
        aaron.app.completeItem("Walk the dog");
    }

    @Then("^he sees the item striked out$")
    public void he_sees_the_item_striked_out() throws Throwable {
        OutlineNode outlineNode = aaron.app.assertSeesItem("Walk the dog");
        assertThat(outlineNode.isCompleted()).isTrue();
    }

    @Given("^Aaron has an outline$")
    public void aaron_has_an_outline() throws Throwable {
        assertThat(aaron.app.inspectOutline()).isNotNull();
    }

    @When("^Aaron adds some items$")
    public void aaron_adds_some_items() throws Throwable {
        aaron.app.addItem("Chores");
        aaron.app.enter("Chores");
        aaron.app.addItem("Walk the dog");
        aaron.app.addItem("Buy milk");
        aaron.app.goUp();
    }

    @Then("^he sees those items in the outline$")
    public void he_sees_those_items_in_the_outline() throws Throwable {
        aaron.app.assertSeesItem("Chores");
        aaron.app.enter("Chores");
        aaron.app.assertSeesItem("Walk the dog");
        aaron.app.assertSeesItem("Buy milk");
    }
}
