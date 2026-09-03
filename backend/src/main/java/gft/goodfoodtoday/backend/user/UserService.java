package gft.goodfoodtoday.backend.user;

import gft.goodfoodtoday.backend.user.dto.UserCreateRequest;
import gft.goodfoodtoday.backend.user.dto.UserUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        User user = new User(request.email(), request.name(), request.avatarUrl(), request.department());
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User update(Long id, User currentUser, UserUpdateRequest request) {
        User target = getById(id);
        if (!target.getId().equals(currentUser.getId())) {
            throw new NotProfileOwnerException();
        }
        target.setName(request.name());
        target.setAvatarUrl(request.avatarUrl());
        target.setDepartment(request.department());
        return userRepository.save(target);
    }
}
