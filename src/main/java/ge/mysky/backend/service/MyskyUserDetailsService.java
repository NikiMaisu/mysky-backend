package ge.mysky.backend.service;

import ge.mysky.backend.domain.User;
import ge.mysky.backend.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyskyUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public MyskyUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public MyskyUserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        var user = users.findByEmailIgnoreCase(identifier)
                .or(() -> users.findByPhone(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("No user with email/phone " + identifier));
        return new MyskyUserDetails(user);
    }

    public MyskyUserDetails loadUserById(Long id) {
        var user = users.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("No user with id " + id));
        return new MyskyUserDetails(user);
    }

    public record MyskyUserDetails(User user) implements UserDetails {

        @Override
        public List<SimpleGrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(user.getRole().authority()));
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail() != null ? user.getEmail() : user.getPhone();
        }
    }
}
