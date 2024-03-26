package study.querydsl.Entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Hello {

    @Id @GeneratedValue
    private Long id;
    //queryDsl 은 큐타입이란걸 뽑아서 사용한다.
}
