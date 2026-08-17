package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.AfterCinemaBirthday;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    @NotNull(groups = OnUpdate.class, message = "Id должен быть указан при обновлении")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Название не может быть пустым при создании")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(groups = OnCreate.class, message = "Дата релиза обязательна при создании")
    @AfterCinemaBirthday
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private long duration;

    private final Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new LinkedHashSet<>();

    private Mpa mpa;
}
