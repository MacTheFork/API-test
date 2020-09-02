package apitests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/*
This class is annotated as a Spring Boot Test to enable configuration from an application.yml file
The active spring profile can be selected at runtime allowing for configuration depending on the target environment

The app config then allows for a profile specific properties file to be read into an Environment instance
The properties file has values for the variables to be used in the tests,
mapping from a meaningful key to the actual value to use for this particular environment
 */
@Configuration
@PropertySource(value = "classpath:${data}/request/test.properties", encoding = "UTF-8")
@SpringBootTest(classes = {ApiTests.class, Config.class})
public class ApiTests {
    // log levels can be set in the application.yml
    final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ResponseSpecification responseSpecOK;
    private RequestSpecification requestSpec;

    // these are injected from Config class and provide the target environment details - URI, paths etc.
    @Autowired
    Config config;

    // specific test data will be loaded from the properties file @PropertySource into the Environment variable
    // this can be done independently of the target environment setup in case common datasets are in use (maybe local is a copy of dev)
    @Autowired
    Environment testData;

    static boolean hasRun = false;

    @BeforeEach
    public void setup() {

        // Would rather this setup was run once but JUnit insists that it is static ig
        // In this case just log URI on first invocation
        // TestNG might be a better choice if it allows non-static @BeforeAll annotations
        if (!hasRun) {
            hasRun = true;
            log.info("baseUri is {}", config.baseUri);

        }

        // enable Rest Assured logging for all requests and responses when root log level is set to DEBUG
        if (log.isDebugEnabled()) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }

        // request characteristics common to all tests in this class
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(config.baseUri)
                .setAccept("application/json")
                .build();

