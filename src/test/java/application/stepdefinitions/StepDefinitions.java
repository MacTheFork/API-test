package application.stepdefinitions;

import application.commons.Client;
import io.cucumber.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.lang.invoke.MethodHandles;

@SpringBootTest()
public class StepDefinitions implements En {
    final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    Client client;

    @Autowired
    Environment testData;

    public StepDefinitions() {

        Given("client requests instructions", () -> {
            log.info("######### base Uri {}", client.getBaseUri());
        });

    }
}
