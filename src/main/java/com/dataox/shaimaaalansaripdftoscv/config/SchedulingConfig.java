package com.dataox.shaimaaalansaripdftoscv.config;

import com.dataox.shaimaaalansaripdftoscv.domain.ConvertData;
import com.dataox.shaimaaalansaripdftoscv.entities.EmailEntity;
import com.dataox.shaimaaalansaripdftoscv.repositories.EmailRepository;
import com.dataox.shaimaaalansaripdftoscv.services.ConvertService;
import com.dataox.shaimaaalansaripdftoscv.services.ReceivingFilesService;
import com.dataox.shaimaaalansaripdftoscv.services.SendingEmailsService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Log4j2
@Component
@AllArgsConstructor
public class SchedulingConfig {

    private final ReceivingFilesService receivingFilesService;
    private final SendingEmailsService sendingEmailsService;
    private final ConvertService convertService;
    private final EmailRepository emailRepository;

    @Scheduled(cron = "${morning.scheduler}")
    @Scheduled(cron = "${day.scheduler}")
    public void send() {
        log.info("Start to create PDFs.");
        List<EmailEntity> emails = emailRepository.findAllByHandledIsFalseAndUpdateAttachmentNotNull();
        if (isEmpty(emails)) {
            log.info("No emails.");
            return;
        }

        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        emails = emails.stream().filter(email -> email.getUpdateAttachment().getName().contains(dateToday)).collect(Collectors.toList());
        ConvertData data = convertService.createPdfFiles(emails);

        if (!isEmpty(data.getAttachments())) {
            sendingEmailsService.createEmailAndSendToClient(data.getAttachments());
            log.info("Start to send email.");
        }
    }

    private void checkThatEmailHasErrorWhileSending(List<EmailEntity> emails) {
        emails.forEach(email -> {
            email.setHasSendingError(true);
            email.setHandled(true);
            emailRepository.save(email);
        });
    }

    private void allNotHandledEmailsHasBeenSent(List<EmailEntity> correctEmails) {
        LocalDateTime now = LocalDateTime.now();
        for (EmailEntity email : correctEmails) {
            email.setHandled(true);
            email.setHasSendingError(false);
            email.setSendingTime(now);
            emailRepository.save(email);
        }
    }

    //    @Scheduled(cron = "${morning.scheduler}")
    //    @Scheduled(cron = "${day.scheduler}")
    public void resend() {
        log.info("Start to resend emails.");
        List<EmailEntity> emails = emailRepository.findAllByHasSendingErrorIsTrue();
        if (isEmpty(emails)) {
            log.info("No emails.");
            return;
        }
        ConvertData data = convertService.createPdfFiles(emails);

        if (!isEmpty(data.getAttachments())) {
            sendingEmailsService.createEmailAndSendToClient(data.getAttachments());
            log.info("Email with attachments has been sent.");
        }

        data.getFailedEmails().forEach(e -> {
            e.setHasSendingError(false);
            emailRepository.save(e);
        });
    }

    @Scheduled(fixedRate = 100000, initialDelay = 1000)
    public void receive() {
        receivingFilesService.receiveAttachmentsAndSaveInDB();
    }

}
