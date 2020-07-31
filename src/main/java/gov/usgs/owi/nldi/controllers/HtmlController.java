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
import java.util.Objects;

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
		//remove it if user put it somewhere in the middle
		String queryString = Objects.requireNonNullElse(oldQueryString, "");
		queryString = queryString.replace("&f=html", "");
		//remove it if user put it at the beginning
		queryString = queryString.replace("f=html", "");
        if (queryString.startsWith("&")) {
        	queryString = queryString.substring(1);
		}
        return queryString;
	}

	public String getJsonRedirectLink(HttpServletRequest request) {
		// Get the base URL
		StringBuffer redirectUrl = new StringBuffer(configurationService.getRootUrl());

		String requestUrl = request.getRequestURL().toString();

		//append all the path variables
		if (requestUrl.contains("linked-data")) {
			redirectUrl = addQueryString(requestUrl, redirectUrl, "linked-data");
		} else if (requestUrl.contains("lookups")) {
			redirectUrl = addQueryString(requestUrl, redirectUrl, "lookups");
		}

		String queryString = removeHtmlFormatFromQueryString(request.getQueryString());
		redirectUrl.append("?f=json");

		if (!StringUtils.isEmpty(queryString)) {
			redirectUrl.append("&");
			redirectUrl.append(queryString);
		}

		String redirectLink = ensureIsHttps(redirectUrl);
		return redirectLink;
	}

	public StringBuffer addQueryString(String requestUrl, StringBuffer redirectUrl, String root) {
		String[] tempArr = requestUrl.split(root);
		redirectUrl.append("/");
		redirectUrl.append(root);
		if (tempArr.length > 1) {
			redirectUrl.append(tempArr[1]);
		}
		return redirectUrl;

	}

	// on labs-dev, both request.getRequestUrl() and configurationService.getRootUrl()
	// are reporting as "http", which causes the redirect link to fail.  Force it to
	// https if we know it is https.
	public String ensureIsHttps(StringBuffer redirectUrl) {
		String redirectLink = redirectUrl.toString();
		if (redirectLink.toLowerCase().contains("usgs.gov") && !redirectLink.toLowerCase().contains("owi-test")) {
			redirectLink = redirectLink.replace("http://", "https://");
		}
		return redirectLink;
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
