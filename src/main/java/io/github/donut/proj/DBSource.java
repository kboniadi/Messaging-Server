package io.github.donut.proj;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public abstract class DBSource {
    private static BasicDataSource basicDS;

    static {
        try {
            basicDS = new BasicDataSource();
            Properties properties = new Properties();
            // Loading properties file
            properties.load(DBSource.class.getResourceAsStream("db.properties"));
            basicDS.setDriverClassName(properties.getProperty("DRIVER_CLASS")); //loads the jdbc driver
            basicDS.setUrl(properties.getProperty("DB_CONNECTION_URL"));
            basicDS.setUsername(properties.getProperty("DB_USER"));
            basicDS.setPassword(properties.getProperty("DB_PWD"));
            // Parameters for connection pooling
            basicDS.setInitialSize(10);
            basicDS.setMaxTotal(10);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        return basicDS;
    }
}
