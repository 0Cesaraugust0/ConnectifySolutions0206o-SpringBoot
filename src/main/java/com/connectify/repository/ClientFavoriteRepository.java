package com.connectify.repository;

import com.connectify.entity.ClientFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientFavoriteRepository extends JpaRepository<ClientFavorite, Long> {

    List<ClientFavorite> findByClient_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Optional<ClientFavorite> findByClient_IdAndEvent_Id(Long clientId, Long eventId);

    boolean existsByClient_IdAndEvent_Id(Long clientId, Long eventId);

    long countByClient_Id(Long clientId);
}
