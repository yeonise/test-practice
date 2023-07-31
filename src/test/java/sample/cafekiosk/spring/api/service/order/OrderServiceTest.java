package sample.cafekiosk.spring.api.service.order;

import static org.assertj.core.api.Assertions.*;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.api.service.order.request.OrderCreateServiceRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

class OrderServiceTest extends IntegrationTestSupport {

	/**
	 * Business Layer
	 * - 비즈니스 로직을 구현하는 역할
	 * - Persistence Layer와의 상호작용(Data를 읽고 쓰는 행위)을 통해 비즈니스 로직을 전개시킨다
	 * - 트랜잭션을 보장해야 한다
	 * - 작업 단위의 원자성을 보장하는 책임을 갖는다
	 */

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderProductRepository orderProductRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private OrderService orderService;

	@AfterEach
	void tearDown() {
		orderProductRepository.deleteAllInBatch();
		productRepository.deleteAllInBatch();
		orderRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("주문 번호 리스트를 받아서 주문을 생성한다.")
	@Test
	void createOrder() {
		// given
		LocalDateTime registeredDateTime = LocalDateTime.now();

		Product product1 = createProduct(HANDMADE, "001", 1000);
		Product product2 = createProduct(HANDMADE, "002", 3000);
		Product product3 = createProduct(HANDMADE, "003", 5000);
		productRepository.saveAll(List.of(product1, product2, product3));

		OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
			.productNumbers(List.of("001", "002"))
			.build();

		// when
		OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

		// then
		assertThat(orderResponse.getId()).isNotNull();
		assertThat(orderResponse)
			.extracting("registeredDateTime", "totalPrice")
			.contains(registeredDateTime, 4000);
		assertThat(orderResponse.getProducts()).hasSize(2)
			.extracting("productNumber", "price")
			.containsExactlyInAnyOrder(
				tuple("001", 1000),
				tuple("002", 3000)
			);
	}

	@DisplayName("재고와 관련된 상품이 포함되어 있는 주문 번호 리스트를 받아서 주문을 생성한다.")
	@Test
	void createOrderWithStock() {
		// given
		LocalDateTime registeredDateTime = LocalDateTime.now();

		Product product1 = createProduct(BOTTLE, "001", 1000);
		Product product2 = createProduct(BAKERY, "002", 3000);
		Product product3 = createProduct(HANDMADE, "003", 5000);
		productRepository.saveAll(List.of(product1, product2, product3));

		Stock stock1 = Stock.create("001", 2);
		Stock stock2 = Stock.create("002", 2);
		stockRepository.saveAll(List.of(stock1, stock2));

		OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
			.productNumbers(List.of("001", "001", "002", "003"))
			.build();

		// when
		OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

		// then
		assertThat(orderResponse.getId()).isNotNull();
		assertThat(orderResponse)
			.extracting("registeredDateTime", "totalPrice")
			.contains(registeredDateTime, 10000);
		assertThat(orderResponse.getProducts()).hasSize(4)
			.extracting("productNumber", "price")
			.containsExactlyInAnyOrder(
				tuple("001", 1000),
				tuple("001", 1000),
				tuple("002", 3000),
				tuple("003", 5000)
			);

		List<Stock> stocks = stockRepository.findAll();
		assertThat(stocks).hasSize(2)
			.extracting("productNumber", "quantity")
			.containsExactlyInAnyOrder(
				tuple("001", 0),
				tuple("002", 1)
			);
	}

	@DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
	@Test
	void createOrderWithNoStock() {
		// given
		LocalDateTime registeredDateTime = LocalDateTime.now();

		Product product1 = createProduct(BOTTLE, "001", 1000);
		Product product2 = createProduct(BAKERY, "002", 3000);
		Product product3 = createProduct(HANDMADE, "003", 5000);
		productRepository.saveAll(List.of(product1, product2, product3));

		Stock stock1 = Stock.create("001", 1);
		Stock stock2 = Stock.create("002", 2);
		stockRepository.saveAll(List.of(stock1, stock2));

		OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
			.productNumbers(List.of("001", "001", "002", "003"))
			.build();

		// when // then
		assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("재고가 부족한 상품이 있습니다.");
	}

	@DisplayName("중복되는 상품 번호 리스트로 주문을 생성할 수 있다.")
	@Test
	void createOrderWithDuplicateProductNumber() {
		// given
		LocalDateTime registeredDateTime = LocalDateTime.now();

		Product product1 = createProduct(HANDMADE, "001", 1000);
		Product product2 = createProduct(HANDMADE, "002", 3000);
		Product product3 = createProduct(HANDMADE, "003", 5000);
		productRepository.saveAll(List.of(product1, product2, product3));

		OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
			.productNumbers(List.of("001", "001"))
			.build();

		// when
		OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

		// then
		assertThat(orderResponse.getId()).isNotNull();
		assertThat(orderResponse)
			.extracting("registeredDateTime", "totalPrice")
			.contains(registeredDateTime, 2000);
		assertThat(orderResponse.getProducts()).hasSize(2)
			.extracting("productNumber", "price")
			.containsExactlyInAnyOrder(
				tuple("001", 1000),
				tuple("001", 1000)
			);
	}

	private Product createProduct(ProductType type, String productNumber, int price) {
		return Product.builder()
			.type(type)
			.productNumber(productNumber)
			.price(price)
			.sellingStatus(SELLING)
			.name("메뉴 이름")
			.build();
	}

}
