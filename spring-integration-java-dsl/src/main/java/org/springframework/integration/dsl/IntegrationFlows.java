/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.dsl;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.channel.MessageChannelSpec;
import org.springframework.integration.dsl.core.ComponentsRegistration;
import org.springframework.integration.dsl.core.MessageProducerSpec;
import org.springframework.integration.dsl.core.MessageSourceSpec;
import org.springframework.integration.dsl.core.MessagingGatewaySpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.FixedSubscriberChannelPrototype;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.MessageChannelReference;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

/**
 * The central factory for fluent {@link IntegrationFlowBuilder} API.
 *
 * @author Artem Bilan
 */
public final class IntegrationFlows {

	/**
	 * @param messageChannelName the name of existing {@link org.springframework.messaging.MessageChannel} bean.
	 *                           The new {@link org.springframework.integration.channel.DirectChannel} bean will be
	 *                           created on context startup, if there is no bean with this name.
	 * @return new {@link IntegrationFlowBuilder}
	 */
	public static IntegrationFlowBuilder from(String messageChannelName) {
		return from(new MessageChannelReference(messageChannelName));
	}

	/**
	 * @param messageChannelName the name for {@link org.springframework.integration.channel.FixedSubscriberChannel}
	 *                           to be created on context startup, not reference.
	 * @return new {@link IntegrationFlowBuilder}
	 */
	public static IntegrationFlowBuilder fromFixedMessageChannel(String messageChannelName) {
		return from(new FixedSubscriberChannelPrototype(messageChannelName));
	}

	public static IntegrationFlowBuilder from(Function<Channels, MessageChannelSpec<?, ?>> channels) {
		Assert.notNull(channels);
		return from(channels.apply(new Channels()));
	}

	public static IntegrationFlowBuilder from(MessageChannelSpec<?, ?> messageChannelSpec) {
		Assert.notNull(messageChannelSpec);
		return from(messageChannelSpec.get());
	}

	public static IntegrationFlowBuilder from(MessageChannel messageChannel) {
		return new IntegrationFlowBuilder().channel(messageChannel);
	}

	public static IntegrationFlowBuilder fromMessageSource(
			Function<MessageSources, MessageSourceSpec<?, ?>> messageSources) {
		return fromMessageSource(messageSources, null);
	}

	public static IntegrationFlowBuilder fromMessageSource(
			Function<MessageSources, MessageSourceSpec<?, ?>> messageSources,
			Consumer<SourcePollingChannelAdapterSpec> endpointConfigurer) {
		Assert.notNull(messageSources);
		return from(messageSources.apply(new MessageSources()), endpointConfigurer);
	}

	public static IntegrationFlowBuilder from(MessageSourceSpec<?, ? extends MessageSource<?>> messageSourceSpec) {
		return from(messageSourceSpec, null);
	}

	public static IntegrationFlowBuilder from(MessageSourceSpec<?, ? extends MessageSource<?>> messageSourceSpec,
			Consumer<SourcePollingChannelAdapterSpec> endpointConfigurer) {
		Assert.notNull(messageSourceSpec);
		return from(messageSourceSpec.get(), endpointConfigurer, registerComponents(messageSourceSpec));
	}

	public static IntegrationFlowBuilder from(MessageSource<?> messageSource) {
		return from(messageSource, null);
	}

	public static IntegrationFlowBuilder from(MessageSource<?> messageSource,
			Consumer<SourcePollingChannelAdapterSpec> endpointConfigurer) {
		return from(messageSource, endpointConfigurer, null);
	}

	private static IntegrationFlowBuilder from(MessageSource<?> messageSource,
			Consumer<SourcePollingChannelAdapterSpec> endpointConfigurer,
			IntegrationFlowBuilder integrationFlowBuilder) {
		SourcePollingChannelAdapterSpec spec = new SourcePollingChannelAdapterSpec(messageSource);
		if (endpointConfigurer != null) {
			endpointConfigurer.accept(spec);
		}
		if (integrationFlowBuilder == null) {
			integrationFlowBuilder = new IntegrationFlowBuilder();
		}
		return integrationFlowBuilder.addComponent(spec)
				.currentComponent(spec);
	}

	public static IntegrationFlowBuilder fromMessageProducer(
			Function<MessageProducers, MessageProducerSpec<?, ?>> messageProducerFactory) {
		return from(messageProducerFactory.apply(new MessageProducers()));
	}

	public static IntegrationFlowBuilder from(MessageProducerSpec<?, ?> messageProducerSpec) {
		return from(messageProducerSpec.get(), registerComponents(messageProducerSpec));
	}

	public static IntegrationFlowBuilder from(MessageProducerSupport messageProducer) {
		return from(messageProducer, null);
	}

	private static IntegrationFlowBuilder from(MessageProducerSupport messageProducer,
			IntegrationFlowBuilder integrationFlowBuilder) {
		DirectFieldAccessor dfa = new DirectFieldAccessor(messageProducer);
		MessageChannel outputChannel = (MessageChannel) dfa.getPropertyValue("outputChannel");
		if (outputChannel == null) {
			outputChannel = new DirectChannel();
			messageProducer.setOutputChannel(outputChannel);
		}
		if (integrationFlowBuilder == null) {
			integrationFlowBuilder = from(outputChannel);
		}
		else {
			integrationFlowBuilder.channel(outputChannel);
		}
		return integrationFlowBuilder.addComponent(messageProducer);
	}

	public static IntegrationFlowBuilder fromMessagingGateway(
			Function<MessagingGateways, MessagingGatewaySpec<?, ?>> gatewayFactory) {
		return from(gatewayFactory.apply(new MessagingGateways()));
	}

	public static IntegrationFlowBuilder from(MessagingGatewaySpec<?, ?> inboundGatewaySpec) {
		return from(inboundGatewaySpec.get(), registerComponents(inboundGatewaySpec));
	}

	public static IntegrationFlowBuilder from(MessagingGatewaySupport inboundGateway) {
		DirectFieldAccessor dfa = new DirectFieldAccessor(inboundGateway);
		MessageChannel outputChannel = (MessageChannel) dfa.getPropertyValue("requestChannel");
		if (outputChannel == null) {
			outputChannel = new DirectChannel();
			inboundGateway.setRequestChannel(outputChannel);
		}
		return from(outputChannel).addComponent(inboundGateway);
	}

	private static IntegrationFlowBuilder from(MessagingGatewaySupport inboundGateway,
			IntegrationFlowBuilder integrationFlowBuilder) {
		DirectFieldAccessor dfa = new DirectFieldAccessor(inboundGateway);
		MessageChannel outputChannel = (MessageChannel) dfa.getPropertyValue("requestChannel");
		if (outputChannel == null) {
			outputChannel = new DirectChannel();
			inboundGateway.setRequestChannel(outputChannel);
		}
		if (integrationFlowBuilder == null) {
			integrationFlowBuilder = from(outputChannel);
		}
		else {
			integrationFlowBuilder.channel(outputChannel);
		}
		return integrationFlowBuilder.addComponent(inboundGateway);
	}

	private static IntegrationFlowBuilder registerComponents(Object spec) {
		if (spec instanceof ComponentsRegistration) {
			return new IntegrationFlowBuilder()
					.addComponents(((ComponentsRegistration) spec).getComponentsToRegister());
		}
		return null;
	}

	private IntegrationFlows() {
	}

}
