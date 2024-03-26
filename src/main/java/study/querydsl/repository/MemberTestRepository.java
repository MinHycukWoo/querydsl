package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.Entity.Member;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.repository.support.Querydsl4RepositorySupport;


import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.querydsl.jpa.JPAExpressions.selectFrom;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository(){
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(QMember.member)
                .from(QMember.member)
                .fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(QMember.member)
                .fetch();
    }

    //3.0 버전
    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable){
        JPAQuery<Member> query = selectFrom(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable,query)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable , query::fetchCount);
    }

    public Page<Member> applyPagination(MemberSearchCondition condition , Pageable pageable){
        Page<Member> result = applyPagination(pageable, query ->
                query.selectFrom(QMember.member)
                        .leftJoin(QMember.member.team, QTeam.team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
        return result;
    }

    public Page<Member> applyPagination2(MemberSearchCondition condition , Pageable pageable){
        //content 쿼리 카운터 쿼리 나누기

        Page<Member> result = applyPagination(pageable, contentQuery ->
                        contentQuery.selectFrom(QMember.member)
                        .leftJoin(QMember.member.team, QTeam.team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ) , 
                countQuery -> countQuery
                .select(QMember.member.id)
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())


        );
        return result;
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
