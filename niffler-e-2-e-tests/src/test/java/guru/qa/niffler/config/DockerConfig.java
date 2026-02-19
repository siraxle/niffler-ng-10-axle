package guru.qa.niffler.config;


import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

enum DockerConfig implements Config {
    INSTANCE;

    @Override
    @Nonnull
    public String frontUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String registerUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String ghUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String authUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String gatewayUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String userdataUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String spendUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String authJdbcUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String spendJdbcUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String userdataJdbcUrl() {
        return "";
    }

    @Override
    @Nonnull
    public String currencyJdbcUrl() {
        return "";
    }

    @NotNull
    @Override
    public String currencyGrpcAddress() {
        return "";
    }

    @Override
    public int currencyGrpcPort() {
        return Config.super.currencyGrpcPort();
    }

}
