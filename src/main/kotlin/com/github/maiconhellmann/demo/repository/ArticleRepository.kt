package com.github.maiconhellmann.demo.repository

import com.github.maiconhellmann.demo.model.Article
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : JpaRepository<Article, Long>