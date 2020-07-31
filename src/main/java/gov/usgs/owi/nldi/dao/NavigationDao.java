package gov.usgs.owi.nldi.dao;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NavigationDao extends BaseDao {

	//deprecated
	public static final String NAVIGATE = "navigate";

	public static final String NAVIGATION = "navigation";

	public static final String NAVIGATE_CACHED = NAVIGATE + "_cached";
	private static final String NS = NAVIGATE + ".";
	private static final String GET_CACHE = "getCache";

	@Autowired
	public NavigationDao(SqlSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	public Map<String, String> navigate(Map<String, Object> parameterMap) {
		return getSqlSession().selectOne(NS + NAVIGATE, parameterMap);
	}

	public String getCache(Map<String, Object> parameterMap) {
		return getSqlSession().selectOne(NS + GET_CACHE, parameterMap);
	}

}
