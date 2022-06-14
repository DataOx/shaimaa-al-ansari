package com.dataox.shaimaaalansaripdftoscv.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "update_attachment")
public class UpdateAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long id;
    public String wellNo;
    public String tgTarget;
    public String profile;
    public String dsCompany;
    public String area;
    public String team;
    public String kocTeamLeader;
    public String RIG;
    public String drillingBHA;
    public String presentActivity;
    public String formation;
    public Date date;
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "bit_hydraulics_id")
    public BITHydraulicsEntity BITHydraulics;
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "non_productive_time_id")
    public NonProductiveTimeEntity nonProductiveTime;

}
