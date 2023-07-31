package sample.cafekiosk.spring.api.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import sample.cafekiosk.spring.api.ApiResponse;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.OrderService;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;

import java.time.LocalDateTime;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    /**
     * Presentation Layer
     * - 외부 세계의 요청을 가장 먼저 받는 계층
     * - 넘겨온 값들에 대한 검증이 가장 중요
     * - 비즈니스 로직을 전개시키기 전에 유효한지 검사하는 것이 최우선
     * - 파라미터에 대한 최소한의 검증을 수행
     * - 하위 레이어들을 모두 moking 처리를 하고 단위 테스트 느낌으로 진행
     */

    @PostMapping("/api/v1/orders/new")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        LocalDateTime registeredDateTime = LocalDateTime.now();
        return ApiResponse.ok(orderService.createOrder(request.toServiceRequest(), registeredDateTime));
    }

}
