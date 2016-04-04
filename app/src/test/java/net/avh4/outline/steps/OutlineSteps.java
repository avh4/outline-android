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
}
