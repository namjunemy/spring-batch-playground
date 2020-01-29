package io.namjune.batch.job;

import io.namjune.batch.domain.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcBatchItemWriterJobConfiguration {
    private static final int chunkSize = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job jdbcBatchItemWriterJob() {
        return jobBuilderFactory.get("jdbcBatchItemWriterJob")
            .start(jdbcBatchItemWriterStep())
            .build();
    }

    @Bean
    public Step jdbcBatchItemWriterStep() {
        return stepBuilderFactory.get("jdbcBatchItemWriterStep")
            .<Pay, Pay>chunk(chunkSize)
            .reader(jdbcBatchItemReader())
            .writer(jdbcBatchItemWriter())
            .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcBatchItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
            .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
            .name("jdbcCursorItemReader")
            .build();
    }

    // beanMapped() 를 사용할때는 필수.
    @Bean
    public JdbcBatchItemWriter<Pay> jdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<Pay>()
            .dataSource(dataSource)
            .sql("INSERT INTO pay2(amount, tx_name, tx_date_time) VALUE (:amount, :txName, :txDateTime)")
            .beanMapped()
            .build();
    }
}
