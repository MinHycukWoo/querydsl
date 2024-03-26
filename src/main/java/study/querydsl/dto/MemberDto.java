package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    //@Data는 기본생성자를 만들어주진 않는다.
    //기본 생성자를 만들거나 @NoArgsConstuctor를 사용하면 된다.

    @QueryProjection
    public MemberDto(String username , int age){
        this.username = username;
        this.age = age;
    }
}
