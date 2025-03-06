package com.example.WordBot.service;

import com.example.WordBot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
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

    // Добавьте методы для остальных шагов
}