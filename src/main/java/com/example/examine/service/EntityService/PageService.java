package com.example.examine.service.EntityService;

import com.example.examine.dto.request.PageRequest;
import com.example.examine.dto.response.PageResponse;
import com.example.examine.entity.Page;
import com.example.examine.repository.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepo;

    // ✅ 페이지 생성
    public PageResponse create(PageRequest request) {
        Page page = Page.builder()
                .link(request.link())
                .title(request.title())
                .level(request.level())
                .viewCount(0L)
                .bookmarkCount(0L)
                .build();
        Page saved = pageRepo.save(page);
        return PageResponse.fromEntity(saved);
    }

    // ✅ 페이지 수정
    public PageResponse update(Long pageId, PageRequest request) {
        Page page = pageRepo.findById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 페이지입니다."));

        page.setLink(request.link());
        page.setTitle(request.title());
        page.setLevel(request.level());

        Page updated = pageRepo.save(page);
        return PageResponse.fromEntity(updated);
    }

    public List<PageResponse> sort(Sort sort) {
        List<Page> pages = pageRepo.findAll(sort);
        return pages.stream()
                .map(PageResponse::fromEntity)
                .toList();
    }

    public List<PageResponse> search(String keyword, Sort sort) {
        Optional<Page> pages = pageRepo.findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(keyword,keyword,sort);
        return pages.stream()
                .map(PageResponse::fromEntity)
                .toList();
    }

    public Long getId(String link){
        Page page = pageRepo.findByLink(link)
                .orElseThrow(() -> new IllegalArgumentException("페이지가 존재하지 않습니다."));
        return page.getId();
    }

    // ✅ 페이지 삭제
    public void delete(Long pageId) {
        if (!pageRepo.existsById(pageId)) {
            throw new IllegalArgumentException("존재하지 않는 페이지입니다.");
        }
        pageRepo.deleteById(pageId);
    }
}

