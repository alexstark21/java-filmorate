package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.validation.OnCreate;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldHaveErrorsWhenFilmFieldsAreNull() {
        Film film = new Film();
        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        assertFalse(violations.isEmpty());
        System.out.println(violations);
        assertEquals(3, violations.size());
    }

    @Test
    void shouldHaveErrorWhenNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        boolean hasNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassWhenDescriptionIs200Symbols() {
        Film film = new Film();
        film.setName("Title");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        film.setMpa(new Mpa(1, "G"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldHaveErrorWhenDescriptionIs201Symbols() {
        Film film = new Film();
        film.setName("Title");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        boolean hasDescriptionError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
        assertTrue(hasDescriptionError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldHaveErrorWhenReleaseDateIsBeforeCinemaBirthday() {
        Film film = new Film();
        film.setName("Title");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        boolean hasReleaseDateError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
        assertTrue(hasReleaseDateError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldHaveErrorWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Title");
        film.setDuration(-1);
        film.setReleaseDate(LocalDate.now());

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        boolean hasDurationError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
        assertTrue(hasDurationError);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldHaveErrorWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Title");
        film.setDuration(0);
        film.setReleaseDate(LocalDate.now());

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);

        boolean hasDurationError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
        assertTrue(hasDurationError);
        assertFalse(violations.isEmpty());
    }
}
