package apitests;

import org.springframework.beans.factory.annotation.Value;

/*
 * This class uses @Value annotation to load spring profile specific values from the application.yml file
 * The spring profile to be used can be set at runtime
 * If no spring profile is set then the default profile is used
 *
 */
public class Config {

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

}
