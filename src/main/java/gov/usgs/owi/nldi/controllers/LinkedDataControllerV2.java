package gov.usgs.owi.nldi.controllers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

import gov.usgs.owi.nldi.dao.BaseDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;

@RestController
public class LinkedDataControllerV2 extends BaseController {

	@Autowired
	private LinkedDataController controllerV1;

	private static final String LINKED_DATA_V1 = "linked-data/";
	private static final String LINKED_DATA_V2 = "linked-data/v2/";

	@Autowired
	public LinkedDataControllerV2(LookupDao inLookupDao, StreamingDao inStreamingDao,
			Navigation inNavigation, Parameters inParameters, ConfigurationService configurationService,
			LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, configurationService, inLogService);
	}

	//swagger documentation for /linked-data/v2/{featureSource}/{featureID}/navigate/{navigationMode} endpoint
	@Operation(summary = "getNavigateOptions", description = "returns the navigation options for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/v2/{featureSource}/{featureID}/navigate/{navigationMode}", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getNavigationOptions(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
		@PathVariable(Parameters.FEATURE_ID) String featureID,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);

		try {
			List<Map<String, Object>> dataSources = controllerV1.getDataSources(request, response);
			List<Map<String, Object>> newDataSources = new ArrayList<>();
			String newNavigationUrl = createNewNavigationUrl(request);
			for (Map<String, Object> dataSource: dataSources) {
				if (Parameters.COMID.equals(dataSource.get(LookupDao.SOURCE))) {
					dataSource.put(LookupDao.SOURCE, "Flowlines");
					dataSource.put(LookupDao.SOURCE_NAME, "NHDPlus flowlines");
					String flowlinesV2Url = newNavigationUrl.replace(LINKED_DATA_V1, LINKED_DATA_V2);
					dataSource.put(BaseDao.FEATURES, flowlinesV2Url  + "flowlines");
					newDataSources.add(dataSource);
				} else {
					dataSource.put(BaseDao.FEATURES, newNavigationUrl
						+ dataSource.get(LookupDao.SOURCE).toString().toLowerCase());
					newDataSources.add(dataSource);
				}
			}
			return newDataSources;

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return null;
	}

	//swagger documentation for /linked-data/{featureSource}/{featureID}/navigate/{navigationMode} endpoint
	@Operation(summary = "getFlowlines", description = "returns the flowlines for the specified navigation in WGS84 lat/lon GeoJSON")
	@GetMapping(value="linked-data/v2/{featureSource}/{featureID}/navigate/{navigationMode}/flowlines", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getFlowlines(
		HttpServletRequest request, HttpServletResponse response,
		@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
		@PathVariable(Parameters.FEATURE_ID) String featureID,
		@PathVariable(Parameters.NAVIGATION_MODE) @Pattern(regexp=REGEX_NAVIGATION_MODE) String navigationMode,
		@RequestParam(value=Parameters.STOP_COMID, required=false) @Range(min=1, max=Integer.MAX_VALUE) String stopComid,
		@Parameter(description=Parameters.DISTANCE_DESCRIPTION)
		@RequestParam(value=Parameters.DISTANCE, required=false, defaultValue=Parameters.MAX_DISTANCE)
		@Pattern(message=Parameters.DISTANCE_VALIDATION_MESSAGE, regexp=Parameters.DISTANCE_VALIDATION_REGEX) String distance,
		@RequestParam(value=Parameters.LEGACY, required=false) String legacy) throws Exception {

		BigInteger logId = logService.logRequest(request);

		try {
			String comid = getComid(featureSource, featureID);
			if (null == comid) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				streamFlowLines(response, comid, navigationMode, stopComid, distance, isLegacy(legacy, navigationMode));
			}
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}


	// We need to create navigation urls for the various options (see test file navigate_V2.json)
	// We do this by starting with the linked-data url from the configuration service
	// then adding all the request-specific elements from the request we received
	private String createNewNavigationUrl(HttpServletRequest request) {
		String newUrl = configurationService.getLinkedDataUrl();
		String requestUrl = request.getRequestURL().toString();
		String[] arr = requestUrl.split("linked-data/v2");
		newUrl += arr[1];
		newUrl += "/";
		return newUrl;
	}


}
