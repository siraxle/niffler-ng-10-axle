package guru.qa.niffler.data.entity.user;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserJson;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class UserEntity implements Serializable {
    private UUID id;
    private String username;
    private CurrencyValues currency;
    private String firstname;
    private String surname;
    private String fullname;
    private byte[] photo;
    private byte[] photoSmall;

    public UserEntity() {
    }

    public static UserEntity fromJson(UserJson userJson) {
        UserEntity entity = new UserEntity();
        entity.setId(userJson.id());
        entity.setUsername(userJson.username());
//        entity.setUsername(null);
        entity.setCurrency(userJson.currency());
        entity.setFirstname(userJson.firstname());
        entity.setSurname(userJson.surname());
        entity.setFullname(userJson.fullname());
        entity.setPhoto(userJson.photo());
        entity.setPhotoSmall(userJson.photoSmall());
        return entity;
    }

    public static UserEntity toUserEntity(UserJson user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        entity.setCurrency(user.currency());
        entity.setFirstname(user.firstname());
        entity.setSurname(user.surname());
        entity.setFullname(user.fullname());
        entity.setPhoto(user.photo());
        entity.setPhotoSmall(user.photoSmall());
        return entity;
    }

}
