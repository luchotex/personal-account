package com.g2.personalaccount.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 20:06
 */
@Component
public class EmailProxy {
  @Autowired private JavaMailSender javaMailSender;

  public void sendPin(String toEmail, Integer pin) {

    sendEmail(
        toEmail,
        "IMPORTANT: Pin Notification",
        String.format("G2 Bank Pin Notification %nYour pin is %s", pin));
  }

  public void sendConfirmation(String toEmail, String url, String confirmId) {

    sendEmail(
        toEmail,
        "IMPORTANT: Account creation confirmation",
        String.format(
            "G2 Bank Pin confirm your creation in the following URL: %s/creation-confirmation/%s",
            url, confirmId));
  }

  public void sendEmail(String toEmail, String subject, String content) {

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(toEmail);

    msg.setSubject(subject);
    msg.setText(content);

    javaMailSender.send(msg);
  }
}
