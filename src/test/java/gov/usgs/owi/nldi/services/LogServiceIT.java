package gov.usgs.owi.nldi.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.dao.LogDao;
import gov.usgs.owi.nldi.dao.LogDaoIT;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment=WebEnvironment.NONE,
		classes={DbTestConfig.class, LogService.class, LogDao.class})
@DatabaseSetup("classpath:/testData/webServiceLog.xml")
public class LogServiceIT extends BaseIT {

	@Autowired
	private LogService logService;

	@Test
	@ExpectedDatabase(
			value="classpath:/testResult/log/start.xml",
			table="nldi_data.web_service_log",
			query=LogDaoIT.TEST_QUERY,
			assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED,
			modifiers={IdModifier.class, DateTimeModifier.class})
	public void logRequestTest() {
		MockHttpServletRequest request= new MockHttpServletRequest();
		request.addHeader(LogDao.REFERER, LogDaoIT.REFERER_TEST_VALUE);
		request.addHeader(LogDao.USER_AGENT, LogDaoIT.USER_AGENT_TEST_VALUE);
		request.setRequestURI(LogDaoIT.REQUEST_URI_TEST_VALUE);
		request.setQueryString(LogDaoIT.QUERY_STRING_TEST_VALUE);
		id = logService.logRequest(request);
	}

	@Test
	@ExpectedDatabase(
			value="classpath:/testResult/log/end.xml",
					table="nldi_data.web_service_log",
					query=LogDaoIT.TEST_QUERY,
					assertionMode=DatabaseAssertionMode.NON_STRICT_UNORDERED,
					modifiers=DateTimeModifier.class)
	public void logRequestCompleteTest() {
		logService.logRequestComplete(LogDaoIT.ID_TEST_VALUE, LogDaoIT.HTTP_STATUS_CODE_TEST_VALUE);
	}

}
