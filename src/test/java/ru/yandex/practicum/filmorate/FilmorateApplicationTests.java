package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        User user = new User();
        user.setEmail("invalid_email");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(10));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLoginHasSpaces() throws Exception {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("my login"); // Пробел (если в модели @Pattern или кастомная валидация)
        user.setBirthday(LocalDate.now().minusYears(10));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() throws Exception {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("cool_user");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.now().minusYears(20));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("cool_user"));
    }

    @Test
    void shouldReturnBadRequestWhenFilmNameIsEmpty() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionIsTooLong() throws Exception {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNegative() throws Exception {
        Film film = new Film();
        film.setName("Film Name");
        film.setDuration(-1); // @Positive

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenReleaseDateIsTooEarly() throws Exception {
        Film film = new Film();
        film.setName("Old Film");
        film.setReleaseDate(LocalDate.of(1800, 1, 1)); // Слишком рано
        film.setDuration(100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

}
