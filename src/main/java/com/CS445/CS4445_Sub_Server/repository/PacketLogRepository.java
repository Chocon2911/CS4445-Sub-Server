package com.CS445.CS4445_Sub_Server.repository;

import com.CS445.CS4445_Sub_Server.entity.PacketLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketLogRepository extends JpaRepository<PacketLog, Long> {
    List<PacketLog> findByPacketId(String packetId);
}
