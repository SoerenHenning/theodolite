package theodolite.uc1.application;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.streams.KafkaStreams;
import theodolite.commons.kafkastreams.ConfigurationKeys;
import theodolite.uc1.streamprocessing.Uc1KafkaStreamsBuilder;
import titan.ccp.common.configuration.ServiceConfigurations;

/**
 * A microservice that manages the history and, therefore, stores and aggregates incoming
 * measurements.
 *
 */
public class HistoryService {

  private final Configuration config = ServiceConfigurations.createWithDefaults();

  private final CompletableFuture<Void> stopEvent = new CompletableFuture<>();

  /**
   * Start the service.
   */
  public void run() {
    this.createKafkaStreamsApplication();
  }

  /**
   * Build and start the underlying Kafka Streams application of the service.
   *
   */
  private void createKafkaStreamsApplication() {

    final Uc1KafkaStreamsBuilder uc1KafkaStreamsBuilder = new Uc1KafkaStreamsBuilder();
    uc1KafkaStreamsBuilder.inputTopic(this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC));

    final KafkaStreams kafkaStreams = uc1KafkaStreamsBuilder
        .applicationName(this.config.getString(ConfigurationKeys.APPLICATION_NAME))
        .applicationVersion(this.config.getString(ConfigurationKeys.APPLICATION_VERSION))
        .numThreads(this.config.getInt(ConfigurationKeys.NUM_THREADS))
        .commitIntervalMs(this.config.getInt(ConfigurationKeys.COMMIT_INTERVAL_MS))
        .cacheMaxBytesBuffering(this.config.getInt(ConfigurationKeys.CACHE_MAX_BYTES_BUFFERING))
        .bootstrapServers(this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS))
        .schemaRegistry(this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL))
        .build();

    this.stopEvent.thenRun(kafkaStreams::close);

    kafkaStreams.start();
  }

  public static void main(final String[] args) {
    new HistoryService().run();
  }

}