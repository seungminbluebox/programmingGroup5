package kr.ac.dankook.group5.azit.schedule.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dankook.group5.azit.schedule.entity.Schedule;
import kr.ac.dankook.group5.azit.user.Member;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	Optional<Schedule> findByIdAndMember(Long id, Member member);

	<T> Set<T> findByMemberAndDate(Member member, LocalDate date, Class<T> type);

	Set<Schedule> findByMemberAndDate(Member member, LocalDate date);

	<T> Set<T> findByMemberInAndDate(Collection<Member> members, LocalDate date,
			Class<T> type);

	Set<Schedule> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);

	Set<Schedule> findByMemberInAndDateBetween(Collection<Member> members, LocalDate startDate, LocalDate endDate);
}
