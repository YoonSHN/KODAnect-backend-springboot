package kodanect.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전역 CORS 설정
 *
 * 웹 프론트엔드와의 도메인 간 통신 허용 정책 구성
 *
 * 역할
 * - 프론트엔드 요청에 대한 교차 출처 허용 설정
 * - 허용 도메인, 메서드, 헤더, 인증 정보 정의
 *
 * 특징
 * - WebMvcConfigurer 구현 방식 사용
 * - 모든 요청 경로("/**")에 CORS 정책 적용
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/admin/kindeditor/attached/**")
                .addResourceLocations("file:/app/uploads/admin/kindeditor/attached/");
    }
}


