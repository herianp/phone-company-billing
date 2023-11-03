package com.phonecompany.billing.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneBillCalculatorImplTest {
    private TelephoneBillCalculatorImpl calculator;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @BeforeEach
    void setUp() {
        calculator = new TelephoneBillCalculatorImpl();
    }

    String testingData() {
        return "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00\n" +
                "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420779562353,18-01-2020 07:59:20,18-01-2020 08:10:00";
    }

    @Test
    void testCalculateTotalCost() throws ParseException {
        BigDecimal totalCost = calculator.calculate(testingData());
        assertEquals(new BigDecimal("10.30"), totalCost);
    }

    @Test
    void testCalculateSingleCall() {
        try {
            Date start = DATE_FORMAT.parse("13-01-2020 18:10:15");
            Date end = DATE_FORMAT.parse("13-01-2020 18:12:57");
            long durationInMillis = end.getTime() - start.getTime();
            long minutes = (long) Math.ceil(durationInMillis / 60000.0);
            assertEquals(new BigDecimal("1.50"), calculator.calculateCallCost(start, end, minutes));
        }catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testIsStandardRate() {
        Calendar calendar = Calendar.getInstance();
        try {
            // Before 8:00
            Date start = DATE_FORMAT.parse("13-01-2020 07:59:59");
            calendar.setTime(start);
            assertFalse(calculator.isStandardRate(calendar));
            // After 8:00
            start = DATE_FORMAT.parse("13-01-2020 08:00:00");
            calendar.setTime(start);
            assertTrue(calculator.isStandardRate(calendar));
            // Before 16:00
            start = DATE_FORMAT.parse("13-01-2020 15:59:59");
            calendar.setTime(start);
            assertTrue(calculator.isStandardRate(calendar));
            // After 16:00
            start = DATE_FORMAT.parse("13-01-2020 16:00:00");
            calendar.setTime(start);
            assertFalse(calculator.isStandardRate(calendar));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCalculateWithParseException() {
        String wrongPhoneLog = "420774577453,13-01-2020 xx:xx,13-01-2020 08:92:57";
        assertThrows(ParseException.class, () -> {
            calculator.calculate(wrongPhoneLog);
        });
    }
}