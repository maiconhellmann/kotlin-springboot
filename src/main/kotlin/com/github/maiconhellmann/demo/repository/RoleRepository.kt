package com.github.maiconhellmann.demo.repository

import com.github.maiconhellmann.demo.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
}