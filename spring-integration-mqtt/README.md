Spring Integration MQTT Support
===============================

`inbound` and `outbound` channel adapters are provided for [MQ Telemetry Transport (MQTT)][]. The current implementation uses the [Eclipse Paho][] client.

## Example Configurations

```xml
<int-mqtt:message-driven-channel-adapter id="twoTopicsAdapter"
	client-id="foo"
	url="tcp://localhost:1883"
	topics="bar, baz"
	channel="out" />

<int-mqtt:outbound-channel-adapter id="withDefaultConverter"
		client-id="foo"
		url="tcp://localhost:1883"
		default-qos="1"
		default-retained="true"
		default-topic="bar"
		channel="target" />
```

*Spring Integration* messages sent to the outbound adapter can have headers `mqtt_topic, mqtt_qos, mqtt_retained` which will override the defaults configured on the adapter.

Inbound messages will have headers 

    mqtt_topic       - the topic from which the message was received
    mqtt_duplicate   - true if the message is a duplicate
    mqtt_qos         - the quality of service

Both adapters use a `MqttPahoClientFactory` to get a client instance; the same factory also provides connection options from configured properties (such as user/password). The client factory bean (`DefaultMqttPahoClientFactory`) is provided to the adapter using the `client-factory` attribute. When not provided, a default factory instance is used.

Currently tested with the RabbitMQ MQTT plugin.

## Support

Check out the [Spring Integration forums][] and the [spring-integration][spring-integration tag] tag
on [Stack Overflow][]. [Commercial support][] is available, too.

## Resources

* [Eclipse Paho][]

## Related GitHub projects

* [Spring Integration][]
* [Spring Integration Samples][]
* [Spring Integration Templates][]
* [Spring Integration Dsl Groovy][]
* [Spring Integration Dsl Scala][]
* [Spring Integration Pattern Catalog][]

For more information, please also don't forget to visit the [Spring Integration][] website.

[Spring Integration]: https://github.com/SpringSource/spring-integration
[Commercial support]: http://springsource.com/support/springsupport
[Spring Integration forums]: http://forum.springsource.org/forumdisplay.php?42-Integration
[spring-integration tag]: http://stackoverflow.com/questions/tagged/spring-integration
[Spring Integration Samples]: https://github.com/SpringSource/spring-integration-samples
[Spring Integration Templates]: https://github.com/SpringSource/spring-integration-templates/tree/master/si-sts-templates
[Spring Integration Dsl Groovy]: https://github.com/SpringSource/spring-integration-dsl-groovy
[Spring Integration Dsl Scala]: https://github.com/SpringSource/spring-integration-dsl-scala
[Spring Integration Pattern Catalog]: https://github.com/SpringSource/spring-integration-pattern-catalog
[Stack Overflow]: http://stackoverflow.com/faq
[Eclipse Paho]: http://www.eclipse.org/paho/
[open paho bug]: https://bugs.eclipse.org/bugs/show_bug.cgi?id=382471
[MQ Telemetry Transport (MQTT)]: http://mqtt.org/