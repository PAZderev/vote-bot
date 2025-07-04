package com.example.votebot.bot;

import com.example.votebot.model.*;
import com.example.votebot.repository.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService {

    /* ---------- константы ---------- */
    private static final String ADMIN_USERNAME = "minik9I";
    private static final List<String> VOTE_LABELS = List.of("Да", "Нет", "Воздержусь");
    private static final String ADD_BTN = "➕ Добавить кандидата";

    /* ---------- зависимости ---------- */
    private final BotConfig            config;
    private final UserRepository       userRepo;
    private final CandidateRepository  candRepo;
    private final VoteOptionRepository optRepo;
    private final VoteRepository       voteRepo;

    /* ---------- состояние ---------- */
    /** кто за какого кандидата голосует прямо сейчас */
    private final Map<Long, Integer> currentCand = new ConcurrentHashMap<>();

    private TelegramBot bot;

    /* ---------- инициализация ---------- */
    @PostConstruct
    public void init() {
        bot = new TelegramBot(config.telegramToken());
        log.info("Бот запущен");

        bot.setUpdatesListener(updates -> {
            for (Update upd : updates) {
                try {
                    if (upd.message() != null && upd.message().text() != null) {
                        handleText(upd);
                    }
                } catch (Exception ex) {
                    log.error("Ошибка обработки update", ex);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @PreDestroy
    public void shutdown() {
        if (bot != null) bot.removeGetUpdatesListener();
    }

    /* ===================================================================== */
    /* ================== входящие текстовые сообщения ===================== */
    private void handleText(Update upd) {
        String txt   = upd.message().text().trim();
        User   from  = upd.message().from();
        Long   uid   = from.id();
        Long   chat  = upd.message().chat().id();

        MDC.put("tgChatId", String.valueOf(chat));
        MDC.put("userId",   String.valueOf(uid));

        /* -------- /start -------- */
        if ("/start".equalsIgnoreCase(txt)) {
            registerUser(from);
            bot.execute(new SendMessage(chat,
                "Добро пожаловать, " + from.firstName() + "!"));
            sendNextCandidate(uid, chat, from.username());
            return;
        }

        /* -------- /add <имя> (только админ) -------- */
        if (txt.startsWith("/add ") && ADMIN_USERNAME.equals(from.username())) {
            String name = txt.substring(5).trim();
            if (name.isEmpty()) {
                bot.execute(new SendMessage(chat, "Укажите имя после /add"));
                return;
            }
            candRepo.save(new Candidate(null, name, null));
            bot.execute(new SendMessage(chat, "Кандидат «" + name + "» добавлен."));
            return;
        }

        /* -------- нажатие кнопки “Добавить кандидата” (админ) -------- */
        if (ADD_BTN.equals(txt) && ADMIN_USERNAME.equals(from.username())) {
            bot.execute(new SendMessage(chat, "Пришлите /add <Имя кандидата>"));
            return;
        }

        /* -------- обработка голоса -------- */
        if (VOTE_LABELS.contains(txt)) {
            Integer candId = currentCand.get(uid);
            if (candId == null) {
                bot.execute(new SendMessage(chat, "Сначала введите /start"));
                return;
            }
            saveVote(uid, candId, txt, chat);
            sendNextCandidate(uid, chat, from.username());
            return;
        }

        /* -------- неизвестная команда -------- */
        bot.execute(new SendMessage(chat,
            "Команда не распознана. Используйте /start или /add (для админа)."));
    }

    /* ===================================================================== */
    /* ========================== сохранение голоса ======================== */
    private void saveVote(Long uid, Integer candId, String label, Long chat) {
        String code = switch (label) {
            case "Да"          -> "Y";
            case "Нет"         -> "N";
            default            -> "A";
        };

        Vote v = new Vote(new VoteId(uid, candId),
            userRepo.getReferenceById(uid),
            candRepo.getReferenceById(candId),
            optRepo.getReferenceById(code),
            OffsetDateTime.now());
        try {
            voteRepo.save(v);
            bot.execute(new SendMessage(chat, "Ваш голос учтён 👍"));
        } catch (DataIntegrityViolationException e) {
            bot.execute(new SendMessage(chat, "Вы уже голосовали за этого кандидата."));
        }
    }

    /* ===================================================================== */
    /* ============= отправляем кандидата + reply-клавиатуру =============== */
    private void sendNextCandidate(Long uid, Long chat, String username) {

        Candidate cand = candRepo
            .findUnvoted(uid, PageRequest.of(0, 1))
            .stream().findFirst().orElse(null);

        if (cand == null) {
            currentCand.remove(uid);
            bot.execute(new SendMessage(chat,
                "Вы проголосовали за всех кандидатов. Спасибо!"));
            return;
        }
        currentCand.put(uid, cand.getId());

        /* формируем клавиатуру */
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Да", "Нет", "Воздержусь"});
        if (ADMIN_USERNAME.equals(username)) {
            rows.add(new String[]{ADD_BTN});
        }
        ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup(rows.toArray(String[][]::new))
            .resizeKeyboard(true)
            .oneTimeKeyboard(false);

        bot.execute(new SendMessage(chat,
            "едет ли: *" + cand.getName() + "*")
            .parseMode(ParseMode.Markdown)
            .replyMarkup(kb));
    }

    /* ===================================================================== */
    /* ==================== регистрация пользователя ======================= */
    private void registerUser(User tgUser) {
        TelegramUser u = userRepo.findById(tgUser.id()).orElseGet(TelegramUser::new);
        u.setTelegramId(tgUser.id());
        u.setUsername(tgUser.username());
        userRepo.save(u);
    }
}
