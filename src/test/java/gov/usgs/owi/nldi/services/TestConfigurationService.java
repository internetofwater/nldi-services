package gov.usgs.owi.nldi.services;

/**
 *
 * @author mbucknel
 */
public class TestConfigurationService extends ConfigurationService {

	@Override
	public String getDisplayProtocol() {
		return "http";
	}

	@Override
	public String getDisplayHost() {
		return "owi-test.usgs.gov:8080";
	}


	@Override
	public String getDisplayPath() {
		return "/test-url";
	}

}
