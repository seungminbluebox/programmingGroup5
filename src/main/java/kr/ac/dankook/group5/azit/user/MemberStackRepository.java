package kr.ac.dankook.group5.azit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MemberStackRepository extends JpaRepository<MemberStack, Long> {
    List<MemberStack> findAllByMember(Member member);

    @Modifying
    @Query("DELETE FROM MemberStack ms WHERE ms.member = :member")
    void deleteByMember(@Param("member") Member member);
}
