package com.playdata.homelesscode.repository;

import com.playdata.homelesscode.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, String> {
    List<Channel> findByServerId(String id);
}
