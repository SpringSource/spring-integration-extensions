/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.integration.cassandra.config.xml;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler.OperationType;
import org.springframework.integration.config.ExpressionFactoryBean;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Filippo Balicchia
 *
 */
public class CassandraOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CassandraMessageHandler.class);
        
        
        String cassandraTemplate = element.getAttribute("cassandra-template");
        String operationType = element.getAttribute("operation-type");
        String writeOptionsInstance = element.getAttribute("write-options");
        String ingestQuery = element.getAttribute("cql-ingest");
        String outputChannel = element.getAttribute("output-channel");
        String query = element.getAttribute("query");
        String producesReply = element.getAttribute("produces-reply");

		if (StringUtils.isEmpty(cassandraTemplate)){
			parserContext.getReaderContext().error("cassandra-template is empty", element);
		}

		if (StringUtils.isEmpty(operationType)) {
			parserContext.getReaderContext().error("operation-type need to be specified", element);
		}
		
		OperationType queryType = CassandraMessageHandler.OperationType.toType(operationType);
		builder.addConstructorArgValue(new RuntimeBeanReference(cassandraTemplate));
		builder.addPropertyValue("queryType", queryType);
		
		if(StringUtils.isNotEmpty(writeOptionsInstance)){
			builder.addPropertyReference("writeOptions", writeOptionsInstance);
		}
		
		
		if(StringUtils.isNotEmpty(outputChannel)){
			builder.addPropertyReference("outputChannel", outputChannel);
		}
		
		if (StringUtils.isNotEmpty(ingestQuery) && !"INSERT".equalsIgnoreCase(operationType)) {
			parserContext.getReaderContext().error( "Ingest cql query can be apply only with insert operation",element);
		}
		else if (StringUtils.isNotEmpty(ingestQuery)) {
			builder.addPropertyValue("ingestQuery", ingestQuery);
		}
		
        if (StringUtils.isNotEmpty(query)){
            builder.addPropertyValue("query", query);
        }

        if (StringUtils.isNotEmpty(producesReply)){
            builder.addPropertyValue("producesReply", producesReply);
        }
		
		
		List<Element> parameterExpression = DomUtils.getChildElementsByTagName(element, "parameter-expressions");
		if (!CollectionUtils.isEmpty(parameterExpression)) {
					ManagedMap<String, Object> parameterExpressionsMap = new ManagedMap<String, Object>();
					for (Element parmaterExpressionElement : parameterExpression) {
						String name = parmaterExpressionElement.getAttribute("name");
						String expression = parmaterExpressionElement.getAttribute("expression");
						BeanDefinitionBuilder factoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExpressionFactoryBean.class);
						factoryBeanBuilder.addConstructorArgValue(expression);
						parameterExpressionsMap.put(name,  factoryBeanBuilder.getBeanDefinition());
					}
					builder.addPropertyValue("parameterExpressions", parameterExpressionsMap);
		}
		
		
		
		return builder.getBeanDefinition();
    }
    
    
}