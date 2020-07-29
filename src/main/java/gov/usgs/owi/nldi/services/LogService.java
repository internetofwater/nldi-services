package gov.usgs.owi.nldi.services;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usgs.owi.nldi.dao.LogDao;

@Component
public class LogService {
	private LogDao logDao;

	@Autowired
	public LogService(LogDao inLogDao) {
		this.logDao = inLogDao;
	}

	public BigInteger logRequest(HttpServletRequest request) {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(LogDao.ID, null);
		if (null != request) {
			parameterMap.put(LogDao.REFERER, request.getHeader(LogDao.REFERER));
			parameterMap.put(LogDao.USER_AGENT, request.getHeader(LogDao.USER_AGENT));
			parameterMap.put(LogDao.REQUEST_URI, request.getRequestURI());
			parameterMap.put(LogDao.QUERY_STRING, request.getQueryString());
		}

		return logDao.start(parameterMap);
	}

	public void logRequestComplete(BigInteger logId, int httpStatusCode) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(LogDao.ID, logId);
		parameterMap.put(LogDao.HTTP_STATUS_CODE, httpStatusCode);
		logDao.end(parameterMap);
	}

}
