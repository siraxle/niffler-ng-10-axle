package guru.qa.niffler.service;

import com.google.protobuf.Empty;
import guru.qa.niffler.ex.NotFoundException;
import guru.qa.niffler.ex.SameUsernameException;
import guru.qa.niffler.grpc.*;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.model.UserJsonBulk;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class GrpcUserdataService extends NifflerUserdataServiceGrpc.NifflerUserdataServiceImplBase {

    private final UserService userService;

    @Autowired
    public GrpcUserdataService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public void getCurrentUser(GetCurrentUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserJson user = userService.getCurrentUser(request.getUsername());
            responseObserver.onNext(convertToUserResponse(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            guru.qa.niffler.data.CurrencyValues currency = null;
            if (request.hasCurrency()) {
                currency = fromGrpcCurrency(request.getCurrency());
            }

            UserJson userJson = new UserJson(
                    null,
                    request.getUsername(),
                    request.hasFirstname() ? request.getFirstname() : null,
                    request.hasSurname() ? request.getSurname() : null,
                    request.hasFullname() ? request.getFullname() : null,
                    currency,
                    request.hasPhoto() ? request.getPhoto() : null,
                    null,
                    null
            );

            UserJson updated = userService.update(userJson);
            responseObserver.onNext(convertToUserResponse(updated));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getAllUsers(GetAllUsersRequest request, StreamObserver<UserPageResponse> responseObserver) {
        try {
            Sort sort = parseSort(request.getSortList());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Page<UserJsonBulk> page = userService.allUsers(
                    request.getUsername(),
                    pageable,
                    request.hasSearchQuery() ? request.getSearchQuery() : null
            );

            UserPageResponse response = UserPageResponse.newBuilder()
                    .setTotalElements((int) page.getTotalElements())
                    .setTotalPages(page.getTotalPages())
                    .setFirst(page.isFirst())
                    .setLast(page.isLast())
                    .setSize(page.getSize())
                    .addAllEdges(page.getContent().stream()
                            .map(this::convertToUserResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getAllUsersList(GetAllUsersListRequest request, StreamObserver<UserListResponse> responseObserver) {
        try {
            List<UserJsonBulk> users = userService.allUsers(
                    request.getUsername(),
                    request.hasSearchQuery() ? request.getSearchQuery() : null
            );

            UserListResponse response = UserListResponse.newBuilder()
                    .addAllUsers(users.stream()
                            .map(this::convertToUserResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getFriends(GetFriendsRequest request, StreamObserver<UserPageResponse> responseObserver) {
        try {
            Sort sort = parseSort(request.getSortList());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Page<UserJsonBulk> page = userService.friends(
                    request.getUsername(),
                    pageable,
                    request.hasSearchQuery() ? request.getSearchQuery() : null
            );

            UserPageResponse response = UserPageResponse.newBuilder()
                    .setTotalElements((int) page.getTotalElements())
                    .setTotalPages(page.getTotalPages())
                    .setFirst(page.isFirst())
                    .setLast(page.isLast())
                    .setSize(page.getSize())
                    .addAllEdges(page.getContent().stream()
                            .map(this::convertToUserResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getFriendsList(GetFriendsListRequest request, StreamObserver<UserListResponse> responseObserver) {
        try {
            List<UserJsonBulk> friends = userService.friends(
                    request.getUsername(),
                    request.hasSearchQuery() ? request.getSearchQuery() : null
            );

            UserListResponse response = UserListResponse.newBuilder()
                    .addAllUsers(friends.stream()
                            .map(this::convertToUserResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void removeFriend(RemoveFriendRequest request, StreamObserver<Empty> responseObserver) {
        try {
            userService.removeFriend(request.getUsername(), request.getTargetUsername());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (SameUsernameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void sendFriendshipRequest(SendFriendshipRequestRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserJson user = userService.createFriendshipRequest(
                    request.getUsername(),
                    request.getTargetUsername()
            );
            responseObserver.onNext(convertToUserResponse(user));
            responseObserver.onCompleted();
        } catch (SameUsernameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void acceptFriendshipRequest(AcceptFriendshipRequestRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserJson user = userService.acceptFriendshipRequest(
                    request.getUsername(),
                    request.getTargetUsername()
            );
            responseObserver.onNext(convertToUserResponse(user));
            responseObserver.onCompleted();
        } catch (SameUsernameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void declineFriendshipRequest(DeclineFriendshipRequestRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserJson user = userService.declineFriendshipRequest(
                    request.getUsername(),
                    request.getTargetUsername()
            );
            responseObserver.onNext(convertToUserResponse(user));
            responseObserver.onCompleted();
        } catch (SameUsernameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    // TODO: Реализовать когда появятся методы в UserService
    @Override
    public void getIncomeInvitations(GetIncomeInvitationsRequest request, StreamObserver<UserListResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("Method not implemented")
                .asRuntimeException());
    }

    @Override
    public void getOutcomeInvitations(GetOutcomeInvitationsRequest request, StreamObserver<UserListResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED
                .withDescription("Method not implemented")
                .asRuntimeException());
    }

    // Конвертеры
    private UserResponse convertToUserResponse(UserJson user) {
        UserResponse.Builder builder = UserResponse.newBuilder()
                .setId(user.id() != null ? user.id().toString() : "")
                .setUsername(user.username() != null ? user.username() : "")
                .setCurrency(toGrpcCurrency(user.currency()));

        if (user.firstname() != null) {
            builder.setFirstname(user.firstname());
        }
        if (user.surname() != null) {
            builder.setSurname(user.surname());
        }
        if (user.fullname() != null) {
            builder.setFullname(user.fullname());
        }
        if (user.photo() != null) {
            builder.setPhoto(com.google.protobuf.ByteString.copyFrom(
                    user.photo().getBytes(StandardCharsets.UTF_8)));
        }
        if (user.photoSmall() != null) {
            builder.setPhotoSmall(com.google.protobuf.ByteString.copyFrom(
                    user.photoSmall().getBytes(StandardCharsets.UTF_8)));
        }
        if (user.friendshipStatus() != null) {
            builder.setFriendshipStatus(toGrpcFriendshipStatus(user.friendshipStatus()));
        }

        return builder.build();
    }

    private UserResponse convertToUserResponse(UserJsonBulk user) {
        UserResponse.Builder builder = UserResponse.newBuilder()
                .setId(user.id() != null ? user.id().toString() : "")
                .setUsername(user.username() != null ? user.username() : "")
                .setCurrency(toGrpcCurrency(user.currency()));

        if (user.firstname() != null) {
            builder.setFirstname(user.firstname());
        }
        if (user.surname() != null) {
            builder.setSurname(user.surname());
        }
        if (user.fullname() != null) {
            builder.setFullname(user.fullname());
        }
        if (user.photo() != null) {
            builder.setPhoto(com.google.protobuf.ByteString.copyFrom(
                    user.photo().getBytes(StandardCharsets.UTF_8)));
        }
        if (user.photoSmall() != null) {
            builder.setPhotoSmall(com.google.protobuf.ByteString.copyFrom(
                    user.photoSmall().getBytes(StandardCharsets.UTF_8)));
        }
        if (user.friendshipStatus() != null) {
            builder.setFriendshipStatus(toGrpcFriendshipStatus(user.friendshipStatus()));
        }

        return builder.build();
    }

    // Конвертация из gRPC в model
    private guru.qa.niffler.data.CurrencyValues fromGrpcCurrency(guru.qa.niffler.grpc.CurrencyValues currency) {
        switch (currency) {
            case USD: return guru.qa.niffler.data.CurrencyValues.USD;
            case EUR: return guru.qa.niffler.data.CurrencyValues.EUR;
            case KZT: return guru.qa.niffler.data.CurrencyValues.KZT;
            case CURRENCY_UNSPECIFIED:
            default: return guru.qa.niffler.data.CurrencyValues.RUB;
        }
    }

    // Конвертация из model в gRPC
    private guru.qa.niffler.grpc.CurrencyValues toGrpcCurrency(guru.qa.niffler.data.CurrencyValues currency) {
        if (currency == null) return guru.qa.niffler.grpc.CurrencyValues.CURRENCY_UNSPECIFIED;

        switch (currency) {
            case USD: return guru.qa.niffler.grpc.CurrencyValues.USD;
            case EUR: return guru.qa.niffler.grpc.CurrencyValues.EUR;
            case KZT: return guru.qa.niffler.grpc.CurrencyValues.KZT;
            default: return guru.qa.niffler.grpc.CurrencyValues.RUB;
        }
    }

    // Конвертация из model в gRPC для статуса дружбы
    private guru.qa.niffler.grpc.FriendshipStatus toGrpcFriendshipStatus(guru.qa.niffler.model.FriendshipStatus status) {
        if (status == null) return guru.qa.niffler.grpc.FriendshipStatus.FRIENDSHIP_STATUS_UNSPECIFIED;

        switch (status) {
            case INVITE_RECEIVED: return guru.qa.niffler.grpc.FriendshipStatus.INVITE_RECEIVED;
            case FRIEND: return guru.qa.niffler.grpc.FriendshipStatus.FRIEND;
            default: return guru.qa.niffler.grpc.FriendshipStatus.INVITE_SENT;
        }
    }

    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortParams.stream()
                .map(param -> {
                    String[] parts = param.split(",");
                    String property = parts[0];
                    Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList());

        return Sort.by(orders);
    }
}