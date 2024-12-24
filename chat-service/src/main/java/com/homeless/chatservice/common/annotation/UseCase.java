package com.homeless.chatservice.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE}) // 클래스,인터페이스, 열거형에만 적용
@Retention(RetentionPolicy.RUNTIME) // 런타임에 유지됨
@Documented //javaDoc에 포함됨 (문서화시에)
@Component // Bean 등록
public @interface UseCase {
    @AliasFor(annotation = Component.class) // 두 어노테이션 속성을 동기화
    String value() default "";
}