package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT id, name FROM mpa_ratings ORDER BY id ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getInt("id"), rs.getString("name"))
        );
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Mpa(rs.getInt("id"), rs.getString("name")), id)
                .stream()
                .findFirst();
    }
}
