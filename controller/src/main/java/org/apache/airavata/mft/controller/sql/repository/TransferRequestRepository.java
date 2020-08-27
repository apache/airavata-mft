package org.apache.airavata.mft.controller.sql.repository;

import org.apache.airavata.mft.controller.sql.entity.TransferRequestEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends CrudRepository<TransferRequestEntity, String> {

    @Modifying
    @Query("update TransferRequestEntity requestEntity set requestEntity.status = ?2 where requestEntity.transferId = ?1")
    void updateTransferRequestStatus(String transferId, String status);

    List<TransferRequestEntity> findAll();
}
