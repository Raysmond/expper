package com.expper.repository;

import com.expper.domain.Tag;
import com.expper.domain.User;
import com.expper.service.TagService;
import com.expper.service.UserService;

import java.time.ZonedDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    @Override
    @Caching(evict = {
        @CacheEvict(value = UserService.CACHE_USER, key = "#p0.id+'_user'"),
        @CacheEvict(value = UserService.CACHE_USER, key = "#p0.login+'_user'")
    })
    <S extends User> S save(S entity);

    Optional<User> findOneByLogin(String login);

    Optional<User> findOneByLoginOrEmail(String login, String email);

    @Cacheable(value = UserService.CACHE_USER, key = "#p0+'_user'")
    User findByLogin(String login);

    Optional<User> findOneById(Long userId);

    @Override
    @Caching(evict = {
        @CacheEvict(value = UserService.CACHE_USER, key = "#p0.id+'_user'"),
        @CacheEvict(value = UserService.CACHE_USER, key = "#p0.login+'_user'")
    })
    void delete(User t);

}
