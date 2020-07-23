package gov.usgs.owi.nldi.controllers;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.transform.CharacteristicMetadataTransformer;

@RestController
public class LookupController extends BaseController {

	@Autowired
	public LookupController(LookupDao inLookupDao, StreamingDao inStreamingDao,
			Navigation inNavigation, Parameters inParameters, ConfigurationService configurationService,
			LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, configurationService, inLogService);
	}

	//swagger documentation for /lookups/{characteristicType}/characteristics endpoint
	@Operation(summary = "getCharacteristics", description = "Returns available characteristics metadata")
	@GetMapping(value="lookups/{characteristicType}/characteristics",produces=MediaType.APPLICATION_JSON_VALUE)
	public void getCharacteristics(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(Parameters.CHARACTERISTIC_TYPE) String characteristicType) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try (CharacteristicMetadataTransformer transformer = new CharacteristicMetadataTransformer(response)) {
			Map<String, Object> parameterMap = new HashMap<> ();
			parameterMap.put(Parameters.CHARACTERISTIC_TYPE, characteristicType.toLowerCase());
			addContentHeader(response);
			streamResults(transformer, BaseDao.CHARACTERISTICS_METADATA, parameterMap);
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}
}
