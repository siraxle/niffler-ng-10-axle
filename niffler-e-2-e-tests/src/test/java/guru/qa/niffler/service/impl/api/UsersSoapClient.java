package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.UserSoapApi;
import guru.qa.niffler.api.core.converter.SoapConverterFactory;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.service.RestClient;
import io.qameta.allure.Step;
import jaxb.userdata.CurrentUserRequest;
import jaxb.userdata.UserResponse;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public final class UsersSoapClient extends RestClient {

    private static final Config CFG = Config.getInstance();
    private final UserSoapApi userSoapApi;

    public UsersSoapClient() {
        super(CFG.userdataUrl(), false, SoapConverterFactory.create("niffler-userdata"), HttpLoggingInterceptor.Level.BODY);
        this.userSoapApi = create(UserSoapApi.class);
    }

    @Step("Найти пользователя используя SOAP")
    @Nonnull
    public UserResponse currentUser(CurrentUserRequest request) throws IOException {
        return userSoapApi.currentUser(request).execute().body();
    }

}