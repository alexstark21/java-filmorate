package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    //private static final int DESCRIPTION-MAX-LENGTH = 200;
    private static final LocalDate THE_BIRTHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список всех фильмов. Всего фильмов: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Попытка создания фильма: {}", film);
        validateDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан фильм с id={}: {}", film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("Попытка обновления фильма: {}", newFilm);
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {

            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null) {
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null) {
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                validateDate(newFilm);
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() != 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            films.put(newFilm.getId(), oldFilm);
            log.info("Обновлен фильм с id={}: {}", oldFilm.getId(), oldFilm);
            return oldFilm;
        }
        log.warn("Фильм с id = {} не найден при попытке обновления", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(THE_BIRTHDAY_OF_CINEMA)) {
            log.warn("Дата релиза {} раньше дня рождения кино {}. Фильм: {}",
                    film.getReleaseDate(), THE_BIRTHDAY_OF_CINEMA, film);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года;");
        }
    }

    /*
        private void validate(Film film) {
            if (film.getName() == null || film.getName().isBlank()) {
                log.warn("Название фильма пустое. Фильм: {}", film);
                throw new ValidationException("Название не может быть пустым");
            }

            if (film.getDescription() != null && film.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
                log.warn("Превышена длина описания ({} > {}). Фильм: {}",
                        film.getDescription().length(), DESCRIPTION-MAX-LENGTH, film);
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }

            if (film.getReleaseDate().isBefore(THE_BIRTHDAY_OF_CINEMA)) {
                log.warn("Дата релиза {} раньше дня рождения кино {}. Фильм: {}",
                        film.getReleaseDate(), THE_BIRTHDAY_OF_CINEMA, film);
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года;");
            }

            if (film.getDuration() < 0) {
                log.warn("Отрицательная продолжительность {}. Фильм: {}", film.getDuration(), film);
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
        }
    */
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый id: {}", newId);
        return newId;
    }
}
