package com.bot.arzzezzan.javabot.Command.VKCommand.Command.News;

import com.bot.arzzezzan.javabot.Command.Command;
import com.bot.arzzezzan.javabot.Service.SendBotMessageService;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.newsfeed.responses.GetListsResponse;
import com.vk.api.sdk.objects.newsfeed.responses.GetResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.oneofs.NewsfeedNewsfeedItemOneOf;
import com.vk.api.sdk.queries.groups.GroupsGetByIdQueryWithObjectLegacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.bot.arzzezzan.javabot.Command.VKCommand.Command.Friend.FriendManagerName.*;
import static com.bot.arzzezzan.javabot.Command.VKCommand.Command.News.NewsManagerName.LIST;
import static com.bot.arzzezzan.javabot.Command.VKCommand.CommandManagerName.NEWS;

public class NewsCommand implements Command {
    private SendBotMessageService sendBotMessageService;
    private VkApiClient vk;
    private UserActor userActor;
    private Update update;
    private static final String friendMessage = "You can manage your list of friends!";

    public NewsCommand(SendBotMessageService sendBotMessageService, VkApiClient vk, UserActor userActor) {
        this.userActor = userActor;
        this.vk = vk;
        this.sendBotMessageService = sendBotMessageService;
    }
    @Override
    public void execute(Update update) {
        this.update = update;
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        message.setText("Управляйте своими новостями!");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton listButton = new InlineKeyboardButton();

        listButton.setText("Список");
        listButton.setCallbackData(LIST.getCommandName());

        rowInLine.add(listButton);
        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        sendBotMessageService.sendMessageMarkup(message);
    }
    public void callbackHandler(Update update, String callbackData) {
        this.update = update;
        if(callbackData.equals(LIST.getCommandName())){
            sendBotMessageService.sendMessage(update.getCallbackQuery().getMessage().getChatId().toString(),
                    getLists());
        }
    }
    private String getLists() {
        StringBuilder newsBuilder = new StringBuilder();
        int count = 0;
        try {
            GetResponse newsList = vk.newsfeed().get(userActor).count(5).execute();
            for(NewsfeedNewsfeedItemOneOf item : newsList.getItems()) {
                String text = item.getOneOf0().getText();

                // Получаем информацию о сообществе, если новость из него
                if (item.getOneOf0().getSourceId() < 0) {
                    Group group = vk.groups().getByIdObjectLegacy(userActor).
                            groupId(String.valueOf(Math.abs(item.getOneOf0().getSourceId()))).execute().get(0);
                    String groupName = group.getName();
                    text = "[" + groupName + "]\n" + text;
                }

                List<WallpostAttachment> attachments = item.getOneOf0().getAttachments();
                if (attachments != null && !attachments.isEmpty()) {
                    for (WallpostAttachment attachment : attachments) {
                        if(attachment.getPhoto() != null){
                            Photo photo = attachment.getPhoto();
                            String photoUrl = String.valueOf(photo.getSizes().get(photo.getSizes().size() - 1).getUrl());
                            sendBotMessageService.sendPhoto(update.getCallbackQuery().getMessage().getChatId().toString(),
                                    photoUrl);
                        }
                    }
                }
                newsBuilder.append(++count).append(". ").append(text).append("\n");
            }
        } catch (ClientException | ApiException e) {
            e.printStackTrace();
        }
        return newsBuilder.toString();
    }
}
