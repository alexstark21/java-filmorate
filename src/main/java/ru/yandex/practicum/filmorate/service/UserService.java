package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с id={} и id={} теперь друзья", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи с id={} и id={} удалены из друзей друг у друга", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = findById(userId);
        log.info("Запрошен список друзей пользователя с id={}", userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherFriends = otherUser.getFriends();

        if (userFriends == null || otherFriends == null) {
            log.info("Общих друзей у пользователей с id={} и id={} нет", userId, otherId);
            return Collections.emptyList();
        }

        log.info("Запрошен список общих друзей пользователей с id={} и id={}", userId, otherId);
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }
}
