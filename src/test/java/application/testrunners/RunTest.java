package application.testrunners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features"
        , extraGlue = {"application.stepdefinitions"}
        , plugin = {"html:target/cucumber"}  // 'pretty' doesn't display prettily with lambdas
        //, monochrome = true
        , tags = "@functional"
//        , tags = {"@thisone"}
)

public class RunTest {
}
