package com.joboffers.domain.offer;

import java.util.List;
import java.util.Optional;

public interface OfferRepository {
    List<Offer> findAll();

    Offer save(Offer offer);

    List<Offer> saveAll(List<Offer> offers);

    Optional<Offer> findById(String id);

    Optional<Offer> findByOfferUrl(String offerUrl);

    boolean existsByOfferUrl(String offerUrl);
}
