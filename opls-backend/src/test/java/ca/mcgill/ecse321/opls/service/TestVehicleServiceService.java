package ca.mcgill.ecse321.opls.service;

import static ca.mcgill.ecse321.opls.TestUtils.assertThrowsApiException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.*;

import ca.mcgill.ecse321.opls.model.VehicleService;
import ca.mcgill.ecse321.opls.repository.VehicleServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class TestVehicleServiceService {

    @Mock
    private VehicleServiceRepository vehicleServiceRepository;

    @InjectMocks
    private VehicleServiceService vehicleServiceService;

    private static final String id = "tire-repair", displayName = "tire repair", displayName2 = "oil change", id2 = "oil-change", notId = "notid", notDisplayName = "";

    private static final int duration = 15, duration2 = 5, notDuration = -1;

    private static final double fee = 50, fee2 = 20, notFee = -1;

    private VehicleService vehicleService;

    @BeforeEach
    public void setupMocks() {
        vehicleService = new VehicleService();
        vehicleService.setDisplayName(displayName);
        vehicleService.setDuration(duration);
        vehicleService.setFee(fee);

        VehicleService[] vehicleServices = new VehicleService[1];
        vehicleServices[0] = vehicleService;

        lenient().when(vehicleServiceRepository.findVehicleServiceById(id)).
                thenAnswer((InvocationOnMock invocation) -> vehicleServices[0]);

        lenient().when(vehicleServiceRepository.findVehicleServiceById(notId)).
                thenAnswer((InvocationOnMock invocation) -> null);

        lenient().when(vehicleServiceRepository.findAll()).
                thenAnswer((InvocationOnMock invocation) -> {
                    Iterable<VehicleService> vsl = Arrays.asList(vehicleServices);
                    return vsl;
                });

        lenient().when(vehicleServiceRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));

        lenient().doAnswer((InvocationOnMock invocation) -> vehicleServices[0] = null).
                when(vehicleServiceRepository).delete(vehicleService);
    }

    @Test
    public void testGetVehicleService() {
        // test valid request using id
        VehicleService vs1 = vehicleServiceService.getVehicleService(id);
        assertNotNull(vs1);
        assertEquals(duration, vs1.getDuration());
        assertEquals(fee, vs1.getFee());

        // test invalid request using invalid id
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found", 
        () -> vehicleServiceService.getVehicleService(notId));

        //test invalid request using empty id
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request", 
        () -> vehicleServiceService.getVehicleService(""));

    }

    @Test
    public void testGetAllVehicleServices() {
        Iterable<VehicleService> vsl = vehicleServiceService.getAllVehicleServices();
        assertNotNull(vsl);
        List<VehicleService> vslList = new ArrayList<VehicleService>();
        for (VehicleService vs : vsl){
            vslList.add(vs);
        }
        assertEquals(1, vslList.size());

    }

    @Test
    public void testCreateVehicleService() {

        VehicleService vs1 = vehicleServiceService.createVehicleService(displayName2, duration2, fee2);
        assertNotNull(vs1);
        assertEquals(duration2, vs1.getDuration());
        assertEquals(fee2, vs1.getFee());

    }

    @Test
    public void testUpdateVehicleService() {

        VehicleService vs1 = vehicleServiceService.updateVehicleService(id, displayName2, duration2, fee2);
        assertNotNull(vs1);
        assertEquals(duration2, vs1.getDuration());
        assertEquals(fee2, vs1.getFee());
        
    }

    @Test
    public void testDeleteVehicleService() {
        // test valid request
        vehicleServiceService.deleteVehicleService(id);
        assertThrowsApiException(HttpStatus.NOT_FOUND, "not_found", 
        () -> vehicleServiceService.getVehicleService(id));

        //test invalid request using empty id
        assertThrowsApiException(HttpStatus.BAD_REQUEST, "invalid_request", 
        () -> vehicleServiceService.deleteVehicleService(""));
    }
}
