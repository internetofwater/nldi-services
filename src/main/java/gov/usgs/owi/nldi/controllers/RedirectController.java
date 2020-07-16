package gov.usgs.owi.nldi.controllers;

import gov.usgs.owi.nldi.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Hidden;

@RestController
public class RedirectController {

	protected final ConfigurationService configurationService;

	@Autowired
	public RedirectController(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@GetMapping(value="/swagger")
	@Hidden
	public RedirectView getSwagger() {
		String url = configurationService.getRootUrl()
				+ "/swagger-ui/index.html?configUrl="
				+ configurationService.getSwaggerApiDocsUrl();
		return new RedirectView(url, true, true);
	}

}
