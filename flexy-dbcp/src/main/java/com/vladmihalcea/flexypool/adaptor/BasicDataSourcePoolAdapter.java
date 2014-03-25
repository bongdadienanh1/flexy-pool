package com.vladmihalcea.flexypool.adaptor;

import com.vladmihalcea.flexypool.exception.AcquireTimeoutException;
import com.vladmihalcea.flexypool.metric.Metrics;
import com.vladmihalcea.flexypool.util.ConfigurationProperties;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * <code>BasicDataSourcePoolAdapter</code> extends {@link AbstractPoolAdapter} and it adapts the required API to
 * communicate with the DBCP {@link BasicDataSource}
 *
 * @author Vlad Mihalcea
 * @version %I%, %E%
 * @since 1.0
 */
public class BasicDataSourcePoolAdapter extends AbstractPoolAdapter<BasicDataSource> {

    public static final String ACQUIRE_TIMEOUT_MESSAGE = "Cannot get a connection, pool error Timeout waiting for idle object";

    public static final PoolAdapterBuilder<BasicDataSource> BUILDER = new PoolAdapterBuilder<BasicDataSource>() {

        @Override
        public PoolAdapter<BasicDataSource> build(
                ConfigurationProperties<BasicDataSource, Metrics, PoolAdapter<BasicDataSource>> configurationProperties) {
            return new BasicDataSourcePoolAdapter(configurationProperties);
        }
    };

    public BasicDataSourcePoolAdapter(ConfigurationProperties<BasicDataSource, Metrics, PoolAdapter<BasicDataSource>> configurationProperties) {
        super(configurationProperties);
    }

    @Override
    public int getMaxPoolSize() {
        return getTargetDataSource().getMaxActive();
    }

    @Override
    public void setMaxPoolSize(int maxPoolSize) {
        getTargetDataSource().setMaxActive(maxPoolSize);
    }

    /**
     * Translate the DBCP Exception to AcquireTimeoutException.
     *
     * @param e exception
     * @return translated exception
     */
    @Override
    protected SQLException translateException(Exception e) {
        if (e.getMessage() != null &&
                Pattern.matches(ACQUIRE_TIMEOUT_MESSAGE, e.getMessage())) {
            return new AcquireTimeoutException(e);
        }
        return new SQLException(e);
    }
}
