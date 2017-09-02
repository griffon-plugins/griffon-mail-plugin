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
package org.codehaus.griffon.runtime.mail

import com.icegreen.greenmail.junit.GreenMailRule
import com.icegreen.greenmail.util.ServerSetupTest
import griffon.core.test.GriffonUnitRule
import griffon.plugins.mail.MailHandler
import griffon.plugins.mail.MailOptions
import griffon.plugins.mail.MimeType
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

import static java.util.concurrent.TimeUnit.SECONDS
import static javax.mail.Message.RecipientType.CC
import static javax.mail.Message.RecipientType.TO
import static org.awaitility.Awaitility.await

class MailSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    final GriffonUnitRule griffon = new GriffonUnitRule()

    @Rule
    final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP)

    @Inject
    private MailHandler mailHandler

    @Unroll
    def 'Send email with specific options'() {
        given:
        String sender = 'test@acme.com'
        String receiver = 'griffon@acme.com'
        String cc = 'copy@acme.com'
        String bcc = 'hidden@acme.com'
        String content = '<html><body>Test<body></html>'

        MailOptions options = new MailOptions.Builder()
            .withHost('localhost')
            .withPort(3025)
            .withFrom(sender)
            .withTo(receiver)
            .withCc(cc)
            .withBcc(bcc)
            .withSubject('Test')
            .withMimeType(MimeType.HTML)
            .withContent(content)
            .build()

        when:
        mailHandler.sendMail(options)
        await().timeout(3, SECONDS)
            .until({ greenMail.receivedMessages.size() == 3 })

        then:
        greenMail.getReceivedMessages()[0].from*.toString() == [sender]
        greenMail.getReceivedMessages()[0].getRecipients(TO)*.toString() == [receiver]
        greenMail.getReceivedMessages()[1].from*.toString() == [sender]
        greenMail.getReceivedMessages()[1].getRecipients(CC)*.toString() == [cc]
        greenMail.getReceivedMessages()[2].from*.toString() == [sender]
    }
}