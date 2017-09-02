/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.mail;

import griffon.core.GriffonApplication;
import griffon.exceptions.GriffonException;
import griffon.plugins.mail.MailHandler;
import griffon.plugins.mail.MailOptions;
import griffon.plugins.mail.MimeType;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static griffon.util.ConfigUtils.getConfigValue;
import static griffon.util.ConfigUtils.getConfigValueAsBoolean;
import static griffon.util.ConfigUtils.getConfigValueAsInt;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonClassUtils.requireState;
import static griffon.util.GriffonNameUtils.isBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class DefaultMailHandler implements MailHandler {
    private final GriffonApplication griffonApplication;

    @Inject
    public DefaultMailHandler(@Nonnull GriffonApplication griffonApplication) {
        this.griffonApplication = requireNonNull(griffonApplication, "Argument 'griffonApplication' must not be null");
    }

    @Override
    public void sendMail(@Nonnull MailOptions options) {
        requireNonNull(options, "Argument 'options' must not be null");

        Map<String, Object> defaultOptions = griffonApplication.getConfiguration().getAs("mail.options", Collections.<String, Object>emptyMap());
        options = merge(options, defaultOptions);

        requireState(!isBlank(options.getHost()), "Argument 'host' must not be blank");
        requireState(!isBlank(options.getTo()), "Argument 'recipient' must not be blank");

        Properties properties = System.getProperties();
        properties.putAll(options.getMailProperties());

        try {
            Session session = Session.getInstance(properties);
            Message message = new MimeMessage(session);

            if (!isBlank(options.getFrom())) {
                message.setFrom(new InternetAddress(options.getFrom()));
            }

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(options.getTo()));
            if (!isBlank(options.getCc())) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(options.getCc()));
            }
            if (!isBlank(options.getBcc())) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(options.getBcc()));
            }
            message.setSubject(options.getSubject());

            MimeMultipart content = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(options.getContent(), options.getMimeType().getCode());
            content.addBodyPart(textPart);
            for (String path : options.getAttachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource src = new FileDataSource(path);
                attachmentPart.setDataHandler(new DataHandler(src));
                attachmentPart.setFileName(new File(path).getName());
                content.addBodyPart(attachmentPart);
            }
            message.setContent(content);

            message.setHeader("X-Mailer", options.getMailer());
            message.setSentDate(new Date());

            Transport transport = session.getTransport(options.getTransport().name().toLowerCase());
            if (options.isAuth()) {
                transport.connect(options.getHost(), options.getUsername(), options.getPassword());
            } else {
                transport.connect();
            }
            transport.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            throw new GriffonException(e);
        }
    }

    private MailOptions merge(MailOptions options, Map<String, Object> defaultOptions) {
        MailOptions.Builder builder = new MailOptions.Builder();

        builder.withTo(options.getTo());
        builder.withMailer(mergeString(options.getMailer(), getConfigValueAsString(defaultOptions, "mailer"), "Griffon Mail Server"));
        builder.withTransport(mergeTransport(options.getTransport(), getConfigValueAsString(defaultOptions, "transport"), griffon.plugins.mail.Transport.SMTP));
        builder.withHost(mergeString(options.getHost(), getConfigValueAsString(defaultOptions, "host"), ""));
        builder.withPort(mergeInteger(options.getPort(), getConfigValueAsInt(defaultOptions, "port"), 25));
        builder.withAuth(mergeBoolean(options.isAuth(), getConfigValueAsBoolean(defaultOptions, "auth"), false));
        builder.withUsername(mergeString(options.getUsername(), getConfigValueAsString(defaultOptions, "username"), ""));
        builder.withPassword(mergeString(options.getPassword(), getConfigValueAsString(defaultOptions, "password"), ""));
        builder.withFrom(mergeString(options.getFrom(), getConfigValueAsString(defaultOptions, "from"), ""));
        builder.withCc(mergeString(options.getCc(), getConfigValueAsString(defaultOptions, "cc"), ""));
        builder.withBcc(mergeString(options.getBcc(), getConfigValueAsString(defaultOptions, "bcc"), ""));
        builder.withSubject(mergeString(options.getSubject(), getConfigValueAsString(defaultOptions, "subject"), ""));
        builder.withContent(mergeString(options.getContent(), getConfigValueAsString(defaultOptions, "content"), ""));
        builder.withMimeType(mergeMimeType(options.getMimeType(), getConfigValueAsString(defaultOptions, "mime-type"), MimeType.TEXT));

        Map<String, Object> defaultProps = getConfigValue(defaultOptions, "props", Collections.<String, Object>emptyMap());
        builder.withProps(defaultProps);
        builder.withProps(options.getProps());

        return builder.build();
    }

    private griffon.plugins.mail.Transport mergeTransport(griffon.plugins.mail.Transport value1, String value2, griffon.plugins.mail.Transport defaultValue) {
        if (value1 != null) { return value1; }
        if (!isBlank(value2)) { return griffon.plugins.mail.Transport.valueOf(value2.toUpperCase()); }
        return defaultValue;
    }

    private String mergeString(String value1, String value2, String defaultValue) {
        if (!isBlank(value1)) { return value1; }
        if (!isBlank(value2)) { return value2; }
        return defaultValue;
    }

    private Integer mergeInteger(Integer value1, Integer value2, Integer defaultValue) {
        if (value1 != null) { return value1; }
        if (value2 != null) { return value2; }
        return defaultValue;
    }

    private Boolean mergeBoolean(Boolean value1, Boolean value2, Boolean defaultValue) {
        if (value1 != null) { return value1; }
        if (value2 != null) { return value2; }
        return defaultValue;
    }

    private MimeType mergeMimeType(MimeType value1, String value2, MimeType defaultValue) {
        if (value1 != null) { return value1; }
        if (!isBlank(value2)) { return MimeType.valueOf(value2.toUpperCase()); }
        return defaultValue;
    }
}
