/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.kafka.samples;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;

public class OutboundRunner {

    public static void main(String args[]) {
       ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                       "../config/xml/kafkaOutboundAdapterParserTests-context.xml",
               OutboundRunner.class);


        ctx.start();

        final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);
                System.out.println(channel.getClass());


               for(int i= 0; i < 1000; i++)  {
                channel.send(new GenericMessage<String>("hello Fom ob adapter -  " + i));
               }
        System.out.println("message sent");
    }

}
