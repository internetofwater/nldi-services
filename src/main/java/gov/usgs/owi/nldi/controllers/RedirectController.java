package gov.usgs.owi.nldi.controllers;

import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Hidden;

@RestController
public class RedirectController extends BaseController {

	private String urlRoot = null;

	public RedirectController(LookupDao inLookupDao, StreamingDao inStreamingDao, Navigation inNavigation, Parameters inParameters, ConfigurationService inConfigurationService, LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, inConfigurationService, inLogService);
	}


	//Used for integration test ... see RedirectControllerIT.java
	void setRootUrl(String url) {
		this.urlRoot = url;
	}

	@GetMapping(value="/swagger")
	@Hidden
	public RedirectView getSwagger() {
		if (urlRoot == null) {
			urlRoot = configurationService.getRootUrl();
		}
		return new RedirectView(this.urlRoot + "/swagger-ui/index.html?configUrl="
									+ configurationService.getSwaggerApiDocsUrl(), true, true);
	}

}
