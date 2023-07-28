package com.bot.arzzezzan.javabot.Service;

import com.bot.arzzezzan.javabot.Command.VKCommand.Bot.TelegramBot;
import com.vk.api.sdk.objects.docs.Doc;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.video.responses.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class SendBotMessageServiceImpl implements SendBotMessageService {
    private TelegramBot telegramBot;
    @Autowired
    public SendBotMessageServiceImpl(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    @Override
    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableHtml(true);
        sendMessage.setText(message);

        try {
            telegramBot.execute(sendMessage);
        } catch(TelegramApiException telegramApiException) {
            telegramApiException.printStackTrace();
        }
    }
    @Override
    public void sendMessageMarkup(EditMessageReplyMarkup sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch(TelegramApiException telegramApiException) {
            telegramApiException.printStackTrace();
        }
    }
    @Override
    public void sendMessageMarkup(SendMessage sendMessage) {
        try {
            telegramBot.execute(sendMessage);
        } catch(TelegramApiException telegramApiException) {
            telegramApiException.printStackTrace();
        }
    }

    @Override
    public void sendPhoto(String chatId, Photo photo, String text) throws MalformedURLException {
        String photoUrl = photo.getSizes().get(photo.getSizes().size() - 1).getUrl().toString();
        URL url = new URL(photoUrl);
        try(InputStream inputStream = url.openStream()) {
            byte[] photoBytes = inputStream.readAllBytes();
            InputFile file = new InputFile(new ByteArrayInputStream(photoBytes), "photo.jpg");

            SendPhoto sendPhoto = new SendPhoto(chatId, file);
            sendPhoto.setCaption(text);
            telegramBot.execute(sendPhoto);
        } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendVideo(String chatId, GetResponse response, String text) {
        System.out.println("Video");
        VideoFull vi = response.getItems().get(0);
        String videoUrl = String.valueOf(vi.getPlayer());
        try {
            InputFile file = new InputFile(new URL(videoUrl).openStream(), "video.mp4");
            saveFile(file, "/Users/killeral/JavaBot/video.mp4");
            SendVideo sendVideo = new SendVideo(chatId, new InputFile(new URL(videoUrl).openStream(), "video.mp4"));
            sendVideo.setCaption(text);
            telegramBot.execute(sendVideo);
        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDoc(String chatId, Doc doc, String text) {
        try {
            System.out.println("Document");
            SendDocument sendDocument = new SendDocument(chatId, new InputFile(new URL(doc.getUrl().toString()).openStream(), "doc.pdf"));
            sendDocument.setCaption(text);
            telegramBot.execute(sendDocument);
        } catch(IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(InputFile inputFile, String filePath) throws IOException {
        try (InputStream inputStream = inputFile.getNewMediaStream();
             OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
