package guru.qa.niffler.test.gql;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Error;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.rx2.Rx2Apollo;
import guru.qa.FriendsCategoriesQuery;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.jupiter.annotation.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CategoriesAccessGraphQlTest extends BaseGraphQlTest {

    @User(
            friends = 1
    )
    @Test
    @ApiLogin
    public void cannotQueryCategoriesForAnotherUser(@Token String token) {
        ApolloCall<FriendsCategoriesQuery.Data> call = apolloClient.query(new FriendsCategoriesQuery())
                .addHttpHeader("Authorization", token);

        ApolloResponse<FriendsCategoriesQuery.Data> response = Rx2Apollo.single(call).blockingGet();

        assertThat(response.hasErrors()).isTrue();
        assertThat(response.errors)
                .isNotEmpty()
                .extracting(Error::getMessage)
                .anyMatch(message -> message.contains("Can`t query categories for another user"));
    }
}