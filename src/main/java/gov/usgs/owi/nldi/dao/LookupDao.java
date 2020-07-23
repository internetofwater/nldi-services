package gov.usgs.owi.nldi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LookupDao extends BaseDao {

	public static final String ROOT_URL = "rootUrl";
    public static final String NAME = "name";
    public static final String URI = "uri";
    public static final String SHAPE = "shape";
    public static final String IDENTIFIER = "identifier";
	public static final String FEATURE_SOURCE = "featureSource";
	public static final String SOURCE = "source";
	public static final String SOURCE_NAME = "sourceName";
	public static final String COMID_LAT_LON = "comidLatLon";
	
	private static final String NS = "lookup.";

	@Autowired
	public LookupDao(SqlSessionFactory sqlSessionFactory) {
		super(sqlSessionFactory);
	}

	public Map<String, Object> getComid(String objectType, Map<String, Object> parameterMap) {
		return getSqlSession().selectOne(NS + objectType, parameterMap);
	}

	public Integer getComidByLatitudeAndLongitude(Map<String, Object> parameterMap) {
		return getSqlSession().selectOne(NS + COMID_LAT_LON, parameterMap);
	}

	public List<Map<String, Object>> getList(String objectType, Map<String, Object> parameterMap) {
		return getSqlSession().selectList(NS + objectType, parameterMap);
	}

}
