package io.wkrzywiec.sandbox.testconatiners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.ClassUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.jdbc.ContainerDatabaseDriver;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class TestcontainersSanboxTest {

    // will be started before and stopped after each test method
    @Container
    private PostgreSQLContainer posgtres = new PostgreSQLContainer("postgres:9.6.12")
            .withDatabaseName("public");

    // will be started before and stopped after each test method
//    @Container
//    private PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer()
//            .withDatabaseName("foo")
//            .withUsername("foo")
//            .withPassword("secret");

    private JdbcTemplate jdbcTemplate;

    @Test
    void containerForEachTestMethod() {

        Long result = jdbcTemplate.queryForObject("SELECT DISTINCT 1 FROM information_schema.tables", Long.class);
        posgtres.getLogs();

        assertEquals(1L, result);


    }



    @BeforeEach
    void init() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final String driverClassName = posgtres.getDriverClassName();
        final String jdbcUrl = posgtres.getJdbcUrl();
        final String username = posgtres.getUsername();
        final String password = posgtres.getPassword();


        final Class<?> driverClass = ClassUtils.resolveClassName(driverClassName, this.getClass().getClassLoader());
        final Driver driver = (Driver) ClassUtils.getConstructorIfAvailable(driverClass).newInstance();
        final DataSource dataSource = DataSourceBuilder.create().driverClassName(driverClassName).url(jdbcUrl).username(username).password(password).build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
