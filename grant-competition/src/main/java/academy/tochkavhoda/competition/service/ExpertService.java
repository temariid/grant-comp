package academy.tochkavhoda.competition.service;

import academy.tochkavhoda.competition.dao.DataBase;
import academy.tochkavhoda.competition.dto.request.LoginDtoRequest;
import academy.tochkavhoda.competition.dto.request.RateApplicationDtoRequest;
import academy.tochkavhoda.competition.dto.request.RegisterExpertDtoRequest;
import academy.tochkavhoda.competition.dto.response.LoginDtoResponse;
import academy.tochkavhoda.competition.model.Application;
import academy.tochkavhoda.competition.model.Expert;
import academy.tochkavhoda.competition.model.UserSession;
import academy.tochkavhoda.competition.server.ServerResponse;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExpertService {
    private final DataBase dataBase;
    private final Gson gson = new Gson();

    public ExpertService(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public ServerResponse registerExpert(String requestJsonString) {
        try {
            RegisterExpertDtoRequest request = gson.fromJson(requestJsonString, RegisterExpertDtoRequest.class);
            validateRegisterRequest(request);

            if (dataBase.findParticipantByLogin(request.getLogin()).isPresent() ||
                dataBase.findExpertByLogin(request.getLogin()).isPresent()) {
                return ServerResponse.error("Этот логин уже используется");
            }

            Set<String> directions = request.getDirections().stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            Expert expert = new Expert(
                    0,
                    request.getFirstName().trim(),
                    request.getLastName().trim(),
                    directions,
                    request.getLogin().trim(),
                    request.getPassword()
            );
            dataBase.addExpert(expert);
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse login(String requestJsonString) {
        try {
            LoginDtoRequest request = gson.fromJson(requestJsonString, LoginDtoRequest.class);
            Validation.validateLogin(request.getLogin());
            Validation.validateNotEmpty(request.getPassword(), "Пароль");

            Expert expert = dataBase.findExpertByLogin(request.getLogin().trim())
                    .orElse(null);
            if (expert == null || !expert.getPassword().equals(request.getPassword())) {
                return ServerResponse.error("Неверный логин или пароль");
            }

            String token = UUID.randomUUID().toString();
            UserSession session = new UserSession(token, expert.getId(), UserSession.UserType.EXPERT);
            dataBase.addSession(session);

            LoginDtoResponse response = new LoginDtoResponse(token);
            return ServerResponse.success(gson.toJson(response));
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse logout(String token) {
        try {
            getExpertSession(token);
            dataBase.removeSession(token);
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    public ServerResponse leave(String token) {
        try {
            UserSession session = getExpertSession(token);
            dataBase.deleteExpert(session.getUserId());
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    public ServerResponse rateApplication(String token, String requestJsonString) {
        try {
            if (dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Конкурс закрыт, оценка заявок невозможна");
            }
            UserSession session = getExpertSession(token);
            RateApplicationDtoRequest request = gson.fromJson(requestJsonString, RateApplicationDtoRequest.class);
            Validation.validateScore(request.getScore());

            Application app = dataBase.findApplicationById(request.getApplicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

            Expert expert = dataBase.findExpertById(session.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Эксперт не найден"));

            // Check if expert can rate this application (has common directions)
            boolean canRate = app.getDirections().stream()
                    .anyMatch(d -> expert.getDirections().stream()
                            .anyMatch(ed -> ed.equalsIgnoreCase(d)));
            if (!canRate) {
                return ServerResponse.error("Вы не можете оценивать заявки по данным направлениям");
            }

            app.addOrUpdateRating(expert.getId(), request.getScore());
            expert.addRatedApplicationId(app.getId());
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse deleteRating(String token, String requestJsonString) {
        try {
            if (dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Конкурс закрыт, удаление оценок невозможно");
            }
            UserSession session = getExpertSession(token);
            var map = gson.fromJson(requestJsonString, java.util.Map.class);
            int appId = ((Number) map.get("applicationId")).intValue();

            Application app = dataBase.findApplicationById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

            if (!app.getRatings().containsKey(session.getUserId())) {
                return ServerResponse.error("Вы не ставили оценку этой заявке");
            }

            app.removeRating(session.getUserId());
            dataBase.findExpertById(session.getUserId()).ifPresent(e -> e.removeRatedApplicationId(appId));
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse getApplicationsByDirections(String token, String requestJsonString) {
        try {
            UserSession session = getExpertSession(token);
            Expert expert = dataBase.findExpertById(session.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Эксперт не найден"));

            Set<String> filterDirections;
            if (requestJsonString != null && !requestJsonString.isBlank() && !requestJsonString.equals("{}")) {
                var map = gson.fromJson(requestJsonString, java.util.Map.class);
                if (map.containsKey("directions")) {
                    @SuppressWarnings("unchecked")
                    List<String> dirs = (List<String>) map.get("directions");
                    filterDirections = new HashSet<>(dirs);
                    // Restrict to expert's directions
                    filterDirections.retainAll(expert.getDirections());
                } else {
                    filterDirections = expert.getDirections();
                }
            } else {
                filterDirections = expert.getDirections();
            }

            List<Application> apps = dataBase.findApplicationsByDirections(filterDirections);
            return ServerResponse.success(gson.toJson(apps));
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse getRatedApplications(String token) {
        try {
            UserSession session = getExpertSession(token);
            Expert expert = dataBase.findExpertById(session.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Эксперт не найден"));

            List<Application> apps = expert.getRatedApplicationIds().stream()
                    .map(id -> dataBase.findApplicationById(id).orElse(null))
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
            return ServerResponse.success(gson.toJson(apps));
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    private UserSession getExpertSession(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Токен не указан");
        }
        UserSession session = dataBase.findSessionByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Неверный или устаревший токен"));
        if (session.getUserType() != UserSession.UserType.EXPERT) {
            throw new IllegalArgumentException("Операция доступна только экспертам");
        }
        return session;
    }

    private void validateRegisterRequest(RegisterExpertDtoRequest request) {
        Validation.validateMinLength(request.getFirstName(), Validation.MIN_NAME_LENGTH, "Имя");
        Validation.validateMinLength(request.getLastName(), Validation.MIN_NAME_LENGTH, "Фамилия");
        if (request.getDirections() == null || request.getDirections().isEmpty()) {
            throw new IllegalArgumentException("Необходимо указать хотя бы одно направление");
        }
        Validation.validateLogin(request.getLogin());
        Validation.validatePassword(request.getPassword());
    }
}
