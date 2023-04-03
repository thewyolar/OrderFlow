package com.thewyolar.orderflow.orderservice.service.mapper;

import com.thewyolar.orderflow.orderservice.dto.OrderDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.orderservice.dto.OrderStatusResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    private final ModelMapper modelMapper;

    public OrderMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Order toOrder(OrderDTO orderDTO) {
        return modelMapper.map(orderDTO, Order.class);
    }

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        return modelMapper.map(order, OrderResponseDTO.class);
    }

    public OrderResponseWrapper toOrderResponseWrapper(Order order) {
        return modelMapper.map(order, OrderResponseWrapper.class);
    }

    public OrderStatusResponseDTO toOrderStatusResponseDTO(Order order) {
        return modelMapper.map(order, OrderStatusResponseDTO.class);
    }
}
