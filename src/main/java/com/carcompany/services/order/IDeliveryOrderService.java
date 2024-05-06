package com.carcompany.services.order;

import com.carcompany.dtos.order.CalculateDTO;
import com.carcompany.dtos.order.DeliveryOrderDTO;
import com.carcompany.dtos.order.OrderUpdateDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.DeliveryOrder;
import com.carcompany.responses.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

public interface IDeliveryOrderService {
    DeliveryOrder getOrderById(Long id) throws Exception;
    Page<OrderResponse> getAllOrders(PageRequest pageRequest);
    List<DeliveryOrder> getOrdersByUserId(Long userId);
    DeliveryOrder createOrder(DeliveryOrderDTO orderDTO) throws DataNotFoundException, IOException;
    DeliveryOrder updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) throws DataNotFoundException, IOException;
    CalculateDTO calculate(DeliveryOrderDTO orderDTO) throws IOException, DataNotFoundException;
    void deleteOrder(long id);
}
