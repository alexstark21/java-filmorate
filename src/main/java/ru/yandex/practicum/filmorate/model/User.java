package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    @NotNull(groups = OnUpdate.class, message = "Id должен быть указан при обновлении")
    private Long id;

    @Email(message = "Электронная почта должна содержать символ @")
    @NotBlank(groups = OnCreate.class, message = "Электронная почта не может быть пустой")
    private String email;

    @NotBlank(groups = OnCreate.class, message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(groups = OnCreate.class, message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private final Set<Long> friends = new HashSet<>();
}
