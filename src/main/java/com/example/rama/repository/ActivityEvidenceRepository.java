package com.example.rama.repository;

import com.example.rama.model.ActivityEvidence;
import com.example.rama.model.ClassActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityEvidenceRepository extends JpaRepository<ActivityEvidence, Long> {
    
    List<ActivityEvidence> findByActivity(ClassActivities activity);
    
    List<ActivityEvidence> findByActivityId(Long activityId);
    
    @Query("SELECT e FROM ActivityEvidence e WHERE e.activity.id = :activityId ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByActivityIdOrderByUploadedAtDesc(@Param("activityId") Long activityId);
    
    @Query("SELECT e FROM ActivityEvidence e WHERE e.uploadedBy = :uploadedBy ORDER BY e.uploadedAt DESC")
    List<ActivityEvidence> findByUploadedByOrderByUploadedAtDesc(@Param("uploadedBy") String uploadedBy);
    
    void deleteByActivityId(Long activityId);
}
