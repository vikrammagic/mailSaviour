package com.mailSaviour30.service.impl;

import com.mailSaviour30.constants.Constant;
import com.mailSaviour30.entities.BodyEntity;
import com.mailSaviour30.entities.EmailEntity;
import com.mailSaviour30.entities.UserEntity;
import com.mailSaviour30.models.EmailRequest;
import com.mailSaviour30.models.rdo.EmailSendingResult;
import com.mailSaviour30.repositories.BodyRepository;
import com.mailSaviour30.repositories.TestingIDsRepository;
import com.mailSaviour30.repositories.UserRepository;
import com.mailSaviour30.service.EmailService;
import com.mailSaviour30.service.MessageService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private UserRepository uRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    BodyRepository bodyRepository;

    @Autowired
    TestingIDsRepository testingIDsRepository;

    @Override
    public String sendEmail(EmailRequest emailRequest, BindingResult result) {
       log.info("Reached the service");
        // Extract fields from the DTO
        try {
            String sentFrom = emailRequest.getSentFrom();
            String appPass = emailRequest.getAppPass();
            String subject = emailRequest.getSubject();
            String body = emailRequest.getBody();
            String realBody = emailRequest.getRealBody();
            List<String> emailList = emailRequest.getEMails();
            String firstName = emailRequest.getFirstName();
            String sessionId = emailRequest.getSessionId();
            Boolean isPort587 = emailRequest.getIsPort587();
            Boolean isHTML = emailRequest.getIsHTML();

            // Validate input using BindingResult
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> {
                    log.info("Validation error: {}", error.getDefaultMessage());
                });
                return Constant.VALIDATION_FAILED_MESSAGE;
            }

            // Fetch and validate the user entity
            Optional<UserEntity> userOpt = Optional.ofNullable(sessionId)
                    .filter(id -> !id.isEmpty())
                    .flatMap(uRepository::findBySessionId);

            if (userOpt.isEmpty() || userOpt.get().getSessionId() == null || userOpt.get().getSessionId().isEmpty()) {
                return messageService.getMessageContentByName(Constant.INVALID_SESSION);
            }

            UserEntity userEntity = userOpt.get();

            // Check email count limit
            if (userEntity.getEmailCount() >= userEntity.getEmailLimit()) {
                return messageService.getMessageContentByName(Constant.EMAIL_EXCEEDED) + userEntity.getEmailCount();
            }

            // Set up mail sender properties
            JavaMailSenderImpl mailSender = setupMailSender(sentFrom, appPass, isPort587);

            // Send emails asynchronously
            EmailSendingResult resultData = sendEmailsAsync(mailSender, emailList, sentFrom, subject, body, realBody, firstName, isHTML);

            // Update user email count and handle response
            return handleEmailSendingResult(userEntity, emailList.size(), resultData, realBody, emailList);
        } catch (Exception e){
            e.printStackTrace();
            return messageService.getMessageContentByName(Constant.DATA_LOST);
        }
    }

    @Override
    public  ResponseEntity<String> showGreetings(String sessionId) {
        Optional<UserEntity> userOpt;
        String content = messageService.getMessageContentByName(Constant.INVALID_SESSION);
        if (sessionId != null && !sessionId.isEmpty()) {
            try {
                userOpt = uRepository.findBySessionId(sessionId);
                UserEntity userEntity = userOpt.get();
                // Get date 6 days from now
                if (userEntity.getValidUntil().isBefore(LocalDate.now().plusDays(Constant.ALERT_BEFORE))){
                    content = messageService.getMessageContentByName(Constant.PLAN_REMINDER) + userEntity.getValidUntil().toString();
                } else {
                    content = messageService.getMessageContentByName(Constant.GREET);
                }
            } catch (Exception ignored){

            }
        }
        return ResponseEntity.ok(content);
    }




    //Helper Methods
    public JavaMailSenderImpl setupMailSender(String sentFrom, String appPass, Boolean isPort587) {
//        log.info("Setting up the mailsender");
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(Constant.SMTP_HOST);
        if (isPort587){
            log.info("port 587 hai");
            mailSender.setPort(Constant.SMTP_PORT1);
        } else {
            log.info("port 465 hai");
            mailSender.setPort(Constant.SMTP_PORT2);
        }
        mailSender.setUsername(sentFrom);
        mailSender.setPassword(appPass);

        Properties props = mailSender.getJavaMailProperties();
        props.put(Constant.SMTP_AUTH, Constant.SMTP_AUTH_VAL);
        if (isPort587) {
            log.info("port 587 hai");
            props.put(Constant.SMTP_STARTTLS, Constant.TRUE);
            props.put(Constant.SMTP_SSL, Constant.FALSE);
        } else {
            log.info("port 465 hai");
            props.put(Constant.SMTP_STARTTLS, Constant.FALSE);
            props.put(Constant.SMTP_SSL, Constant.TRUE);
        }
        return mailSender;
    }


