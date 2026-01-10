package gr.hua.dit.mockgov.repository;

import gr.hua.dit.mockgov.api.CitizenIdentityDto;

import java.util.Optional;

public interface CitizenDirectory {
    Optional<CitizenIdentityDto> findByCredentials(String afm, String amka, String lastName);
}
