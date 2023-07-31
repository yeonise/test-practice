package sample.cafekiosk.spring.api.service.product;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.cafekiosk.spring.domain.product.ProductRepository;

@RequiredArgsConstructor
@Component
public class ProductNumberFactory {

	private final ProductRepository productRepository;

	public String createNextProductNumber() {
		String latestProductNumber = productRepository.findLatestProductNumber();

		if (latestProductNumber == null) {
			return "001";
		}

		int latestProductNumberInt = Integer.parseInt(latestProductNumber);
		int nextProductNumberInt = latestProductNumberInt + 1;

		return String.format("%03d", nextProductNumberInt);
	}

}
