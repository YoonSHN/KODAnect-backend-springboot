package kodanect.domain.logging.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link FaviconController}의 "/favicon.ico" 요청 처리에 대한 단위 테스트입니다.
 *
 * 테스트 목적:
 * - 204 No Content 응답이 정상적으로 반환되는지 검증
 * - 공통 응답 형식(ApiResponse)을 유지하는지 확인
 */
@RunWith(SpringRunner.class)
@WebMvcTest(FaviconController.class)
public class FaviconControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSourceAccessor messageSource;

    /**
     * 테스트 실행 전 기본 메시지 설정을 위한 Mock 초기화 작업을 수행합니다.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(messageSource.getMessage(eq("log.favicon.ignored"), any(Object[].class)))
                .thenReturn("Favicon 요청 무시됨");
    }

    /**
     * GIVEN: 브라우저가 자동으로 "/favicon.ico" 요청을 보낼 때
     * WHEN: 해당 경로로 GET 요청을 수행하면
     * THEN: 204 No Content 응답과 공통 응답 포맷을 반환해야 한다
     */
    @Test
    public void faviconRequestReturnsNoContent() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("Favicon 요청 무시됨"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

}
