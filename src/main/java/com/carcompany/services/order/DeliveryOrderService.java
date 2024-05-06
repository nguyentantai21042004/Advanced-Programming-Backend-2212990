package com.carcompany.services.order;

import com.carcompany.dtos.order.CalculateDTO;
import com.carcompany.dtos.order.DeliveryOrderDTO;
import com.carcompany.dtos.order.OrderUpdateDTO;
import com.carcompany.exceptions.DataNotFoundException;
import com.carcompany.models.DeliveryOrder;
import com.carcompany.models.User;
import com.carcompany.models.Vehicle;
import com.carcompany.repositories.DeliveryOrderRepository;
import com.carcompany.repositories.UserRepository;
import com.carcompany.repositories.VehicleRepository;
import com.carcompany.responses.order.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryOrderService implements IDeliveryOrderService {
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    @Value("${api.google.maps.api-key}") // Assuming you have the API key defined in application.properties or application.yml
    private String apiKey;


    @Override
    public DeliveryOrder getOrderById(Long id) throws Exception{
        return deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id =" + id));
    }

    @Override
    public Page<OrderResponse> getAllOrders(PageRequest pageRequest) {
        Page<DeliveryOrder> orderPage = deliveryOrderRepository.searchOrders(pageRequest);

        return orderPage.map(OrderResponse::fromDeliveryOrder);
    }

    @Override
    public List<DeliveryOrder> getOrdersByUserId(Long userId) {
        return deliveryOrderRepository.findByUserId(userId);
    }

    @Override
    public DeliveryOrder createOrder(DeliveryOrderDTO orderDTO) throws DataNotFoundException, IOException {
        Vehicle existringVehicle = vehicleRepository.findById(orderDTO.getVehicleId())
                .orElseThrow(() -> new DataNotFoundException("Do not have this car"));

        User existingDriver = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Do not have this driver"));

        CalculateDTO calculateDTO = calculate(orderDTO);

        DeliveryOrder newDeliveryOrder = DeliveryOrder.builder()
                .vehicle(existringVehicle)
                .user(existingDriver)
                .startPlace(orderDTO.getStartPlace())
                .endPlace(orderDTO.getEndPlace())
                .startDate(orderDTO.getStartDate())
                .phoneNumber(orderDTO.getPhoneNumber())
                .distance(calculateDTO.getDistance())
                .duration(calculateDTO.getDuration())
                .status("IN QUEUE")
                .vehiclePrice(calculateDTO.getVehiclePrice())
                .driverPrice(calculateDTO.getDriverPrice())
                .sumOfExpense(calculateDTO.getSumOfExpense())
                .profit(calculateDTO.getProfit())
                .build();



        return deliveryOrderRepository.save(newDeliveryOrder);
    }

    @Override
    public DeliveryOrder updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) throws DataNotFoundException, IOException {
        DeliveryOrder existingOrder = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("No order found with id = " + id));

        User existingDriver = userRepository.findById(existingOrder.getUser().getId())
                .orElseThrow(() -> new DataNotFoundException("No driver found with id = " + existingOrder.getUser().getId()));

        Vehicle existingVehicle = vehicleRepository.findById(existingOrder.getVehicle().getId())
                .orElseThrow(() -> new DataNotFoundException("No vehicle found with id = " + existingOrder.getVehicle().getId()));

        if (orderUpdateDTO.getStatus() != null) {
            if(orderUpdateDTO.getStatus().equals("CANCELED") || orderUpdateDTO.getStatus().equals("DONE") ) {
                existingOrder.setStatus(orderUpdateDTO.getStatus());
                existingDriver.setStatus("AVAILABLE");
                existingVehicle.setStatus("AVAILABLE");
            }
            else if(orderUpdateDTO.getStatus().equals("PROCESSING") || orderUpdateDTO.getStatus().equals("PENDING")){
                existingOrder.setStatus(orderUpdateDTO.getStatus());
                existingDriver.setStatus(orderUpdateDTO.getStatus());
                existingVehicle.setStatus(orderUpdateDTO.getStatus());
            }
        }

        userRepository.save(existingDriver);
        vehicleRepository.save(existingVehicle);
        return deliveryOrderRepository.save(existingOrder);
    }


    @Override
    public CalculateDTO calculate(DeliveryOrderDTO orderDTO) throws IOException, DataNotFoundException {
        Vehicle existringVehicle = vehicleRepository.findById(orderDTO.getVehicleId())
                .orElseThrow(() -> new DataNotFoundException("Do not have this car"));

        String url = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/directions/json")
                .queryParam("origin", orderDTO.getStartPlace())
                .queryParam("destination", orderDTO.getEndPlace())
                .queryParam("departure_time", orderDTO.getStartDate().getTime() / 1000) // Convert milliseconds to seconds
                .queryParam("region", "vi")
                .queryParam("key", apiKey)
                .toUriString();

        // Kiểm tra kết nối trước khi gửi yêu cầu
        if (!isUrlReachable(url)) {
            throw new IOException("Could not establish connection to Google Maps API.");
        }

        JsonNode responseNode = get(new URL(url));

        // Trích xuất các giá trị cần thiết từ JSON response
        JsonNode routesNode = responseNode.get("routes").get(0); // Chỉ xem xét tuyến đường đầu tiên
        JsonNode legsNode = routesNode.get("legs").get(0); // Chỉ xem xét chân đầu tiên

        // Lấy giá trị của distance và duration
        double distanceInM = legsNode.get("distance").get("value").asDouble();
        int durationInSeconds = legsNode.get("duration").get("value").asInt();

        // Lấy start_address và end_address
        String startAddress = legsNode.get("start_address").asText();
        String endAddress = legsNode.get("end_address").asText();

        // Tính toán giá trị của các trường khác
        double distanceInKm = distanceInM / 1000;
        double durationInHours = durationInSeconds / 3600.0;

        double vehiclePrice = (double) (existringVehicle.getRentalPrice() * distanceInKm); // Thay thế calculateVehiclePrice bằng phương thức tính giá của phương tiện
        double driverPrice = (double) (distanceInKm * 100); // Hệ số 100
        double sumOfExpense = (vehiclePrice + driverPrice) * 1.3; // Tổng chi phí
        double profit = (vehiclePrice + driverPrice) * 0.3;

        return new CalculateDTO(distanceInKm, durationInHours, vehiclePrice, driverPrice, sumOfExpense, profit);
    }

    @Override
    public void deleteOrder(long id) {
        Optional<DeliveryOrder> optionalDeliveryOrder = deliveryOrderRepository.findById(id);
        optionalDeliveryOrder.ifPresent(deliveryOrderRepository::delete);
    }


    public static JsonNode get(URL url) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(url);
    }

    // Phương thức để kiểm tra kết nối đến URL
    private boolean isUrlReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            return responseCode == 200; // Trả về true nếu kết nối thành công (response code là 200)
        } catch (IOException e) {
            return false; // Trả về false nếu có lỗi xảy ra trong quá trình kết nối
        }
    }
}
