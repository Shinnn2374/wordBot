package com.example.WordBot.bot;

import com.example.WordBot.data.UserData;
import com.example.WordBot.helper.WordDocumentHelper;
import com.example.WordBot.utils.UserState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MyBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private Map<Long, UserState> userStates = new HashMap<>();
    private Map<Long, UserData> userDataMap = new HashMap<>();

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            UserState currentState = userStates.getOrDefault(chatId, UserState.START);

            switch (currentState) {
                case START:
                    if (messageText.equals("/start")) {
                        sendConsentForm(chatId);
                        userStates.put(chatId, UserState.CONSENT);
                    }
                    break;

                case CONSENT:
                    // Обработка согласия происходит через callback, поэтому здесь ничего не делаем
                    break;

                case FULL_NAME:
                    if (isValidFullName(messageText)) {
                        userDataMap.put(chatId, new UserData(messageText, null, null, null));
                        sendMessage(chatId, "Введите дату рождения в формате dd.MM.yyyy:");
                        userStates.put(chatId, UserState.BIRTH_DATE);
                    } else {
                        sendMessage(chatId, "Пожалуйста, введите Фамилию и Имя.");
                    }
                    break;

                case BIRTH_DATE:
                    if (isValidDate(messageText)) {
                        UserData userData = userDataMap.get(chatId);
                        userData.setBirthDate(messageText);
                        sendGenderSelection(chatId);
                        userStates.put(chatId, UserState.GENDER);
                    } else {
                        sendMessage(chatId, "Неверный формат даты. Введите дату в формате dd.MM.yyyy.");
                    }
                    break;

                case GENDER:
                    // Обработка пола происходит через callback, поэтому здесь ничего не делаем
                    break;

                case PHOTO:
                    if (update.getMessage().hasPhoto()) {
                        List<PhotoSize> photos = update.getMessage().getPhoto();
                        String fileId = photos.get(photos.size() - 1).getFileId();
                        saveUserPhoto(chatId, fileId);
                        sendMessage(chatId, "Спасибо! Ваши данные сохранены. Ожидайте документ.");
                        generateAndSendDocument(chatId);
                        userStates.remove(chatId);
                        userDataMap.remove(chatId);
                    } else {
                        sendMessage(chatId, "Пожалуйста, отправьте вашу фотографию.");
                    }
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long chatId = callbackQuery.getMessage().getChatId();
            String callbackData = callbackQuery.getData();

            switch (callbackData) {
                case "consent":
                    sendMessage(chatId, "Введите ваше ФИО (Фамилия и Имя):");
                    userStates.put(chatId, UserState.FULL_NAME);
                    break;

                case "decline":
                    sendMessage(chatId, "Вы отказались от обработки персональных данных. Бот завершает работу.");
                    userStates.remove(chatId);
                    break;

                case "male":
                case "female":
                    UserData userData = userDataMap.get(chatId);
                    userData.setGender(callbackData.equals("male") ? "Мужской" : "Женский");
                    sendMessage(chatId, "Отправьте вашу фотографию:");
                    userStates.put(chatId, UserState.PHOTO);
                    break;
            }
        }
    }

    private boolean isValidFullName(String fullName) {
        return fullName.split(" ").length >= 2; // Проверяем, что введены хотя бы Фамилия и Имя
    }

    private boolean isValidDate(String date) {
        return date.matches("\\d{2}\\.\\d{2}\\.\\d{4}"); // Простая проверка формата даты
    }

    private void saveUserPhoto(long chatId, String fileId) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            java.io.File downloadedFile = downloadFile(file);
            // Сохраняем файл или его путь в userDataMap
            UserData userData = userDataMap.get(chatId);
            userData.setPhotoPath(downloadedFile.getAbsolutePath());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void generateAndSendDocument(long chatId) {
        UserData userData = userDataMap.get(chatId);
        try {
            File document = WordDocumentHelper.createDocument(
                    userData.getFullName(),
                    userData.getBirthDate(),
                    userData.getGender(),
                    userData.getPhotoPath()
            );
            sendDocument(chatId, document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendGenderSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите ваш пол:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton maleButton = new InlineKeyboardButton();
        maleButton.setText("Мужской");
        maleButton.setCallbackData("male");

        InlineKeyboardButton femaleButton = new InlineKeyboardButton();
        femaleButton.setText("Женский");
        femaleButton.setCallbackData("female");

        rowInline.add(maleButton);
        rowInline.add(femaleButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDocument(long chatId, File document) {
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(String.valueOf(chatId)); // Указываем chatId
        sendDocumentRequest.setDocument(new InputFile(document)); // Указываем файл для отправки

        try {
            execute(sendDocumentRequest); // Отправляем документ
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendMessage(chatId, "Произошла ошибка при отправке документа. Пожалуйста, попробуйте снова.");
        }
    }

    private void sendConsentForm(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId)); // Указываем chatId
        message.setText("Согласитесь на обработку персональных данных:"); // Текст сообщения

        // Создаем inline-кнопки
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        // Кнопка "Согласиться"
        InlineKeyboardButton consentButton = new InlineKeyboardButton();
        consentButton.setText("Согласиться");
        consentButton.setCallbackData("consent"); // Данные, которые будут переданы при нажатии

        // Кнопка "Отказаться"
        InlineKeyboardButton declineButton = new InlineKeyboardButton();
        declineButton.setText("Отказаться");
        declineButton.setCallbackData("decline"); // Данные, которые будут переданы при нажатии

        // Добавляем кнопки в строку
        rowInline.add(consentButton);
        rowInline.add(declineButton);

        // Добавляем строку в список строк
        rowsInline.add(rowInline);

        // Устанавливаем клавиатуру в сообщение
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message); // Отправляем сообщение с кнопками
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}