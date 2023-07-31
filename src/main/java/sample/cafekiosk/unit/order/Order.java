package sample.cafekiosk.unit.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sample.cafekiosk.unit.beverage.Beverage;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class Order {

    private final LocalDateTime orderDateTime;
    private final List<Beverage> beverages;

}
