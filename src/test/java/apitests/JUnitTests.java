package apitests;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class JUnitTests {

	@BeforeAll
	public static void setup() {
		RestAssured.baseURI = "http://bpdts-test-app-v2.herokuapp.com";
	}

	@Test
	public void readInstructions() {
		RestAssured.basePath = "/instructions";
		String instructions = "Create a short automated test for this API. Check that the data returned by the API is valid, and that ensure that each valid operation can be successfully called for each endpoint. Once you've built the tests, push the answer to Github or Gitlab, and send us a link. ";
		
		String responseBody = 
				given().when().get()
				.then()
				.statusCode(HttpStatus.OK.value())
				.contentType(ContentType.JSON)
				.extract().body().asString();

		assertThatJson(responseBody).node("todo").isEqualTo(instructions);
		
	}

	@Test
	public void getAllUsers() {
		RestAssured.basePath = "/users";
		
		String responseBody = given().when().get()
				.then().statusCode(HttpStatus.OK.value())
				.contentType(ContentType.JSON)
				.extract().body().asString();

		assertThatJson(responseBody).isArray();
		assertThatJson(responseBody).isArray().hasSize(1000);
		assertThatJson(responseBody).node("[0].id").isEqualTo(1);
		assertThatJson(responseBody).node("[0].first_name").isEqualTo("Maurise");
		assertThatJson(responseBody).node("[0].last_name").isEqualTo("Shieldon");
		assertThatJson(responseBody).node("[0].email").isEqualTo("mshieldon0@squidoo.com");
		assertThatJson(responseBody).node("[0].ip_address").isEqualTo("192.57.232.111");
		assertThatJson(responseBody).node("[0].latitude").isEqualTo(34.003135);
		assertThatJson(responseBody).node("[0].longitude").isEqualTo(-117.7228641);
		assertThatJson(responseBody).node("[0].city").isAbsent();
	}

	@Test
	public void getUserById() {
		RestAssured.basePath = "/user/{userid}";
		
		int userId = 666;
		String responseBody = given().pathParam("userid", userId).when().get()
				.then()
				.statusCode(HttpStatus.OK.value())
				.contentType(ContentType.JSON)
				.extract().body().asString();
		
		assertThatJson(responseBody).isEqualTo(
				"{\"id\": 666,\r\n" + 
				"\"first_name\": \"Aviva\",\r\n" + 
				"\"last_name\": \"Colegrove\",\r\n" + 
				"\"email\": \"acolegroveih@sogou.com\",\r\n" + 
				"\"ip_address\": \"105.56.41.74\",\r\n" + 
				"\"latitude\": 41.6,\r\n" + 
				"\"longitude\": -93.61,\r\n" +
				"\"city\": \"Des Moines\"}");
	}

	
	@Test
	public void getUsersByCity() {
		RestAssured.basePath = "/city/{city}/users";
		
		String city = "London";
		String responseBody = given().pathParam("city", city).when().get().then()
				.statusCode(HttpStatus.OK.value())
				.contentType(ContentType.JSON)
				.extract().body().asString();
		
		assertThatJson(responseBody).isArray();
		assertThatJson(responseBody).isArray().hasSize(6);
		
		assertThatJson(responseBody).node("[0]").isEqualTo(
				"{\"id\": 135,\r\n" + 
				"\"first_name\": \"Mechelle\",\r\n" + 
				"\"last_name\": \"Boam\",\r\n" + 
				"\"email\": \"mboam3q@thetimes.co.uk\",\r\n" + 
				"\"ip_address\": \"113.71.242.187\",\r\n" + 
				"\"latitude\": -6.5115909,\r\n" + 
				"\"longitude\": 105.652983}");
	}

	@Test
	public void getUserByIdNotFound() {
		RestAssured.basePath = "/user/{userid}";
		
		int userId = 9999;

		given().pathParam("userid", userId).when().get().then().statusCode(HttpStatus.NOT_FOUND.value());
	}
	
}
