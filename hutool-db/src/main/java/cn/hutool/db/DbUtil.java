package cn.hutool.db;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.sql.SqlLog;
import cn.hutool.log.Log;
import cn.hutool.log.level.Level;
import cn.hutool.setting.Setting;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * 数据库操作工具类
 *
 * @author Luxiaolei
 */
public final class DbUtil {
	private final static Log log = Log.get();

	/**
	 * 连续关闭一系列的SQL相关对象<br>
	 * 这些对象必须按照顺序关闭，否则会出错。
	 *
	 * @param objsToClose 需要关闭的对象
	 */
	public static void close(final Object... objsToClose) {
		for (final Object obj : objsToClose) {
			if (null != obj) {
				if (obj instanceof AutoCloseable) {
					IoUtil.close((AutoCloseable) obj);
				} else {
					log.warn("Object {} not a ResultSet or Statement or PreparedStatement or Connection!", obj.getClass().getName());
				}
			}
		}
	}

	/**
	 * 获得默认数据源
	 *
	 * @return 默认数据源
	 */
	public static DataSource getDs() {
		return DSFactory.get();
	}

	/**
	 * 获取指定分组的数据源
	 *
	 * @param group 分组
	 * @return 数据源
	 */
	public static DataSource getDs(final String group) {
		return DSFactory.get(group);
	}

	/**
	 * 获得JNDI数据源
	 *
	 * @param jndiName JNDI名称
	 * @return 数据源
	 */
	public static DataSource getJndiDsWithLog(final String jndiName) {
		try {
			return getJndiDs(jndiName);
		} catch (final DbRuntimeException e) {
			log.error(e.getCause(), "Find JNDI datasource error!");
		}
		return null;
	}

	/**
	 * 获得JNDI数据源
	 *
	 * @param jndiName JNDI名称
	 * @return 数据源
	 */
	public static DataSource getJndiDs(final String jndiName) {
		try {
			return (DataSource) new InitialContext().lookup(jndiName);
		} catch (final NamingException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * 移除配置文件中的Show SQL相关配置项<br>
	 * 此方法用于移除用户配置在分组下的配置项目
	 *
	 * @param setting 配置项
	 * @since 5.7.2
	 */
	public static void removeShowSqlParams(final Setting setting) {
		setting.remove(SqlLog.KEY_SHOW_SQL);
		setting.remove(SqlLog.KEY_FORMAT_SQL);
		setting.remove(SqlLog.KEY_SHOW_PARAMS);
		setting.remove(SqlLog.KEY_SQL_LEVEL);
	}

	/**
	 * 从配置文件中读取SQL打印选项，读取后会去除相应属性
	 *
	 * @param setting 配置文件
	 * @since 4.1.7
	 */
	public static void setShowSqlGlobal(final Setting setting) {
		// 初始化SQL显示
		final boolean isShowSql = Convert.toBool(setting.remove(SqlLog.KEY_SHOW_SQL), false);
		final boolean isFormatSql = Convert.toBool(setting.remove(SqlLog.KEY_FORMAT_SQL), false);
		final boolean isShowParams = Convert.toBool(setting.remove(SqlLog.KEY_SHOW_PARAMS), false);
		String sqlLevelStr = setting.remove(SqlLog.KEY_SQL_LEVEL);
		if (null != sqlLevelStr) {
			sqlLevelStr = sqlLevelStr.toUpperCase();
		}
		final Level level = Convert.toEnum(Level.class, sqlLevelStr, Level.DEBUG);
		log.debug("Show sql: [{}], format sql: [{}], show params: [{}], level: [{}]", isShowSql, isFormatSql, isShowParams, level);
		setShowSqlGlobal(isShowSql, isFormatSql, isShowParams, level);
	}

	/**
	 * 设置全局配置：是否通过debug日志显示SQL
	 *
	 * @param isShowSql    是否显示SQL
	 * @param isFormatSql  是否格式化显示的SQL
	 * @param isShowParams 是否打印参数
	 * @param level        SQL打印到的日志等级
	 * @see GlobalDbConfig#setShowSql(boolean, boolean, boolean, Level)
	 * @since 4.1.7
	 */
	public static void setShowSqlGlobal(final boolean isShowSql, final boolean isFormatSql, final boolean isShowParams, final Level level) {
		GlobalDbConfig.setShowSql(isShowSql, isFormatSql, isShowParams, level);
	}

	/**
	 * 设置全局是否在结果中忽略大小写<br>
	 * 如果忽略，则在Entity中调用getXXX时，字段值忽略大小写，默认忽略
	 *
	 * @param caseInsensitive 否在结果中忽略大小写
	 * @see GlobalDbConfig#setCaseInsensitive(boolean)
	 * @since 5.2.4
	 */
	public static void setCaseInsensitiveGlobal(final boolean caseInsensitive) {
		GlobalDbConfig.setCaseInsensitive(caseInsensitive);
	}

	/**
	 * 设置全局是否INSERT语句中默认返回主键（默认返回主键）<br>
	 * 如果false，则在Insert操作后，返回影响行数
	 * 主要用于某些数据库不支持返回主键的情况
	 *
	 * @param returnGeneratedKey 是否INSERT语句中默认返回主键
	 * @see GlobalDbConfig#setReturnGeneratedKey(boolean)
	 * @since 5.3.10
	 */
	public static void setReturnGeneratedKeyGlobal(final boolean returnGeneratedKey) {
		GlobalDbConfig.setReturnGeneratedKey(returnGeneratedKey);
	}

	/**
	 * 自定义数据库配置文件路径（绝对路径或相对classpath路径）
	 *
	 * @param dbSettingPath 自定义数据库配置文件路径（绝对路径或相对classpath路径）
	 * @see GlobalDbConfig#setDbSettingPath(String)
	 * @since 5.8.0
	 */
	public static void setDbSettingPathGlobal(final String dbSettingPath) {
		GlobalDbConfig.setDbSettingPath(dbSettingPath);
	}
}
