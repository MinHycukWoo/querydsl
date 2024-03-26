package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.Entity.Member;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.springframework.util.StringUtils.hasText;


@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public MemberJpaRepository(EntityManager em){
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class , id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m" , Member.class)
                .getResultList();
    }
    //queryDSL로 변경
    public List<Member> findAll_Queryds(){
        return queryFactory
                .selectFrom(QMember.member)
                .fetch();
    }

    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username" , username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        //where의 조건조합
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(QMember.member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getUsername())) {
            builder.and(QTeam.team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null){
            builder.and(QMember.member.age.goe(condition.getAgeGoe()));
        }

        if(condition.getAgeLoe() != null){
            builder.and(QMember.member.age.goe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")))
                .from(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")))
                .from(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    public List<Member> searchMember(MemberSearchCondition condition){
        return queryFactory
                .selectFrom(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                        //ageBetween(condition.getAgeLoe(),condition.getAgeGoe())
                )
                .fetch();
    }

    //null체크만 조심하면 조립도 가능하고 재사용도 가능하다
    private BooleanExpression ageBetween(int ageLoe , int ageGoe){
        return ageGoe(ageLoe).and(ageGoe(ageGoe));
    }
    public BooleanExpression usernameEq(String username){
        return hasText(username) ? QMember.member.username.eq(username) : null;
    }
    public BooleanExpression teamNameEq(String teamName){
        return hasText(teamName) ? QTeam.team.name.eq(teamName) : null;
    }
    public BooleanExpression ageGoe(Integer ageGoe){
        return ageGoe != null ? QMember.member.age.goe(ageGoe) : null;
    }
    public BooleanExpression ageLoe(Integer ageLoe){
        return ageLoe != null ? QMember.member.age.loe(ageLoe) : null;
    }
}
