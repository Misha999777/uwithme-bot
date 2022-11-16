package education.uwithme.bot.telegram;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import education.uwithme.bot.client.EducationAppClient;
import education.uwithme.bot.dto.educationapp.BotData;
import education.uwithme.bot.dto.educationapp.LessonApi;
import education.uwithme.bot.entity.BotUser;
import education.uwithme.bot.repository.BotUserRepository;
import education.uwithme.bot.util.AuthUtility;
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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppData;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.key}")
    private String key;
    @Value("${bot.name}")
    private String name;
    @Value("${bot.firstWeekStart:#{null}}")
    private String firstWeekStart;

    private static final String NO_ELEMENTS_MESSAGE = "Дані відсутні";
    private static final String SELECT_MESSAGE = "Оберіть для подробець";
    private static final String AUTHORIZE_BUTTON_NAME = "Авторизуватися";
    private static final String WELCOME_MESSAGE = "Вітаємо";
    private static final String FAIL_MESSAGE = "Ви не є студентом групи";

    private final TelegramBotsApi telegramBotsApi;
    private final BotUserRepository botUserRepository;
    private final AuthUtility authUtility;
    private final EducationAppClient studentsClient;

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
                .filter(message -> Objects.nonNull(message.getWebAppData()))
                .ifPresent(message -> onLoginComplete(getChatId(update), message.getWebAppData()));

        Optional.ofNullable(update.getMessage())
                .filter(message -> Objects.equals(message.getText(), "/start"))
                .map(Message::getChatId)
                .ifPresent(this::sendWelcome);
    }

    public void onLoginComplete(Long chatId, WebAppData webAppData) {
        var token = authUtility.getToken(webAppData.getData());

        var botUser = botUserRepository.findById(chatId).orElseThrow();

        try {
            var educationAppUser = studentsClient.getUser(token.getUserId());
            Objects.requireNonNull(educationAppUser);
            Objects.requireNonNull(educationAppUser.getStudyGroupId());

            botUser.setGroupId(educationAppUser.getStudyGroupId());
            botUserRepository.save(botUser);

            deleteLoginMessages(chatId);
            sendMessageWithText(chatId, WELCOME_MESSAGE + " " + educationAppUser.getFirstName());
            sendMenu(chatId);
        } catch (Exception ignored) {
            deleteLoginMessages(chatId);
            sendMessageWithText(chatId, FAIL_MESSAGE);
            sendWelcome(chatId);
        }
    }

    private void processCallback(long chatId, CallbackQuery query) {
        var groupId = botUserRepository.findById(chatId)
                                       .map(BotUser::getGroupId)
                                       .orElse(null);
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
            case TIMETABLE:
                Map<String, String> days = Arrays.stream(Day.values())
                                                 .map(Day::getMessage)
                                                 .collect(LinkedHashMap::new,
                                                          (map, item) -> map.put(item.getFirst(), item.getSecond()),
                                                          Map::putAll);

                sendMap(chatId, days);
                break;
            case STUDENTS:
                sendList(chatId, studentsClient.getStudents(getUserGroup(chatId)));
                break;
            case TEACHERS:
                sendList(chatId, studentsClient.getTeachers(getUserGroup(chatId)));
                break;
            case LECTURES:
                sendList(chatId, studentsClient.getFiles(getUserGroup(chatId)).stream()
                                               .filter(fileApi -> fileApi.getType() == 1)
                                               .collect(Collectors.toList()));
                break;
            case TASKS:
                sendList(chatId, studentsClient.getFiles(getUserGroup(chatId)).stream()
                                               .filter(fileApi -> fileApi.getType() == 0)
                                               .collect(Collectors.toList()));
                break;
            case EXIT:
                var botUser = botUserRepository.findById(chatId);

                if (botUser.isPresent()) {
                    botUserRepository.deleteById(chatId);
                }

                sendWelcome(chatId);
        }
    }

    @SneakyThrows
    private void processDetail(long chatId, String id, Callback callback) {
        switch (callback) {
            case STUDENT:
            case TEACHER:
                sendMessageWithText(chatId, studentsClient.getUser(id).getData());
                break;
            case DAY:
                sendMessageWithText(chatId, studentsClient.getLessons(getUserGroup(chatId))
                                                          .stream()
                                                          .filter(lessonApi -> Objects.equals(lessonApi.getWeekDay(),
                                                                                              Long.parseLong(id)))
                                                          .filter(lesson -> Objects.equals(lesson.getWeekNumber(),
                                                                                           getWeek()))
                                                          .sorted(Comparator.comparingLong(LessonApi::getLessonTime))
                                                          .map(LessonApi::getData)
                                                          .collect(Collectors.joining(System.lineSeparator())));
                break;
            case LECTURE:
            case TASK:
                try (var body = studentsClient.getFile(Long.parseLong(id)).body()){
                    sendFile(chatId, new InputFile(body.asInputStream(), id));
                }
        }
    }

    private void sendWelcome(Long chatId) {
        var groupId = botUserRepository.findById(chatId)
                                       .map(BotUser::getGroupId)
                                       .orElse(null);

        if (Objects.isNull(groupId)) {
            var keyButton = KeyboardButton.builder()
                                          .text(AUTHORIZE_BUTTON_NAME)
                                          .webApp(WebAppInfo.builder()
                                                            .url(authUtility.constructLoginUri())
                                                            .build())
                                          .build();
            var keyboard = List.of(new KeyboardRow(List.of(keyButton)));
            var markup = ReplyKeyboardMarkup.builder()
                                            .keyboard(keyboard)
                                            .resizeKeyboard(true)
                                            .build();

            var message = SendMessage.builder()
                                     .chatId(chatId.toString())
                                     .text("Пожалуйста, авторизируйтесь")
                                     .replyMarkup(markup)
                                     .build();
            var loginMessageId = executeSilent(message);

            var botUser = botUserRepository.findById(chatId)
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
                                             .collect(LinkedHashMap::new,
                                                      (map, item) -> map.put(item.getFirst(), item.getSecond()),
                                                      Map::putAll);

        sendMap(chatId, commands);
    }

    private void sendMap(Long chatId, Map<String, String> map) {
        final AtomicInteger counter = new AtomicInteger();

        var keyboard = new ArrayList<>(map.entrySet().stream()
                                          .map(command -> InlineKeyboardButton.builder()
                                                                              .text(command.getKey())
                                                                              .callbackData(command.getValue())
                                                                              .build())
                                          .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 2))
                                          .values());

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
        var botUser = botUserRepository.findById(chatId).orElseThrow();

        botUser.getLoginMessageIds().stream()
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
        List<List<InlineKeyboardButton>> keyboard =
                collection.stream()
                          .map(element -> InlineKeyboardButton.builder()
                                                              .text(element.getDisplayName())
                                                              .callbackData(element.getCallbackName() +
                                                                                    " " +
                                                                                    element.getId())
                                                              .build())
                          .map(List::of)
                          .collect(Collectors.toList());

        return InlineKeyboardMarkup.builder()
                                   .keyboard(keyboard)
                                   .build();
    }

    private Long getUserGroup(Long chatId) {
        return botUserRepository.findById(chatId).orElseThrow().getGroupId();
    }

    private Long getWeek() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        String dateStr = firstWeekStart != null
                ? firstWeekStart
                : now.format(formatter);
        LocalDate date = LocalDate.parse(dateStr, formatter);

        int weeks = Period.between(date, now).getDays() / 7;
        return (weeks % 2 == 0)
                ? 1L
                : 2L;
    }

    private Integer executeSilent(SendMessage method) {
        try {
            return this.execute(method).getMessageId();
        } catch (TelegramApiException e) {
            log.error("Cant send message {}. Exception: {}", method, e);
            return null;
        }
    }

    private void executeSilent(AnswerCallbackQuery method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant answer callback {}. Exception: {}", method, e);
        }
    }

    private void executeSilent(DeleteMessage method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant delete message {}. Exception: {}", method, e);
        }
    }

    private void executeSilent(SendDocument method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("Cant send document {}. Exception: {}", method, e);
        }
    }

    private Long getChatId(Update update) {
        Message message = Optional.ofNullable(update.getMessage())
                                  .orElseGet((() -> update.getCallbackQuery().getMessage()));

        return message.getChatId();
    }
}
