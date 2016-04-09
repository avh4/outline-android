package net.avh4.outline.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;
import net.avh4.outline.OutlineNode;
import net.avh4.outline.domain.FakeTime;
import net.avh4.outline.domain.FeatureTestPerson;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

@StepDefAnnotation
public class OutlineSteps {
    private final FeatureTestPerson aaron;
    private final FakeTime time;

    public OutlineSteps(FeatureTestPerson aaron, FakeTime time) {
        this.aaron = aaron;
        this.time = time;
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

    @Given("^Aaron completes an item$")
    public void aaron_completes_an_item() throws Throwable {
        aaron.app.addItem("Feed the cat");
        aaron.app.completeItem("Feed the cat");
    }

    @When("^one day passes$")
    public void one_day_passes() throws Throwable {
        time.advance(1, TimeUnit.DAYS);
    }

    @Then("^he no longer sees the completed item$")
    public void he_no_longer_sees_the_completed_item() throws Throwable {
        assertThat(aaron.app.seeItem("Feed the cat")).isNull();
    }
}
