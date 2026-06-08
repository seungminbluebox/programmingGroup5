package kr.ac.dankook.group5.azit.schedule.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.dankook.group5.azit.schedule.entity.DayOfWeek;
import kr.ac.dankook.group5.azit.schedule.entity.Routine;
import kr.ac.dankook.group5.azit.user.Member;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

	Optional<Routine> findByIdAndMember(Long id, Member member);

	@Query("SELECT r FROM Routine r WHERE (r.member = :member) AND ((:date BETWEEN r.startDate AND r.endDate) OR (r.startDate IS NULL) OR (r.endDate IS NULL)) AND (:dayOfWeek = r.dayOfWeek)")
	<T> Set<T> findByMemberAndDate(Member member, LocalDate date, DayOfWeek dayOfWeek, Class<T> type);

	@Query("SELECT r FROM Routine r WHERE (r.member = :member) AND ((:date BETWEEN r.startDate AND r.endDate) OR (r.startDate IS NULL) OR (r.endDate IS NULL)) AND (:dayOfWeek = r.dayOfWeek)")
	Set<Routine> findByMemberAndDate(Member member, LocalDate date, DayOfWeek dayOfWeek);

	@Query("SELECT r FROM Routine r WHERE (r.member IN :members) AND ((:date BETWEEN r.startDate AND r.endDate) OR (r.startDate IS NULL) OR (r.endDate IS NULL)) AND (:dayOfWeek = r.dayOfWeek)")
	<T> Set<T> findByMemberInAndDate(Collection<Member> members, LocalDate date,
			DayOfWeek fromBuiltin, Class<T> type);

	@Query("SELECT r FROM Routine r WHERE (r.member = :member) AND (((:startDate <= r.startDate) AND (r.endDate <= :endDate)) OR (r.startDate IS NULL) OR (r.endDate IS NULL))")
	Set<Routine> findByMemberAndDateBetween(Member member, LocalDate startDate, LocalDate endDate);

	@Query("SELECT r FROM Routine r WHERE (r.member IN :members) AND (((:startDate <= r.startDate) AND (r.endDate <= :endDate)) OR (r.startDate IS NULL) OR (r.endDate IS NULL))")
	Set<Routine> findByMemberInAndDateBetween(Collection<Member> members, LocalDate startDate, LocalDate endDate);
}
