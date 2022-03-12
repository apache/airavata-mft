package org.apache.airavata.mft.resource.server.backend.sql.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class LocalStorageEntity {

    @Id
    @Column(name = "SCP_STORAGE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String storageId;

    @Column(name = "AGENT_ID")
    private String agentId;
}
