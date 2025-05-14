package max.iv.usersubscriptionservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import max.iv.usersubscriptionservice.models.enums.ServiceName;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Subscription name cannot be blank")
    @Size(max = 100)
    @Column(name = "service_name")
    private ServiceName serviceName;

    @NotNull(message = "Start date cannot be null")
    @Column(name = "start_date")
    private LocalDate startDate;
    @FutureOrPresent(message = "End date must be in the present or future")
    @Column(name = "end_date")
    private LocalDate endDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", serviceName=" + serviceName  +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + (user != null ? user.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
