# log4j2-elasticsearch5-bulkprocessor
This log4j2 appender plugin uses Elasticsearch `org.elasticsearch.action.bulk.BulkProcessor` to push logs to Elasticsearch 5.x and 6.x clusters. By default, FasterXML is used generate output via `org.apache.logging.log4j.core.layout.JsonLayout`.

## Maven

To use it, add this XML snippet to your `pom.xml` file:
```xml
<dependency>
    <groupId>org.appenders.log4j</groupId>
    <artifactId>log4j2-elasticsearch5-bulkprocessor</artifactId>
    <version>1.3.5</version>
</dependency>
```

## Appender configuration

Add this snippet to `log4j2.xml` configuration:
```xml
<Appenders>
    <Elasticsearch name="elasticsearchAsyncBatch">
        <IndexName indexName="log4j2" />
        <AsyncBatchDelivery>
            <IndexTemplate name="log4j2" path="classpath:indexTemplate.json" />
            <ElasticsearchBulkProcessor serverUris="tcp://localhost:9300" />
        </AsyncBatchDelivery>
    </Elasticsearch>
</Appenders>
```

or [configure programmatcally](https://github.com/rfoltyns/log4j2-elasticsearch/blob/master/log4j2-elasticsearch5-bulkprocessor/src/test/java/org/appenders/log4j2/elasticsearch/bulkprocessor/smoke/SmokeTest.java).

It's highly encouraged to put this plugin behind `Async` appender or `AsyncLogger`. See [log4j2.xml](https://github.com/rfoltyns/log4j2-elasticsearch/blob/master/log4j2-elasticsearch5-bulkprocessor/src/test/resources/log4j2.xml) example.

### Delivery frequency
Delivery frequency can be adjusted via `AsyncBatchDelivery` attributes:
* `deliveryInterval` - millis between deliveries
* `batchSize` - maximum (rough) number of logs in one batch

Delivery is triggered each `deliveryInterval` or when number of undelivered logs reached `batchSize`.

`deliveryInterval` is the main driver of delivery. However, in high load scenarios, both parameters should be configured accordingly to prevent sub-optimal behaviour. See [Indexing performance tips](https://www.elastic.co/guide/en/elasticsearch/guide/current/indexing-performance.html) and [Performance Considerations](https://www.elastic.co/blog/performance-considerations-elasticsearch-indexing) for more info.

### Message output
There are at least three ways to generate output
* (default) JsonLayout will serialize LogEvent using Jackson mapper configured in log4j-core
* `messageOnly="true"` can be configured set to make use of user-provided (or default) `org.apache.logging.log4j.message.Message.getFormattedMessage()` implementation
* custom `org.apache.logging.log4j.core.layout.AbstractStringLayout` can be provided to appender config to use any other serialization mechanism

### Failover
Each unsuccessful batch can be redirected to any given `FailoverPolicy` implementation. By default, each log entry will be separately delivered to configured strategy class, but this behaviour can be amended by providing custom `ClientObjectFactory` implementation.

### Index name
Since 1.1, index name can be defined using `IndexName` tag:

```xml
<Elasticsearch name="elasticsearchAsyncBatch">
    ...
    <IndexName indexName="log4j2" />
    ...
</Elasticsearch>
```

### Index rollover
Since 1.1, rolling index can be defined using `RollingIndexName` tag:

```xml
<Elasticsearch name="elasticsearchAsyncBatch">
    ...
    <!-- zone is optional. OS timezone is used by default -->
    <RollingIndexName indexName="log4j2" pattern="yyyy-MM-dd" timeZone="Europe/Warsaw" />
    ...
</Elasticsearch>
```

`pattern` accepts any valid date pattern with years down to millis (although rolling daily or weekly should be sufficient for most use cases)
`IndexName` and `RollingIndexName` are mutually exclusive. Only one per appender should be defined, otherwise they'll override each other.

### Index template
Since 1.1, [Index templates](https://www.elastic.co/guide/en/elasticsearch/reference/5.0/indices-templates.html) can be created during appender startup. Template can be loaded from specified file or defined directly in the XML config:

```xml
<AsyncBatchDelivery>
    <IndexTemplate name="template1" path="<absolute_path_or_classpath>" />
    ...
</AsyncBatchDelivery>
```
or
```xml
<AsyncBatchDelivery>
    <IndexTemplate name="template1" >
    {
        // your index template in JSON format
    }
    </IndexTemplate>
    ...
</AsyncBatchDelivery>
```

### SSL/TLS
Since 1.2, secure TCP transport can be configured using `XPackAuth` tag:

#### PEM cert config
```xml
<ElasticsearchBulkProcessor serverUris="tcp://localhost:9300">
    <XPackAuth>
        <BasicCredentials username="admin" password="changeme" />
        <PEM keyPath="${sys:pemCertInfo.keyPath}"
             keyPassphrase="${sys:pemCertInfo.keyPassphrase}"
             clientCertPath="${sys:pemCertInfo.clientCertPath}"
             caPath="${sys:pemCertInfo.caPath}" />
    </XPackAuth>
</ElasticsearchBulkProcessor>
```

### Compatibility matrix

Feature/Version | 2.x | 5.x | 6.x
------------ | ------------- | ------------- | -------------
IndexTemplate | Not tested| Yes | Yes
BasicCredentials | Not tested | Yes | Yes
PEM | Not tested | Yes | Yes

## Dependencies

Be aware that following jars have to be provided by user for this library to work in default mode:
* Jackson FasterXML: `com.fasterxml.jackson.core:jackson-core,jackson-databind,jackson-annotations`
* Log4j2: `org.apache.logging.log4j:log4-api,log4j-core`
* Disruptor (if using `AsyncAppender`): `com.lmax:distuptor`
* XPack Client Transport (if using `XPackAuth`): `org.elasticsearch.client:x-pack-transport`

See `pom.xml` or deps summary at [Maven Repository](https://mvnrepository.com/artifact/org.appenders.log4j/log4j2-elasticsearch5-bulkprocessor/latest) for a list of dependencies.
