/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public final class MailOptions {
    private final String mailer;
    private final Transport transport;
    private final String host;
    private final Integer port;
    private final Boolean auth;
    private final String username;
    private final String password;
    private final String from;
    private final String to;
    private final String cc;
    private final String bcc;
    private final String subject;
    private final String content;
    private final MimeType mimeType;
    private final List<String> attachments = new ArrayList<>();
    private final Properties props = new Properties();

    private MailOptions(String mailer, Transport transport, String host, Integer port, Boolean auth, String username, String password, String from, String to, String cc, String bcc, String subject, String content, MimeType mimeType) {
        this.mailer = mailer;
        this.transport = transport;
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.username = username;
        this.password = password;
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.content = content;
        this.mimeType = mimeType;
    }

    public String getMailer() {
        return mailer;
    }

    public Transport getTransport() {
        return transport;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Boolean isAuth() {
        return auth;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCc() {
        return cc;
    }

    public String getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public Properties getProps() {
        return props;
    }

    public Properties getMailProperties() {
        Properties props = new Properties();
        props.putAll(this.props);
        String transport = this.transport.name().toLowerCase();
        props.put("mail." + transport + ".host", host);
        props.put("mail." + transport + ".port", port);
        if (auth) {
            props.put("mail." + transport + ".auth", "true");
        }
        return props;
    }

    public static class Builder {
        private String mailer = "Griffon Mail Service";
        private Transport transport = Transport.SMTP;
        private String host;
        private Integer port = 25;
        private Boolean auth = false;
        private String username;
        private String password;
        private String from;
        private String to;
        private String cc;
        private String bcc;
        private String subject = "";
        private String content;
        private MimeType mimeType = MimeType.TEXT;
        private List<String> attachments = new ArrayList<>();
        private Properties props = new Properties();

        public MailOptions build() {
            MailOptions ops = new MailOptions(mailer, transport, host, port, auth, username, password, from, to, cc, bcc, subject, content, mimeType);
            ops.getAttachments().addAll(attachments);
            ops.getProps().putAll(props);
            return ops;
        }

        public Builder withMailer(String mailer) {
            this.mailer = mailer;
            return this;
        }

        public Builder withTransport(Transport transport) {
            this.transport = transport;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder withAuth(Boolean auth) {
            this.auth = auth;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder withTo(String to) {
            this.to = to;
            return this;
        }

        public Builder withCc(String cc) {
            this.cc = cc;
            return this;
        }

        public Builder withBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withMimeType(MimeType mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withAttachments(List<String> attachments) {
            this.attachments = attachments;
            return this;
        }

        public Builder withProps(Properties props) {
            this.props.putAll(props);
            return this;
        }

        public Builder withProps(Map<String, Object> props) {
            this.props.putAll(props);
            return this;
        }
    }
}
