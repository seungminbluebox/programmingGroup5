package kr.ac.dankook.group5.azit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(String email, String password, String name, Integer age, String bio) {
        Member member = new Member();
        member.setEmail(email);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setAge(age);
        member.setBio(bio);
        memberRepository.save(member);
    }
}
