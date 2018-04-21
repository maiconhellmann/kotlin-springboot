package com.github.maiconhellmann.demo.controller

import com.github.maiconhellmann.demo.model.Article
import com.github.maiconhellmann.demo.repository.ArticleRepository
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/article")
@Api(value="Article API", description="It controlls articles")
class ArticleController(private val articleRepository: ArticleRepository) {

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STANDARD_USER')")
    fun getAllArticles(): List<Article> = articleRepository.findAll()

    @PostMapping
    fun createNewArticle(@Valid @RequestBody article: Article): Article {
        return articleRepository.save(article)
    }

    @GetMapping("/{id}")
    fun getArticleById(@PathVariable("id") articleId: Long): ResponseEntity<Article> {
        return articleRepository.findById(articleId).map {
            ResponseEntity.ok(it)
        }.orElse(ResponseEntity.notFound().build())
    }

    @PutMapping("/{id}")
    fun updateArticleById(@PathVariable(value = "id") articleId: Long,
                          @Valid @RequestBody newArticle: Article): ResponseEntity<Article> {

        return articleRepository.findById(articleId).map { existingArticle ->
            val updatedArticle: Article = existingArticle
                    .copy(title = newArticle.title, content = newArticle.content)
            ResponseEntity.ok().body(articleRepository.save(updatedArticle))
        }.orElse(ResponseEntity.notFound().build())

    }

    @DeleteMapping("/{id}")
    fun deleteArticleById(@PathVariable(value = "id") articleId: Long): ResponseEntity<Void> {

        return articleRepository.findById(articleId).map { article ->
            articleRepository.delete(article)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())

    }
}