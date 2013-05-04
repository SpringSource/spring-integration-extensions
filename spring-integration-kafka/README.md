Spring Integration Kafka Adapter
=================================================


Welcome to the *Spring Integration Kafka adapter*. Apache Kafka is a distributed publish-subscribe messaging system that is designed for handling terra bytes of high throughput
data at constant time. For more information on Kafka and its design goals, please see [Kafka main page](http://kafka.apache.org/)

Spring Integration Kafka adapters are built for Kafka 0.8 and since Kafka 0.8 is not backward compatible with any previous versions of Kafka, Spring Integration will not
support any Kafka versions prior to 0.8. As of this writing, Kafka 0.8 is still WIP.

Spring Integration Kafka project currently supports the following two components. Please keep in mind that
this is very early stage in development and do not yet fully make use of all the features that Kafka provides.

* Outbound Channel Adapter
* Inbound Channel Adapter based on the High level consumer API

Outbound Channel Adapter:
--------------------------------------------

The Outbound channel adapter is used to send messages to Kafka. Messages are read from a Spring Integration channel. One can specify this channel in the application context and then wire
this in the application where messages are sent to kafka.

Once a channel is configured, then messages can be sent to Kafka through this channel. Obviously, Spring Integration specific messages are sent to the adapter and then it will
internally get converted into Kafka messages before sending. In the current version of the outbound adapter,
you have to specify a message key and the topic as header values and the message to send as the payload.
Here is an example.

```java
    final MessageChannel channel = ctx.getBean("inputToKafka", MessageChannel.class);

    channel.send(
            MessageBuilder.withPayload(payload).
                    setHeader("messageKey", "key")
                    .setHeader("topic", "test").build());
```

This would create a message with a payload. In addition to this, it also creates two header entries as key/value pairs - one for
the message key and another for the topic that this message belongs to.

Here is how kafka outbound channel adapter is configured:

```xml
    <int-kafka:outbound-channel-adapter id="kafkaOutboundChannelAdapter"
                                        kafka-producer-context-ref="kafkaProducerContext"
                                        auto-startup="false"
                                        channel="inputToKafka"
            >
        <int:poller fixed-delay="1000" time-unit="MILLISECONDS" receive-timeout="0" task-executor="taskExecutor"/>
    </int-kafka:outbound-channel-adapter>
```

The key aspect in this configuration is the producer-context-ref. Producer context contains all the producer configuration for all the topics that this adapter is expected to handle.
A channel in which messages are arriving is configured with the adapter and therefore
any message sent to that channel will be handled by this adapter. You can also configure a poller
depending on the
type of the channel used. For example, in the above configuration, we use a queue based channel
and thus a poller is configured with a task executor. If no messages are available in the queue it will timeout immediately because of
the receive-timeout configuration. Then it will poll again with a delay of 1 second.

Producer context is at the heart of the kafka outbound adapter. Here is an example of how it is configured.

```xml
    <int-kafka:producer-context id="kafkaProducerContext">
        <int-kafka:producer-configurations>
            <int-kafka:producer-configuration broker-list="localhost:9092"
                       key-class-type="java.lang.String"
                       value-class-type="java.lang.String"
                       topic="test1"
                       value-encoder="kafkaEncoder"
                       key-encoder="kafkaEncoder"
                       compression-codec="default"/>
            <int-kafka:producer-configuration broker-list="localhost:9092"
                       topic="test2"
                       compression-codec="default"
                       async="true"/>
        </int-kafka:producer-configurations>
    </int-kafka:producer-context>
```

There are a few things going on here. So, lets go one by one. First of all, producer context is simply a holder of, as the name
indicates, a context for the Kafa producer. It contains one ore more producer configurations. Each producer configuration
is ultimately gets translated into a Kafka native producer. Each producer configuration is per topic based right now.
If you go by the above example, there are two producers generated from this configuration - one for topic named
test1 and another for test2. Each producer can take the following:

    broker-list            list of comma separated brokers that this producer connects to
    topic                  topic name
    compression-codec      any compression to be used. Default is no compression. Supported compression codec are gzip and snappy. Anything else would
                           result in no compression
    value-encoder          serializer to be used for encoding messages.
    key-encoder            serializer to be used for encoding the partition key
    key-class-type         Type of the key class. This will be ignored if no key-encoder is provided
    value-class-type       The type of the value class. This will be ignored if no value-encoder is provided.
    partitioner            custom implementation of a Kafka Partitioner interface.
    async                  true/false - default is false. Setting this to true would make the Kafka producer to use
                           an async producer
    batch-num-messages     number of messages to batch at the producer. If async is false, then this has no effect.

The value-encoder and key-encoder are referring to other spring beans. They are essentially implementations of an
interface provided by Kafka, the Encoder interface. Similarly, partitioner also refers a Spring bean which implements
the Kafka Partitioner interface.

Here is an example of configuring an encoder.

```xml
    <bean id="kafkaEncoder" class="org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaEncoder">
        <constructor-arg value="java.lang.String" />
    </bean>
```

Spring Integration Kafaka adapter provides Apache Avro backed encoders out of the box, as this is a popular choice
for serialization in the big data spectrum. If no encoders are specified as beans, the default encoders provided
by Kafka will be used. On that not, if the encoder is configured only for the message and not for the key, the same encoder
will be used for both. These are standard Kafka behaviors. Spring Integration Kafka adapter does simply enforce those behaviours.
Kafka default encoder expects the data to come as byte arrays and it is a no-op encoder, i.e. it just takes the byte array as it is.
When default encoders are used, there are two ways a message can be sent.
Either, the sender of the message to the channel
can simply put byte arrays as message key and payload. Or, the key and value can be sent as Java Serializable object.
In the latter case, the Kafka adapter will automatically convert them to byte arrays before sending it to Kafka broker.
If the encoders are default and the objets sent are not serializalbe, then that would cause an error. By providing explicit encoders
it is totally up to the developer to configure how the objects are serialized. In that case, the objects may or may not implement
the Serializable interface.

Kafka provides a StringEncoder out of the box. It takes a Kafka specific VerifiableProperties object along with its
constructor that wraps a regular Java.util.Properties object. The StringEncoder is great when writing a direct Java client.
However, when using Spring Integration Kafka adapter, a wrapper class for this same StringEncoder is available which makes
using it from Spring a bit easier as you don't have to create any Kafka specific objects to create a StringEncoder. Rather, you can inject
any properties to it in the Spring way. Kafka StringEncoder looks at a specific property for the type of encoding scheme used from the properties provided.
This same value can be injected as a property on the spring bean provided by the kafka support. Spring Integration provided StringEncoder is available
in the package org.springframework.integration.kafka.serializer.common.StringEncoder. The avro support for serialization is
also available in a package called avro under serializer.

Inbound Channel Adapter:
--------------------------------------------

The Inbound channel adapter is used to consume messages from Kafka. These messages will be placed into a Spring Integration channel as Spring Integration specific Messages.
Kafka provides two types of consumer API's primarily. One is a high level consumer and the other is called Simple Consumer. Highlevel consumer is pretty complex inside and handles
a lot of logic. Nonetheless, for the client, using the high level API is straightforward. Although easy to use, High level consumer
does not provide any offset management. So, if you want to rewind and re-fetch messages, it is not possible to do so using the
high level conusmer API. Offsets are managed by the Zookeeper internally. If your use case does not require you to manage offsets
or re-read messages from the same consumer, then high level consumer is a perfect fit. Spring Integration Kafka inbound channel adapter
currently support only the high level consumer. Here are the details of configuring one.

```xml
	<int-kafka:inbound-channel-adapter id="kafkaInboundChannelAdapter"
                                           kafka-consumer-context-ref="consumerContext"
                                           auto-startup="false"
                                           channel="inputFromKafka">
            <int:poller fixed-delay="10" time-unit="MILLISECONDS" max-messages-per-poll="5"/>
        </int-kafka:inbound-channel-adapter>
```

Since this inbound channel adapter uses a Polling Channel under the hood, it must be configured with a Poller. A notable difference
between the poller configured with this inbound adapter and other pollers is that the receive-timeout specified on this poller
does not have any effect. The reason for this is because of the way Kafka implements iterators on the consumer stream.
It is using a BlockingQueue internally and thus it would wait indefinitely. Instead of interrupting the underlying thread,
we are leveraging on direct Kafka support for consumer time out. It is configured on the consumer context. Everyting else
 is pretty much the same as in a regular inbound adapter. Any messages that it receives will be sent to the channel configured with it.

Inbound Kafka Adapter must specify a kafka-consumer-context-ref element and here is how it may be configured:

```xml
   <int-kafka:consumer-context id="consumerContext"
                                   consumer-timeout="4000"
                                   zookeeper-connect="zookeeperConnect">
           <int-kafka:consumer-configurations>
               <int-kafka:consumer-configuration group-id="default"
                       value-decoder="valueDecoder"
                       key-decoder="valueDecoder"
                       max-messages="5000">
                   <int-kafka:topic id="test1" streams="4"/>
                   <int-kafka:topic id="test2" streams="4"/>
               </int-kafka:consumer-configuration>
           </int-kafka:consumer-configurations>
       </int-kafka:consumer-context>
```

Consumer context requires a referecence to a zookeeper-connect which dictates all the zookeeper specific configuration details.
Here is how a zookeeper-connect is configured.

```xml
    <int-kafka:zookeeper-connect id="zookeeperConnect" zk-connect="localhost:2181" zk-connection-timeout="6000"
                        zk-session-timeout="6000"
                        zk-sync-time="2000" />
```

zk-connect attribute is where you would specify the zookeeper connection. All the other attributes would get translated into their
zookeeper counter-part attributes by the consumer.

In the above consumer context, you can also specify a consumer-timeout value which would be used to timeout the consumer in case of no messages to consume.
This timeout would be applicable to all the streams (threads) in the consumer. The default value for this in Kafka is -1 which would make it wait
indefinitely. However, Sping Integration overrides it to be 5 seconds in order to make sure that no threads are blocking indefinitely in the lifecycle of the application and thereby
giving them a chance to free up any resources or locks that they hold. It is recommended to override this value so as to meet any special use cases.
By providing a reasonable consumer-timeout and a fixed-delay value on the poller, this inbound adapter could essentially simulate a message driven behaviour.

consumer context takes consumer-confgurations which are at the center piece of the inbound adapter. It is a group of one or more
consumer-configuration elements which consists of a consumer group dicatated by the group-id. Each consumer-configuration
can be configured with one or more kafka-topic.

In the above example provided, we have only one consumer-configuration provided that consumes messages from two topics each having 4 streams. These streams
are fundamentally same as the number of partitions that a topic is configured with in the producer. For instance, if you configure your topic with
4 partitions, then the maximum number of streams that you may have in the consumer is also 4. Any more than this would be a no-op.
If you have less number of streams than the available partitions, then messages from multiple partitions will be sent to available streams.
Therefore, it is a good practice to limit the number of streams for a topic in the consumer configuration to the number of partitions configured for the topic. There may be situations
in which a partition may be gone during runtime and in that case the stream receiving data from the partition will simply timeout and whenever the partition comes back up again it would start read data from it again.

Consumer configuration can also be configured with optional decoders for key and value. The default ones provided by Kafka are basically no-ops and would consume as byte arrays.
If you provided an encoder for key/value in the producer, then it is recommended to provide corresponding decoders.
Spring Integration Kafka adapter gives Apache Avro based data serialization components
out of the box. You can use any serialization component for this purpose. Here is how you would configure a kafka decoder that is Avro backed.

```xml
   <bean id="kafkaDecoder" class="org.springframework.integration.kafka.serializer.avro.AvroBackedKafkaDecoder">
           <constructor-arg type="java.lang.Class" value="java.lang.String" />
   </bean>
```


Another important attribute for the consumer-configuraton is the max-messages. Please note that this is different from the max-messages-per-poll configured on the inbound adapter element.
There it means the number of times the receive method on the adapter called. The max-messages on consumer configuration is different.
Kafka is used mainly for big data purposes and usually that means the influx of large amount of data constantly. Because of this, each time a receive is invoked
on the adapter, you would basically get a collection of messages. The maximum number of messages to retrieve for a topic in each execution of the
receive is what configured through the max-messages attribute on the consumer-configuration. Basically, if the use case is to receive a constant stream of
large number of data, simply specifying a receive-timeout alone would not be enough. You would also need to specify the max number of messages to receive.

The type of the payload of the Message returned by the adapter and put into the channel configured is the following:

```java
Map<String, Map<Integer, List<Object>>>
```

It is a java.util.Map that contains the topic string consumed as the key and another Map as the value.
The inner map's key will be the stream (partition) number and value will be a list of message payloads. The reason for this complex return type is
due to the way Kafka orders the messages. In the high level consumer, all the messages received in a single stream for a single partition
is guaranteed to be in order. For example, if I have a topic named test configured with 4 partitions and I have 4 corresponding streams
in the consumer, then I would receive data in all the consumer streams in the same order as they were put
in the corresponding partitions. This is another reason to set the number of consumer streams for a topic same
as the number of broker partitions configured for that topic. Lets say that the number of streams are less than the number of partitions. Then there is no
guarantee for any order other than just the fact that a single stream will contain messages from multiple partitions and the messages from a single partition received will
still be kept contiguously. Then, probably there is no way to find out which message came from which partition.
By keeping the partition information in the response from the adapter, we make sure that the order sent by the producer
is preserved.

A downstream component which receives the data from the inbound adapter can cast the SI payload to the above
Map.

If your use case does not require ordering of messages during consumption, then you can easily pass this
payload to a standard SI transformer and just get a full dump of the actual payload sent by Kafka.





