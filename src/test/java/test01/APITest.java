package test01;

import org.testng.Assert;
import org.testng.annotations.Test;

import Utilities.*;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class APITest {
	
	
	@Test
	public void getApiKey() throws Exception  {
		
System.out.println("====GET api_key====");

		RestAssured.baseURI = Constants.BASE_URL;
		
		Response response = RestAssured.given()
		.when()
		.get(Constants.GetApiKeyEndPoint)
		.prettyPeek();
		
		String api_key = response.body().jsonPath().getString("api_key");
		System.out.println(api_key);
		//Assert get api_key status code
		int expectedStatusCode = response.getStatusCode();
		int actualStatusCode = 200;
		
		Assert.assertEquals(expectedStatusCode, actualStatusCode);
        System.out.println("Status code of get api_key request is -> " + response.getStatusCode());
		System.out.println();

System.out.println("====GET patient====");

		RestAssured.baseURI = Constants.BASE_URL;
		
		Response patient = RestAssured.given()
		.when()
		.get(Constants.GetPatientEndPoint)
		.prettyPeek();
		
		String patientBody = patient.getBody().asString();
        System.out.println("Patientbody ====> " + patientBody);
		
		//Assert getpatient status code
		int expectedPatientStatusCode = 200;
		int actualPatientStatusCode = patient.getStatusCode();
		Assert.assertEquals(expectedPatientStatusCode, actualPatientStatusCode);
		
		int size = patient.body().jsonPath().getList("").size();
		System.out.println("Total number of patients is " + size);
		System.out.println();

System.out.println("====Verify there are 1 or more appointments in June 2022====");
		
		int JuneApp = 0;
 
		for(int i = 0; i < size; i++) {
			if(patient.body().jsonPath().getString("["+i+"].appointment_date").contains("2022-06")) {
			System.out.println(patient.body().jsonPath().getString("["+i+"]"));
			System.out.println(i);
			JuneApp++;
			}
		}
        System.out.println("There are " + JuneApp + " patients with June 2022 appointments");
        System.out.println();
          		
System.out.println("====Fail if Steve Rogers not in response body====");

		for(int i = 0; i < size; i++) {
			if(patient.body().jsonPath().getString("["+i+"].id").contains("SR19760827202206208364")) {
				System.out.println(patient.body().jsonPath().getString("["+i+"]"));
				System.out.println(i);
				Assert.assertEquals(patient.body().jsonPath().getString("["+i+"]") , GlobalVariables.SteveRogers);
			}
		}
		System.out.println();
		
System.out.println("====Verify ID format for all patients====");

		//just for reference
		String id = "{\r\n"
				+ "      \"id\": \"SR19760827202206208364\",\r\n"
				+ "      \"birthdate\": \"1976-08-27\",\r\n"
				+ "      \"phone\": \"347-555-9876\",\r\n"
				+ "      \"appointment_date\": \"2022-06-20\",\r\n"
				+ "      \"name\": {\r\n"
				+ "         \"lastName\": \"Rogers\",\r\n"
				+ "         \"firstName\": \"Steve\"\r\n"
				+ "      },\r\n"
				+ "      \"address\": {\r\n"
				+ "         \"street\": \"45 W 45th St\",\r\n"
				+ "         \"city\": \"New York\",\r\n"
				+ "         \"state\": \"NY\",\r\n"
				+ "         \"zip\": \"10036\"\r\n"
				+ "      }\r\n"
				+ "   }";
		
		for(int i = 0; i < size; i++) {
			
			char firstInitial = patient.body().jsonPath().getString("["+i+"].name.firstName").charAt(0);
		    //System.out.println(firstInitial);
			char lastInitial = patient.body().jsonPath().getString("["+i+"].name.lastName").charAt(0);
			//System.out.println(lastInitial);
			String birthdate = patient.body().jsonPath().getString("["+i+"].birthdate").replaceAll("[^0-9]", "");
			//System.out.println(birthdate);
			String appointYear = patient.body().jsonPath().getString("["+i+"].appointment_date").replaceAll("[^0-9]", "");
			//System.out.println(appointYear);
			
			String idPart = ""+firstInitial+"" + ""+lastInitial+"" + ""+birthdate+"" + ""+appointYear+"";
			System.out.println(idPart);
			
			boolean IDformat = patient.body().jsonPath().getString("["+i+"].id").contains(idPart);
			
			System.out.println("Is the ID format correct? " + IDformat);
			System.out.println();
		}
		
System.out.println("====PATCH patient data====");		

		String payload = " {\r\n"
				+ "        \"id\": \"SR19760827202206208364\",\r\n"
				+ "        \"name\": {\r\n"
				+ "            \"lastName\": \"Awesome\",\r\n"
				+ "            \"firstName\": \"Tester\"\r\n"
				+ "        },\r\n"
				+ "        \"address\": {\r\n"
				+ "            \"street\": \"123 Sunny St\",\r\n"
				+ "            \"city\": \"Miami\",\r\n"
				+ "            \"state\": \"FL\",\r\n"
				+ "            \"zip\": \"10000\"\r\n"
				+ "        }\r\n"
				+ " }";		
		
		//patch request
		RestAssured.baseURI = Constants.BASE_URL;
		
		Response patch = RestAssured.given()
				         .queryParam("api_key", GlobalVariables.api_key)
				         .body(payload)
				         .when()
				         .patch(Constants.UpdatePatientEndPoint)
				         .prettyPeek();
		
		//Assert status code
		int expectedPatchStatusCode = 200;
		int actualPatchStatusCode = patch.statusCode();
		Assert.assertEquals(expectedPatchStatusCode, actualPatchStatusCode);
		
		String patchResponseBody = patch.getBody().asString();
		String expectedPatchBody = "{\"id\": \"SR19760827202206208364\", \"birthdate\": \"1976-08-27\", \"phone\": \"347-555-9876\", \"appointment_date\": \"2022-06-20\", \"name\": {\"lastName\": \"Awesome\", \"firstName\": \"Tester\"}, \"address\": {\"street\": \"123 Sunny St\", \"city\": \"Miami\", \"state\": \"FL\", \"zip\": \"10000\"}}";
		System.out.println("patch request body ===> " + patchResponseBody);
		
		System.out.println("expected body ===> " + expectedPatchBody);
		Assert.assertEquals(patchResponseBody, expectedPatchBody);
		
		//Assert either this way
		Assert.assertTrue(patchResponseBody.contains("\"id\": \"SR19760827202206208364\""));
		//or
		//Assert id, birthdate, phone, appointment_date
		String expectedID = "SR19760827202206208364";
		String actualID = patch.body().jsonPath().getString("id");
		Assert.assertEquals(expectedID, actualID);
		
		//Assert birthdate is unchanged
		String expectedBirthdate = "1976-08-27";
		String actualBirthdate = patch.body().jsonPath().getString("birthdate");
		Assert.assertEquals(expectedBirthdate, actualBirthdate);
		
		//Assert phone is unchanged
		String expectedPhone = "347-555-9876";
		String actualPhone = patch.body().jsonPath().getString("phone");
		Assert.assertEquals(expectedPhone, actualPhone);
		
		//Assert appointment date is unchanged
		String expectedAppointmentDate = "2022-06-20";
		String actualAppointmentDate = patch.body().jsonPath().getString("appointment_date");
		Assert.assertEquals(expectedAppointmentDate, actualAppointmentDate);
		
		//Assert lastName
		String expectedLastName = "Awesome";
		String actualLastName = patch.body().jsonPath().getString("name.lastName");
		Assert.assertEquals(expectedLastName, actualLastName);
		
		//Assert firstName
		String expectedFirstName = "Tester";
		String actualFirstName = patch.body().jsonPath().getString("name.firstName");
		Assert.assertEquals(expectedFirstName, actualFirstName);
		
		//Assert street
		String expectedStreet = "123 Sunny St";
		String actualStreet = patch.body().jsonPath().getString("address.street");
		Assert.assertEquals(expectedStreet, actualStreet);
		
		//Assert city 
		String expectedCity = "Miami";
		String actualCity = patch.body().jsonPath().getString("address.city");
		Assert.assertEquals(expectedCity, actualCity);
		
	    //Assert state
		String expectedState = "FL";
		String actualState = patch.body().jsonPath().getString("address.state");
		Assert.assertEquals(expectedState, actualState);
		
		//Assert zip
		String expectedZip = "10000";
		String actualZip = patch.body().jsonPath().getString("address.zip");
		Assert.assertEquals(expectedZip, actualZip);
		
		
		//Post request
		String postPayload = "{\r\n"
				+ "\"firstName\": \"Tester\",\r\n"
				+ "\"lastName\": \"Awesome\",\r\n"
				+ "\"url\": \"www.tester.com\"\r\n"
				+ "}";
		
		RestAssured.baseURI = Constants.BASE_URL;
		
		Response post = RestAssured.given()
				.queryParam("api_key", api_key)
				.body(postPayload)
				.when()
				.post(Constants.Post)
				.prettyPeek();
		
		//Assert status code 201 created
		int expectedPostStatusCode = 201;
		int actualPostStatusCode = post.statusCode();
		Assert.assertEquals(expectedPostStatusCode, actualPostStatusCode);
		System.out.println(actualPostStatusCode);
			
	}
}
