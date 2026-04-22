package com.school.elearning.model;

import com.school.elearning.model.enums.RoleLive;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "live_rooms")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class LiveRoom {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String roomName;
 
    @Column(nullable = false)
    private String providerRoomId; // ID côté LiveKit
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_session_id", nullable = false, unique = true)
    private LiveSession liveSession;

}
