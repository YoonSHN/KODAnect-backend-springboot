package kodanect.domain.logging.flusher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.buffer.BackendLogBuffer;
import kodanect.common.buffer.FrontendLogBuffer;
import kodanect.common.buffer.SystemInfoBuffer;
import kodanect.common.constant.CrudCode;
import kodanect.common.constant.MdcContext;
import kodanect.domain.logging.dto.BackendLogDto;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.SystemInfoDto;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import kodanect.common.constant.UserActionKey;
import kodanect.domain.logging.repository.ActionLogRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * {@link ActionLogFlusher}의 로그 배출 로직을 검증하는 단위 테스트 클래스입니다.
 *
 * CRUD 임계값 조건, 전체 로그 배출, 직렬화 실패 시 예외 발생 등
 * 로그 저장 로직이 올바르게 동작하는지 확인합니다.
 *
 * 또한 테스트 중 {@link MdcContext}를 정적으로 mocking 하여
 * IP 주소가 항상 고정된 값("127.0.0.1")으로 반환되도록 설정합니다.
 */
public class ActionLogFlusherTest {

    private ActionLogFlusher flusher;

    private FrontendLogBuffer frontendBuffer;
    private BackendLogBuffer backendBuffer;
    private SystemInfoBuffer systemInfoBuffer;
    private ActionLogRepository actionLogRepository;
    private ObjectMapper objectMapper;

    private final String sessionId = "test-session";
    private final UserActionKey key = new UserActionKey(sessionId, CrudCode.R);

    /**
     * 테스트 실행 전에 필요한 의존 객체들을 Mock으로 생성하고
     * 이를 주입하여 {@link ActionLogFlusher} 인스턴스를 초기화합니다.
     */
    @Before
    public void setUp() {
        frontendBuffer = mock(FrontendLogBuffer.class);
        backendBuffer = mock(BackendLogBuffer.class);
        systemInfoBuffer = mock(SystemInfoBuffer.class);
        actionLogRepository = mock(ActionLogRepository.class);
        objectMapper = mock(ObjectMapper.class);

        flusher = new ActionLogFlusher(
                frontendBuffer,
                backendBuffer,
                systemInfoBuffer,
                actionLogRepository,
                objectMapper
        );
    }

    /**
     * GIVEN: Frontend 및 Backend 버퍼에 CRUD 코드 기준으로 로그가 1개씩 있을 때
     * WHEN: flushByCrudCode() 호출하면
     * THEN: ActionLogRepository에 저장 요청이 1건 들어가야 한다.
     */
    @Test
    public void flushByCrudCode_shouldFlushLogsAndSave() throws Exception {
        List<FrontendLogDto> feLogs = List.of(FrontendLogDto.builder().pageUrl("/page").build());
        List<BackendLogDto> beLogs = List.of(BackendLogDto.builder().endpoint("/endpoint").build());
        SystemInfoDto systemInfo = SystemInfoDto.builder().build();

        when(frontendBuffer.drainIfThresholdMet(CrudCode.R, 1)).thenReturn(Map.of(key, feLogs));
        when(backendBuffer.drainIfThresholdMet(CrudCode.R, 1)).thenReturn(Map.of(key, beLogs));
        when(systemInfoBuffer.get(sessionId)).thenReturn(Optional.of(systemInfo));
        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");

        try (MockedStatic<MdcContext> mockedMdc = mockStatic(MdcContext.class)) {
            mockedMdc.when(MdcContext::getIpAddress).thenReturn("127.0.0.1");

            flusher.flushByCrudCode(CrudCode.R, 1);

            verify(actionLogRepository, times(1)).saveAll(argThat(logs ->
                    logs instanceof Collection<?> && ((Collection<?>) logs).size() == 1
            ));
        }
    }

    /**
     * GIVEN: 버퍼에 Frontend 로그 1개, Backend 로그 1개 존재할 때
     * WHEN: flushAll() 호출하면
     * THEN: 저장 요청이 발생해야 한다.
     */
    @Test
    public void flushAll_shouldFlushAllLogsAndSave() throws Exception {
        List<FrontendLogDto> feLogs = List.of(FrontendLogDto.builder().pageUrl("/p").build());
        List<BackendLogDto> beLogs = List.of(BackendLogDto.builder().endpoint("/e").build());

        when(frontendBuffer.drainAll()).thenReturn(Map.of(key, feLogs));
        when(backendBuffer.drainAll()).thenReturn(Map.of(key, beLogs));
        when(systemInfoBuffer.get(sessionId)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}json{}value");

        try (MockedStatic<MdcContext> mockedMdc = mockStatic(MdcContext.class)) {
            mockedMdc.when(MdcContext::getIpAddress).thenReturn("127.0.0.1");

            flusher.flushAll();

            verify(actionLogRepository).saveAll(anyList());
        }
    }

    /**
     * GIVEN: ObjectMapper가 JSON 변환 중 예외를 발생시키면
     * WHEN: flushByCrudCode() 호출 시
     * THEN: ActionLogConversionException 예외가 발생해야 한다.
     */
    @Test(expected = ActionLogJsonSerializationException.class)
    public void flush_shouldThrowException_whenJsonFails() throws Exception {
        when(frontendBuffer.drainIfThresholdMet(CrudCode.R, 1)).thenReturn(Map.of(key, List.of()));
        when(backendBuffer.drainIfThresholdMet(CrudCode.R, 1)).thenReturn(Map.of(key, List.of()));
        when(systemInfoBuffer.get(sessionId)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        try (MockedStatic<MdcContext> mockedMdc = mockStatic(MdcContext.class)) {
            mockedMdc.when(MdcContext::getIpAddress).thenReturn("127.0.0.1");

            flusher.flushByCrudCode(CrudCode.R, 1);
        }
    }

}
