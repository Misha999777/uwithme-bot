package education.uwithme.bot.telegram;

import com.mborodin.uwm.api.LessonApi;
import com.mborodin.uwm.api.UserApi;
import com.mborodin.uwm.api.enums.FileType;
import com.mborodin.uwm.api.structure.GroupApi;
import education.uwithme.bot.client.EducationAppClient;
import education.uwithme.bot.dto.BotData;
import education.uwithme.bot.dto.Lesson;
import education.uwithme.bot.dto.User;
import education.uwithme.bot.entity.BotUser;
import education.uwithme.bot.repository.BotUserRepository;
import feign.FeignException;
import feign.Response.Body;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final String NO_ELEMENTS_MESSAGE = "Дані відсутні";
    private static final String SELECT_MESSAGE = "Оберіть для подробець";
    private static final String AUTH_MESSAGE = "Будь ласка, авторизуйтеся";
    private static final String AUTHORIZE_BUTTON_NAME = "Авторизуватися";
    private static final String WELCOME_MESSAGE = "Вітаємо";
    private static final String FAIL_MESSAGE = "Ви не є студентом групи";

    @NonNull
    private final TelegramBotsApi telegramBotsApi;
    @NonNull
    private final BotUserRepository botUserRepository;
    @NonNull
    private final EducationAppClient studentsClient;

    @Value("${bot.key}")
    private String key;
    @Value("${bot.name}")
    private String name;
    @Value("${education-app.ui.uri}")
    private String uiUri;
    @Value("${bot.firstWeekStart:#{null}}")
    private String firstWeekStart;

    @PostConstruct
    private void postConstruct() throws TelegramApiException {
        this.telegramBotsApi.registerBot(this);
        log.info("Bot registered");
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return key;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Optional.ofNullable(update.getCallbackQuery())
                .ifPresent(callbackQuery -> processCallback(getChatId(update), callbackQuery));

        Optional.ofNullable(update.getMessage())
                .filter(message -> Objects.equals(message.getText(), "/start"))
                .map(Message::getChatId)
                .ifPresent(this::sendWelcome);
    }

    public void onLoginComplete(Long chatId, String uwmUserId) {
        BotUser botUser = botUserRepository.findById(chatId).orElseThrow();

        try {
            User educationAppUser = studentsClient.getUser(uwmUserId);
            Objects.requireNonNull(educationAppUser);
            Objects.requireNonNull(educationAppUser.getGroup());

            botUser.setUserId(uwmUserId);
            botUserRepository.save(botUser);

            deleteLoginMessages(chatId);
            sendMessageWithText(chatId, WELCOME_MESSAGE + " " + educationAppUser.getFirstName());
            sendMenu(chatId);
        } catch (Exception e) {
            deleteLoginMessages(chatId);
            sendMessageWithText(chatId, FAIL_MESSAGE);
            sendWelcome(chatId);

            log.warn("Can't login:", e);
        }
    }

    private void processCallback(long chatId, CallbackQuery query) {
        Long groupId = getUserGroup(chatId);
        if (Objects.isNull(groupId)) {
            sendWelcome(chatId);
            return;
        }

        answerQuery(query);

        String payload = query.getData();
        Optional<Command> command = Command.fromMessage(payload);

        if (command.isPresent()) {
            processCommand(chatId, command.get());
            return;
        }

        String[] parts = payload.split(" ");
        Optional<Callback> optionalCallback = Callback.fromCallbackText(parts[0]);
        optionalCallback.ifPresent(callback -> processDetail(chatId, parts[1], callback));
    }

    private void answerQuery(CallbackQuery query) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(query.getId())
                .build();

        executeSilent(answer);
    }

    private void processCommand(Long chatId, Command payload) {
        switch (payload) {
            case TIMETABLE -> {
                Map<String, String> days = Arrays.stream(Day.values())
                        .map(Day::getMessage)
                        .collect(LinkedHashMap::new, (map, item) -> map.put(item.getFirst(), item.getSecond()),
                                Map::putAll);
                sendMap(chatId, days);
            }
            case STUDENTS -> sendList(chatId, studentsClient.getStudents(getUserGroup(chatId)));
            case TEACHERS -> sendList(chatId, studentsClient.getTeachers(getUserGroup(chatId)));
            case LECTURES -> sendList(chatId, studentsClient.getFiles(getUserGroup(chatId))
                    .stream()
                    .filter(fileApi -> fileApi.getFileType() == FileType.LECTURE)
                    .collect(Collectors.toList()));
            case TASKS -> sendList(chatId, studentsClient.getFiles(getUserGroup(chatId))
                    .stream()
                    .filter(fileApi -> fileApi.getFileType() == FileType.TASK)
                    .collect(Collectors.toList()));
            case EXIT -> {
                Optional<BotUser> botUser = botUserRepository.findById(chatId);
                if (botUser.isPresent()) {
                    botUserRepository.deleteById(chatId);
                }
                sendWelcome(chatId);
            }
        }
    }

    @SneakyThrows
    private void processDetail(long chatId, String id, Callback callback) {
        switch (callback) {
            case STUDENT, TEACHER -> sendMessageWithText(chatId, studentsClient.getUser(id).getData());
            case DAY -> sendMessageWithText(chatId, studentsClient.getLessons(getUserGroup(chatId))
                    .stream()
                    .filter(lesson -> Objects.equals(lesson.getWeekDay(), Long.parseLong(id)))
                    .filter(lesson -> Objects.equals(lesson.getWeekNumber(), getWeek()))
                    .sorted(Comparator.comparingLong(LessonApi::getLessonTime))
                    .map(Lesson::getData)
                    .collect(Collectors.joining(System.lineSeparator())));
            case LECTURE, TASK -> {
                try (Body body = studentsClient.getFile(Long.parseLong(id)).body()) {
                    sendFile(chatId, new InputFile(body.asInputStream(), id));
                }
            }
        }
    }

    private void sendWelcome(Long chatId) {
        Long groupId = getUserGroup(chatId);

        if (Objects.isNull(groupId)) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(AUTHORIZE_BUTTON_NAME)
                    .loginUrl(LoginUrl.builder()
                            .url(uiUri)
                            .build())
                    .build();
            List<List<InlineKeyboardButton>> keyboard = List.of(List.of(button));
            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                    .keyboard(keyboard)
                    .build();

            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(AUTH_MESSAGE)
                    .replyMarkup(markup)
                    .build();
            Integer loginMessageId = executeSilent(message);

            BotUser botUser = botUserRepository.findById(chatId)
                    .orElse(new BotUser());

            botUser.getLoginMessageIds().add(loginMessageId);
            botUser.setChatId(chatId);

            botUserRepository.save(botUser);
        } else {
            sendMenu(chatId);
        }
    }

    private void sendMenu(Long chatId) {
        Map<String, String> commands = Arrays.stream(Command.values())
                .map(Command::getMessage)
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getFirst(), item.getSecond()), Map::putAll);

        sendMap(chatId, commands);
    }

    private void sendMap(Long chatId, Map<String, String> map) {
        final AtomicInteger counter = new AtomicInteger();

        Collection<List<InlineKeyboardButton>> keyboard = map.entrySet()
                .stream()
                .map(command -> InlineKeyboardButton.builder()
                        .text(command.getKey())
                        .callbackData(command.getValue())
                        .build())
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 2))
                .values();

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();

        sendMessageWithMarkup(chatId, markup);
    }

    private void sendList(Long chatId, List<? extends BotData> list) {
        if (list.isEmpty()) {
            sendMessageWithText(chatId, null);
        } else {
            sendMessageWithMarkup(chatId, createMarkup(list));
        }
    }

    private void sendMessageWithText(Long chatId, String info) {
        if (!StringUtils.hasText(info)) {
            info = NO_ELEMENTS_MESSAGE;
        }

        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(info)
                .build();

        executeSilent(message);
    }

    private void sendFile(Long chatId, InputFile file) {
        SendDocument message = SendDocument.builder()
                .chatId(chatId.toString())
                .document(file)
                .build();

        executeSilent(message);
    }

    private void sendMessageWithMarkup(Long chatId, InlineKeyboardMarkup markup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(TelegramBot.SELECT_MESSAGE)
                .replyMarkup(markup)
                .build();

        executeSilent(message);
    }

    private void deleteLoginMessages(Long chatId) {
        BotUser botUser = botUserRepository.findById(chatId)
                .orElseThrow();

        botUser.getLoginMessageIds()
                .stream()
                .map(messageId -> buildDeleteMessage(chatId, messageId))
                .forEach(this::executeSilent);

        botUser.getLoginMessageIds().clear();

        botUserRepository.save(botUser);
    }

    private DeleteMessage buildDeleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();

        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(messageId);

        return deleteMessage;
    }

    private InlineKeyboardMarkup createMarkup(Collection<? extends BotData> collection) {
        List<List<InlineKeyboardButton>> keyboard = collection.stream()
                        .map(element -> InlineKeyboardButton.builder()
                                .text(element.getDisplayName())
                                .callbackData(element.getCallbackName() + " " + element.getDataId())
                                .build())
                        .map(List::of)
                        .collect(Collectors.toList());

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    private Long getUserGroup(Long chatId) {
        Optional<String> userId = botUserRepository.findById(chatId)
                .map(BotUser::getUserId);
        if (userId.isEmpty()) {
            return null;
        }

        try {
            return Optional.of(studentsClient.getUser(userId.get()))
                    .map(UserApi::getGroup)
                    .map(GroupApi::getId)
                    .orElse(null);
        } catch (FeignException.NotFound ignored) {
            return null;
        }
    }

    private Long getWeek() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        String dateStr = firstWeekStart != null ? firstWeekStart : now.format(formatter);
        LocalDate date = LocalDate.parse(dateStr, formatter);

        int weeks = Period.between(date, now).getDays() / 7;
        return weeks % 2 == 0 ? 1L : 2L;
    }

    private Integer executeSilent(SendMessage method) {
        try {
            return this.execute(method).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Cant send message {}", method, e);
            return null;
        }
    }

    private void executeSilent(AnswerCallbackQuery method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant answer callback {}", method, e);
        }
    }

    private void executeSilent(DeleteMessage method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant delete message {}", method, e);
        }
    }

    private void executeSilent(SendDocument method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant send document {}", method, e);
        }
    }

    private Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }

        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }

        throw new RuntimeException("Can't get chat id");
    }
}
