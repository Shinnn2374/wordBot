package com.example.WordBot.bot;

import com.example.WordBot.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class DocumentBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;

    public DocumentBot(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Вы сказали: " + messageText);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "WordBotTestTask_bot";
    }

    @Override
    public String getBotToken() {
        return "7905945256:AAFE_WSeD2NymlOyDknh38BvxPnLeWBQ7xI";
    }
}