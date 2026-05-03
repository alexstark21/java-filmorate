package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список всех фильмов. Всего фильмов: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Попытка создания фильма: {}", film);
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
