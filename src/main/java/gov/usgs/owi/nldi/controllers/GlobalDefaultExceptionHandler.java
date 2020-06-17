package gov.usgs.owi.nldi.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public @ResponseBody String handleUncaughtException(Exception ex, WebRequest request, HttpServletResponse response) throws IOException {
		return handleError(ex, response);
	}

	public static String handleError(Exception ex, HttpServletResponse response) {
		if (ex instanceof MissingServletRequestParameterException
				|| ex instanceof HttpMediaTypeNotSupportedException
				|| ex instanceof ConstraintViolationException) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return ex.getLocalizedMessage();
		} else if (ex instanceof HttpMessageNotReadableException) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			if (ex.getLocalizedMessage().contains("\n")) {
				//This exception's message contains implementation details after the new line, so only take up to that.
				return ex.getLocalizedMessage().substring(0, ex.getLocalizedMessage().indexOf("\n"));
			} else {
				return ex.getLocalizedMessage().replaceAll("([a-zA-Z]+\\.)+","");
			}
		} else {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			int hashValue = response.hashCode();
			//Note: we are giving the user a generic message.  
			//Server logs can be used to troubleshoot problems.
			String msgText = "Something bad happened. Contact us with Reference Number: " + hashValue;
			LOG.error(msgText, ex);
			return msgText;
		}
	}
}