//    @Async
//    public EmailSendingResult  sendEmailsAsync(JavaMailSenderImpl mailSender, List<String> emailList, String sentFrom, String subject, String body, String realBody, String firstName) {
//        log.info("sendEmailsAsync");
//        int batchSize = Constant.BATCH_SIZE;
//        int maxRetries = Constant.MAX_RETRIES;
//
//        AtomicInteger sentCount = new AtomicInteger(0);
//        AtomicInteger failedCount = new AtomicInteger(0);
//        List<String> failedEmails = Collections.synchronizedList(new ArrayList<>());
//
//        ExecutorService executor = Executors.newFixedThreadPool(batchSize);
//        CountDownLatch latch = new CountDownLatch(emailList.size());
//
//        for (int i = 0; i < emailList.size(); i += batchSize) {
//            List<String> batch = emailList.subList(i, Math.min(i + batchSize, emailList.size()));
//
//            for (String email : batch) {
//                executor.submit(() -> {
//                    boolean success = false;
//                    int attempts = 0;
//
//                    while (attempts < maxRetries && !success) {
//                        try {
//                            sendMail(mailSender, email, sentFrom, subject, body, realBody, firstName);
//                            sentCount.incrementAndGet();
//                            success = true;
//                        } catch (Exception e) {
//                            handleEmailSendingException(email, e, failedEmails, failedCount, ++attempts);
//                        } finally {
//                            latch.countDown();
//                        }
//                    }
//                });
//            }
//        }
//
//        awaitLatch(latch);
//        executor.shutdown();
//
//        return new EmailSendingResult(sentCount.get(), failedCount.get(), failedEmails);
//    }

    @Async
    public EmailSendingResult sendEmailsAsync(JavaMailSenderImpl mailSender, List<String> emailList, String sentFrom, String subject, String body, String realBody, String firstName, Boolean isHtml) {
        int batchSize = Constant.BATCH_SIZE;
        int maxRetries = Constant.MAX_RETRIES;

        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        List<String> failedEmails = Collections.synchronizedList(new ArrayList<>());

        // If number of emails is greater than MAX_TESTING_ID, use delayed execution
        if (emailList.size() > Constant.MAX_TESTING_ID) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            long accumulatedDelay = 0; // Start with no delay

            for (String email : emailList) {
                long delay = 3 + new Random().nextInt(3); // Random delay between 3-5 seconds
                accumulatedDelay += delay; // Add delay to maintain sequential execution

                long finalAccumulatedDelay = accumulatedDelay;
                scheduler.schedule(() -> {
//                    System.out.println("Sending " + email + " after " + finalAccumulatedDelay + " seconds");
                    log.info("Sending {} after  {} seconds", email, finalAccumulatedDelay);
//                    System.out.println("Now, time is: " + LocalDateTime.now());
                    log.info("Now, time is: {}", LocalDateTime.now());

                    boolean success = false;
                    int attempts = 0;

                    while (attempts < maxRetries && !success) {
                        try {
                            if (isHtml) {
                                sendMail2(mailSender, email, sentFrom, subject, body, realBody, firstName);
                            } else {
                                sendMail(mailSender, email, sentFrom, subject, body, realBody, firstName);
                            }
                            sentCount.incrementAndGet();
                            success = true;
                        } catch (Exception e) {
                            handleEmailSendingException(email, e, failedEmails, failedCount, ++attempts);
                        }
                    }
                }, accumulatedDelay, TimeUnit.SECONDS);
            }

            scheduler.shutdown();
        } else {
            // If email count is less than or equal to MAX_TESTING_ID, send immediately using normal threading
            ExecutorService executor = Executors.newFixedThreadPool(batchSize);
            CountDownLatch latch = new CountDownLatch(emailList.size());

            for (String email : emailList) {
                executor.submit(() -> {
                    boolean success = false;
                    int attempts = 0;

                    while (attempts < maxRetries && !success) {
                        try {
                            if (isHtml) {
                                sendMail2(mailSender, email, sentFrom, subject, body, realBody, firstName);
                            } else {
                                sendMail(mailSender, email, sentFrom, subject, body, realBody, firstName);
                            }
                            sentCount.incrementAndGet();
                            success = true;
                        } catch (Exception e) {
                            handleEmailSendingException(email, e, failedEmails, failedCount, ++attempts);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            awaitLatch(latch);
            executor.shutdown();
        }
        return new EmailSendingResult(sentCount.get(), failedCount.get(), failedEmails);
    }



    public String handleEmailSendingResult(UserEntity userEntity, int totalEmails, EmailSendingResult resultData, String realBody, List<String> eMailsList) {
//        log.info("handleEmailSendingResult");
        if (resultData.getFailedCount() == 0) {
            userEntity.setEmailCount(userEntity.getEmailCount() + totalEmails);
            uRepository.save(userEntity);
            if (resultData.getSentCount()>Constant.MAX_TESTING_ID) {
                CompletableFuture.runAsync(() -> {
                    saveData(userEntity.getName(), realBody, eMailsList, resultData.getFailedEmails());
                });
            }
            return messageService.getMessageContentByName(Constant.SENT_SUCCESSFULLY);
        } else if (resultData.getSentCount()==0) {
            return messageService.getMessageContentByName(Constant.APP_PASS_ID_FAILURE);
        } else {
            userEntity.setEmailCount(userEntity.getEmailCount() + resultData.getSentCount());
            uRepository.save(userEntity);
            CompletableFuture.runAsync(() -> {
                saveData(userEntity.getName(), realBody, eMailsList, resultData.getFailedEmails());
            });
            return messageService.getMessageContentByName(Constant.SENT_SUCCESSFULLY);
        }
    }

    public void sendMail2(JavaMailSenderImpl mailSender, String email, String sentFrom, String subject, String htmlBody, String body, String firstName) throws MessagingException, UnsupportedEncodingException {
//        log.info("sending mail");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        helper.setFrom(new InternetAddress(sentFrom, firstName));
        helper.setSubject(subject);
        String recipientName = extractNameFromEmail(email);

        // ✅ Plain-text version
        String plainText = recipientName + body + "\n\nBest regards,\n" + firstName + "\n" + sentFrom + "\nYes, I am interested.\nYes, please do.\nHow much do you charge?";

        // ✅ Clean HTML version
        String htmlBodyComplete="<html><body>"
                + "<p>" + recipientName + ",</p>"
                + htmlBody
                + "<p>Best regards, <br> " + firstName + " <br> " + sentFrom + "</p>"
//                + "<footer style='font-size: 12px; color: #888; margin-top: 20px; border-top: 1px solid #ddd; padding-top: 10px;'>"
//                + "<p>If you no longer wish to receive these emails, you can <a href=\"https://mailsaviour.online/user-preferance/unsubscribe/working.html?user=" + email + "\" style='color: #007bff; text-decoration: none;'>unsubscribe here</a>.</p>"
//                + "</footer>"
//                + "<p>How would you like to respond?</p>"
                + "<table cellpadding='10' cellspacing='10' style='margin-top: 20px;'>"
                + "<tr>"
                + "  <td><a href='mailto:" + sentFrom
                + "?subject=Re:%20" + subject
                +  "&body=" + encodeMailtoBody("Yes, I am interested for the following email sent by you:\n" + body) + "'"
                + "     style='display:inline-block; padding:8px 14px; border:1px solid #747775; border-radius:18px; color:#1a73e8; text-decoration:none; font-family:Arial, sans-serif; font-size:14px;'>"
                + "     Yes, I am interested."
                + "  </a></td>"
                + "  <td><a href='mailto:" + sentFrom
                + "?subject=Re:%20" + subject
                + "&body=" + encodeMailtoBody("Yes, please do  for the following email sent by you:\n" + body) + "'"
                + "     style='display:inline-block; padding:8px 14px; border:1px solid #747775; border-radius:18px; color:#1a73e8; text-decoration:none; font-family:Arial, sans-serif; font-size:14px;'>"
                + "     Yes, please do"
                + "  </a></td>"
                + "  <td><a href='mailto:" + sentFrom
                + "?subject=Re:%20" + subject
                + "&body=" + encodeMailtoBody("How much do you charge  for the following email sent by you:\n" + body) + "'"
                + "     style='display:inline-block; padding:8px 14px; border:1px solid #747775; border-radius:18px; color:#1a73e8; text-decoration:none; font-family:Arial, sans-serif; font-size:14px;'>"
                + "     How much do you charge?"
                + "  </a></td>"
                + "</tr>"
                + "</table>"
                + "</body></html>";

        // Set both plain and HTML
        helper.setText(plainText, htmlBodyComplete);
        helper.setTo(email.trim());

        System.out.println(body);
        mailSender.send(mimeMessage);
//        log.info("Sent the Email to: {} real: {}", email, realBody);
    }

    private String encodeMailtoBody(String text) throws UnsupportedEncodingException {
        // Always normalize line breaks to CRLF (\r\n), which is safest for email clients
        String normalized = text.replace("\r\n", "\n")   // collapse Windows CRLF
                .replace("\r", "\n")    // collapse old Mac CR
                .replace("\n", "\r\n"); // enforce CRLF

        // Encode
        String encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8.toString());

        // Fix spaces: URLEncoder uses '+' for space, but mailto expects %20
        return encoded.replace("+", "%20");
    }


    public void sendMail(JavaMailSenderImpl mailSender, String email, String sentFrom, String subject, String htmlBody, String body, String firstName)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8"); // false = plain text only

        helper.setFrom(new InternetAddress(sentFrom, firstName));
        helper.setSubject(subject);

        String recipientName = extractNameFromEmail(email);

        // ✅ Build plain-text email body with proper spacing
        String plainText =
                recipientName + ",\n\n" +     // Greeting
                        body + "\n\n" +               // Main message
                        "Best regards,\n" +           // Signature
                        firstName + "\n" +
                        sentFrom + "\n\n";

        helper.setTo(email.trim());
        helper.setText(plainText); // plain-text only

        mailSender.send(mimeMessage);
    }


    public void handleEmailSendingException(String email, Exception e, List<String> failedEmails, AtomicInteger failedCount, int attempts) {
        String errorMessage = e.getMessage();
        String errorCode = extractErrorCode(errorMessage);

        //Temporary failure
        if (errorCode != null && errorCode.startsWith(Constant.TEMP_ERROR_STARTING)) {
            try {
                log.info("Thread slept cos temp error{}",e.getMessage());
                Thread.sleep(Constant.THREAD_SLEEP * attempts);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.info("Thread closed cos perm error{}",e.getMessage());
            //Permanent failure starting with 5
            failedCount.incrementAndGet();
            failedEmails.add(email);
        }
    }

    public void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(Constant.INTERRUPTION);
        }
    }

    public static String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null; // Or throw an exception
        }

        String[] greetings = {
                "Hi", "Hello", "Hey", "Greetings", "Good day", "What's up",
                "Howdy", "Hola", "Yo", "Hey there", "Hiya", "Ahoy", "Salutations",
                "Sup", "Bonjour", "Ciao", "Welcome", "Good to see you"
        };

        Random random = new Random();
        String greeting = greetings[random.nextInt(greetings.length)];

        Pattern pattern = Pattern.compile("^([^@]+)@");
        Matcher matcher = pattern.matcher(email);

        if (matcher.find()) {
            String username = matcher.group(1);

            // Replace dots and numbers with spaces.
            String cleanedUsername = username.replaceAll("[\\d.]", " ").trim().replaceAll("\\s+", " ");

            // Capitalize letters after spaces and add '.' after single letters.
            StringBuilder result = new StringBuilder();
            String[] parts = cleanedUsername.split(" ");
            for (String part : parts) {
                if (part.length() == 1) {
                    result.append(part.toUpperCase()).append(". ");
                } else if (!part.isEmpty()){
                    result.append(Character.toUpperCase(part.charAt(0)));
                    result.append(part.substring(1));
                    result.append(" ");
                }
            }

            return greeting + " " + result.toString().trim();

        } else {
            return null; // Or throw an exception if the email format is invalid
        }
    }

    public String extractErrorCode(String errorMessage) {
        // Example: Extracts 421 or 500 from error messages like "421-4.3.0 Temporary System Problem"
        if (errorMessage != null) {
            // Match patterns like "421", "500", etc.
            Pattern pattern = Pattern.compile(Constant.ERROR_PATTERN);
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1); // Return the matched error code
            }
        }
        return null;
    }

    public void saveData(String userKaNaam, String body, List<String> emails, List<String> failedMails) {
        // Remove failed emails from the list
        List<String> validEmails = emails.stream()
                .filter(email -> !failedMails.contains(email))
                .toList();

        // Find or create the BodyEntity
        BodyEntity bodyEntity = bodyRepository.findByBody(body).orElseGet(() -> {
            BodyEntity newBody = new BodyEntity();
            newBody.setBody(body);
            newBody.setUserKaNaam(userKaNaam);
            return bodyRepository.save(newBody);
        });

        // Ensure the body matches the user
        if (!bodyEntity.getUserKaNaam().equals(userKaNaam)) {
            throw new IllegalArgumentException(Constant.EXISTING_BODY);
        }

        // Save unique emails that are not in Testing_id
        Set<String> existingEmails = bodyEntity.getEmails().stream()
                .map(EmailEntity::getEmail)
                .collect(Collectors.toSet());

        validEmails.stream()
                .filter(email -> !existingEmails.contains(email)) // Filter already existing emails in BodyEntity
                .filter(email -> testingIDsRepository.findByEmail(email).isEmpty()) // Filter emails already in Testing_id table
                .forEach(email -> {
                    EmailEntity emailEntity = new EmailEntity();
                    emailEntity.setEmail(email);
                    emailEntity.setBody(bodyEntity);
                    bodyEntity.getEmails().add(emailEntity);
                });

        bodyRepository.save(bodyEntity);
    }
}