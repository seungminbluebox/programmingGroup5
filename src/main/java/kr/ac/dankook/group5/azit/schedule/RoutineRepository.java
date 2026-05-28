package kr.ac.dankook.group5.azit.schedule;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.dankook.group5.azit.user.Member;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
	@Query("SELECT r FROM Routine r WHERE (r.member = :member) AND (:date BETWEEN r.startDate AND r.endDate) AND (:dayOfWeek = r.dayOfWeek)")
	<T> Set<T> findByMemberAndDate(Member member, LocalDate date, DayOfWeek dayOfWeek, Class<T> type);

	@Query("SELECT r FROM Routine r WHERE (r.member IN :members) AND (:date BETWEEN r.startDate AND r.endDate) AND (:dayOfWeek = r.dayOfWeek)")
	<T> Set<T> findByMemberInAndDate(Collection<Member> members, LocalDate date,
			DayOfWeek fromBuiltin, Class<T> type);

	@Query("SELECT r FROM Routine r WHERE (r.member = :member) AND (:startDate <= r.startDate) AND (r.endDate <= :endDate)")
	Set<Routine> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);

	@Query("SELECT r FROM Routine r WHERE (r.member IN :members) AND (:startDate <= r.startDate) AND (r.endDate <= :endDate)")
	Set<Routine> findByMemberInAndDateBetween(Collection<Member> members, LocalDate startDate, LocalDate endDate);
}
