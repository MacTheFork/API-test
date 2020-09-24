package application.commons;

import io.cucumber.java8.En;
import io.cucumber.java8.Scenario;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.invoke.MethodHandles;

@CucumberContextConfiguration
@SpringBootTest()
public class CommonStepDefs implements En {
    final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CommonStepDefs() {

        Before("", (Scenario scenario) -> {
            log.info("==== Begin Scenario: {} =====", scenario.getName());
//            System.out.println("==== Scenario: " + scenario.getName() + " =====");
        });

        After("", (Scenario scenario) -> {
            log.info("==== End Scenario: {} =====", scenario.getName());
//            System.out.println("==== End Scenario: " + scenario.getName() + " =====");
        });

    }

}
