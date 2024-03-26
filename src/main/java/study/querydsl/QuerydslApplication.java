package study.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.QTeam;

import javax.persistence.EntityManager;

import static org.springframework.util.StringUtils.hasText;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	@Bean
	JPAQueryFactory jpaQueryFactory(EntityManager em){
		return new JPAQueryFactory(em);
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
