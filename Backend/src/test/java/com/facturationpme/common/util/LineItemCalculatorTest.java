package com.facturationpme.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.facturationpme.common.util.LineItemCalculator.LineInput;
import com.facturationpme.common.util.LineItemCalculator.Totals;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LineItemCalculatorTest {

  @Test
  void computeLineTotalShouldMultiplyQuantityByUnitPrice() {
    BigDecimal result =
        LineItemCalculator.computeLineTotal(
            new LineInput(
                BigDecimal.valueOf(4), BigDecimal.valueOf(250000), BigDecimal.valueOf(18)));

    assertThat(result).isEqualByComparingTo("1000000.00");
  }

  @Test
  void computeTotalsShouldSumLinesAndApplyPerLineTaxRate() {
    List<LineInput> lines =
        List.of(
            new LineInput(
                BigDecimal.valueOf(4), BigDecimal.valueOf(250000), BigDecimal.valueOf(18)),
            new LineInput(BigDecimal.ONE, BigDecimal.valueOf(200000), BigDecimal.valueOf(18)));

    Totals totals = LineItemCalculator.computeTotals(lines);

    assertThat(totals.amountExclTax()).isEqualByComparingTo("1200000.00");
    assertThat(totals.taxAmount()).isEqualByComparingTo("216000.00");
    assertThat(totals.totalAmount()).isEqualByComparingTo("1416000.00");
  }

  @Test
  void computeTotalsShouldSupportMixedTaxRatesAcrossLines() {
    List<LineInput> lines =
        List.of(
            new LineInput(BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.valueOf(18)),
            new LineInput(BigDecimal.TEN, BigDecimal.valueOf(100), BigDecimal.ZERO));

    Totals totals = LineItemCalculator.computeTotals(lines);

    assertThat(totals.amountExclTax()).isEqualByComparingTo("2000.00");
    assertThat(totals.taxAmount()).isEqualByComparingTo("180.00");
    assertThat(totals.totalAmount()).isEqualByComparingTo("2180.00");
  }

  @Test
  void computeTotalsShouldReturnZeroForEmptyLineList() {
    Totals totals = LineItemCalculator.computeTotals(List.of());

    assertThat(totals.amountExclTax()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(totals.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(totals.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
  }
}
