package gov.usgs.owi.nldi.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Feature {
	@JsonProperty("name")
	public String name;
	@JsonProperty("uri")
	public String uri;
	@JsonProperty("comid")
	public String comid;
}
