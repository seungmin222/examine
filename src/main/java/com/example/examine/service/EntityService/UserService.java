package com.example.examine.service.EntityService;

import com.example.examine.dto.request.UserProductRequest;
import com.example.examine.dto.request.UserRequest;
import com.example.examine.dto.response.UserResponse.UserResponse;
import com.example.examine.entity.Alarm;
import com.example.examine.entity.Page;
import com.example.examine.entity.Product;
import com.example.examine.entity.User.*;
import com.example.examine.repository.AlarmRepository;
import com.example.examine.repository.PageRepository;
import com.example.examine.repository.ProductRepository;
import com.example.examine.repository.UserRepository.UserAlarmRepository;
import com.example.examine.repository.UserRepository.UserPageRepository;
import com.example.examine.repository.UserRepository.UserProductRepository;
import com.example.examine.repository.UserRepository.UserRepository;
import com.example.examine.service.Redis.JwtProperties;
import com.example.examine.service.Redis.RedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final JwtProperties jwtProperties;
    private final PageRepository pageRepo;
    private final ProductRepository productRepo;
    private final UserPageRepository userPageRepo;
    private final UserProductRepository userProductRepo;


    public boolean findByUsername(String username) {
        return userRepo.findByUsername(username).isPresent();
    }


    public ResponseEntity<String> create(UserRequest req) {
        if (findByUsername(req.username())) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다");
        }
        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .build();

        userRepo.save(user);

        return ResponseEntity.ok("가입 성공");
    }

    public ResponseEntity<String> updatePassword(String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);

        return ResponseEntity.ok("비밀번호 변경 완료");
    }

    public ResponseEntity<String> updateRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        user.setRole(role.toUpperCase());
        userRepo.save(user);

        return ResponseEntity.ok("역할 변경 완료");
    }

    @Transactional
    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthenticated");
        }

        User principal = (User) authentication.getPrincipal();
        User user = userRepo.findById(principal.getId()).orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // ✅ 사용자 활동 감지 → Refresh TTL 연장
        redisService.extendTTL("refresh", user.getUsername(), jwtProperties.getRefreshExpiration());

        return UserResponse.fromEntity(user);
    }

    public boolean checkDuplication(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    public ResponseEntity<String> deleteMe(UserRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        userRepo.delete(user);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    public ResponseEntity<String> deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        userRepo.delete(user);
        return ResponseEntity.ok("사용자 삭제 완료");
    }

    @Transactional
    public ResponseEntity<String> addBookmark(Authentication authentication, Long id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthenticated");
        }

        User user = (User) authentication.getPrincipal();

        Page page = pageRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("페이지가 존재하지 않습니다."));

        // 이미 북마크 돼 있는지 확인
        boolean alreadyBookmarked = userPageRepo.existsByUserAndPage(user, page);
        if (alreadyBookmarked) {
            throw new IllegalStateException("이미 북마크한 페이지입니다.");
        }

        if(user.getLevel() < page.getLevel()){
            throw new IllegalStateException("북마크할 수 없는 페이지입니다.");
        }

        UserPage bookmark = UserPage.builder()
                .id(new UserPageId(user.getId(), page.getId()))
                .user(user)
                .page(page)
                .build();


        userPageRepo.save(bookmark);
        page.setBookmarkCount(page.getBookmarkCount() + 1);
        return ResponseEntity.ok("북마크 저장 완료");
    }

    @Transactional
    public ResponseEntity<String> addCart(Authentication auth, Long productId, int quantity) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = (User) auth.getPrincipal();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제품입니다."));

        UserProductId id = new UserProductId(user.getId(), productId);
        UserProduct userProduct = userProductRepo.findById(id).orElse(null);
        if (userProduct != null) {
            userProduct.setQuantity(userProduct.getQuantity() + quantity);
            return ResponseEntity.ok("장바구니 개수 증가.");
        }

        UserProduct cartItem = UserProduct.builder()
                .id(id)
                .user(user)
                .product(product)
                .quantity(quantity)
                .checked(true)
                .build();

        userProductRepo.save(cartItem);
        return ResponseEntity.ok("🛒 장바구니에 추가 완료");
    }

    @Transactional
    public ResponseEntity<String> updateCart(Authentication auth, List<UserProductRequest> cartItems) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = (User) auth.getPrincipal();

        // Step 1: ID 추출
        Set<UserProductId> ids = cartItems.stream()
                .map(e -> new UserProductId(user.getId(), e.id()))
                .collect(Collectors.toSet());

        // Step 2: 일괄 조회
        List<UserProduct> userProducts = userProductRepo.findAllById(ids);
        Map<Long, UserProduct> productMap = userProducts.stream()
                .collect(Collectors.toMap(p -> p.getId().getProductId(), p -> p));

        // Step 3: 수정 or 삭제
        for (UserProductRequest req : cartItems) {
            UserProduct product = productMap.get(req.id());
            if (product == null) continue;

            if (req.quantity() < 1) {
                userProductRepo.delete(product);
            } else {
                product.setQuantity(req.quantity());
                product.setChecked(req.isChecked());
            }
        }

        return ResponseEntity.ok("🛒 장바구니에 추가 완료");
    }



    @Transactional
    public ResponseEntity<String> deleteBookmark(Authentication authentication, Long id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthenticated");
        }

        User user = (User) authentication.getPrincipal();
        UserPageId userPageId = new UserPageId(user.getId(), id);
        UserPage userPage = userPageRepo.findById(userPageId).orElseThrow(() -> new UsernameNotFoundException("북마크 하지 않은 페이지입니다."));
        Page page = userPage.getPage();
        page.setBookmarkCount(page.getBookmarkCount() - 1);

        userPageRepo.delete(userPage);

        return ResponseEntity.ok("북마크 삭제 완료");
    }

    @Transactional
    public ResponseEntity<String> removeCart(Authentication auth, Long productId) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = (User) auth.getPrincipal();
        UserProductId id = new UserProductId(user.getId(), productId);

        UserProduct item = userProductRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 상품은 장바구니에 없습니다."));

        userProductRepo.delete(item);
        return ResponseEntity.ok("❌ 장바구니에서 제거 완료");
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
        return user;
    }
}
