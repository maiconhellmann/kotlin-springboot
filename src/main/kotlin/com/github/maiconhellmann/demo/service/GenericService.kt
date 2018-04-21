package com.github.maiconhellmann.demo.service

import com.github.maiconhellmann.demo.model.Article
import com.github.maiconhellmann.demo.model.User

interface GenericService {
    fun findByUsername(username: String): User?

    fun findAllUsers(): List<User>

    fun findAllArticles(): List<Article>
}
