package gov.usgs.owi.nldi.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import gov.usgs.owi.nldi.NavigationMode;

@Service
public class Parameters {
	private static final Logger LOG = LoggerFactory.getLogger(Navigation.class);

	public static final String COMID = "comid";
	public static final String FEATURE_ID = "featureID";
	public static final String DISTANCE = "distance";
	public static final String DISTANCE_DESCRIPTION = "distance in kilometers";
	public static final String COORDS = "coords";
	public static final String COORDS_DESCRIPTION = "coordinates in the form 'POINT(longitude latitude)'";
	public static final String MAX_DISTANCE = "9999";
	public static final String NAVIGATION_MODE = "navigationMode";
	public static final String STOP_COMID = "stopComid";
	public static final String LEGACY = "legacy";
	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "lon";

	// Validates that the "coords" parameter is in the form POINT(-89.35 43.0864))
	public static final String POINT_VALIDATION_MESSAGE = "coords must be specified as a point with longitude and latitude, i.e. POINT(-89.35 43.0864)";
	public static final String POINT_VALIDATION_REGEX = "POINT\\([0-9.-]+\\s[0-9.-]+\\)";
	public static final String FORMAT = "f";
	public static final String CHARACTERISTIC_TYPE = "characteristicType";
	public static final String CHARACTERISTIC_ID = "characteristicId";
	public static final String DISTANCE_VALIDATION_MESSAGE = "distance must be between 1 and 9999 kilometers";
	// Validates that distances is either an empty string or a string representing 1 to 9999 kilometers
	public static final String DISTANCE_VALIDATION_REGEX = "^[0-9]{1,4}+$|^$|^\\s$";

	public Map<String, Object> processParameters(final String comid, final String navigationMode,
			final String distance, final String stopComid) {
		Map<String, Object> parameterMap = new HashMap<> ();

		Integer comidInt = validateComid(comid, false);
		if (null != comidInt) {
			parameterMap.put(COMID, comidInt);
		}

		if (isValidNavigationMode(navigationMode)) {
			LOG.debug("navigationMode:" + navigationMode);
			parameterMap.put(NAVIGATION_MODE, navigationMode);
		}

		if (StringUtils.hasText(distance)) {
			LOG.debug("distance:" + distance);
			parameterMap.put(DISTANCE, NumberUtils.parseNumber(distance, BigDecimal.class));
		}

		Integer stopComidInt = validateComid(stopComid, true);
		if (null != stopComidInt) {
			parameterMap.put(STOP_COMID, stopComidInt);
		}

		LOG.debug("Request Parameters:" + parameterMap.toString());

		return parameterMap;
	}

	public Integer validateComid(final String comid, boolean optional) {
		try {
			LOG.debug("comid:" + comid);
			return NumberUtils.parseNumber(comid, Integer.class);
		} catch (Exception e) {
			if (!optional) {
				LOG.info("Bad comid given:" + comid, e);
			}
			return null;
		}
	}

	public boolean isValidNavigationMode(final String navigationMode) {
		try {
			LOG.debug("navigationMode:" + navigationMode);
			return StringUtils.hasText(navigationMode) && null != NavigationMode.valueOf(navigationMode);
		} catch (Exception e) {
			LOG.info("Bad navigationMode given:" + navigationMode, e);
			return false;
		}
	}

}
