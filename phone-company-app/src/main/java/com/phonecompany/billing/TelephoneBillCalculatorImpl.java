package com.phonecompany.billing;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final BigDecimal STANDARD_RATE = new BigDecimal("1.00");
    private static final BigDecimal REDUCED_RATE = new BigDecimal("0.50");
    private static final BigDecimal SPECIAL_RATE = new BigDecimal("0.20");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static void main(String[] args) throws ParseException {
        TelephoneBillCalculatorImpl telephoneBillCalculator = new TelephoneBillCalculatorImpl();
        BigDecimal total_cost = telephoneBillCalculator.calculate("420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00\n" + "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420779562353,18-01-2020 07:59:20,18-01-2020 08:10:00");
        System.out.println("Total cost is: " +total_cost + " Kč");
    }

    @Override
    public BigDecimal calculate(String phoneLog) throws ParseException {
        String[] call_list = phoneLog.split("\\r?\\n");
        Map<String, Integer> callCounts = new HashMap<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        phoneNumberValidation(call_list);

        for (String call : call_list) {
            String[] details = call.split(",");
            String phoneNumber = details[0];
            callCounts.put(phoneNumber, callCounts.getOrDefault(phoneNumber, 0) + 1);
            try {
                Date start = DATE_FORMAT.parse(details[1]);
                Date end = DATE_FORMAT.parse(details[2]);
                long durationInMillis = end.getTime() - start.getTime();
                long minutes = (long) Math.ceil(durationInMillis / 60000.0);

                if (minutes == 0) minutes = 1; // Charge for at least one minute

                BigDecimal callCost = calculateCallCost(start, end, minutes);
                totalCost = totalCost.add(callCost);
            } catch (ParseException exception) {
                throw exception;
            }
        }

        String mostCalledNumber = callCounts.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), Collections.max(callCounts.values()))) // Filtr pro maximální hodnotu
                .map(Map.Entry::getKey)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (callCounts.get(mostCalledNumber) >= 1) {
            totalCost = totalCost.subtract(calculateCostForMostCalledNumber(call_list, mostCalledNumber));
        }

        return totalCost;
    }

    public void phoneNumberValidation(String[] call_list) {
        for (String call : call_list) {
            String[] details = call.split(",");
            String phoneNumber = details[0];
            if (!phoneNumber.matches("420\\d{9}")) {
                throw new RuntimeException("Phone number:" + phoneNumber + " is invalid!");
            }
        }
    }

    public BigDecimal calculateCallCost(Date start, Date end, long minutes) {
        BigDecimal cost = BigDecimal.ZERO;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        for (int i = 0; i < minutes; i++) {
            BigDecimal currentRate = (isStandardRate(calendar)) ? STANDARD_RATE : REDUCED_RATE;
            if (i >= 4) { // Starting from the 5th minute
                currentRate = SPECIAL_RATE;
            }
            cost = cost.add(currentRate);
            calendar.add(Calendar.MINUTE, 1);
        }

        return cost;
    }

    public BigDecimal calculateCostForMostCalledNumber(String[] calls, String mostCalledNumber) throws ParseException {
        BigDecimal cost = BigDecimal.ZERO;

        for (String call : calls) {
            String[] details = call.split(",");
            if (details[0].equals(mostCalledNumber)) {
                try {
                    Date start = DATE_FORMAT.parse(details[1]);
                    Date end = DATE_FORMAT.parse(details[2]);
                    long durationInMillis = end.getTime() - start.getTime();
                    long minutes = (long) Math.ceil(durationInMillis / 60000.0);
                    if (minutes == 0) minutes = 1; // Charge for at least one minute
                    cost = cost.add(calculateCallCost(start, end, minutes));
                } catch (ParseException exception) {
                    throw exception;
                }
            }
        }
        return cost;
    }

    public boolean isStandardRate(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour < 16;
    }
}
