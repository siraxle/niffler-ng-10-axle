package guru.qa.niffler.config;

public interface Config {

    static Config getInstance() {
        return "docker".equals(System.getProperty("test.env"))
                ? DockerConfig.INSTANCE :
                LocalConfig.INSTANCE;
    }

    String frontUrl();

    String authUrl();

    String gatewayUrl();

    String userdataUrl();

    String spendUrl();

    String ghUrl();

    String registerUrl();

    String authJdbcUrl();

    String userdataJdbcUrl();

    String spendJdbcUrl();

    String currencyJdbcUrl();

}
