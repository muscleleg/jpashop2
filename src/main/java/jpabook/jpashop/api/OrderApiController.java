package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            String name = order.getMember().getName();//강제 초기화해서 프록시를 실제 객체로 만다는 것
            Address address = order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName(); //Order->OrderItem->Item
//
//            }
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream().map(o->new OrderDto(o)).collect(toList());
        return result;

    }
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream().map(o->new OrderDto(o)).collect(toList());
        return result;
    }
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(value = "offset",defaultValue = "0") int offset,
            @RequestParam(value = "limit",defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> result = orders.stream().map(o->new OrderDto(o)).collect(toList());
        return result;
    }
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDto();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
       return orderQueryRepository.findAllByDto_optimization();
    }
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        Map<OrderQueryDto, List<OrderItemQueryDto>> collect = flats.stream().collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                ));
    }
    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;


        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderStatus = order.getStatus();
            orderDate = order.getOrderDate();
            address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o->o.getItem().getName());//값을 가져오는게 아니라 sql날리려고 일부로 데이터에 접근하는거임
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(toList());
        }
    }
    @Getter
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getTotalPrice();
            count = orderItem.getCount();
        }
    }

}
