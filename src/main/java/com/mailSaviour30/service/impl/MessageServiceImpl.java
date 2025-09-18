package com.mailSaviour30.service.impl;

import com.mailSaviour30.entities.MessageEntity;
import com.mailSaviour30.repositories.MessageRepository;
import com.mailSaviour30.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public String getMessageContentByName(String messageName) {
        // Find the message by messageName
        MessageEntity message = messageRepository.findByMessageName(messageName)
                .orElseThrow(() -> new IllegalArgumentException("Message with name '" + messageName + "' not found."));

        // Return the message content
        return message.getMeassageContent();
    }
}