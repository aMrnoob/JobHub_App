package com.example.befindingjob.entity;

import com.example.befindingjob.entity.enumm.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "applicationId")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer applicationId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private java.time.LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "cv_url")
    private String cvUrl;

    @JsonIgnore
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resume resume;
}
