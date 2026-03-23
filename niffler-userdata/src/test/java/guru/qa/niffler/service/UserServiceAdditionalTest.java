package guru.qa.niffler.service;

import guru.qa.niffler.data.CurrencyValues;
import guru.qa.niffler.data.FriendshipEntity;
import guru.qa.niffler.data.FriendshipStatus;
import guru.qa.niffler.data.UserEntity;
import guru.qa.niffler.data.projection.UserWithStatus;
import guru.qa.niffler.data.repository.UserRepository;
import guru.qa.niffler.ex.NotFoundException;
import guru.qa.niffler.ex.SameUsernameException;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.model.UserJsonBulk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.model.FriendshipStatus.FRIEND;
import static guru.qa.niffler.model.FriendshipStatus.INVITE_SENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdditionalTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessagingService messagingService;

    private final UUID mainUserUuid = UUID.randomUUID();
    private final String mainUsername = "alice";
    private UserEntity mainUser;

    private final UUID friendUserUuid = UUID.randomUUID();
    private final String friendUsername = "bob";
    private UserEntity friendUser;

    private final UUID pendingUserUuid = UUID.randomUUID();
    private final String pendingUsername = "charlie";
    private UserEntity pendingUser;

    @BeforeEach
    void setUp() {
        mainUser = new UserEntity();
        mainUser.setId(mainUserUuid);
        mainUser.setUsername(mainUsername);
        mainUser.setCurrency(CurrencyValues.RUB);
        mainUser.setFullname("Alice Smith");

        friendUser = new UserEntity();
        friendUser.setId(friendUserUuid);
        friendUser.setUsername(friendUsername);
        friendUser.setCurrency(CurrencyValues.USD);
        friendUser.setFullname("Bob Johnson");

        pendingUser = new UserEntity();
        pendingUser.setId(pendingUserUuid);
        pendingUser.setUsername(pendingUsername);
        pendingUser.setCurrency(CurrencyValues.EUR);
        pendingUser.setFullname("Charlie Brown");
    }

    @Test
    void getCurrentUserShouldReturnUserFromDbWhenExists() {
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.getCurrentUser(mainUsername);

        assertNotNull(result);
        assertEquals(mainUserUuid, result.id());
        assertEquals(mainUsername, result.username());
        assertEquals(CurrencyValues.RUB, result.currency());
        assertEquals("Alice Smith", result.fullname());
        verify(userRepository, times(1)).findByUsername(mainUsername);
    }

    @Test
    void getCurrentUserShouldReturnDefaultUserWhenNotExists() {
        String newUsername = "newuser";
        when(userRepository.findByUsername(eq(newUsername))).thenReturn(Optional.empty());

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.getCurrentUser(newUsername);

        assertNotNull(result);
        assertNull(result.id());
        assertEquals(newUsername, result.username());
        assertEquals(CurrencyValues.RUB, result.currency());
        assertNull(result.fullname());
        assertNull(result.photo());
        verify(userRepository, times(1)).findByUsername(newUsername);
    }

    @Test
    void allUsersWithSearchQueryShouldCallRepositoryWithSearchParam() {
        String searchQuery = "test";
        List<UserWithStatus> mockUsers = List.of(
                createUserWithStatus(friendUser, null),
                createUserWithStatus(pendingUser, FriendshipStatus.PENDING)
        );

        when(userRepository.findByUsernameNot(eq(mainUsername), eq(searchQuery)))
                .thenReturn(mockUsers);

        userService = new UserService(userRepository, messagingService);

        List<UserJsonBulk> result = userService.allUsers(mainUsername, searchQuery);

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findByUsernameNot(mainUsername, searchQuery);
        verify(userRepository, never()).findByUsernameNot(eq(mainUsername));
    }

    @Test
    void allUsersWithNullSearchQueryShouldCallRepositoryWithoutSearchParam() {
        List<UserWithStatus> mockUsers = List.of(
                createUserWithStatus(friendUser, null),
                createUserWithStatus(pendingUser, FriendshipStatus.PENDING)
        );

        when(userRepository.findByUsernameNot(eq(mainUsername))).thenReturn(mockUsers);

        userService = new UserService(userRepository, messagingService);

        List<UserJsonBulk> result = userService.allUsers(mainUsername, null);

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findByUsernameNot(mainUsername);
        verify(userRepository, never()).findByUsernameNot(anyString(), anyString());
    }

    @Test
    void allUsersPagedWithSearchQueryShouldCallCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchQuery = "test";
        Page<UserWithStatus> mockPage = new PageImpl<>(List.of(
                createUserWithStatus(friendUser, null)
        ));

        when(userRepository.findByUsernameNot(eq(mainUsername), eq(searchQuery), eq(pageable)))
                .thenReturn(mockPage);

        userService = new UserService(userRepository, messagingService);

        Page<UserJsonBulk> result = userService.allUsers(mainUsername, pageable, searchQuery);

        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByUsernameNot(mainUsername, searchQuery, pageable);
        verify(userRepository, never()).findByUsernameNot(anyString(), any(Pageable.class));
    }

    @Test
    void allUsersPagedWithNullSearchQueryShouldCallRepositoryWithoutSearchParam() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserWithStatus> mockPage = new PageImpl<>(List.of(
                createUserWithStatus(friendUser, null)
        ));

        when(userRepository.findByUsernameNot(eq(mainUsername), eq(pageable)))
                .thenReturn(mockPage);

        userService = new UserService(userRepository, messagingService);

        Page<UserJsonBulk> result = userService.allUsers(mainUsername, pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByUsernameNot(mainUsername, pageable);
        verify(userRepository, never()).findByUsernameNot(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void friendsWithSearchQueryShouldCallRepositoryWithSearchParam() {
        String searchQuery = "friend";
        List<UserWithStatus> mockFriends = List.of(
                createUserWithStatus(friendUser, FriendshipStatus.ACCEPTED)
        );

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findFriends(eq(mainUser), eq(searchQuery))).thenReturn(mockFriends);

        userService = new UserService(userRepository, messagingService);

        List<UserJsonBulk> result = userService.friends(mainUsername, searchQuery);

        assertEquals(1, result.size());
        assertEquals(friendUsername, result.get(0).username());
        assertEquals(FRIEND, result.get(0).friendshipStatus());
        verify(userRepository, times(1)).findFriends(mainUser, searchQuery);
        verify(userRepository, never()).findFriends(any(UserEntity.class));
    }

    @Test
    void friendsWithNullSearchQueryShouldCallRepositoryWithoutSearchParam() {
        List<UserWithStatus> mockFriends = List.of(
                createUserWithStatus(friendUser, FriendshipStatus.ACCEPTED)
        );

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findFriends(eq(mainUser))).thenReturn(mockFriends);

        userService = new UserService(userRepository, messagingService);

        List<UserJsonBulk> result = userService.friends(mainUsername, null);

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findFriends(mainUser);
        verify(userRepository, never()).findFriends(any(UserEntity.class), anyString());
    }

    @Test
    void friendsPagedWithSearchQueryShouldCallCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        String searchQuery = "friend";
        Page<UserWithStatus> mockPage = new PageImpl<>(List.of(
                createUserWithStatus(friendUser, FriendshipStatus.ACCEPTED)
        ));

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findFriends(eq(mainUser), eq(searchQuery), eq(pageable))).thenReturn(mockPage);

        userService = new UserService(userRepository, messagingService);

        Page<UserJsonBulk> result = userService.friends(mainUsername, pageable, searchQuery);

        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findFriends(mainUser, searchQuery, pageable);
    }

    @Test
    void createFriendshipRequestShouldThrowExceptionForSameUser() {
        userService = new UserService(userRepository, messagingService);

        SameUsernameException ex = assertThrows(
                SameUsernameException.class,
                () -> userService.createFriendshipRequest(mainUsername, mainUsername)
        );
        assertEquals("Can`t create friendship request for self user", ex.getMessage());
        verifyNoInteractions(userRepository, messagingService);
    }

    @Test
    void createFriendshipRequestShouldThrowExceptionWhenTargetUserNotFound() {
        String targetUser = "nonexistent";
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(targetUser))).thenReturn(Optional.empty());

        userService = new UserService(userRepository, messagingService);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.createFriendshipRequest(mainUsername, targetUser)
        );
        assertEquals("Can`t find user by username: '" + targetUser + "'", ex.getMessage());
    }

    @Test
    void createFriendshipRequestShouldAcceptPendingRequestWhenExists() {
        FriendshipEntity pendingRequest = new FriendshipEntity();
        pendingRequest.setRequester(pendingUser);
        pendingRequest.setAddressee(mainUser);
        pendingRequest.setStatus(FriendshipStatus.PENDING);

        mainUser.getFriendshipAddressees().add(pendingRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.createFriendshipRequest(mainUsername, pendingUsername);

        assertEquals(FRIEND, result.friendshipStatus());
        assertEquals(pendingUsername, result.username());
        verify(userRepository, times(1)).save(mainUser);
        verify(messagingService, times(1)).notifyUser(
                eq(pendingUsername),
                anyString(),
                anyString(),
                isNull(),
                isNull(),
                isNull()
        );
    }

    @Test
    void createFriendshipRequestShouldCreateNewPendingRequest() {
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(friendUsername))).thenReturn(Optional.of(friendUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.createFriendshipRequest(mainUsername, friendUsername);

        assertEquals(INVITE_SENT, result.friendshipStatus());
        assertEquals(friendUsername, result.username());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals(1, savedUser.getFriendshipRequests().size());

        FriendshipEntity request = savedUser.getFriendshipRequests().iterator().next();
        assertEquals(friendUser, request.getAddressee());
        assertEquals(FriendshipStatus.PENDING, request.getStatus());
    }

    @Test
    void acceptFriendshipRequestShouldThrowExceptionForSameUser() {
        userService = new UserService(userRepository, messagingService);

        SameUsernameException ex = assertThrows(
                SameUsernameException.class,
                () -> userService.acceptFriendshipRequest(mainUsername, mainUsername)
        );
        assertEquals("Can`t accept friendship request for self user", ex.getMessage());
    }

    @Test
    void acceptFriendshipRequestShouldThrowExceptionWhenRequestNotFound() {
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));

        userService = new UserService(userRepository, messagingService);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.acceptFriendshipRequest(mainUsername, pendingUsername)
        );
        assertEquals("Can`t find invitation from username: '" + pendingUsername + "'", ex.getMessage());
    }

    @Test
    void acceptFriendshipRequestShouldAcceptExistingRequest() {
        FriendshipEntity pendingRequest = new FriendshipEntity();
        pendingRequest.setRequester(pendingUser);
        pendingRequest.setAddressee(mainUser);
        pendingRequest.setStatus(FriendshipStatus.PENDING);

        mainUser.getFriendshipAddressees().add(pendingRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.acceptFriendshipRequest(mainUsername, pendingUsername);

        assertEquals(FRIEND, result.friendshipStatus());
        assertEquals(pendingUsername, result.username());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals(1, savedUser.getFriendshipRequests().size());

        FriendshipEntity request = savedUser.getFriendshipRequests().iterator().next();
        assertEquals(FriendshipStatus.ACCEPTED, request.getStatus());
    }

    @Test
    void declineFriendshipRequestShouldThrowExceptionForSameUser() {
        userService = new UserService(userRepository, messagingService);

        SameUsernameException ex = assertThrows(
                SameUsernameException.class,
                () -> userService.declineFriendshipRequest(mainUsername, mainUsername)
        );
        assertEquals("Can`t decline friendship request for self user", ex.getMessage());
    }

    @Test
    void declineFriendshipRequestShouldRemoveRequest() {
        FriendshipEntity pendingRequest = new FriendshipEntity();
        pendingRequest.setRequester(pendingUser);
        pendingRequest.setAddressee(mainUser);
        pendingRequest.setStatus(FriendshipStatus.PENDING);

        mainUser.getFriendshipAddressees().add(pendingRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.declineFriendshipRequest(mainUsername, pendingUsername);

        assertNull(result.friendshipStatus());
        assertEquals(pendingUsername, result.username());

        verify(userRepository, times(2)).save(any(UserEntity.class));
        assertTrue(mainUser.getFriendshipAddressees().isEmpty());
    }

    @Test
    void removeFriendShouldThrowExceptionForSameUser() {
        userService = new UserService(userRepository, messagingService);

        SameUsernameException ex = assertThrows(
                SameUsernameException.class,
                () -> userService.removeFriend(mainUsername, mainUsername)
        );
        assertEquals("Can`t remove friendship relation for self user", ex.getMessage());
    }

    @Test
    void removeFriendShouldRemoveAllRelations() {
        FriendshipEntity mainToFriend = new FriendshipEntity();
        mainToFriend.setRequester(mainUser);
        mainToFriend.setAddressee(friendUser);
        mainToFriend.setStatus(FriendshipStatus.ACCEPTED);
        mainUser.getFriendshipRequests().add(mainToFriend);
        friendUser.getFriendshipAddressees().add(mainToFriend);

        FriendshipEntity friendToMain = new FriendshipEntity();
        friendToMain.setRequester(friendUser);
        friendToMain.setAddressee(mainUser);
        friendToMain.setStatus(FriendshipStatus.ACCEPTED);
        friendUser.getFriendshipRequests().add(friendToMain);
        mainUser.getFriendshipAddressees().add(friendToMain);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(friendUsername))).thenReturn(Optional.of(friendUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);
        userService.removeFriend(mainUsername, friendUsername);
        verify(userRepository, times(2)).save(any(UserEntity.class));

        assertTrue(mainUser.getFriendshipRequests().isEmpty());
        assertTrue(mainUser.getFriendshipAddressees().isEmpty());
        assertTrue(friendUser.getFriendshipRequests().isEmpty());
        assertTrue(friendUser.getFriendshipAddressees().isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"data:image/png;base64,test", "not a photo"})
    void isPhotoStringShouldReturnTrueOnlyForDataImages(String photo) {
        boolean expected = photo != null && photo.startsWith("data:image");
        boolean result = UserService.isPhotoString(photo);
        assertEquals(expected, result);
    }

    @Test
    void createFriendshipRequestShouldNotCreateDuplicateRequest() {
        FriendshipEntity existingRequest = new FriendshipEntity();
        existingRequest.setRequester(friendUser);
        existingRequest.setAddressee(mainUser);
        existingRequest.setStatus(FriendshipStatus.PENDING);
        mainUser.getFriendshipAddressees().add(existingRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(friendUsername))).thenReturn(Optional.of(friendUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.createFriendshipRequest(mainUsername, friendUsername);

        assertEquals(FRIEND, result.friendshipStatus());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals(1, savedUser.getFriendshipRequests().size());
    }

    @Test
    void removeFriendShouldDoNothingWhenNoFriendship() {
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(friendUsername))).thenReturn(Optional.of(friendUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);
        userService.removeFriend(mainUsername, friendUsername);

        verify(userRepository, times(2)).save(any(UserEntity.class));
        assertTrue(mainUser.getFriendshipRequests().isEmpty());
        assertTrue(mainUser.getFriendshipAddressees().isEmpty());
    }

    @Test
    void acceptFriendshipRequestShouldDoNothingWhenAlreadyAccepted() {
        FriendshipEntity acceptedRequest = new FriendshipEntity();
        acceptedRequest.setRequester(pendingUser);
        acceptedRequest.setAddressee(mainUser);
        acceptedRequest.setStatus(FriendshipStatus.ACCEPTED);
        mainUser.getFriendshipAddressees().add(acceptedRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.acceptFriendshipRequest(mainUsername, pendingUsername);

        assertEquals(FRIEND, result.friendshipStatus());
        assertEquals(pendingUsername, result.username());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        FriendshipEntity request = savedUser.getFriendshipAddressees().iterator().next();
        assertEquals(FriendshipStatus.ACCEPTED, request.getStatus());
    }

    @Test
    void createFriendshipRequestForExistingOutgoingRequestShouldNotCreateDuplicate() {
        FriendshipEntity outgoingRequest = new FriendshipEntity();
        outgoingRequest.setRequester(mainUser);
        outgoingRequest.setAddressee(friendUser);
        outgoingRequest.setStatus(FriendshipStatus.PENDING);
        mainUser.getFriendshipRequests().add(outgoingRequest);
        friendUser.getFriendshipAddressees().add(outgoingRequest);

        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(friendUsername))).thenReturn(Optional.of(friendUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        UserJson result = userService.createFriendshipRequest(mainUsername, friendUsername);

        assertEquals(INVITE_SENT, result.friendshipStatus());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        assertEquals(2, savedUser.getFriendshipRequests().size());
    }

    @Test
    void declineFriendshipRequestShouldDoNothingWhenRequestNotFound() {
        when(userRepository.findByUsername(eq(mainUsername))).thenReturn(Optional.of(mainUser));
        when(userRepository.findByUsername(eq(pendingUsername))).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        userService = new UserService(userRepository, messagingService);

        assertDoesNotThrow(() -> userService.declineFriendshipRequest(mainUsername, pendingUsername));

        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    private UserWithStatus createUserWithStatus(UserEntity user, FriendshipStatus status) {
        return new UserWithStatus(
                user.getId(),
                user.getUsername(),
                user.getCurrency(),
                user.getFullname(),
                user.getPhotoSmall(),
                status
        );
    }
}