package io.wkrzywiec.sandbox.testconatiners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class TestcontainersSanboxTest {

    // will be started before and stopped after each test method
    @Container
    private PostgreSQLContainer posgtres = new PostgreSQLContainer("postgres:9.6.12")
            .withDatabaseName("public");

    // will be shared between test methods
    @Container
    private static final GenericContainer keycloak = new GenericContainer(DockerImageName.parse("jboss/keycloak:11.0.2"))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/auth/realms/master"));

    private JdbcTemplate jdbcTemplate;

    @Test
    void postgresContainerTest() {

        Long result = jdbcTemplate.queryForObject("SELECT DISTINCT 1 FROM information_schema.tables", Long.class);
        posgtres.getLogs();

        assertEquals(1L, result);
    }

    @Test
    void keycloakContainerTest() {

        when()
                .get("http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/auth/realms/master/.well-known/openid-configuration")
                .prettyPeek()
        .then()
                .statusCode(200);
    }



    @BeforeEach
    void init() {
        final String driverClassName = posgtres.getDriverClassName();
        final String jdbcUrl = posgtres.getJdbcUrl();
        final String username = posgtres.getUsername();
        final String password = posgtres.getPassword();

        final DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(jdbcUrl)
                .username(username)
                .password(password).build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
