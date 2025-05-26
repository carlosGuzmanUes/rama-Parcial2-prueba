package com.example.rama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.rama.model.Login;

public interface LoginService extends JpaRepository<Login, Long> {    
}
