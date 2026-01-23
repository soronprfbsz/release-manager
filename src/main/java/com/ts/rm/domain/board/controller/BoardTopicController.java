package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardTopicDto;
import com.ts.rm.domain.board.service.BoardService;
import com.ts.rm.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BoardTopic Controller
 *
 * <p>게시판 토픽 조회 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/board/topics")
@RequiredArgsConstructor
public class BoardTopicController implements BoardTopicControllerDocs {

    private final BoardService boardService;

    /**
     * 토픽 목록 조회
     *
     * @return 토픽 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardTopicDto.ListResponse>>> getTopics() {
        log.info("토픽 목록 조회 요청");

        List<BoardTopicDto.ListResponse> response = boardService.getTopics();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 토픽 상세 조회
     *
     * @param id 토픽 ID
     * @return 토픽 상세
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardTopicDto.Response>> getTopic(
            @PathVariable String id) {

        log.info("토픽 상세 조회 요청 - topicId: {}", id);

        BoardTopicDto.Response response = boardService.getTopic(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
