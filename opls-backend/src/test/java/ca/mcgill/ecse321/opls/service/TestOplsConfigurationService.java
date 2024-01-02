package ca.mcgill.ecse321.opls.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.mcgill.ecse321.opls.model.ParkingLotSystem;
import ca.mcgill.ecse321.opls.repository.ParkingLotSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestOplsConfigurationService {

    @Mock
    private ParkingLotSystemRepository parkingLotSystemRepository;

    @InjectMocks
    private OplsConfigurationService oplsConfigurationService;

    private static final Double monthlyFee = 123.45, monthlyFee1 = 234.56, incrementFee = 678.90, incrementFee1 = 789.12;

    private static final int incrementTime = 15, incrementTime1 = 25, maxIncrementTime = 75, maxIncrementTime1 = 125;

    private ParkingLotSystem pls;

    @BeforeEach
    public void setupMocks() {
        pls = new ParkingLotSystem();
        pls.setMonthlyFee(monthlyFee);
        pls.setIncrementFee(incrementFee);
        pls.setIncrementTime(incrementTime);
        pls.setMaxIncrementTime(maxIncrementTime);

        lenient().when(parkingLotSystemRepository.getActiveParkingLotSystem()).
                thenAnswer((InvocationOnMock invocation) -> pls);

        lenient().when(parkingLotSystemRepository.save(any())).
                thenAnswer((InvocationOnMock invocation) -> invocation.getArgument(0));
    }

    @Test
    public void testGetParkingLotSystem() {
        // test valid request
        ParkingLotSystem pls = oplsConfigurationService.getParkingLotSystem();
        assertNotNull(pls);
        assertEquals(monthlyFee, pls.getMonthlyFee());
        assertEquals(incrementFee, pls.getIncrementFee());
        assertEquals(incrementTime, pls.getIncrementTime());
        assertEquals(maxIncrementTime, pls.getMaxIncrementTime());
    }

    @Test
    public void testUpdateParkingLotSystem() {
        // test valid request
        ParkingLotSystem pls = oplsConfigurationService.
                updateParkingLotSystemConfig(monthlyFee1, incrementFee1, incrementTime1, maxIncrementTime1);
        assertNotNull(pls);
        assertEquals(monthlyFee1, pls.getMonthlyFee());
        assertEquals(incrementFee1, pls.getIncrementFee());
        assertEquals(incrementTime1, pls.getIncrementTime());
        assertEquals(maxIncrementTime1, pls.getMaxIncrementTime());
    }

    @Test
    public void testSetParkingLotSystemAttributes() {
        // test valid requests
        ParkingLotSystem pls1 = oplsConfigurationService.setParkingLotSystemMonthlyFee(monthlyFee1);
        assertEquals(monthlyFee1, pls1.getMonthlyFee());

        ParkingLotSystem pls2 = oplsConfigurationService.setParkingLotSystemIncrementFee(incrementFee1);
        assertEquals(incrementFee1, pls1.getIncrementFee());

        ParkingLotSystem pls3 = oplsConfigurationService.setParkingLotSystemIncrementTime(incrementTime1);
        assertEquals(incrementTime1, pls3.getIncrementTime());

        ParkingLotSystem pls4 = oplsConfigurationService.setParkingLotSystemMaxIncrementTime(maxIncrementTime1);
        assertEquals(maxIncrementTime1, pls4.getMaxIncrementTime());
    }
}
