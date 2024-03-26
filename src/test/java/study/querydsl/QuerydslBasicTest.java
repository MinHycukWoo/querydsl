package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Entity.Member;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.QTeam;
import study.querydsl.Entity.Team;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1" , 10 , teamA);
        Member member2 = new Member("member2" , 20 , teamA);

        Member member3 = new Member("member3" , 30 , teamB);
        Member member4 = new Member("member4" , 40 , teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        //member1을 찾아라.
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString , Member.class)
                .setParameter("username" , "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    @Test
    public void startQuerydsl(){
        //entity Manager 를 같이 넘겨주어야 한다.
        //JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        // QMember 가 없을떈 gradle에 compileQuerydsl 을 실행
        QMember m1 = new QMember("m1"); //이름을 하나 줘야한다.

        //QMember m = QMember.member;

        //이 이름은 구분하기 위한 이름 , 나중엔 안씀
        //jpql에서는 parameter를 바인딩해주는데
        //여기선 eq로 바로 넣어줄수 있다.

        Member findMember = queryFactory
                .select(m1)
                .from(m1)
                .where(m1.username.eq("member1"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

        //jpql은 문자로 쿼리문을 작성해서 런타임 단계에서 오류를 확인할수 있는데
        //QueryDSL은 문법적 오류로 작성단계 컴파일에서 확인이 가능하다.
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1")
                        .and(QMember.member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1")
                        .and(QMember.member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFerch(){
        List<Member> fetch = queryFactory
                .selectFrom(QMember.member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(QMember.member)
                .fetchResults();

         results.getTotal();
         List<Member> content = results.getResults();

         long total = queryFactory
                 .selectFrom(QMember.member)
                 .fetchCount();
    }

    //회원 정렬순서
    //1. 회원 나리 내림차순 (desc)
    //2. 회원 이름 올림차순 (asc)
    //단 2에서 회원 이름이 없으면 마지막에 출력(nulls last )
    @Test
    public void sort(){
        em.persist(new Member(null , 100));
        em.persist(new Member("member5" , 100));
        em.persist(new Member("member6" , 100));

        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(100))
                .orderBy(QMember.member.age.desc() ,
                        QMember.member.username.asc().nullsLast())//nullFirst
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1(){
        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(0)
                .limit(2)
                .fetch();
        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2(){ //전체조회수
        QueryResults<Member> result = queryFactory
                .selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        Assertions.assertThat(result.getTotal()).isEqualTo(4);
        Assertions.assertThat(result.getLimit()).isEqualTo(2);
        Assertions.assertThat(result.getOffset()).isEqualTo(1);
        Assertions.assertThat(result.getResults().size()).isEqualTo(2);
    }

    //실무에서는 단순한 경우에만 results 를 사용하고 복잡한경우는 따로 작성해야한다
    @Test
    public void aggregation(){
        List<Tuple> result = queryFactory
                .select(QMember.member.count(),
                        QMember.member.age.sum(),
                        QMember.member.age.avg(),
                        QMember.member.age.max(),
                        QMember.member.age.min()
                )
                .from(QMember.member)
                .fetch();

        Tuple tuple = result.get(0);
        Assertions.assertThat(tuple.get(QMember.member.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(QMember.member.age.sum())).isEqualTo(60);
        Assertions.assertThat(tuple.get(QMember.member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(QMember.member.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(QMember.member.age.min())).isEqualTo(10);
    }
    
    //팀 이름과 각 팀의 평균 연령을 구해라
    @Test
    public void group() throws Exception{
        List<Tuple> result = queryFactory
                .select(QTeam.team.name , QMember.member.age.avg())
                .from(QMember.member)
                .join(QMember.member.team , QTeam.team)
                .groupBy(QTeam.team.name)
                .fetch();

        //groupBy(item.price)
        //having(item.price.gt(1000))

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(QMember.member.team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(QMember.member.age.avg())).isEqualTo(15);
        Assertions.assertThat(teamA.get(QMember.member.team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamA.get(QMember.member.age.avg())).isEqualTo(35);
    }

    //팀 A에 소속된 모든 회원
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .join(QMember.member.team , QTeam.team)
                .where(QTeam.team.name.eq("teamA"))
                .fetch();

        //member1 inner join member1.team as team 형태로 조인된다,
        //left join , right join 등도 가능하다.
        //on등도 가능은 지만 이후에 설명
        //연관관계가 없어도 조인은 가능한데 이를 세타조인 이라한다.

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("member1" , "member2");
    }
    
    //세타 조인
    //회원의 이름이 팀 이름과 같은 회원 조회
    //모든 맴버 모든 팀을 전부 조인
    //주의점 
    //from 절에 여러 엔티티를 선택해서 세타조인가능 (연관관계 없는 테이블끼리 조인)
    //외부조인 불가능 , 다음에 설명할 조인 on을 사용하면 외부조인 가능
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result =queryFactory
                .select(QMember.member)
                .from(QMember.member , QTeam.team)
                .where(QMember.member.username.eq(QTeam.team.name))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA" , "teamB");
    }

    //회원과 팀을 조인하면서 팅 이름이 teamA인 팀만 조인 , 회원은 모두 조인
    //JPQL : select m , t from Member m left join m.team t on t.name = 'teamA'
    @Test
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(QMember.member , QTeam.team)
                .from(QMember.member)
                .leftJoin(QMember.member.team , QTeam.team)
                .on(QTeam.team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple" + tuple);
        }

        /*on절을 활용해 조인 대상을 필터링 할때 외부조인이 아니라 내부조인을 사용하면
        where절에서 필터링 하는것과 기능이 동일하다, 따라서 on절을 활용한
        조인대상 필터링을 사용할때 내부조인 이면 익숙한 where 절오 해결하고 정말 외부조인이
        필요한 경우에만 이 기능을 사용하자*/



    }


    //연과관꼐 없는 엔티티 외부조인
    //회원의 이름이 팀 이름과 같은 대상 외부조인
    //일반조인 leftJoin(member.team , team)
    //on조인 from(member).leftJoin(team).on(xxx)
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result =queryFactory
                .select(QMember.member , QTeam.team)
                .from(QMember.member )
                .leftJoin(QTeam.team)
                .on(QMember.member.username.eq(QTeam.team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple" + tuple);
        }
    }


    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        //지연로딩된 필드는 바로 조인이 되지않고 프록시로 오게된다.

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //불러왓는지 아닌지 확인
        Assertions.assertThat(loaded).as("페치조인 미적용").isFalse();
    }


    @Test
    public void fetchJoinYes(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .join(QMember.member.team , QTeam.team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        //지연로딩된 필드는 바로 조인이 되지않고 프록시로 오게된다.

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        //불러왓는지 아닌지 확인
        Assertions.assertThat(loaded).as("페치조인 미적용").isTrue();
    }

    //나이가 가장 많은 회원 조회
    @Test
    public void subQuery(){
        //서브쿼리는 바깥에 alias와 겹치면 안된다.
        QMember memberSub = new QMember("memberSub");
        //alias가 겹치면 안될경우엔 이렇게 새로 만들어주면된다,

        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age")
                .containsExactly(40);
    }

    //나이가 평균이상인 회원
    @Test
    public void subQueryGoe(){
        //서브쿼리는 바깥에 alias와 겹치면 안된다.
        QMember memberSub = new QMember("memberSub");
        //alias가 겹치면 안될경우엔 이렇게 새로 만들어주면된다,

        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age")
                .containsExactly(40);
    }

    //나이가 평균이상인 회원
    @Test
    public void subQueryIn(){
        //서브쿼리는 바깥에 alias와 겹치면 안된다.
        QMember memberSub = new QMember("memberSub");
        //alias가 겹치면 안될경우엔 이렇게 새로 만들어주면된다,

        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age")
                .containsExactly(20,30,40);
    }

    //select절에 서브쿼리
    @Test
    public void selectSubQuery(){

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(QMember.member.username ,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(QMember.member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple" + tuple);
        }
    }
    
    @Test
    public void basicCase(){
        //심플케이스
        List<String> result = queryFactory
                .select(QMember.member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(QMember.member)
                .fetch();

        for (String s : result) {
            System.out.println("s =" + s);
        }
    }

    //복잡한조건
    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(QMember.member.age.between(0,20)).then("0~20살")
                        .when(QMember.member.age.between(21,30)).then("21~30살")
                        .otherwise("기타"))
                .from(QMember.member)
                .fetch();

        for (String s : result) {
            System.out.println("s=" + s );
        }
    }

    //상수 추가.
    @Test
    public void constant(){
        List<Tuple> result = queryFactory
                .select(QMember.member.username, Expressions.constant("A"))
                .from(QMember.member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple =" + tuple);
        }
    }

    //문자더하기

    @Test
    public void concat(){

        //username + _ + age
        List<String> result = queryFactory
                .select(QMember.member.username.concat("_").concat(QMember.member.age.stringValue()))
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }
    
    @Test
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                .fetch();

        for (String s : result) {
            System.out.println("s=" + s);
        }

    }

    @Test
    public void tupleProejction(){
        //repository계층을 넘어서 서비스계층등에서 사용하는건 좋은 설계가 아니다.
        //repository계층을 넘어서 나갈떈 DTO로 변환해서 나가는걸 추천
        List<Tuple> result = queryFactory
                .select(QMember.member.username , QMember.member.age)
                .from(QMember.member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(QMember.member.username);
            Integer age = tuple.get(QMember.member.age);
            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }

    }

    //순수 jpql
    @Test
    public void findDtoByJPQL(){
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username , m.age) from Member m " , MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto =" + memberDto);
        }

    }

    //기본생성자를 필요로한다.
    //Projections.bean 이 setter , getter를 필요로한다,
    @Test
    public void findDtoBySetter(){
        List<MemberDto> result =queryFactory
                .select(Projections.bean(MemberDto.class ,
                        QMember.member.username,
                        QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto );
        }
    }

    //field는 getter setter가 없어도 field에 값을 바로 넣어버린다.
    @Test
    public void findDtoByField(){
        List<MemberDto> result =queryFactory
                .select(Projections.fields(MemberDto.class ,
                        QMember.member.username,
                        QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto );
        }
    }

    @Test
    public void findDtoByConstructor(){
        List<MemberDto> result =queryFactory
                .select(Projections.constructor(MemberDto.class ,
                        QMember.member.username,
                        QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto );
        }
    }

    @Test
    public void findUserDto(){
        QMember memberSub = new QMember ("memberSub");
        List<UserDto> result =queryFactory
                .select(Projections.fields(UserDto.class ,
                        QMember.member.username.as("name"),

                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(QMember.member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto =" + userDto);
        }
    }

    @Test
    public void findDtoByConstructor2(){
        List<UserDto> result =queryFactory
                .select(Projections.constructor(UserDto.class ,
                        QMember.member.username,
                        QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (UserDto memberDto : result) {
            System.out.println("memberDto = " + memberDto );
        }
    }

    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> result= queryFactory
                .select(new QMemberDto(QMember.member.username , QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto =" + memberDto);

        }
    }

    @Test
    public void dynamicQuery_BooleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam,ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond , Integer ageCond){
        //파라미터 값이 null이냐에 따라 바뀌어야 한다.
        //null인 파라미터 값은 조건에서 제외 시키는 예제

        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null) {
            //builder는 and 와 or를 조합할수 있다.
            //파라미터 값이 null 아니면 값을 and 로 추가
            builder.and(QMember.member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(QMember.member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(QMember.member)
                .where(builder)
                .fetch();

    }

    @Test
    public void dynamicQuery_WhereParam(){
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember2(usernameParam,ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(QMember.member)
                //.where(usernameEq(usernameCond) , ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();

    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? QMember.member.age.eq(ageCond) : null;
    }

    private BooleanExpression usernameEq(String usernameCond) {
        if(usernameCond == null){
            return null; //where 에 null이 오면 조건이 무시된다.
        } else {
            return QMember.member.username.eq(usernameCond);
        }
        //usernameCond != null ? QMembmer.username.eq(usernameCond) : null;
    }

    private Predicate allEq(String usernameCond , Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    //@Commit
    public void bulkUpdate(){

        long count = queryFactory
                .update(QMember.member)
                .set(QMember.member.username, "비회원")
                .where(QMember.member.age.lt(28))
                .execute();


        //벌크 연산은 항상 실행한 다음 clear , flush를 해주어야 한다.

    }

    //모든 값에 더하기 쿼리 뺴기는 -1 add박에 메소드가 없다.
    @Test
    public void bulkAdd(){
        long count = queryFactory
                .update(QMember.member)
                .set(QMember.member.age, QMember.member.age.add(1))
                .execute();
    }

    //삭제
    @Test
    public void bulkDelete(){
        queryFactory
                .delete(QMember.member)
                .where(QMember.member.age.gt(18))
                .execute();
    }

    @Test
    public void sqlFunction(){
        List<String> result = queryFactory
                .select(
                        Expressions.stringTemplate(
                        "function('replace',{0},{1},{2})",
                                QMember.member.username,"member", "M"))
                .from(QMember.member)
                .fetch();

        for (String s : result) {
            System.out.println("s=" + s);
        }
    }

    @Test
    public void sqlFunction2(){
        List<String> result = queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                /*.where(QMember.member.username.eq(
                        Expressions.stringTemplate("function('lower',{0})",QMember.member.username)))*/
                .where(QMember.member.username.eq(QMember.member.username.lower()))
                .fetch();

        for (String s : result) {
            System.out.println("s=" + s);
        }
    }

}
