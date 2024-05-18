package com.carcompany.controllers;

import com.carcompany.dtos.order.DeliveryOrderDTO;
import com.carcompany.dtos.order.OrderUpdateDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.DeliveryOrder;
import com.carcompany.responses.*;
import com.carcompany.responses.order.OrderListResponse;
import com.carcompany.responses.order.OrderResponse;
import com.carcompany.services.order.IDeliveryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class DeliveryOrderController {
    private final IDeliveryOrderService deliveryOrderService;

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @GetMapping("") // http://localhost:8080/api/v1/orders?page=1&limit=10
    public ResponseEntity<ResponseObject> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").descending());
        Page<OrderResponse> orderPage = deliveryOrderService.getAllOrders(pageRequest);

        OrderListResponse response = OrderListResponse.builder()
                .orders(orderPage.getContent())
                .totalPages(orderPage.getTotalPages())
                .build();

        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Get all vehicles successfully")
                .status(HttpStatus.OK)
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrderById(@PathVariable Long id) {
        try {
            DeliveryOrder responses = deliveryOrderService.getOrderById(id);

            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Get Order Successfully")
                            .status(HttpStatus.OK)
                            .data(responses)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message(String.join(";", e.getMessage()))
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @GetMapping("/byDriver/{userId}")
    public ResponseEntity<ResponseObject> getOrderByDriverId(@PathVariable Long userId) {
        try {
            List<DeliveryOrder> responses = deliveryOrderService.getOrdersByUserId(userId);

            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Get Order Successfully")
                            .status(HttpStatus.OK)
                            .data(responses)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message(String.join(";", e.getMessage()))
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @PostMapping("") // http://localhost:8080/api/v1/orders
    public ResponseEntity<ResponseObject> insertOrder(
            @Valid @RequestBody DeliveryOrderDTO orderDTO,
            BindingResult result
    ) throws DataNotFoundException, IOException {
        try{
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        ResponseObject.builder()
                                .message(String.join(";", errorMessages))
                                .status(HttpStatus.BAD_REQUEST)
                                .build());
            }
            DeliveryOrder deliveryOrder = deliveryOrderService.createOrder(orderDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Insert order successfully")
                    .status(HttpStatus.OK)
                    .data(deliveryOrder)
                    .build());
        } catch (Exception e){
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message(String.join(";", e.getMessage()))
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }

    }
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/

    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    /* -----------------------------------------------------------------------------------------------------------------*/
    @PutMapping("/{id}") // http://localhost:8080/api/v1/orders/2
    public ResponseEntity<ResponseObject> updateOrder(@PathVariable Long id ,@Valid @RequestBody OrderUpdateDTO orderUpdateDTO) throws Exception {
        try{
            DeliveryOrder updateOrder = deliveryOrderService.updateOrder(id, orderUpdateDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Update order successfully")
                    .status(HttpStatus.OK)
                    .data(updateOrder)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder()
                            .message(String.join(";", e.getMessage()))
                            .status(HttpStatus.BAD_REQUEST)
                            .build());
        }
    }

    @DeleteMapping("/{id}") // http://localhost:8080/api/v1/orders/4
    public ResponseEntity<ResponseObject> deleteOrder(@PathVariable Long id){
        deliveryOrderService.deleteOrder(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .message(String.format("Vehicle with id = %d deleted successfully", id))
                .status(HttpStatus.OK)
                .build());
    }
}
