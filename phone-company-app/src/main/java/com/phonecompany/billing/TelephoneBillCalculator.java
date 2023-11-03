package com.phonecompany.billing;

import java.math.BigDecimal;
import java.text.ParseException;

public interface TelephoneBillCalculator {
    BigDecimal calculate (String phoneLog) throws ParseException;
}
