package gov.usgs.owi.nldi.dao;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao extends SqlSessionDaoSupport {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDao.class);

	public static final String DATA_SOURCES = "dataSources";
	public static final String FEATURE = "feature";
	public static final String FEATURES = "features";
	public static final String FEATURES_COLLECTION = "featuresCollection";
	public static final String FLOW_LINES = "flowLines";
	public static final String BASIN = "basin";
	public static final String CHARACTERISTICS_METADATA = "characteristicMetadata";
	public static final String CHARACTERISTIC_DATA = "characteristicData";

	public static final String COMID = "comid";

	public static final String LEGACY = "Legacy";
	public static final String FEATURES_LEGACY = FEATURES + LEGACY;
	public static final String FLOW_LINES_LEGACY = FLOW_LINES + LEGACY;

	public BaseDao(SqlSessionFactory sqlSessionFactory) {
		LOG.trace(getClass().getName());
		setSqlSessionFactory(sqlSessionFactory);
	}
}
