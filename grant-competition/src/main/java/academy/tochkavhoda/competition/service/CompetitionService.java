package academy.tochkavhoda.competition.service;

import academy.tochkavhoda.competition.dao.DataBase;
import academy.tochkavhoda.competition.model.Application;
import academy.tochkavhoda.competition.server.ServerResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CompetitionService {
    private final DataBase dataBase;
    private final Gson gson = new Gson();
    private final Random random = new Random();

    public CompetitionService(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public ServerResponse setTotalFund(String token, String requestJsonString) {
        // For simplicity, allow any logged-in user or no token check for admin operations
        // In real system would have admin role. Here we skip strict auth for settings.
        try {
            var map = gson.fromJson(requestJsonString, java.util.Map.class);
            long amount = ((Number) map.get("amount")).longValue();
            if (amount < 0) {
                return ServerResponse.error("Сумма фонда не может быть отрицательной");
            }
            dataBase.setTotalFund(amount);
            return ServerResponse.success();
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse setThreshold(String token, String requestJsonString) {
        try {
            var map = gson.fromJson(requestJsonString, java.util.Map.class);
            double threshold = ((Number) map.get("threshold")).doubleValue();
            if (threshold < 0 || threshold > 5) {
                return ServerResponse.error("Пороговое значение должно быть от 0 до 5");
            }
            dataBase.setThreshold(threshold);
            return ServerResponse.success();
        } catch (Exception e) {
            return ServerResponse.error("Некорректный формат запроса");
        }
    }

    public ServerResponse closeAndCalculateResults(String token) {
        try {
            if (dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Конкурс уже закрыт");
            }
            dataBase.closeCompetition();

            List<Application> allApps = new ArrayList<>(dataBase.getAllApplications());

            // Rank by average rating descending, then by amount ascending, then random
            allApps.sort((a1, a2) -> {
                int cmp = Double.compare(a2.getAverageRating(), a1.getAverageRating());
                if (cmp != 0) return cmp;
                cmp = Long.compare(a1.getAmount(), a2.getAmount());
                if (cmp != 0) return cmp;
                return random.nextInt(3) - 1; // random
            });

            long remaining = dataBase.getTotalFund();
            double threshold = dataBase.getThreshold();
            List<Application> winners = new ArrayList<>();

            for (Application app : allApps) {
                double avg = app.getAverageRating();
                if (avg <= threshold) {
                    // stop when we reach applications with avg <= threshold
                    break;
                }
                if (app.getAmount() <= remaining) {
                    winners.add(app);
                    remaining -= app.getAmount();
                }
                // else skip this application
            }

            dataBase.setWinningApplications(winners);
            return ServerResponse.success(gson.toJson(winners));
        } catch (Exception e) {
            return ServerResponse.error("Ошибка при подведении итогов: " + e.getMessage());
        }
    }

    public ServerResponse getResults(String token) {
        try {
            if (!dataBase.isCompetitionClosed()) {
                return ServerResponse.error("Итоги еще не подведены");
            }
            return ServerResponse.success(gson.toJson(dataBase.getWinningApplications()));
        } catch (Exception e) {
            return ServerResponse.error("Ошибка");
        }
    }

    public ServerResponse getAllParticipants(String token, String requestJsonString) {
        try {
            // Any logged in user can view, or even without for simplicity
            return ServerResponse.success(gson.toJson(dataBase.getAllParticipants()));
        } catch (Exception e) {
            return ServerResponse.error("Ошибка");
        }
    }

    public ServerResponse getAllApplications(String token, String requestJsonString) {
        try {
            return ServerResponse.success(gson.toJson(dataBase.getAllApplications()));
        } catch (Exception e) {
            return ServerResponse.error("Ошибка");
        }
    }
}
