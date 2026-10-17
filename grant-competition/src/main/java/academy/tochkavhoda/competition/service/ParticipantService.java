package academy.tochkavhoda.competition.service;

import academy.tochkavhoda.competition.dao.DataBase;
import academy.tochkavhoda.competition.dto.request.AddApplicationDtoRequest;
import academy.tochkavhoda.competition.dto.request.LoginDtoRequest;
import academy.tochkavhoda.competition.dto.request.RegisterParticipantDtoRequest;
import academy.tochkavhoda.competition.dto.response.LoginDtoResponse;
import academy.tochkavhoda.competition.model.Application;
import academy.tochkavhoda.competition.model.Participant;
import academy.tochkavhoda.competition.model.UserSession;
import academy.tochkavhoda.competition.server.ServerResponse;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ParticipantService {
    private final DataBase dataBase;
    private final Gson gson = new Gson();

    public ParticipantService(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public ServerResponse registerParticipant(String requestJsonString) {
        try {
            RegisterParticipantDtoRequest request = gson.fromJson(requestJsonString, RegisterParticipantDtoRequest.class);
            validateRegisterRequest(request);

            if (dataBase.findParticipantByLogin(request.getLogin()).isPresent() ||
                dataBase.findExpertByLogin(request.getLogin()).isPresent()) {
                return ServerResponse.error("Этот логин уже используется");
            }

            Participant participant = new Participant(
                    0,
                    request.getFirstName().trim(),
                    request.getLastName().trim(),
                    request.getCompanyName().trim(),
                    request.getLogin().trim(),
                    request.getPassword()
            );
            dataBase.addParticipant(participant);
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

            Participant participant = dataBase.findParticipantByLogin(request.getLogin().trim())
                    .orElse(null);
            if (participant == null || !participant.getPassword().equals(request.getPassword())) {
                return ServerResponse.error("Неверный логин или пароль");
            }

            String token = UUID.randomUUID().toString();
            UserSession session = new UserSession(token, participant.getId(), UserSession.UserType.PARTICIPANT);
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
            UserSession session = getParticipantSession(token);
            dataBase.removeSession(token);
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    public ServerResponse leave(String token) {
        try {
            UserSession session = getParticipantSession(token);
            dataBase.deleteParticipant(session.getUserId());
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    public ServerResponse addApplication(String token, String requestJsonString) {
        try {
            if (dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Конкурс закрыт, подача заявок невозможна");
            }
            UserSession session = getParticipantSession(token);
            AddApplicationDtoRequest request = gson.fromJson(requestJsonString, AddApplicationDtoRequest.class);
            validateApplicationRequest(request);

            Application application = new Application(
                    0,
                    request.getTitle().trim(),
                    request.getDescription().trim(),
                    new HashSet<>(request.getDirections()),
                    request.getAmount(),
                    session.getUserId()
            );
            dataBase.addApplication(application);
            return ServerResponse.success("{\"id\":" + application.getId() + "}");
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse cancelApplication(String token, String requestJsonString) {
        try {
            if (dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Конкурс закрыт, отмена заявок невозможна");
            }
            UserSession session = getParticipantSession(token);
            // expect {"applicationId": N}
            var map = gson.fromJson(requestJsonString, java.util.Map.class);
            int appId = ((Number) map.get("applicationId")).intValue();

            Application app = dataBase.findApplicationById(appId)
                    .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
            if (app.getParticipantId() != session.getUserId()) {
                return ServerResponse.error("Вы можете отменить только свои заявки");
            }
            dataBase.deleteApplication(appId);
            return ServerResponse.success();
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse getMyApplications(String token) {
        try {
            UserSession session = getParticipantSession(token);
            List<Application> apps = dataBase.findApplicationsByParticipantId(session.getUserId());
            return ServerResponse.success(gson.toJson(apps));
        } catch (IllegalArgumentException e) {
            return ServerResponse.error(e.getMessage());
        }
    }

    private UserSession getParticipantSession(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Токен не указан");
        }
        UserSession session = dataBase.findSessionByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Неверный или устаревший токен"));
        if (session.getUserType() != UserSession.UserType.PARTICIPANT) {
            throw new IllegalArgumentException("Операция доступна только участникам");
        }
        return session;
    }

    private void validateRegisterRequest(RegisterParticipantDtoRequest request) {
        Validation.validateMinLength(request.getFirstName(), Validation.MIN_NAME_LENGTH, "Имя");
        Validation.validateMinLength(request.getLastName(), Validation.MIN_NAME_LENGTH, "Фамилия");
        Validation.validateMinLength(request.getCompanyName(), Validation.MIN_COMPANY_LENGTH, "Название фирмы");
        Validation.validateLogin(request.getLogin());
        Validation.validatePassword(request.getPassword());
    }

    private void validateApplicationRequest(AddApplicationDtoRequest request) {
        Validation.validateMinLength(request.getTitle(), Validation.MIN_TITLE_LENGTH, "Название заявки");
        Validation.validateMinLength(request.getDescription(), Validation.MIN_DESCRIPTION_LENGTH, "Описание заявки");
        if (request.getDirections() == null || request.getDirections().isEmpty()) {
            throw new IllegalArgumentException("Необходимо указать хотя бы одно направление");
        }
        Validation.validateAmount(request.getAmount());
    }
}
