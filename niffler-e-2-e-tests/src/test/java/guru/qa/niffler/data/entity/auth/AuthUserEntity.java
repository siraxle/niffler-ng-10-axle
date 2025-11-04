package guru.qa.niffler.data.entity.auth;

import guru.qa.niffler.model.UserAuthJson;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AuthUserEntity {
    private UUID id;
    private String username;
    private String password;
    private Boolean enabled;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private List<AuthAuthorityEntity> authorities = new ArrayList<>();

    public AuthUserEntity() {
    }

    public static AuthUserEntity fromJson(UserAuthJson userAuthJson) {
        AuthUserEntity entity = new AuthUserEntity();
        entity.setId(userAuthJson.id());
        entity.setUsername(userAuthJson.username());
        entity.setPassword(userAuthJson.password());
        entity.setEnabled(userAuthJson.enabled());
        entity.setAccountNonExpired(userAuthJson.accountNonExpired());
        entity.setAccountNonLocked(userAuthJson.accountNonLocked());
        entity.setCredentialsNonExpired(userAuthJson.credentialsNonExpired());
        return entity;
    }

    public static AuthUserEntity toAuthUserEntity(UserAuthJson user) {
        AuthUserEntity userEntity = new AuthUserEntity();
        userEntity.setUsername(user.username());
        userEntity.setPassword(user.password());
        userEntity.setEnabled(user.enabled());
        userEntity.setAccountNonExpired(user.accountNonExpired());
        userEntity.setAccountNonLocked(user.accountNonLocked());
        userEntity.setCredentialsNonExpired(user.credentialsNonExpired());
        return userEntity;
    }

    public static AuthUserEntity toAuthUserEntityWithId(UserAuthJson user) {
        AuthUserEntity userEntity = toAuthUserEntity(user);
        userEntity.setId(user.id());
        return userEntity;
    }

}
