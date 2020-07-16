package gov.usgs.owi.nldi.controllers;


import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalDefaultExceptionHandlerTest {

	@Mock
	private WebRequest request;

	private GlobalDefaultExceptionHandler controller = new GlobalDefaultExceptionHandler();
	private HttpInputMessage httpInputMessage;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void handleUncaughtExceptionTest() throws IOException {
		HttpServletResponse response = new MockHttpServletResponse();
		assertEquals("Something bad happened. Contact us with Reference Number: ",
				controller.handleUncaughtException(new RuntimeException(), request, response).substring(0, 58));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

		response = new MockHttpServletResponse();
		assertEquals("Required String parameter 'parm' is not present",
				controller.handleUncaughtException(new MissingServletRequestParameterException("parm", "String"), request, response));
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

		response = new MockHttpServletResponse();
		assertEquals("no way",
				controller.handleUncaughtException(new HttpMediaTypeNotSupportedException("no way"), request, response));
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

		response = new MockHttpServletResponse();
		assertEquals("ok to see",
				controller.handleUncaughtException(new HttpMessageNotReadableException("ok to see\nhide this\nand this", httpInputMessage), request, response));
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

		response = new MockHttpServletResponse();
		assertEquals("Some123$Mes\tsage!!.",
				controller.handleUncaughtException(new HttpMessageNotReadableException("Some123$Mes\tsage!!.", httpInputMessage), request, response));
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

}
