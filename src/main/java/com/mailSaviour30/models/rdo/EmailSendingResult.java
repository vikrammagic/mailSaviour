package com.mailSaviour30.models.rdo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EmailSendingResult {
    private final int sentCount;
    private final int failedCount;
    private final List<String> failedEmails;
}
