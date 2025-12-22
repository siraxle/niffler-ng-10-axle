package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import lombok.NonNull;

import java.util.UUID;

public record UserJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("surname")
        String surname,
        @JsonProperty("fullname")
        String fullname,
        @JsonProperty("currency")
        CurrencyValues currency,
        @JsonProperty("photo")
        byte[] photo,
        @JsonProperty("photoSmall")
        byte[] photoSmall,
        @JsonProperty("friendshipStatus")
        FriendshipStatus friendshipStatus,
        @JsonIgnore
        TestData testData) {

    public static @NonNull UserJson fromEntity(@NonNull UserEntity entity, @NonNull FriendshipStatus friendshipStatus) {
        return new UserJson(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getSurname(),
                entity.getFullname(),
                entity.getCurrency(),
                entity.getPhoto(),
                entity.getPhotoSmall(),
                friendshipStatus,
                null
        );
    }

    public @NonNull UserJson addTestData(TestData testData) {
        return new UserJson(
                id,
                username,
                firstname,
                surname,
                fullname,
                currency,
                photo,
                photoSmall,
                friendshipStatus,
                testData
        );
    }
}