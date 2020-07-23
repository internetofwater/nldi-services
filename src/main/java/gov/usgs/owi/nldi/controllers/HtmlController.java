package gov.usgs.owi.nldi.controllers;

import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.math.BigInteger;

@RestController
public class HtmlController {

	@Autowired
	private LogService logService;

	@GetMapping(value="/linked-data/{featureSource}/**", produces= MediaType.TEXT_HTML_VALUE)
	public String getLinkedDataHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name= Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		
		return processHtml(request, response);
	}

	@GetMapping(value="/linked-data/comid/**", produces= MediaType.TEXT_HTML_VALUE)
	public String getNetworkHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name=Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		
		return processHtml(request, response);
	}

	@GetMapping(value="/lookups/**", produces= MediaType.TEXT_HTML_VALUE)
	public String getLookupsHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name=Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		
		return processHtml(request, response);
	}

	private String getHtmlString(HttpServletRequest request) throws IOException {
		StringBuffer url = request.getRequestURL();
		String queryString = request.getQueryString();
		//remove it if user put it somewhere in the middle
		queryString = queryString.replace("&f=html", "");
		//remove it if user put it at the beginning
		queryString = queryString.replace("f=html", "");
		url.append("?f=json");
		if (!StringUtils.isEmpty(queryString)) {
			if (!queryString.startsWith("&")) {
				url.append("&");
			}
			url.append(queryString);
		}
		String html = new String(FileCopyUtils.copyToByteArray(new ClassPathResource("/html/htmlresponse.html").getInputStream()));
		return html.replace("URL_MARKER", url);
	}

	private String processHtml(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try {
			return getHtmlString(request);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}
}
