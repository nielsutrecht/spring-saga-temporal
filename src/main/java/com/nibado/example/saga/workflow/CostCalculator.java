package com.nibado.example.saga.workflow;

import java.util.List;
import java.util.Map;

public class CostCalculator {
    private static final Map<String, Integer> costs = Map.of("Apple", 10, "Pear", 10);

    public static int calculate(List<ItemQtty> items) {
        return items.stream().mapToInt(CostCalculator::calculate).sum();
    }

    private static int calculate(ItemQtty itemQtty) {
        if(!costs.containsKey(itemQtty.name())) {
            throw new IllegalArgumentException("No item with name " + itemQtty.name());
        }

        return costs.get(itemQtty.name()) * itemQtty.qtty();
    }
}
