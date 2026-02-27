package guru.qa.niffler.test.grpc;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.grpc.NifflerCurrencyServiceGrpc;
import guru.qa.niffler.grpc.NifflerUserdataServiceGrpc;
import guru.qa.niffler.jupiter.annotation.meta.GrpcTest;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.utils.GrpcConsoleInterceptor;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.extension.RegisterExtension;

@GrpcTest
public class BaseGgrpcTest {

    protected static final Config CFG = Config.getInstance();

    protected static final Channel channel = ManagedChannelBuilder
            .forAddress(CFG.currencyGrpcAddress(), CFG.currencyGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    protected static final NifflerCurrencyServiceGrpc.NifflerCurrencyServiceBlockingStub blockingStub =
            NifflerCurrencyServiceGrpc.newBlockingStub(channel);

    // Currency Service
    protected static final Channel currencyChannel = ManagedChannelBuilder
            .forAddress(CFG.currencyGrpcAddress(), CFG.currencyGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    protected static final NifflerCurrencyServiceGrpc.NifflerCurrencyServiceBlockingStub currencyStub =
            NifflerCurrencyServiceGrpc.newBlockingStub(currencyChannel);

    // UserData Service
    protected static final Channel userdataChannel = ManagedChannelBuilder
            .forAddress(CFG.userdataGrpcAddress(), CFG.userdataGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    protected static final NifflerUserdataServiceGrpc.NifflerUserdataServiceBlockingStub userdataStub =
            NifflerUserdataServiceGrpc.newBlockingStub(userdataChannel);

}
