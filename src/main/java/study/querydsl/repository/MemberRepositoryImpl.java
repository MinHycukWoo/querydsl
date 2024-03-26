package study.querydsl.repository;


import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.support.PageableExecutionUtils;
import study.querydsl.Entity.Member;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();//count쿼리와 컨텐츠 쿼리 2번을 날리기 위함

        List<MemberTeamDto> content = results.getResults();//컨텐츠를 가져온다
        long total = results.getTotal();//카운트 값을 가져온다

        return new PageImpl<>(content , pageable , total);
        //pageImpl 은 page의 구현체
    }

    @Override
    public Page<MemberTeamDto> searchComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();//count쿼리와 컨텐츠 쿼리 2번을 날리기 위함

        /*Long total = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount();*/

        JPAQuery<Member> countQuery = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        //count 쿼리가 생략 가능한 경우 생략해서처리
        //페이지 시작이면서 컨텐츠 사이즈가 페이즈 사이즈 보다 작을때
        //마지막 페이지일때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)

        return PageableExecutionUtils.getPage(content,pageable ,countQuery::fetchCount);
        //위 조건이 맞는다면 () -> countQuery.fetchCount()가 카운트커리를 날리지 않아준다.



        //List<MemberTeamDto> content = content.getResults();//컨텐츠를 가져온다
        //long total = content.getTotal();//카운트 값을 가져온다

        //return new PageImpl<>(content , pageable , total);
        //pageImpl 은 page의 구현체
    }


    //null체크만 조심하면 조립도 가능하고 재사용도 가능하다

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
