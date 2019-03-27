package com.javafreelancedeveloper.kalah.repository;

import com.javafreelancedeveloper.kalah.domain.Game;
import com.javafreelancedeveloper.kalah.domain.GameStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends CrudRepository<Game, UUID> {

    List<Game> findAllByStatus(GameStatus gameStatus);

}
