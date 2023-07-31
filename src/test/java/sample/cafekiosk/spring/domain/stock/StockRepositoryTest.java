package sample.cafekiosk.spring.domain.stock;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import sample.cafekiosk.spring.IntegrationTestSupport;

@Transactional
class StockRepositoryTest extends IntegrationTestSupport {

	@Autowired
	private StockRepository stockRepository;

	@DisplayName("상품 번호 리스트로 재고를 조회한다.")
	@Test
	void findAllByProductNumberIn() {
		// given
		Stock stock1 = Stock.create("001", 1);
		Stock stock2 = Stock.create("002", 2);
		Stock stock3 = Stock.create("003", 3);
		stockRepository.saveAll(List.of(stock1, stock2, stock3));

		// when
		List<Stock> stocks = stockRepository.findAllByProductNumberIn(List.of("001", "002"));

		// then
		assertThat(stocks).hasSize(2)
			.extracting("productNumber", "quantity")
			.containsExactlyInAnyOrder(
				tuple("001", 1),
				tuple("002", 2)
			);
	}

}
