package com.github.maiconhellmann.demo.service

import com.github.maiconhellmann.demo.model.Article
import com.github.maiconhellmann.demo.model.User
import com.github.maiconhellmann.demo.repository.ArticleRepository
import com.github.maiconhellmann.demo.repository.UserRepository
import com.github.maiconhellmann.demo.service.GenericService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GenericServiceImpl : GenericService {
    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val articleRepository: ArticleRepository? = null

    override fun findByUsername(username: String): User? {
        return userRepository!!.findByUsername(username)
    }

    override fun findAllUsers(): List<User> {
        return userRepository!!.findAll()
    }

    override fun findAllArticles(): List<Article> {
        return articleRepository!!.findAll()
    }
}
