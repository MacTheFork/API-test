package application.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

/*
 * This class uses @Value annotation to load spring profile specific values from the application.yml file
 * The spring profile to be used can be set at runtime
 * If no spring profile is set then the default profile is used
 *
 */

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
@PropertySource(value = "classpath:${data}/test.properties", encoding = "UTF-8", ignoreResourceNotFound = false)  // put here it will populate Environment
public class Client {

    // combine the distinct parts of the URI
    @Value("${server.scheme}://${server.host}:${server.port}")
    String baseUri;

    // paths or endpoints
    @Value("${path.instructions}")
    String instructions;

    @Value("${path.users}")
    String users;

    @Value("${path.user}")
    String user;

    @Value("${path.city}")
    String city;

    // expected response files will be in the designated folder
    @Value("${data}/response/")
    String responsePath;

    public String getBaseUri() {
        return baseUri;
    }
}
