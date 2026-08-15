package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film testFilm1;
    private Film testFilm2;

    @BeforeEach
    void setUp() {
        Film film1 = new Film();
        film1.setName("Inception");
        film1.setDescription("Dream within a dream");
        film1.setReleaseDate(LocalDate.of(2010, 7, 16));
        film1.setDuration(148);
        film1.setMpa(new Mpa(1, null));

        Set<Genre> genres1 = new LinkedHashSet<>();
        genres1.add(new Genre(1, null));
        film1.setGenres(genres1);
        testFilm1 = filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Interstellar");
        film2.setDescription("Space exploration");
        film2.setReleaseDate(LocalDate.of(2014, 11, 7));
        film2.setDuration(169);
        film2.setMpa(new Mpa(2, null));
        testFilm2 = filmStorage.create(film2);
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.findById(testFilm1.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", testFilm1.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Inception");
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getGenres().size()).isEqualTo(1);
                });
    }

    @Test
    public void testFindAllFilms() {
        Collection<Film> films = filmStorage.findAll();
        assertThat(films.size()).isEqualTo(2);
    }

    @Test
    public void testUpdateFilm() {
        testFilm1.setName("Inception Director's Cut");
        filmStorage.update(testFilm1);

        Optional<Film> updatedFilm = filmStorage.findById(testFilm1.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "Inception Director's Cut")
                );
    }

    @Test
    public void testDeleteFilm() {
        filmStorage.delete(testFilm1.getId());
        Optional<Film> deletedFilm = filmStorage.findById(testFilm1.getId());

        assertThat(deletedFilm).isEmpty();
    }

    @Test
    public void testAddLikeAndGetPopularFilms() {
        User user = new User();
        user.setEmail("liker@email.com");
        user.setLogin("liker");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user = userStorage.create(user);

        filmStorage.addLike(testFilm2.getId(), user.getId());

        Collection<Film> popular = filmStorage.getPopularFilms(10);

        assertThat(popular.size()).isEqualTo(2);
        assertThat(popular.iterator().next().getId()).isEqualTo(testFilm2.getId());
    }

    @Test
    public void testDeleteLike() {
        User user = new User();
        user.setEmail("liker2@email.com");
        user.setLogin("liker2");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user = userStorage.create(user);

        filmStorage.addLike(testFilm1.getId(), user.getId());
        filmStorage.deleteLike(testFilm1.getId(), user.getId());

        Optional<Film> filmOptional = filmStorage.findById(testFilm1.getId());
        assertThat(filmOptional).isPresent();
    }
}
