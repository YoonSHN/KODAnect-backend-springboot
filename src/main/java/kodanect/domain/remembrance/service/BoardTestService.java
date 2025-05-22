package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.entity.BoardTest;

import java.util.List;

public interface BoardTestService {

    List<BoardTest> success();
    void simulateNotFound();
    void simulateInternalError();
}
