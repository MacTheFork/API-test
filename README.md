# Technical Test of API

The Swagger documentation doesn't provide a schema or actual examples or much in the way of description of expected behaviour. This means the tests can only be constructed against actual observed behaviour from the Swagger 'Try it out' feature.  This just tests that 'it does what it does'.<br>

Data stability is an unknown - it appears to be stable but if this assumption is incorrect then the tests will need to be adapted to cope.<br>

Only functional tests have been automated as implied by the hint about which tools to use.<br>
The test methods have been written in Java as a Maven project and placed in a single class and annotated as @Test for execution by JUnit or Maven.<br>

### Libraries used
**Java** has been used for the test methods, which are data driven from files in src/test/resources. Run with a minimum version of Java 8. 
ApiTests class has the annotated tests while Config class sets up the environment specifics.<br>
**Maven** has been used for dependency management and more, including test execution with the SureFire plugin.
**Rest-Assured** has been used as an HTTP client.<br>
**Json-Unit** has been used to validate the responses.<br>
**Spring Boot Framework** has been used to enable a flexible configuration from a single application configuration file. This allows for the test to be easily set for different environments with different sets of data to be used.<br> 
**slf4j and logback** have been used to provide logging while running the tests.<br>

Further refinement could be provided by using Cucumber-JVM and Behaviour Driven Design to allow for straightforward parameterisation of the tests using Scenario Templates (Outlines)
and re-use of the step definitions for setup requests and validation.<br>

#### Usage

Download or clone the test project.<br>

The tests can be run in Eclipse or IntelliJ IDE as JUnit or Maven tests using a run configuration with goal or command "clean test" or from a command line or terminal using the command line: <br>
`    mvn clean test`

To run with defaults, no valid spring profile need be set, in which case a default profile is used.<br>
However, it is possible to pick a specific spring profile by e.g. setting a system property for the JVM<br>
`    -Dspring.profiles.active=local`

or by selecting a specific Maven profile (from those defined in the POM) either in the IDE run configuration or on the mvn command line<br>
`    -P local`

A platform such as Jenkins will have a variety of mechanisms for running the tests in this class <br>

#### Output
Maven surefire plugin will write basic output by default to target/surefire-reports.<br>


### Comments on the API design and Swagger documentation
The design and documentation affect the testing as they should provide a statement of how the API is supposed to behave.<br>
Referring to the following standard https://www.gov.uk/guidance/gds-api-technical-and-data-standards there are some observations below<br>

#####Use HTTPS<br>
  * API is not HTTPS<br>

#####Use of arrays is not recommended<br>
  * API returns array for /users<br>
  * API returns array for /city/{city}/users<br>
	
	I think this last one should actually be /users but filtered by city i.e. /users?city={city}<br>

#####How to respond to data requests<br>
  * The answer should not return any more detail than is required<br>
  * 404 response returns more than necessary?<br>


#####Document your API<br>
  * Introduce your API: contextual/overview information - what the API does, who it might be used by and under what circumstances<br>
  * nothing provided<br>
#####Provide a short description of each of your API resources <br>
  * no descriptions provided - what does latitude and longitude refer to?  clearly not City (London returns a wide range of values - perhaps Country would help) - is it related to ip address?<br>
#####Should use OpenAPI3<br>
  * it uses Swagger 2.0<br>
#####provide sample code to illustrate how to call the API and to let users know what responses they can expect<br>
  * no examples given - relies on the Try It Out service<br>
	
https://www.gov.uk/guidance/writing-api-reference-documentation<br>
#####The API reference describes everything the API does including:<br>
  * example requests and responses<br>
#####no example data provided<br>
  * 404 error for /user/{id} includes a response body with an undocumented "message" object<br>


#####No schemas published<br>
#####no header parameters specified although Try it out adds an Accept application/json<br>
