package io.github.byzatic.utility.prometheus.exporter.docker_raid_metrics_prometheus_exporter.service.Scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class JobDetail {
    private final String uniqueId;
    private final Runnable job;
    private final  CronDateCalculator calculator;
    private final String rawCronExpressionString;

    // Копирующий конструктор
    public JobDetail(JobDetail other) {
        this.uniqueId = other.uniqueId;
        this.job = other.job;
        this.calculator = other.calculator;
        this.rawCronExpressionString = other.rawCronExpressionString;
    }

    public JobDetail(Runnable job, String cronExpressionString) {
        try {
            this.uniqueId = UUID.randomUUID().toString();
            this.job = job;
            this.calculator = new CronDateCalculator(cronExpressionString);
            this.rawCronExpressionString = cronExpressionString;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public JobDetail(String uniqueId, Runnable job, String cronExpressionString) {
        try {
            this.uniqueId = uniqueId;
            this.job = job;
            this.calculator = new CronDateCalculator(cronExpressionString);
            this.rawCronExpressionString = cronExpressionString;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public JobDetail(Runnable job, String cronExpressionString, CronDateCalculator calculator) {
        this.uniqueId = UUID.randomUUID().toString();
        this.job = job;
        this.calculator = calculator;
        this.rawCronExpressionString = cronExpressionString;
    }

    public JobDetail(String uniqueId, Runnable job, String cronExpressionString, CronDateCalculator calculator) {
        this.uniqueId = uniqueId;
        this.job = job;
        this.calculator = calculator;
        this.rawCronExpressionString = cronExpressionString;
    }

    private JobDetail(Builder builder) {
        uniqueId = builder.uniqueId;
        job = builder.job;
        calculator = builder.calculator;
        rawCronExpressionString = builder.rawCronExpressionString;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Runnable getJob() {
        return job;
    }

    public Date getNextExecutionDate(Date now) {
        return calculator.getNextExecutionDate(now);
    }

    public String getRawCronExpressionString() {
        return rawCronExpressionString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDetail jobDetail = (JobDetail) o;
        return Objects.equals(uniqueId, jobDetail.uniqueId) && Objects.equals(rawCronExpressionString, jobDetail.rawCronExpressionString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, rawCronExpressionString);
    }

    @Override
    public String toString() {
        return "JobDetail{" +
                "uniqueId='" + uniqueId + '\'' +
                ", job=" + job +
                ", calculator=" + calculator +
                ", rawCronExpressionString='" + rawCronExpressionString + '\'' +
                '}';
    }

    public static final class Builder {
        private final String uniqueId;
        private final Runnable job;
        private final CronDateCalculator calculator;
        private final String rawCronExpressionString;

        public Builder(String uniqueId, Runnable job, CronDateCalculator calculator, String cronExpressionString) {
            this.uniqueId = uniqueId;
            this.job = job;
            this.calculator = calculator;
            this.rawCronExpressionString = cronExpressionString;
        }

        public Builder(Runnable job, CronDateCalculator calculator, String cronExpressionString) {
            this.uniqueId = UUID.randomUUID().toString();
            this.job = job;
            this.calculator = calculator;
            this.rawCronExpressionString = cronExpressionString;
        }

        public Builder(Runnable job, String cronExpressionString) {
            try {
                this.uniqueId = UUID.randomUUID().toString();
                this.job = job;
                this.calculator = new CronDateCalculator(cronExpressionString);
                this.rawCronExpressionString = cronExpressionString;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public JobDetail build() {
            return new JobDetail(this);
        }
    }
}
