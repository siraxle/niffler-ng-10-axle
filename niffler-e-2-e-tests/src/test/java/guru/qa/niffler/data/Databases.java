package guru.qa.niffler.data;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Databases {
    private Databases() {
    }

    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    private static DataSource getDataSource(final String jdbcUrl) {
        return dataSources.computeIfAbsent(
                jdbcUrl,
                k -> {
                    PGSimpleDataSource dataSource = new PGSimpleDataSource();
                    dataSource.setUser("postgres");
                    dataSource.setPassword("secret");
                    dataSource.setURL(jdbcUrl);
                    return dataSource;
                }
        );
    }

    public static Connection connection(String jdbcUrl) throws SQLException {
        return getDataSource(jdbcUrl).getConnection();
    }

}
