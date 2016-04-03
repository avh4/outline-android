package net.avh4.outline;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;
import net.avh4.outline.domain.FeatureTestPerson;

@StepDefAnnotation
public class OutlineSteps {

    private final FeatureTestPerson aaron = new FeatureTestPerson();
    private String he;
    private String aaronsCsvFile;

    @Given("^Aaron has a CSV file for his existing outline$")
    public void aaron_has_a_CSV_file_for_his_existing_outline() throws Throwable {
        he = "Aaron";
        aaronsCsvFile = "" +
                "\"By time\",\"0\",\"1\",\"0\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Today\",\"0\",\"1\",\"1\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Make meals for two days\",\"0\",\"1\",\"2\",\"100\",\"2\",\"0\",\"\",\"\",\"\",\"2016-03-21\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Check in to flight\",\"0\",\"1\",\"2\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Pack, include sunglasses, swimsuit\",\"0\",\"1\",\"2\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"This week\",\"0\",\"1\",\"1\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Clean up car\",\"0\",\"1\",\"2\",\"0\",\"2\",\"0\",\"\",\"\",\"\",\"\",\"Unfiled\",\"\",\"\",\"\"\n" +
                "\"Clean up car (DONE)\",\"0\",\"1\",\"2\",\"100\",\"2\",\"0\",\"\",\"\",\"\",\"2016-01-23\",\"Unfiled\",\"\",\"\",\"\"\n"
        ;
    }

    @When("^he imports the CSV file$")
    public void he_imports_the_CSV_file() throws Throwable {
        assert he.equals("Aaron");
        String csvFilename = "my-import.csv";
        aaron.putFileOnDevice(csvFilename, aaronsCsvFile);
        aaron.importFile(csvFilename);
    }

    @Then("^he sees the imported outline as a new node$")
    public void he_sees_the_imported_outline_as_a_new_node() throws Throwable {
        aaron.assertHasItem("Import from CSV");
        aaron.assertHasItem("Import from CSV", "By time");
        aaron.assertHasItem("Import from CSV", "By time", "Today");
        aaron.assertHasItem("Import from CSV", "By time", "Today", "Check in to flight");
        aaron.assertHasItem("Import from CSV", "By time", "Today", "Pack, include sunglasses, swimsuit");
        aaron.assertHasItem("Import from CSV", "By time", "This week");
        aaron.assertHasItem("Import from CSV", "By time", "This week", "Clean up car");
        aaron.assertDoesntHaveItem("Import from CSV", "By time", "This week", "Clean up car (DONE)");
        aaron.assertSeesItem("Import from CSV");
    }
}
