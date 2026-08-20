package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        validateMpaExists(film.getMpa());
        validateGenresExist(film.getGenres());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenres(film);
        log.info("Фильм успешно сохранен в БД с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating_id = ? WHERE id = ?";

        findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        validateMpaExists(film.getMpa());
        validateGenresExist(film.getGenres());

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());
        saveGenres(film);

        log.info("Фильм с id={} успешно обновлен в БД", film.getId());
        return film;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            log.warn("Фильм с id = {} не найден для удаления", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.info("Фильм с id = {} успешно удален из БД", id);
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return Optional.of(films.getFirst());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, filmId, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Лайк от пользователя с id = " + userId + " не найден");
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS like_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN likes AS l ON f.id = l.film_id " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        loadGenresForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Genre> uniqueGenres = new ArrayList<>(film.getGenres());

        jdbcTemplate.batchUpdate(sql, uniqueGenres, uniqueGenres.size(), (PreparedStatement ps, Genre genre) -> {
            ps.setLong(1, film.getId());
            ps.setInt(2, genre.getId());
        });
    }

    private void validateMpaExists(Mpa mpa) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpa.getId());

        if (count == null || count == 0) {
            throw new NotFoundException("Нет рейтинга с таким id: " + mpa.getId());
        }
    }

    private void validateGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (genreIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM genres WHERE id IN (" + placeholders + ")";

        Object[] args = genreIds.toArray();

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);

        if (count == null || count < genreIds.size()) {
            throw new NotFoundException("Один или несколько указанных жанров не существуют");
        }
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
            film.setGenres(new LinkedHashSet<>());
        }

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = String.format(
                "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                        "FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.id " +
                        "WHERE fg.film_id IN (%s) " +
                        "ORDER BY g.id ASC", inSql);

        Object[] ids = films.stream().map(Film::getId).toArray(Object[]::new);

        jdbcTemplate.query(sql, ids, (ResultSet rs) -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getGenres().add(genre);
            }
        });
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            filmMap.put(film.getId(), film);
            film.getLikes().clear();
        }

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = String.format("SELECT film_id, user_id FROM likes WHERE film_id IN (%s)", inSql);

        Object[] ids = films.stream().map(Film::getId).toArray();

        jdbcTemplate.query(sql, (ResultSet rs) -> {
            long filmId = rs.getLong("film_id");
            long userId = rs.getLong("user_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getLikes().add(userId);
            }
        }, ids);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        int mpaId = rs.getInt("mpa_rating_id");
        if (!rs.wasNull()) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
        }
        return film;
    }
}
