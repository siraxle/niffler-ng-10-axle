package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.UserSoapApi;
import guru.qa.niffler.api.core.converter.SoapConverterFactory;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.service.RestClient;
import io.qameta.allure.Step;
import jaxb.userdata.*;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

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

    @Step("Получить друзей (с пагинацией и фильтром)")
    @Nonnull
    public UsersResponse friendsPage(FriendsPageRequest request) throws IOException {
        return userSoapApi.friendsPage(request).execute().body();
    }

    @Step("Получить друзей (без пагинации, с фильтром)")
    @Nonnull
    public UsersResponse friends(FriendsRequest request) throws IOException {
        return userSoapApi.friends(request).execute().body();
    }

    @Step("Отправить приглашение в друзья")
    @Nonnull
    public UserResponse sendInvitation(SendInvitationRequest request) throws IOException {
        return userSoapApi.sendInvitation(request).execute().body();
    }

    @Step("Принять приглашение в друзья")
    @Nonnull
    public UserResponse acceptInvitation(AcceptInvitationRequest request) throws IOException {
        return userSoapApi.acceptInvitation(request).execute().body();
    }

    @Step("Отклонить приглашение в друзья")
    @Nonnull
    public UserResponse declineInvitation(DeclineInvitationRequest request) throws IOException {
        return userSoapApi.declineInvitation(request).execute().body();
    }

    @Step("Удалить друга")
    public void removeFriend(RemoveFriendRequest request) throws IOException {
        userSoapApi.removeFriend(request).execute();
    }
}