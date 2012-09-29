/*
 * Copyright 2011-2012 the original author or authors.
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
package org.springframework.integration.splunk.outbound.e2e;


import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.splunk.entity.SplunkData;
import org.springframework.integration.support.MessageBuilder;


/**
 * @author Jarred Li
 * @since 1.0
 * 
 */
public class SplunkOutboundChannelAdapterTests {

	public static void main(String args[]) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"SplunkOutboundChannelAdapterTests-context.xml", SplunkOutboundChannelAdapterTests.class);
		ctx.start();

		generateMessage(ctx);
	}

	private static void generateMessage(ClassPathXmlApplicationContext ctx) {
		QueueChannel channel = ctx.getBean("outputToSplunkWithMessageStore", QueueChannel.class);

		SplunkData data = new SplunkData("spring", "spring:example");
		data.setCommonDesc("description");

		Message<SplunkData> msg = MessageBuilder.withPayload(data).build();
		channel.send(msg);
		msg = MessageBuilder.withPayload(data).build();
		channel.send(msg);
	}

}
