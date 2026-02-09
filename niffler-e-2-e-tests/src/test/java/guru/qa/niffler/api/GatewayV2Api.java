package guru.qa.niffler.api;

import guru.qa.niffler.model.*;
import guru.qa.niffler.model.page.RestResponsePage;
import org.springframework.data.domain.PageImpl;
import retrofit2.Call;
import retrofit2.http.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface GatewayV2Api {

    @GET("api/v2/friends/all")
    Call<RestResponsePage<UserJson>> allFriends(@Header("Authorization") String bearerToken,
                                                @Query("page") int page,
                                                @Query("size") int size,
                                                @Query("sort") List<String> sort,
                                                @Query("searchQuery") @Nullable String searchQuery);

    @GET("api/v2/spends/all")
    Call<RestResponsePage<SpendJson>> allSpends(@Header("Authorization") String bearerToken,
                                    @Query("page") int page,
                                    @Query("size") int size,
                                    @Query("sort") List<String> sort,
                                    @Query("filterCurrency") @Nullable CurrencyValues filterCurrency,
                                    @Query("filterPeriod") @Nullable DataFilterValues filterPeriod,
                                    @Query("searchQuery") @Nullable String searchQuery);

    @GET("api/v2/stat/total")
    Call<StatisticV2Json> totalStat(@Header("Authorization") String bearerToken,
                                    @Query("statCurrency") @Nullable CurrencyValues statCurrency,
                                    @Query("filterCurrency") @Nullable CurrencyValues filterCurrency,
                                    @Query("filterPeriod") @Nullable DataFilterValues filterPeriod);

    @GET("api/v2/users/all")
    Call<RestResponsePage<UserJson>> allUsers(@Header("Authorization") String bearerToken,
                                  @Query("page") int page,
                                  @Query("size") int size,
                                  @Query("sort") List<String> sort,
                                  @Query("searchQuery") @Nullable String searchQuery);

}