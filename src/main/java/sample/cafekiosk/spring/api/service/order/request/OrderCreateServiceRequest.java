package sample.cafekiosk.spring.api.service.order.request;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateServiceRequest {

	private List<String> productNumbers;

	@Builder
	public OrderCreateServiceRequest(final List<String> productNumbers) {
		this.productNumbers = productNumbers;
	}

}
