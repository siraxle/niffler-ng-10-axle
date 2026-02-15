package guru.qa.niffler.test.gql;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.apollo.api.Error;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.rx2.Rx2Apollo;
import guru.qa.DeepRecursiveQuery;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.jupiter.annotation.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DepthLimitGraphQlTest extends BaseGraphQlTest {

    @User(friends = 3)
    @Test
    @ApiLogin
    public void recursiveQueryDepthShouldBeLimited(@Token String token) {
        ApolloCall<DeepRecursiveQuery.Data> call = apolloClient.query(new DeepRecursiveQuery())
                .addHttpHeader("Authorization", token);

        ApolloResponse<DeepRecursiveQuery.Data> response = Rx2Apollo.single(call).blockingGet();

        assertThat(response.hasErrors()).isTrue();
        assertThat(response.errors)
                .isNotEmpty()
                .extracting(Error::getMessage)
                .anyMatch(message ->
                        message.contains("Can`t fetch over 2 friends sub-queries")
                );
    }
}