package org.example.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.springbootdeveloper.entity.Article;
import org.example.springbootdeveloper.dto.AddArticleRequest;
import org.example.springbootdeveloper.dto.UpdateArticleRequest;
import org.example.springbootdeveloper.repository.BlogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor    // final이나 @NotNull이 붙은 필드의 생성자 추가
@Service                    // 해당 클래스를 서블릿 컨테이너에 빈으로 등록
public class BlogService {

    private final BlogRepository blogRepository;

    // save() 메소드는 JpaRepository에서 지원하는 저장 메소드
    // AddArticleRequest(DTO) 클래스에 저장된 값들을 article DB에 저장
    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    // JPA에서 제공하는 findById 메소드를 사용해 게시글 id를 받아 엔티티를 조회하고
    // 없으면 IllegalArgumentException 예외 발생
    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));
    }

    public void delete(long id) {
//        blogRepository.deleteById(id);
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        blogRepository.delete(article);
    }

    // Transactional : 매칭한 메소드를 하나의 트랜잭션으로 묶는 역할을 함
    // update() 메소드는 엔티티의 필드 값이 바뀌면 중간에 에러가 발생해도 제대로 된 값 수정을 보장하게 됨
    // 트랜잭션은 데이터베이스의 데이터를 바꾸기 위해 묶은 작업의 단위
    // authorizeArticleAuthor() 메서드를 통해 수정, 삭제 시 현재 인증 객체에 담겨 있는 사용자와 글을 작성한 사용자 정보를 비교
    @Transactional  // 트랜잭션 메소드 
    public Article update(long id, UpdateArticleRequest request) {
//        Article article = blogRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));
//
//        article.update(request.getTitle(), request.getContent());
//
//        return article;

        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }
}
