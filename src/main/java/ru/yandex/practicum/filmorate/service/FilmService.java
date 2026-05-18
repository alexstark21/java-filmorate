package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addLike(Long filmId, Long userId) {
        userService.findById(userId);
        Film film = findById(filmId);

        film.getLikes().add(userId);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userService.findById(userId);
        Film film = findById(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк от пользователя с id=" + userId + " не найден");
        }

        film.getLikes().remove(userId);
        log.info("Пользователь с id={} удалил лайк у фильма с id={}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Запрошено {} наиболее популярных фильмов", count);
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }
}
