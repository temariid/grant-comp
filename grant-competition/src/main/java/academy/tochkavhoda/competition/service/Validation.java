package academy.tochkavhoda.competition.service;

public class Validation {
    public static final int MIN_LOGIN_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MIN_COMPANY_LENGTH = 2;
    public static final int MIN_TITLE_LENGTH = 3;
    public static final int MIN_DESCRIPTION_LENGTH = 10;

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }

    public static void validateMinLength(String value, int minLength, String fieldName) {
        validateNotEmpty(value, fieldName);
        if (value.trim().length() < minLength) {
            throw new IllegalArgumentException(fieldName + " должен содержать не менее " + minLength + " символов");
        }
    }

    public static void validatePassword(String password) {
        validateMinLength(password, MIN_PASSWORD_LENGTH, "Пароль");
        // Require at least one special character or digit for simplicity
        boolean hasSpecialOrDigit = password.chars().anyMatch(c -> !Character.isLetter(c));
        if (!hasSpecialOrDigit) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну цифру или спецсимвол");
        }
    }

    public static void validateLogin(String login) {
        validateMinLength(login, MIN_LOGIN_LENGTH, "Логин");
        if (!login.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Логин может содержать только буквы, цифры и знак подчеркивания");
        }
    }

    public static void validateScore(int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Оценка должна быть в диапазоне от 1 до 5");
        }
    }

    public static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма гранта должна быть положительной");
        }
    }
}
