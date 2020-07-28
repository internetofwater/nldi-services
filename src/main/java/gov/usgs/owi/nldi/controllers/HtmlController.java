package gov.usgs.owi.nldi.controllers;

import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Parameters;
import io.swagger.v3.oas.annotations.Hidden;
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
import java.math.BigInteger;

@RestController
public class HtmlController {

	@Autowired
	private LogService logService;

	@Autowired
	private ConfigurationService configurationService;


	@GetMapping(value="/linked-data/v2/**", produces= MediaType.TEXT_HTML_VALUE)
	@Hidden
	public String getLinkedDataV2Html(
		HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name= Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		return processHtml(request, response);
	}


	@GetMapping(value="/linked-data/{featureSource}/**", produces= MediaType.TEXT_HTML_VALUE)
	@Hidden
	public String getLinkedDataHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name= Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		return processHtml(request, response);
	}

	@GetMapping(value="/linked-data", produces= MediaType.TEXT_HTML_VALUE)
	@Hidden
	public String getLinkedDataDataSourcesHtml(
		HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name= Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		return processHtml(request, response);
	}

	@GetMapping(value="/linked-data/comid/**", produces= MediaType.TEXT_HTML_VALUE)
	@Hidden
	public String getNetworkHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name=Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		return processHtml(request, response);
	}

	@GetMapping(value="/lookups/**", produces= MediaType.TEXT_HTML_VALUE)
	@Hidden
	public String getLookupsHtml(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name=Parameters.FORMAT, required=false) @Pattern(regexp=BaseController.OUTPUT_FORMAT) String format) throws Exception {
		return processHtml(request, response);
	}

	private String removeHtmlFormatFromQueryString(String oldQueryString) {
		if (StringUtils.isEmpty(oldQueryString)) {
			return oldQueryString;
		}
		//remove it if user put it somewhere in the middle
		String queryString = oldQueryString;
		queryString = queryString.replace("&f=html", "");
		//remove it if user put it at the beginning
		queryString = queryString.replace("f=html", "");
        if (queryString.startsWith("&")) {
        	queryString = queryString.substring(1);
		}
        return queryString;
	}

	private String getJsonRedirectLink(HttpServletRequest request) {
		// Get the base URL
		StringBuffer redirectUrl = new StringBuffer(configurationService.getRootUrl());

		String requestUrl = request.getRequestURL().toString();

		//append all the path variables
		if (requestUrl.contains("linked-data")) {
			String[] tempArr = requestUrl.split("linked-data");
			// ../linked-data is also a legit URL so check that something comes after
			redirectUrl.append("/");
			redirectUrl.append("linked-data");
			if (tempArr.length > 1) {
				redirectUrl.append(tempArr[1]);
			}
		} else if (requestUrl.contains("lookups")) {
			String[] tempArr = requestUrl.split("lookups");
			// ../linked-data is also a legit URL so check that something comes after
			redirectUrl.append("/");
			redirectUrl.append("lookups");
			if (tempArr.length > 1) {
				redirectUrl.append(tempArr[1]);
			}
		}

		String queryString = removeHtmlFormatFromQueryString(request.getQueryString());
		redirectUrl.append("?f=json");

		if (!StringUtils.isEmpty(queryString)) {
			redirectUrl.append("&");
			redirectUrl.append(queryString);
		}

		return redirectUrl.toString();
	}

	private String processHtml(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try {
			String html = new String(FileCopyUtils.copyToByteArray(new ClassPathResource("/html/htmlresponse.html").getInputStream()));
			return html.replace("URL_MARKER", getJsonRedirectLink(request));
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}
}
