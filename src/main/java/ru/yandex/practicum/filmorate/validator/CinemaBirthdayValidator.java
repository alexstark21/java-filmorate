package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class CinemaBirthdayValidator implements ConstraintValidator<AfterCinemaBirthday, LocalDate> {
    private static final LocalDate BIRTHDAY_OF_CINEMA = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !value.isBefore(BIRTHDAY_OF_CINEMA);
    }
}
