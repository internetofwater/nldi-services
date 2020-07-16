package gov.usgs.owi.nldi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment=WebEnvironment.DEFINED_PORT,
		properties={
				"nldi.displayHost=localhost:8080",
				"nldi.displayProtocol=http",
				"nldi.displayPath=/nldi"})
public class RedirectControllerIT  {

	@Value("${nldi.displayHost}")
	private String displayHost;

	@Value("${nldi.displayProtocol}")
	private String displayProtocol;

	@Value("${nldi.displayPath}")
	private String displayPath;

	@Test
	void testProperties(){
		assertThat("protocol is correct", "http".equals(displayProtocol));
		assertThat("display host is correct", "localhost:8080".equals(displayHost));
		assertThat("display path is correct", "/nldi".equals(displayPath));
	}

	@Test
	public void getSwaggerTest(@Autowired TestRestTemplate restTemplate) throws Exception {
		ResponseEntity<String> rtn = restTemplate.getForEntity("/swagger", String.class);
		assertThat(rtn.getStatusCode(), equalTo(HttpStatus.OK));
		assertTrue(rtn.getBody().contains("Swagger UI"));
	}

}
