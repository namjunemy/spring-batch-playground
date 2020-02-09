package io.namjune.batch.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class PaySum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;
    private long amountSum;

    @Builder
    public PaySum(LocalDateTime orderDate, long amountSum) {
        this.orderDate = orderDate;
        this.amountSum = amountSum;
    }
}
