package gov.usgs.owi.nldi.dao;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DbTestConfig.class, LogDao.class})
@DatabaseSetup("classpath:/testData/webServiceLog.xml")
public class LogDaoIT extends BaseIT {

	public static final String TEST_QUERY = "select web_service_log_id, to_char(request_timestamp_utc, 'YYYY-MM-DD HH24:MI') request_timestamp_utc,"
			+ " to_char(request_completed_utc, 'YYYY-MM-DD HH24:MI') request_completed_utc, referer, user_agent, request_uri, query_string"
			+ " from nldi_data.web_service_log";

	public static final String REFERER_TEST_VALUE = "http://localhost:8080/test";
	public static final String USER_AGENT_TEST_VALUE = "Mozilla/5.0";
	public static final String REQUEST_URI_TEST_VALUE = "/test/test125/DM";
	public static final String QUERY_STRING_TEST_VALUE = "distance=15";
	public static final BigInteger ID_TEST_VALUE = BigInteger.valueOf(-1);
	public static final int HTTP_STATUS_CODE_TEST_VALUE = 200;


	@Autowired
	private LogDao logDao;

	@Test
	@ExpectedDatabase(
			value="classpath:/testResult/log/start.xml",
			table="nldi_data.web_service_log",
			query=TEST_QUERY,
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED,
			modifiers={IdModifier.class, DateTimeModifier.class})
	public void startTest() {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LogDao.REFERER, REFERER_TEST_VALUE);
		parameterMap.put(LogDao.USER_AGENT, USER_AGENT_TEST_VALUE);
		parameterMap.put(LogDao.REQUEST_URI, REQUEST_URI_TEST_VALUE);
		parameterMap.put(LogDao.QUERY_STRING, QUERY_STRING_TEST_VALUE);
		id = logDao.start(parameterMap);
		assertNotNull(id);
	}

	@Test
	@ExpectedDatabase(
			value="classpath:/testResult/log/end.xml",
					table="nldi_data.web_service_log",
					query=TEST_QUERY,
					assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED,
					modifiers=DateTimeModifier.class)
	public void endTest() {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LogDao.ID, ID_TEST_VALUE);
		parameterMap.put(LogDao.HTTP_STATUS_CODE, HTTP_STATUS_CODE_TEST_VALUE);
		logDao.end(parameterMap);
	}

}
