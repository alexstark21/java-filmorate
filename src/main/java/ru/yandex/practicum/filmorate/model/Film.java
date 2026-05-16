package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.AfterCinemaBirthday;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull
    @AfterCinemaBirthday
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private long duration;
}
