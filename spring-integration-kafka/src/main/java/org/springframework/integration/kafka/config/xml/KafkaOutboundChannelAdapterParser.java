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
package org.springframework.integration.kafka.config.xml;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Soby Chacko
 *
 */
public class KafkaOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {


    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {

        final BeanDefinitionBuilder kafkaProducerMessageHandlerBuilder =
                                BeanDefinitionBuilder.genericBeanDefinition(KafkaProducerMessageHandler.class);

        String kafkaServerBeanName = element.getAttribute("kafka-producer-context-ref");
        if (StringUtils.hasText(kafkaServerBeanName)) {
            kafkaProducerMessageHandlerBuilder.addConstructorArgReference(kafkaServerBeanName);
        }

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "kafka-encoder");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "kafka-key-encoder");

        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "topic");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "topic");

        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "key-class");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(kafkaProducerMessageHandlerBuilder, element, "value-class");

        return kafkaProducerMessageHandlerBuilder.getBeanDefinition();
    }
}
