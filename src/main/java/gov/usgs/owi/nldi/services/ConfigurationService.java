
package gov.usgs.owi.nldi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

	@Value("${springdoc.version}")
	private String appVersion;

	@Value("${nldi.displayProtocol}")
	private String displayProtocol;

	@Value("${nldi.displayHost}")
	private String displayHost;

	@Value("${nldi.displayPath}")
	private String displayPath;

	public String getDisplayProtocol() {
		return displayProtocol;
	}

	public String getDisplayHost() {
		return displayHost;
	}

	public String getDisplayPath() {
		return displayPath;
	}

	public String getLinkedDataUrl() {
		return getDisplayProtocol() + "://" + getDisplayHost() + getDisplayPath() + "/linked-data";
	}

	public String getRootUrl() {
		return getDisplayProtocol() + "://" + getDisplayHost() + getDisplayPath();
	}

	public String getAppVersion() {
		return appVersion;
	}
}
