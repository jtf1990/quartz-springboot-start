package com.jtf.quartzstarter.support.connectionprovider;

import com.jtf.quartzstarter.ApplicationHolder;
import org.quartz.utils.ConnectionProvider;
import org.quartz.utils.PoolingConnectionProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * spring connection Provider
 * quart 使用的连接池提供者
 * @author jiangtaofeng
 */
public class SpringConnectionProvider implements PoolingConnectionProvider {



    @Override
    public Connection getConnection() throws SQLException {
        return ApplicationHolder.getApplicationContext().getBean(DataSource.class).getConnection();
    }

    @Override
    public void shutdown() throws SQLException {
        // nothingtodo 由spring控制
    }

    @Override
    public void initialize() throws SQLException {
        // nothingto 由spring控制
    }

    @Override
    public DataSource getDataSource() {
       return ApplicationHolder.getApplicationContext().getBean(DataSource.class);
    }
}
