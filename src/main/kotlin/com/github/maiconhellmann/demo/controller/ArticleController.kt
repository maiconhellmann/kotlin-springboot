package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.model.Article
import com.github.maiconhellmann.demo.repository.ArticleRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ArticleController(private val articleRepository: ArticleRepository) {

    @GetMapping("/articles")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STANDARD_USER')")
    fun getAllArticles(): List<Article> = articleRepository.findAll()

    @PostMapping("/articles")
    fun createNewArticle(@Valid @RequestBody article: Article): Article {
        return articleRepository.save(article)
    }

    @GetMapping("/articles/{id}")
    fun getArticleById(@PathVariable("id") articleId: Long): ResponseEntity<Article> {
        return articleRepository.findById(articleId).map {
            ResponseEntity.ok(it)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PutMapping("/articles/{id}")
    fun updateArticleById(@PathVariable(value = "id") articleId: Long,
                          @Valid @RequestBody newArticle: Article): ResponseEntity<Article> {

        return articleRepository.findById(articleId).map { existingArticle ->
            val updatedArticle: Article = existingArticle
                    .copy(title = newArticle.title, content = newArticle.content)
            ResponseEntity.ok().body(articleRepository.save(updatedArticle))
        }.orElse(ResponseEntity.notFound().build())

    }

    @DeleteMapping("/articles/{id}")
    fun deleteArticleById(@PathVariable(value = "id") articleId: Long): ResponseEntity<Void> {

        return articleRepository.findById(articleId).map { article ->
            articleRepository.delete(article)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())

    }
}