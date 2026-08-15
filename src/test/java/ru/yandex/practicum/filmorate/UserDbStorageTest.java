package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("ivan@email.com");
        user1.setLogin("vanya_cool");
        user1.setName("Ivan");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        testUser1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("petr@email.com");
        user2.setLogin("petya_cool");
        user2.setName("Petr");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        testUser2 = userStorage.create(user2);
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.findById(testUser1.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", testUser1.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("login", "vanya_cool");
                });
    }

    @Test
    public void testFindAllUsers() {
        Collection<User> users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void testUpdateUser() {
        testUser1.setName("Ivan Updated");
        userStorage.update(testUser1);

        Optional<User> updatedUser = userStorage.findById(testUser1.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "Ivan Updated")
                );
    }

    @Test
    public void testDeleteUser() {
        userStorage.delete(testUser1.getId());
        Optional<User> deletedUser = userStorage.findById(testUser1.getId());

        assertThat(deletedUser).isEmpty();
    }

    @Test
    public void testAddAndGetFriends() {
        userStorage.addFriend(testUser1.getId(), testUser2.getId());

        Collection<User> user1Friends = userStorage.getFriends(testUser1.getId());
        Collection<User> user2Friends = userStorage.getFriends(testUser2.getId());

        assertThat(user1Friends.size()).isEqualTo(1);
        assertThat(user2Friends.size()).isEqualTo(0);
    }

    @Test
    public void testDeleteFriendStrictlyOneDirection() {
        userStorage.addFriend(testUser1.getId(), testUser2.getId());
        userStorage.deleteFriend(testUser1.getId(), testUser2.getId());

        Collection<User> friends = userStorage.getFriends(testUser1.getId());
        assertThat(friends.isEmpty()).isTrue();
    }

    @Test
    public void testGetCommonFriends() {
        User commonFriend = new User();
        commonFriend.setEmail("common@email.com");
        commonFriend.setLogin("common");
        commonFriend.setBirthday(LocalDate.of(2000, 10, 10));
        commonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(testUser1.getId(), commonFriend.getId());
        userStorage.addFriend(testUser2.getId(), commonFriend.getId());

        Collection<User> common = userStorage.getCommonFriends(testUser1.getId(), testUser2.getId());

        assertThat(common.size()).isEqualTo(1);
        assertThat(common.iterator().next().getId()).isEqualTo(commonFriend.getId());
    }
}
