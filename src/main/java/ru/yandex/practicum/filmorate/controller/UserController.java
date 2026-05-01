package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список всех пользователей. Всего пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        //validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            /*
            if (users.values().stream().anyMatch(u -> u.getEmail().equals(newUser.getEmail()))) {
                log.warn("Имейл {} уже используется", newUser.getEmail());
                throw new ValidationException("Имейл " + newUser.getEmail() + "уже используется");
            }
            */
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null) {
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

    /*
        private void validateUser(User user) {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                log.warn("Попытка регистрации с некорректным email: {}", user.getEmail());
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            }

            if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                log.warn("Попытка использовать существующий email: {}", user.getEmail());
                throw new ValidationException("Этот имейл уже используется");
            }

            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                log.warn("Логин не может быть пустым и содержать пробелы");
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }

            if (user.getBirthday() == null) {
                log.warn("Дата рождения не может быть пустой");
                throw new ValidationException("Дата рождения не может быть пустой");
            } else if (user.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Дата рождения не может быть в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }
    */
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
