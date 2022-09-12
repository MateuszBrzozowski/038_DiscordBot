package pl.mbrzozowski.ranger.server.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(String userID);

    void deleteByChannelId(String channelID);

    Optional<Client> findByChannelId(String channelID);
}