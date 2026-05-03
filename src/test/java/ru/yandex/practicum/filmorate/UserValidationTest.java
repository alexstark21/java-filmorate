package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private Validator validator;
    private UserController userController;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        userController = new UserController();
    }

    @Test
    void shouldHaveErrorsWhenUserFieldsAreNull() {
        User user = new User();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
    }

    @Test
    void shouldHaveErrorWhenEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailError);
    }

    @Test
    void shouldHaveErrorWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailError);
    }

    @Test
    void shouldHaveErrorWhenLoginIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        boolean hasLoginError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertTrue(hasLoginError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldHaveErrorWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        boolean hasLoginError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertTrue(hasLoginError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldReplaceNameWithLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("cool-login");
        user.setName("");
        user.setBirthday(LocalDate.now().minusYears(20));

        User createdUser = userController.create(user);
        assertEquals("cool-login", createdUser.getName());
    }

    @Test
    void shouldPassWhenBirthdayIsToday() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassWhenBirthdayIsTomorrow() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        boolean hasBirthdayError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday"));
        assertTrue(hasBirthdayError);
        assertFalse(violations.isEmpty());
    }
}