        // response validation common to most tests in this class
        responseSpecOK = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.OK.value())
                .expectContentType("application/json")
                .build();

    }

    /*
    Test that a valid userId returns OK with all expected data
    It appears that all valid user Ids are actually integers although the Swagger spec says String
     */
    @Test
    public void user_validUserId_OK() throws IOException {
        String path = config.user;
        // userId chosen here so that response body has city with 1, 2 and 3-byte UTF-8 encoded Unicode characters
        String userId = testData.getProperty("validUserId");

        // basic logging - RestAssured detail can be provided by setting root log level to DEBUG
        log.info("user_validUserId_OK: {} {}", path, userId);

        // use RestAssured as the http client
        // use response spec for common validation
        // extract the response body as a string for later use
        String responseBody = given().spec(requestSpec).pathParam("userId", userId)
                .when().get(path)
                .then().spec(responseSpecOK)
                .extract().body().asString();

        // do a full body comparison against file stored on classpath
        // use JsonUnit to assert as a JSON object
        // this allows for some fine grained control over the validation
        // and sensible reporting of differences on failure
        String expected = getResourceFileAsString(config.responsePath + "user_" + userId + ".json");
        assertThatJson(responseBody).isEqualTo(expected);
    }

    /*
    Test that a request for all Users responds OK
    Size must be at least what is expected for this dataset - depends on environment and data volatility
    The first node is checked for validity - all users could be checked but this is brittle, depending on the stability of the data
     */
    @Test
    public void users_getAll_OK() throws IOException {
        String path = config.users;
        int expectedSize = Integer.parseInt(testData.getProperty("minExpectedAllUsers"));
        log.info("users_getAll_OK: {}", path);

        String responseBody = given().spec(requestSpec)
                .when().get(path)
                .then().spec(responseSpecOK)
                .extract().body().asString();

        assertThatJson(responseBody).isArray().hasSizeGreaterThanOrEqualTo(expectedSize);

        String expected = getResourceFileAsString(config.responsePath + "sampleUser.json");
        assertThatJson(responseBody).node("[0]").isEqualTo(expected);
    }

    /*
    Test that a city path parameter with no URL encoding responds OK with all expected data
     */
    @Test
    public void city_getUsersByCity_OK() throws IOException {
        String path = config.city;
        String city = testData.getProperty("city");
        int expectedSize = Integer.parseInt(testData.getProperty("minExpectedCity"));
        log.info("city_getUsersByCity_OK: {} {}", path, city);

        String responseBody = cityRequest(city, path);
        assertThatJson(responseBody).isArray().hasSizeGreaterThanOrEqualTo(expectedSize);
        cityResponseAssert(city, responseBody);

    }

    /*
    Test that a city path parameter with URL encoding responds OK with all expected data
    */
    @Test
    public void city_getUsersByCityMultiByteUTF8_OK() throws IOException {
        String path = config.city;
        String city = testData.getProperty("cityUTF8");  //includes 1, 2 and 3-byte UTF-8 encoded Unicode characters
        log.info("city_getUsersByCityMultiByteUTF8_OK: {} {}", path, city);

        String responseBody = cityRequest(city, path);
        assertThatJson(responseBody).isArray();
        cityResponseAssert(city, responseBody);
    }

    /*
    Test that a non-existent, but valid, userId responds with Not Found
    Follow the design here rather than the behaviour and don't check for the unexpected body
    However don't fail if a body is returned - this needs further clarification
     */
    @Test
    public void user_userIdNotFound_NotFound() {
        String path = config.user;
        String userId = testData.getProperty("userIdNotFound");
        log.info("user_userIdNotFound_NotFound: {} {}", path, userId);

        String responseBody = given().spec(requestSpec).pathParam("userId", userId)
                .when().get(path)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString();
    }

    /*
    Test that an invalid (long) userId responds with Not Found
    Follow the design here rather than the behaviour and don't check for a specific body
    (which omits the You have requested... text from the message)
    However don't fail in case a body is returned - this needs further clarification
     */
    @Test
    public void user_longUserId_NotFound() {
        String path = config.user;
        String userId = testData.getProperty("longUserId");
        log.info("user_longUserId_NotFound: {} {}", path, userId);

        String responseBody = given().spec(requestSpec).pathParam("userId", userId)
                .when().get(path)
                .then().statusCode(HttpStatus.NOT_FOUND.value())
                .extract().body().asString();
    }

    /*
    Test that a valid string but not present in the data set responds OK but returns no data
     */
    @Test
    public void city_cityNoUsers_NotFound() {
        String path = config.city;
        String city = testData.getProperty("cityNoUsers");
        log.info("city_cityNoUsers_NotFound: {} {}", path, city);

        String responseBody = given().spec(requestSpec).pathParam("city", city)
                .when().get(path)
                .then().spec(responseSpecOK)
                .extract().body().asString();

        // check for empty array
        assertThatJson(responseBody).isArray().isEmpty();
    }

    /*
    Test that a request for instructions responds OK
     */
    @Test
    public void instructions_get_OK() throws IOException {
        String path = config.instructions;
        log.info("instructions_get_OK: {}", path);

        String responseBody = given().spec(requestSpec)
                .when().get(path)
                .then().spec(responseSpecOK)
                .extract().body().asString();

        String expected = getResourceFileAsString(config.responsePath + "instructions.json");
        assertThatJson(responseBody).node("todo").isEqualTo(expected);

    }

    /*
    Helper method to read an expected file from the classpath
     */
    static String getResourceFileAsString(String fileName) throws IOException {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                return null;
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        }
    }

    /*
    This method is to reduce duplication of code
    Makes the request to the city, checks the response then returns the body
     */
    private String cityRequest(String city, String path) {
        return given().spec(requestSpec).pathParam("city", city)
                .when().get(path)
                .then().spec(responseSpecOK)
                .extract().body().asString();
    }
    /*
    This method is to reduce duplication of code
    asserts the response for city matches the JSON file named for the city
     */
    private void cityResponseAssert(String city, String responseBody) throws IOException {
        String expected = getResourceFileAsString(config.responsePath + "city_" + city + ".json");
        assertThatJson(responseBody).isEqualTo(expected);
    }

}
