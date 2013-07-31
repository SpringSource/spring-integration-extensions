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
package org.springframework.integration.kafka.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.integration.Message;

import java.util.Collection;
import java.util.Map;

/**
 * @author Soby Chacko
 * @since 0.5
 */
public class KafkaProducerContext<K,V> implements BeanFactoryAware {
	private Map<String, ProducerConfiguration<K,V>> topicsConfiguration;

	@SuppressWarnings("unchecked")
	public void send(final Message<?> message) throws Exception {
		final ProducerConfiguration<K,V> producerConfiguration =
						getTopicConfiguration(message.getHeaders().get("topic", String.class));

		if (producerConfiguration != null) {
			producerConfiguration.send(message);
		}
	}

	private ProducerConfiguration<K,V> getTopicConfiguration(final String topic){
		final Collection<ProducerConfiguration<K,V>> topics = topicsConfiguration.values();

		for (final ProducerConfiguration<K,V> producerConfiguration : topics){
			if (producerConfiguration.getProducerMetadata().getTopic().equals(topic)){
				return producerConfiguration;
			}
		}

		return null;
	}

	public Map<String, ProducerConfiguration<K,V>> getTopicsConfiguration() {
		return topicsConfiguration;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
		topicsConfiguration =
				(Map<String, ProducerConfiguration<K,V>>) (Object)
				((ListableBeanFactory)beanFactory).getBeansOfType(ProducerConfiguration.class);
	}
}
