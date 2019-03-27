package com.javafreelancedeveloper.kalah.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Version
    private Integer version;
    private String name;
    private UUID playerOneId;
    private UUID playerTwoId;
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    private String state;
    private Date createdTimestamp;
    private Date updatedTimestamp;

    @PrePersist
    public void onCreate() {
        createdTimestamp = new Date();
        updatedTimestamp = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        updatedTimestamp = new Date();
    }
}
