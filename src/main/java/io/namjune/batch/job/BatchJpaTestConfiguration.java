package io.namjune.batch.job;

import io.namjune.batch.domain.PaySum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchJpaTestConfiguration {
    public static final DateTimeFormatter FORMATTER = ofPattern("yyyyMMdd");
    public static final String JOB_NAME = "batchJpaUnitTestJob";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
            .start(batchJpaUnitTestJobStep())
            .build();
    }

    @Bean(BEAN_PREFIX + "step")
    public Step batchJpaUnitTestJobStep() {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
            .<PaySum, PaySum>chunk(chunkSize)
            .reader(batchJpaUnitTestJobReader(null))
            .writer(batchJpaUnitTestJobWriter())
            .build();
    }


    @Bean(BEAN_PREFIX + "reader")
    @StepScope
    public JpaPagingItemReader<PaySum> batchJpaUnitTestJobReader(@Value("#{jobParameters[orderDate]}") String orderDate) {
        Map<String, Object> params = new HashMap<>();

        params.put("orderDate", LocalDate.parse(orderDate, FORMATTER));

        String className = PaySum.class.getName();
        String queryString = String.format(
            "SELECT new %s(p.tx_date_time, SUM(p.amount)) " +
                "FROM Pay p " +
                "WHERE p.tx_date_time =:orderDate "  +
                "GROUP BY p.tx_date_time ", className);

        return new JpaPagingItemReaderBuilder<PaySum>()
            .name(BEAN_PREFIX + "reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(queryString)
            .parameterValues(params)
            .build();
    }

    private JpaItemWriter<PaySum> batchJpaUnitTestJobWriter() {
        JpaItemWriter<PaySum> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(emf);
        return jpaItemWriter;
    }
}
