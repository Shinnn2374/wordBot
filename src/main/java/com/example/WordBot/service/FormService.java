package com.example.WordBot.service;

import com.example.WordBot.entity.User;
import com.example.WordBot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FormService {
    private final UserRepository userRepository;

    public FormService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SendMessage handleStep1(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Согласны ли вы на обработку персональных данных?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Согласен").setCallbackData("agree"));
        rowInline.add(new InlineKeyboardButton().setText("Политика конфиденциальности").setUrl("https://example.com"));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        return message;
    }

    public SendMessage handleStep2(long chatId, String fullName) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (fullName.split(" ").length >= 2) { // Проверяем, что введены хотя бы фамилия и имя
            User user = userRepository.findByChatId(chatId);
            if (user == null) {
                user = new User();
                user.setChatId(chatId);
            }
            user.setFullName(fullName);
            userRepository.save(user);

            message.setText("ФИО успешно сохранено. Введите дату рождения в формате dd.MM.yyyy.");
        } else {
            message.setText("Пожалуйста, введите Фамилию и Имя (минимум два слова).");
        }

        return message;
    }

    public SendMessage handleStep3(long chatId, String birthDate) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            format.setLenient(false);
            Date date = format.parse(birthDate);

            User user = userRepository.findByChatId(chatId);
            if (user != null) {
                user.setBirthDate(date);
                userRepository.save(user);
                message.setText("Дата рождения успешно сохранена. Выберите пол.");
            } else {
                message.setText("Ошибка: пользователь не найден.");
            }
        } catch (ParseException e) {
            message.setText("Неверный формат даты. Введите дату в формате dd.MM.yyyy.");
        }

        return message;
    }

    public SendMessage handleStep4(long chatId, String gender) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        User user = userRepository.findByChatId(chatId);
        if (user != null) {
            user.setGender(gender);
            userRepository.save(user);
            message.setText("Пол успешно сохранен. Пожалуйста, отправьте вашу фотографию.");
        } else {
            message.setText("Ошибка: пользователь не найден.");
        }

        return message;
    }

    public SendMessage handleStep5(long chatId, byte[] photo) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        User user = userRepository.findByChatId(chatId);
        if (user != null) {
            user.setPhoto(photo);
            userRepository.save(user);
            message.setText("Фотография успешно сохранена. Спасибо за заполнение формы!");

            // Здесь можно вызвать метод для создания Word-документа
            WordDocumentService wordDocumentService = new WordDocumentService();
            try {
                wordDocumentService.createDocument(user.getFullName(), user.getBirthDate().toString(), user.getGender());
                message.setText("Форма успешно заполнена. Документ создан.");
            } catch (IOException e) {
                message.setText("Ошибка при создании документа.");
            }
        } else {
            message.setText("Ошибка: пользователь не найден.");
        }

        return message;
    }

    public SendMessage handleStep(long chatId, String text, byte[] photo) {
        User user = userRepository.findByChatId(chatId);
        if (user == null) {
            return handleStep1(chatId);
        }

        if (user.getFullName() == null) {
            return handleStep2(chatId, text);
        } else if (user.getBirthDate() == null) {
            return handleStep3(chatId, text);
        } else if (user.getGender() == null) {
            return handleStep4(chatId, text);
        } else if (user.getPhoto() == null) {
            return handleStep5(chatId, photo);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Форма уже заполнена.");
            return message;
        }
    }
}
