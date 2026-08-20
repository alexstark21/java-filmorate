package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;


@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей. Всего пользователей: {}", users.size());
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        log.debug("Попытка обновления пользователя: {}", newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            users.put(oldUser.getId(), oldUser);
            log.info("Обновлен пользователь: {}", oldUser);
            return oldUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        users.remove(id);
        log.info("Удален пользователь с id = {}", id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        findById(friendId).orElseThrow(() -> new NotFoundException("Друг не найден"));

        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        log.info("Пользователь id = {} отправил запрос на дружбу пользователю id = {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        User user = findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        findById(friendId).orElseThrow(() -> new NotFoundException("Друг не найден"));

        user.getFriends().remove(friendId);
        log.info("Пользователь с id = {} удалил из подписок пользователя с id = {}", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        User user = findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Запрошен список друзей пользователя с id = {}", userId);

        return user.getFriends().keySet().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User otherUser = findById(otherId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Set<Long> userFriends = user.getFriends().keySet();
        Set<Long> otherFriends = otherUser.getFriends().keySet();

        if (userFriends.isEmpty() || otherFriends.isEmpty()) {
            log.info("Общих друзей у пользователей с id = {} и id = {} нет", userId, otherId);
            return Collections.emptyList();
        }

        log.info("Запрошен список общих друзей пользователей с id = {} и id = {}", userId, otherId);
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id: {}", newId);
        return newId;
    }
}
