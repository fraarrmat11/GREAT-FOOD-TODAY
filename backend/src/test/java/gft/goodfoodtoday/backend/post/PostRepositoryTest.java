package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.user.User;
import gft.goodfoodtoday.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndRetrievesPostWithAuthorAndTimestamp() {
        User author = userRepository.saveAndFlush(new User("ana-post@example.com", "Ana", null, null));
        Post saved = postRepository.saveAndFlush(new Post(author, "Paella", "https://example.com/paella.jpg", null));

        Post found = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(found.getText()).isEqualTo("Paella");
        assertThat(found.getPhotoUrl()).isEqualTo("https://example.com/paella.jpg");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void returnsPostsInNewestFirstOrder() {
        User author = userRepository.saveAndFlush(new User("order-post@example.com", "Ana", null, null));
        Post older = postRepository.saveAndFlush(new Post(author, "Older", null, null));
        Post newer = postRepository.saveAndFlush(new Post(author, "Newer", null, null));

        assertThat(postRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(0, 10)).getContent())
                .extracting(Post::getId)
                .containsExactly(newer.getId(), older.getId());
    }
}
